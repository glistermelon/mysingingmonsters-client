package com.glisterbyte.singingmonsters.handling;

import com.glisterbyte.singingmonsters.common.GlobalConfig;
import com.glisterbyte.singingmonsters.networking.exceptions.ClientTimeoutException;
import com.glisterbyte.singingmonsters.networking.WebsocketClient;
import com.glisterbyte.singingmonsters.main.client.Client;
import com.glisterbyte.singingmonsters.main.client.ClientRunnable;
import com.glisterbyte.singingmonsters.exceptions.ClientException;
import com.glisterbyte.singingmonsters.main.island.Island;
import com.glisterbyte.singingmonsters.sfsmodels.SfsCorrelatedEventModel;
import com.glisterbyte.singingmonsters.sfsmodels.SfsRequestModel;
import com.glisterbyte.singingmonsters.sfsmodels.SfsEventModel;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class Request {

    private record ExpectedResponsePair(
            EventHandler<?, ?, ?> handler,
            CompletableFuture<Void> spark,
            CompletableFuture<Void> lockWaiter
    ) { }

    private final List<CompletableFuture<?>> futures = new ArrayList<>();
    private final List<ExpectedResponsePair> expectedResponses = new ArrayList<>();

    private final Client client;
    private final WebsocketClient wsClient;
    private final List<EventHandler<?, ?, ?>> allHandlers;

    public Request(Client client) {
        this.client = client;
        wsClient = client.getWebsocketClient();
        allHandlers = client.getEventHandlerManager().getAllHandlers();
    }

    public <RequestData, EventT extends SfsEventModel, HandledT> CompletableFuture<HandledEvent<RequestData, EventT, HandledT>> expectResponse(
            UncorrelatedEventHandler<RequestData, EventT, HandledT> handler,
            @Nullable RequestData requestData
    ) {
        CompletableFuture<Void> spark = new CompletableFuture<>();
        CompletableFuture<Void> lockWaiter = new CompletableFuture<>();
        expectedResponses.add(new ExpectedResponsePair(handler, spark, lockWaiter));
        AtomicReference<CompletableFuture<HandledEvent<RequestData, EventT, HandledT>>> waiterRef = new AtomicReference<>();
        var future = spark.thenCompose(x -> {
            var waiter = handler.waitForEventAsync(requestData);
            waiterRef.set(waiter);
            lockWaiter.complete(null);
            return waiter;
        });
        future.whenComplete((val, ex) -> {
            var waiter = waiterRef.get();
            if (waiter == null) {
                if (ex != null) lockWaiter.completeExceptionally(ex);
                else lockWaiter.complete(null);
            }
            else {
                if (ex != null) waiter.completeExceptionally(ex);
                else waiter.complete(val);
            }
        });
        futures.add(future);
        return future;
    }

    public <RequestData, EventT extends SfsEventModel, HandledT> CompletableFuture<HandledEvent<RequestData, EventT, HandledT>> expectResponse(
            UncorrelatedEventHandler<RequestData, EventT, HandledT> handler
    ) {
        return expectResponse(handler, null);
    }

    public <RequestData, EventT extends SfsEventModel & SfsCorrelatedEventModel, HandledT>
        CompletableFuture<HandledEvent<RequestData, EventT, HandledT>> expectResponse(
            CorrelatedEventHandler<RequestData, EventT, HandledT> handler,
            long correlationId,
            @Nullable RequestData requestData
        )
    {
        CompletableFuture<Void> spark = new CompletableFuture<>();
        CompletableFuture<Void> lockWaiter = new CompletableFuture<>();
        expectedResponses.add(new ExpectedResponsePair(handler, spark, lockWaiter));
        AtomicReference<CompletableFuture<HandledEvent<RequestData, EventT, HandledT>>> waiterRef = new AtomicReference<>();
        var future = spark.thenCompose(x -> {
            var waiter = handler.waitForEventAsync(correlationId, requestData);
            waiterRef.set(waiter);
            lockWaiter.complete(null);
            return waiter;
        });
        future.whenComplete((val, ex) -> {
            var waiter = waiterRef.get();
            if (waiter == null) {
                if (ex != null) lockWaiter.completeExceptionally(ex);
                else lockWaiter.complete(null);
            }
            else {
                if (ex != null) waiter.completeExceptionally(ex);
                else waiter.complete(val);
            }
        });
        futures.add(future);
        return future;
    }

    public <RequestData, EventT extends SfsEventModel & SfsCorrelatedEventModel, HandledT>
    CompletableFuture<HandledEvent<RequestData, EventT, HandledT>> expectResponse(
            CorrelatedEventHandler<RequestData, EventT, HandledT> handler,
            long correlationId
    ) {
        return expectResponse(handler, correlationId, null);
    }

    public void run(SfsRequestModel request, @Nullable Island island) throws InterruptedException, ClientException {

        ClientRunnable<Void> action = () -> {

            try {

                for (var expectedResponse : expectedResponses.stream().sorted(
                        Comparator.comparing(pair -> allHandlers.indexOf(pair.handler()))
                ).toList()) {
                    expectedResponse.spark().complete(null);
                    expectedResponse.lockWaiter().get(GlobalConfig.defaultWsTimeout, TimeUnit.SECONDS);
                }

                wsClient.send(request);

                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                        .get(GlobalConfig.defaultWsTimeout, TimeUnit.SECONDS);

            }
            catch (ExecutionException ex) {
                for (var future : futures) future.completeExceptionally(ex);
                var cause = ex.getCause();
                if (cause instanceof ClientException clientException) throw clientException;
                else if (cause instanceof InterruptedException interruptedException) throw interruptedException;
                else throw new RuntimeException(cause);
            }
            catch (java.util.concurrent.TimeoutException ex) {
                for (var future : futures) future.completeExceptionally(ex);
                throw new ClientTimeoutException("Request timed out: " + request);
            }

            return null;

        };

        if (island == null) action.run();
        else client.withActiveIsland(island, action);

    }

    public static <RequestData, EventT extends SfsEventModel, HandledT> HandledT simpleRequest(
            UncorrelatedEventHandler<RequestData, EventT, HandledT> handler, SfsRequestModel model, @Nullable Island island
    ) throws InterruptedException, ClientException {
        Request request = new Request(handler.client);
        var future = request.expectResponse(handler);
        request.run(model, island);
        return future.getNow(null).getResult();
    }

    public static <RequestData, EventT extends SfsEventModel & SfsCorrelatedEventModel, HandledT> HandledT simpleRequest(
            CorrelatedEventHandler<RequestData, EventT, HandledT> handler, SfsRequestModel model,
            @Nullable Island island, long correlationId
    ) throws InterruptedException, ClientException {
        Request request = new Request(handler.client);
        var future = request.expectResponse(handler, correlationId);
        request.run(model, island);
        return future.getNow(null).getResult();
    }

}