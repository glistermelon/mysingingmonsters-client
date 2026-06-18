package com.glisterbyte.singingmonsters.networking;

import com.glisterbyte.singingmonsters.common.GlobalConfig;
import com.glisterbyte.singingmonsters.exceptions.*;
import com.glisterbyte.singingmonsters.localization.LocalizedTextManager;
import com.glisterbyte.singingmonsters.networking.exceptions.*;
import com.glisterbyte.singingmonsters.networking.exceptions.ClientTimeoutException;
import com.glisterbyte.singingmonsters.networking.models.AuthParams;
import com.glisterbyte.singingmonsters.networking.websockproto.EventFrame;
import com.glisterbyte.singingmonsters.networking.websockproto.RequestFrame;
import com.glisterbyte.singingmonsters.sfsmapping.*;
import com.glisterbyte.singingmonsters.handling.EventHandlerManager;
import com.glisterbyte.singingmonsters.sfsmapping.exceptions.MapFromSfsException;
import com.glisterbyte.singingmonsters.sfsmapping.exceptions.MapToSfsException;
import com.glisterbyte.singingmonsters.sfsmodels.*;
import com.glisterbyte.singingmonsters.sfsmodels.data.LoginData;
import com.glisterbyte.singingmonsters.sfsmodels.events.LoginResponse;
import com.glisterbyte.singingmonsters.sfsmodels.requests.LoginRequest;
import com.smartfoxserver.v2.entities.data.SFSArray;
import com.smartfoxserver.v2.entities.data.SFSObject;
import okhttp3.*;
import okio.ByteString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

public class WebsocketClient extends WebSocketListener {

    private static final Logger logger = LoggerFactory.getLogger(WebsocketClient.class);

    private EventHandlerManager eventHandlerManager;
    private LocalizedTextManager localizedTextManager;

    private OkHttpClient httpClient;
    private final AtomicReference<WebSocket> socketRef = new AtomicReference<>(null);

    private Thread keepAliveThread;

    final ConcurrentMap<String, ReentrantLock> commandLocks = new ConcurrentHashMap<>();
    final ConcurrentMap<String, CompletableFuture<SfsEventModel>> simpleRequestFutures = new ConcurrentHashMap<>();
    final ConcurrentMap<String, LinkedBlockingQueue<SfsEventModel>> chunkedResponseQueues = new ConcurrentHashMap<>();
    private final AtomicLong sequenceCounter = new AtomicLong(0L);

    private CompletableFuture<Void> openFuture;

    private Instant lastSendTime = Instant.EPOCH;
    private Instant lastDisconnectTime = Instant.EPOCH;

    private boolean closingDone = false;

    public void setEventHandlerManager(EventHandlerManager eventHandlerManager) {
        this.eventHandlerManager = eventHandlerManager;
    }

    public void setLocalizedTextManager(LocalizedTextManager localizedTextManager) {
        this.localizedTextManager = localizedTextManager;
    }

    private WebSocket getSocket() throws ClientDisconnectedException {
        WebSocket socket = socketRef.get();
        if (socket != null) return socket;
        else throw new ClientDisconnectedException();
    }

    @Override
    public void onOpen(@NotNull WebSocket socket, @NotNull Response response) {

        logger.info("Websocket {} connection opened", socket);

        socketRef.set(socket);
        openFuture.complete(null);
        openFuture = null;

    }

    @Override
    public void onClosed(@NotNull WebSocket socket, int code, @NotNull String reason) {

        logger.info("Websocket {} closed with code {} and reason '{}'", socket, code, reason);

        socketRef.set(null);
        close(null);

    }

    @Override
    public void onFailure(@NotNull WebSocket socket, @NotNull Throwable ex, @Nullable Response response) {

        /*
            To match the behavior of closing the app on a phone,
            the client closes the connection by cancelling the socket.
            This sometimes results in onFailure being called, sometimes not.
            Thus, the logging here is 'info' rather than 'error'.
         */

        String responseInfo = null;
        if (response != null) {
            try {
                String body = response.peekBody(Long.MAX_VALUE).string();
                responseInfo = "HTTP " + response.code() + " " + response.message() + "\n" +
                        "Headers: " + response.headers() + "\n" +
                        "Body: " + body;
            } catch (IOException ex2) {
                responseInfo = "Failed to collect response info due to exception: " + ex2;
            }
        }

        logger.info(
                "Websocket {} failed due to exception; Note the websocket failing may be an" +
                        " intentional disconnection by the client; response: {}",
                socket, responseInfo, ex
        );

        socketRef.set(null);
        if (openFuture != null) {
            openFuture.completeExceptionally(new ClientConnectionException("Open websocket connection failed", ex));
            openFuture = null;
        }
        else {
            close(ex);
        }

    }

    @Override
    public void onMessage(@NotNull WebSocket socket, @NotNull ByteString bytes) {

        String serializedMessage = bytes.base64();

        try {

            EventFrame frame = EventFrame.deserialize(bytes);

            logger.debug(
                    "Received message '{}' {} {}",
                    frame.command,
                    frame.data.getCompactDump(),
                    serializedMessage
            );

            Class<? extends SfsEventModel> classType
                    = SfsCmdResponseRegistry.getCmdResponseType(frame.command);

            String command = frame.command;

            SfsEventModel model;
            try {
                model = SfsMapper.mapSFSObjectToClass(classType, frame.data);
                model.setSfsObject(frame.data);
            }
            catch (Throwable ex) {
                var simpleRequestFuture = simpleRequestFutures.remove(command);
                if (simpleRequestFuture != null) {
                    simpleRequestFuture.completeExceptionally(ex);
                    return;
                }
                else throw ex;
            }

            boolean handled = false;

            var simpleRequestFuture = simpleRequestFutures.remove(command);
            if (simpleRequestFuture != null) {
                if (model instanceof SfsResultResponse result && !result.success) {
                    simpleRequestFuture.completeExceptionally(new ActionFailedException(
                            result, localizedTextManager
                    ));
                }
                else {
                    simpleRequestFuture.complete(model);
                }
                handled = true;
            }
            else if (chunkedResponseQueues.containsKey(command)) {
                chunkedResponseQueues.get(command).put(model);
                handled = true;
            }
            else if (eventHandlerManager != null) {
                for (var handler : eventHandlerManager.getAllHandlers()) {
                    if (handler.getEventType().isInstance(model)) {
                        handler.handle(model);
                        handled = true;
                        break;
                    }
                }
            }

            if (!handled) logger.warn("Unhandled response with command '{}'", command);

        }
        catch (ClientDeserializeException ex) {
            logger.error("Failed to deserialize message from the server {}", serializedMessage, ex);
        }
        catch (MissingCommandException ex) {
            logger.warn("Received unrecognized command '{}' from the server", ex.getCommand());
        }
        catch (MapFromSfsException ex) {
            logger.error("Failed to map data from the server {}", serializedMessage, ex);
        }
        catch (Throwable ex) {
            logger.error("Exception thrown when handling message from server {}", serializedMessage, ex);
        }

    }

    public void connect(String url, AuthParams authParams, AuthResults authResults)
            throws InterruptedException, ClientException {

        // Server seems to have a rate limit on this and it just times out
        final Duration reconnectBufferDuration = Duration.ofSeconds(5);
        final Duration timeSinceDisconnect = Duration.between(lastDisconnectTime, Instant.now());
        final Duration remainingBufferTime = reconnectBufferDuration.minus(timeSinceDisconnect);
        if (remainingBufferTime.isPositive()) Thread.sleep(remainingBufferTime);

        synchronized (this) {

            if (httpClient != null || socketRef.get() != null) {
                throw new ClientConnectionException(
                        "Cannot reconnect WebsocketClient while a connection is already open"
                );
            }

            closingDone = false;

            commandLocks.clear();
            simpleRequestFutures.clear();
            chunkedResponseQueues.clear();
            sequenceCounter.set(0L);

            Request request = new Request.Builder()
                    .url(url)
                    .header("access-key", GlobalConfig.ACCESS_KEY)
                    .header("access_key", GlobalConfig.ACCESS_KEY)
                    .header("client-version", GlobalConfig.GAME_VERSION_STRING)
                    .header("client_version", GlobalConfig.GAME_VERSION_STRING)
                    .header("user-agent", authParams.getUserAgent())
                    .build();

            openFuture = new CompletableFuture<>();

            httpClient = new OkHttpClient.Builder().build();
            httpClient.newWebSocket(request, this);

            try {
                openFuture.join();
            }
            catch (CompletionException ex) {
                Throwable cause = ex.getCause();
                if (cause instanceof ClientException clientEx) throw clientEx;
                else throw ex;
            }

        }

        try {
            login(authParams, authResults);
        }
        catch (Throwable ex) {
            disconnect();
            throw ex;
        }

        keepAliveThread = Thread.startVirtualThread(() -> {
            while (true) {
                try {
                    Thread.sleep(Duration.ofSeconds(30));
                    send("alive");
                }
                catch (InterruptedException x) {
                    break;
                }
                catch (ClientException ex) {
                    logger.error("Failed to send keep-alive due to exception", ex);
                }
            }
        });

    }

    private synchronized void close(@Nullable Throwable cause) {

        if (socketRef.get() != null) {
            throw new IllegalStateException("WebsocketClient cannot cleanup while socket is still active");
        }

        if (closingDone) return;

        try {

            // Disconnecting too soon after sending a command results in it not being processed server-side
            final Duration disconnectBuffer = Duration.ofSeconds(2);
            final Duration timeSinceDisconnect = Duration.between(lastSendTime, Instant.now());
            final Duration remainingBufferTime = disconnectBuffer.minus(timeSinceDisconnect);
            if (remainingBufferTime.isPositive()) Thread.sleep(remainingBufferTime);

            Throwable waiterEx = cause != null ? cause : new ClientDisconnectedException();
            simpleRequestFutures.forEach((cmd, future) -> {
                future.completeExceptionally(waiterEx);
                simpleRequestFutures.remove(cmd);
            });
            chunkedResponseQueues.clear();
            commandLocks.clear();

            if (keepAliveThread != null) {
                keepAliveThread.interrupt();
                keepAliveThread.join();
            }

            if (httpClient != null) {
                httpClient.dispatcher().executorService().shutdown();
                httpClient.connectionPool().evictAll();
                httpClient = null;
            }

            lastDisconnectTime = Instant.now();

            closingDone = true;

        }
        catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }

    }

    public synchronized void disconnect() {
        WebSocket socket = socketRef.getAndSet(null);
        if (socket == null) return;
        socket.cancel();
        close(null);
    }

    private void login(AuthParams authParams, AuthResults authResults)
            throws InterruptedException, ClientException {

        LoginData loginData = new LoginData();
        loginData.access_key = GlobalConfig.ACCESS_KEY;
        loginData.attempt_recovery = false;
        loginData.client_device = authParams.device().device_model();
        loginData.client_lang = authParams.device().lang();
        loginData.client_os = authParams.device().os_version();
        loginData.client_platform = authParams.platform();
        loginData.client_version = authParams.clientVersion();
        loginData.last_update_version = GlobalConfig.GAME_VERSION_STRING;
        loginData.last_updated = 0;
        loginData.raw_device_id = authParams.device().device_id();
        loginData.token = authResults.apiToken();

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.data = loginData;
        loginRequest.password = "";
        loginRequest.user = authResults.userGameId();
        loginRequest.zone = "MySingingMonsters";

        request(loginRequest, LoginResponse.class);

    }

    private ReentrantLock takeCommandLock(String command) {
        ReentrantLock lock = commandLocks.computeIfAbsent(command, x -> new ReentrantLock());
        lock.lock();
        return lock;
    }

    private void releaseCommandLock(ReentrantLock lock) {
        lock.unlock();
    }

    public synchronized void send(String cmd, SFSObject data) throws ClientException {

        RequestFrame frame = new RequestFrame();
        frame.command = cmd;
        frame.data = data;

        synchronized (sequenceCounter) {

            frame.seqNum = sequenceCounter.getAndIncrement();
            ByteString serialized = frame.serialize();

            logger.debug(
                    "Sending request '{}' {} {}",
                    frame.command,
                    frame.data.getCompactDump(),
                    serialized.base64()
            );

            getSocket().send(serialized);

        }

        lastSendTime = Instant.now();

    }

    public void send(String cmd) throws ClientException {
        send(cmd, new SFSObject());
    }

    public void send(SfsRequestModel request) throws ClientException {
        try {
            send(request.getCommand(), SfsMapper.mapToSFSObject(request));
        }
        catch (Throwable ex) {
            throw new RuntimeException(ex);
        }
    }

    private SfsEventModel requestChunked(SfsRequestModel request, Class<? extends SfsEventModel> responseModel)
            throws InterruptedException, ClientException {

        final String command = request.getCommand();

        SfsChunkedDbResponse completeResponse;

        SFSArray sfsChunks = new SFSArray();
        try {
            SfsEventModel instance = responseModel.getDeclaredConstructor().newInstance();
            if (instance instanceof SfsChunkedDbResponse chunkedInstance) {
                completeResponse = chunkedInstance;
                completeResponse.sfsObject = new SFSObject();
                completeResponse.sfsObject.putSFSArray(completeResponse.getChunkedPropertyKey(), sfsChunks);
            }
            else {
                throw new ClientChunkingException(
                        "Cannot receive chunked response of type '" + instance.getClass().getName()
                                + "' that does not inherit SfsChunkedResponse"
                );
            }
        }
        catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException ex) {
            throw new RuntimeException(ex);
        }

        var lock = takeCommandLock(command);
        try {

            LinkedBlockingQueue<SfsEventModel> responseQueue = new LinkedBlockingQueue<>();
            chunkedResponseQueues.put(command, responseQueue);
            send(command, SfsMapper.mapToSFSObject(request));

            while (true) {

                SfsEventModel response = responseQueue.poll(GlobalConfig.defaultWsTimeout, TimeUnit.SECONDS);
                if (response == null) throw new ClientTimeoutException("Chunked request for '" + command + "' timed out");

                if (!(response instanceof SfsChunkedDbResponse chunk)) {
                    throw new ClientChunkingException(
                            "Response of type '" + response.getClass().getName() + "' is not SfsChunkedResponse"
                    );
                }

                Integer chunkIndex = chunk.sfsObject.getInt("chunk");
                Integer numChunks = chunk.sfsObject.getInt("numChunks");

                if (chunkIndex == null || numChunks == null) {
                    return chunk;
                }

                completeResponse.last_updated = chunk.last_updated;
                completeResponse.server_time = chunk.server_time;
                completeResponse.sfsObject.putLong("last_updated", chunk.last_updated);
                completeResponse.sfsObject.putLong("server_time", chunk.server_time);

                List<SfsModel> completeList = completeResponse.getChunkedList();
                List<SfsModel> chunkList = chunk.getChunkedList();
                completeList.addAll(chunkList);

                var chunkSfsArray = response.sfsObject.getSFSArray(completeResponse.getChunkedPropertyKey());
                for (int i = 0; i < chunkSfsArray.size(); i++) {
                    sfsChunks.add(chunkSfsArray.get(i));
                }

                if (chunkIndex.equals(numChunks)) break;

            }
        }
        catch (MapToSfsException ex) {
            throw new RuntimeException(ex);
        }
        finally {
            chunkedResponseQueues.remove(command);
            releaseCommandLock(lock);
        }

        return completeResponse;

    }

    public SfsEventModel request(SfsRequestModel request, Class<? extends SfsEventModel> responseModel)
            throws InterruptedException, ClientException {

        if (request.getClass().getAnnotation(SfsHasChunkedResponse.class) != null) {
            return requestChunked(request, responseModel);
        }

        String command;
        try {
            command = responseModel.getDeclaredConstructor().newInstance().getCommand();
        }
        catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException ex) {
            throw new RuntimeException(ex);
        }

        SFSObject data;
        try {
            data = SfsMapper.mapToSFSObject(request);
        }
        catch (MapToSfsException ex) {
            throw new RuntimeException(ex);
        }

        var lock = takeCommandLock(command);
        try {
            CompletableFuture<SfsEventModel> future = new CompletableFuture<>();
            simpleRequestFutures.put(command, future);
            send(command, data);
            return future.get(GlobalConfig.defaultWsTimeout, TimeUnit.SECONDS);
        }
        catch (ExecutionException ex) {
            var cause = ex.getCause();
            if (cause instanceof ClientException clientException) throw clientException;
            else throw new RuntimeException(cause);
        }
        catch (java.util.concurrent.TimeoutException ex) {
            throw new ClientTimeoutException("request with command '" + command + "' timed out");
        }
        finally {
            releaseCommandLock(lock);
        }

    }

}