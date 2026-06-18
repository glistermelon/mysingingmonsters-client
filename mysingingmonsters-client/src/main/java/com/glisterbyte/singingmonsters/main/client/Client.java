package com.glisterbyte.singingmonsters.main.client;

import com.glisterbyte.singingmonsters.common.StringUtil;
import com.glisterbyte.singingmonsters.localization.Language;
import com.glisterbyte.singingmonsters.localization.LocalizedTextManager;
import com.glisterbyte.singingmonsters.main.catalog.*;
import com.glisterbyte.singingmonsters.main.common.MultiCurrencyValue;
import com.glisterbyte.singingmonsters.main.island.ReadableIsland;
import com.glisterbyte.singingmonsters.main.users.*;
import com.glisterbyte.singingmonsters.main.island.Island;
import com.glisterbyte.singingmonsters.main.island.IslandType;
import com.glisterbyte.singingmonsters.networking.*;
import com.glisterbyte.singingmonsters.exceptions.*;
import com.glisterbyte.singingmonsters.handling.*;
import com.glisterbyte.singingmonsters.main.monster.Monster;
import com.glisterbyte.singingmonsters.networking.models.NewGuestAccountResponse;
import com.glisterbyte.singingmonsters.sfsmodels.data.SfsFriend;
import com.glisterbyte.singingmonsters.sfsmodels.data.SfsFriendRequest;
import com.glisterbyte.singingmonsters.sfsmodels.data.SfsPlayer;
import com.glisterbyte.singingmonsters.sfsmodels.data.SfsGenericUpdate;
import com.glisterbyte.singingmonsters.sfsmodels.events.*;
import com.glisterbyte.singingmonsters.sfsmodels.requests.ChangeIslandRequest;
import com.smartfoxserver.v2.entities.data.SFSObject;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Client {

    private AuthClient authClient;

    private final EventHandlerManager eventHandlerManager;
    private final WebsocketClient wsClient;

    private final LocalizedTextManager localizedTextManager = new LocalizedTextManager();

    private final Catalog catalog;

    private volatile Island activeIsland;
    private final ReentrantReadWriteLock activeIslandLock = new ReentrantReadWriteLock();

    private SFSObject playerSfsData;

    private int userId;
    private String country;
    private String displayName;

    private long coins;
    private long diamonds;
    private long shards;
    private long food;
    private long keys;
    private long relics;
    private long starpower;
    private long wildcards;
    private long medals;

    private int level;
    private int xp;

    private ProfileDisplay profileDisplay;

    private String friendCode;

    private final Map<Long, Island> islands = new HashMap<>();

    private final List<Friend> friends = new ArrayList<>();
    private final List<FriendRequest> pendingOutgoingFriendRequests = new ArrayList<>();

    public Client() {

        wsClient = new WebsocketClient();
        eventHandlerManager = new EventHandlerManager(this, wsClient);
        wsClient.setEventHandlerManager(eventHandlerManager);
        wsClient.setLocalizedTextManager(localizedTextManager);

        catalog = new Catalog(wsClient, localizedTextManager);
        catalog.loadFromCache();

    }

    public class EventHandler {

        public Island handleBuyIslandEvent(BuyIslandResponse response) {
            synchronized (Client.this) {
                Island island = new Island(Client.this, response.user_island);
                long userIslandId = island.getUserIslandId();
                Validate.isTrue(
                        !islands.containsKey(userIslandId),
                        "Client already has an island with user island id %s", userIslandId
                );
                islands.put(userIslandId, island);
                return island;
            }
        }

        public synchronized SendFriendRequestResult handleRequestFriendEvent(RequestFriendResponse response)
                throws ClientIllegalArgumentException {
            synchronized (Client.this) {
                if (response.request != null && !response.request.isEmpty()) {
                    FriendRequest request = FriendRequest.buildFriendRequest(catalog, response.request.getFirst());
                    pendingOutgoingFriendRequests.add(request);
                    return new SendFriendRequestResult(request);
                }
                else if (response.friends != null && !response.friends.isEmpty()) {
                    Friend friend = Friend.buildFriend(catalog, response.friends.getFirst());
                    friends.add(friend);
                    return new SendFriendRequestResult(friend);
                }
                else throw new ClientIllegalArgumentException("Send friend request response was invalid");
            }
        }

    }

    public EventHandler getEventHandler() {
        return new EventHandler();
    }

    private synchronized void connect() throws InterruptedException, ClientException {

/*      The server forces you to go through the authentication checkpoint even if you already
        have all the information it's going to give you (an API token and your user ID).
        Since the API token only changes over a longer duration of time and your user ID never changes,
        I'm not really sure why (I suppose it's more secure), but if they ever change that, there's this code.

        if (cachedAuthResults != null) {
              try {
                  connect(cachedAuthResults);
                  return;
              }
              catch (ClientException ex) {
                  logger.info("Connect with cached auth results failed due to exception; trying again from scratch", ex);
                  cachedAuthResults = null;
              }
          }
 */

        AuthResults authResults = authClient.authenticate();
        connect(authResults);
//        cachedAuthResults = authResults;

    }

    private void checkAlreadyConnectedOnce() throws ClientException {
        if (authClient != null) throw new ClientConnectionException(
                "To reconnect a client, use Client::reconnect()"
        );
    }

    private synchronized void connect(AuthResults authResults) throws InterruptedException, ClientException {

        String serverIpString = authResults.serverIp();
        String expectedIpStringPrefix = "tomcat|ssl|";
        if (!(
                serverIpString.startsWith(expectedIpStringPrefix)
                        && StringUtil.countOccurrences(serverIpString, '|') == 2
        )) {
            throw new ClientConnectionException("Unexpected server IP string format: '" + serverIpString + "'");
        }

        String hostname = serverIpString.substring(expectedIpStringPrefix.length());
        String url = "https://" + hostname + "/msm/socket";

        wsClient.connect(url, authClient.getAuthParams(), authResults);

        catalog.update();

        refresh();

    }

    public synchronized void connectWithEmail(String username, String password)
            throws InterruptedException, ClientException {
        checkAlreadyConnectedOnce();
        authClient = new AuthClient(new Credentials(username, password), LoginMethod.EMAIL);
        connect();
    }

    public synchronized void connectAsGuest(String username, String password)
            throws InterruptedException, ClientException {
        checkAlreadyConnectedOnce();
        authClient = new AuthClient(new Credentials(username, password), LoginMethod.GUEST);
        connect();
    }

    public synchronized Credentials connectAsNewGuest() throws InterruptedException, ClientException {
        checkAlreadyConnectedOnce();
        NewGuestAccountResponse newGuestAccountResponse = AuthClient.createGuestAccount();
        authClient = new AuthClient(newGuestAccountResponse);
        connect();
        return newGuestAccountResponse.credentials();
    }

    public synchronized Credentials createNewGuestAccount() throws ClientException {
        return AuthClient.createGuestAccount().credentials();
    }

    public synchronized void disconnect() {

        wsClient.disconnect();

        activeIslandLock.writeLock().lock();
        try {
            activeIsland = null;
        }
        finally {
            activeIslandLock.writeLock().unlock();
        }

    }

    public synchronized void reconnect() throws InterruptedException, ClientException {

        if (authClient == null) {
            throw new ClientConnectionException("Cannot reconnect client that was never connected");
        }

        connect();

    }

    public synchronized void refresh() throws InterruptedException, ClientException {


        var playerData = eventHandlerManager.getGetPlayerHandler().request();

        SfsPlayer sfsPlayer = playerData.sfsPlayer();
        GetFriendsResponse getFriendsResponse = playerData.getFriendsResponse();

        playerSfsData = sfsPlayer.sfsObject;

        userId = sfsPlayer.user_id;
        country = sfsPlayer.country;
        displayName = sfsPlayer.display_name;
        coins = sfsPlayer.coins_actual;
        diamonds = sfsPlayer.diamonds_actual;
        shards = sfsPlayer.ethereal_currency_actual;
        food = sfsPlayer.food_actual;
        keys = sfsPlayer.keys_actual;
        relics = sfsPlayer.relics_actual;
        starpower = sfsPlayer.starpower_actual;
        wildcards = sfsPlayer.egg_wildcards_actual;
        medals = sfsPlayer.battle.medals;
        level = sfsPlayer.level;
        xp = sfsPlayer.xp;

        profileDisplay = ProfileDisplay.buildProfileDisplay(catalog, sfsPlayer.profile.data);

        friendCode = StringUtil.formatFriendCode(sfsPlayer.profile.friend_code);

        for (var sfsIsland : sfsPlayer.islands) {
            long userIslandId = sfsIsland.user_island_id;
            if (islands.containsKey(userIslandId)) islands.get(userIslandId).internalRefresh(sfsIsland, false);
            else islands.put(userIslandId, new Island(this, sfsIsland));
        }

        for (SfsFriend sfsFriend : getFriendsResponse.friends) {
            friends.add(Friend.buildFriend(catalog, sfsFriend));
        }

        for (SfsFriendRequest sfsFriendRequest : getFriendsResponse.pending) {
            pendingOutgoingFriendRequests.add(FriendRequest.buildFriendRequest(catalog, sfsFriendRequest));
        }

        activeIslandLock.writeLock().lock();
        try {
            activeIsland = null;
            for (Island island : islands.values()) {
                if (island.getUserIslandId() == sfsPlayer.active_island) {
                    activeIsland = island;
                    break;
                }
            }
            if (activeIsland == null) {
                throw new ClientIllegalStateException("Could not match active island from fetched player data");
            }
        }
        finally {
            activeIslandLock.writeLock().unlock();
        }

    }

    public WebsocketClient getWebsocketClient() {
        return wsClient;
    }

    public EventHandlerManager getEventHandlerManager() {
        return eventHandlerManager;
    }

    public LocalizedTextManager getLocalizedTextManager() {
        return localizedTextManager;
    }

    public Catalog getCatalog() {
        return catalog;
    }

    public ElementCatalog getElementCatalog() {
        return catalog.getElementCatalog();
    }

    public MonsterCatalog getMonsterCatalog() {
        return catalog.getMonsterCatalog();
    }

    public StructureCatalog getStructureCatalog() {
        return catalog.getStructureCatalog();
    }

    public BakingCatalog getBakingCatalog() {
        return catalog.getBakingCatalog();
    }

    public synchronized void update(SfsGenericUpdate update) {
        coins = update.coins_actual;
        diamonds = update.diamonds_actual;
        food = update.food_actual;
        shards = update.ethereal_currency_actual;
        keys = update.keys_actual;
        relics = update.relics_actual;
        starpower = update.starpower_actual;
        medals = update.medals;
        xp = update.xp;
        level = update.level;
    }

    public void setDefaultLanguage(Language language) {
        localizedTextManager.setDefaultLanguage(language);
    }

    public void setActiveIsland(long userIslandId) throws InterruptedException, ClientException {
        activeIslandLock.writeLock().lock();
        try {
            // API returns failed response with no error message if
            // you try to switch to the island you're already on
            if (userIslandId == activeIsland.getUserIslandId()) return;

            ChangeIslandRequest request = new ChangeIslandRequest();
            request.user_island_id = userIslandId;
            ChangeIslandResponse response = (ChangeIslandResponse)wsClient.request(request, ChangeIslandResponse.class);
            activeIsland = getIsland(response.user_island_id);
        }
        finally {
            activeIslandLock.writeLock().unlock();
        }
    }

    public void setActiveIsland(IslandType islandType) throws InterruptedException, ClientException {
        activeIslandLock.writeLock().lock();
        try {
            Island island = getIsland(islandType);
            if (island == null) {
                throw new ClientIllegalArgumentException("Cannot switch to unowned island " + islandType);
            }
            setActiveIsland(island);
        }
        finally {
            activeIslandLock.writeLock().unlock();
        }
    }

    public void setActiveIsland(ReadableIsland island) throws InterruptedException, ClientException {
        setActiveIsland(island.getUserIslandId());
    }

    public Island getActiveIsland() {
        activeIslandLock.readLock().lock();
        try {
            return activeIsland;
        }
        finally {
            activeIslandLock.readLock().unlock();
        }
    }

    public <R> R withActiveIsland(ReadableIsland island, ClientRunnable<R> action)
            throws InterruptedException, ClientException {
        activeIslandLock.writeLock().lock();
        try {
            setActiveIsland(island);
            activeIslandLock.readLock().lock();
        }
        finally {
            activeIslandLock.writeLock().unlock();
        }
        try {
            return action.run();
        }
        finally {
            activeIslandLock.readLock().unlock();
        }
    }

    public <R> R withActiveIsland(ClientConsumer<Island, R> action) throws InterruptedException, ClientException {
        activeIslandLock.readLock().lock();
        try {
            return action.run(activeIsland);
        }
        finally {
            activeIslandLock.readLock().unlock();
        }
    }

    public synchronized SFSObject getPlayerSfsData() {
        return playerSfsData;
    }

    public synchronized int getUserId() {
        return userId;
    }

    public synchronized String getCountry() {
        return country;
    }

    public synchronized String getDisplayName() {
        return displayName;
    }

    public synchronized long getCoins() {
        return coins;
    }

    public synchronized long getDiamonds() {
        return diamonds;
    }

    public synchronized long getShards() {
        return shards;
    }

    public synchronized long getFood() {
        return food;
    }

    public synchronized long getKeys() {
        return keys;
    }

    public synchronized long getRelics() {
        return relics;
    }

    public synchronized long getStarpower() {
        return starpower;
    }

    public synchronized long getWildcards() {
        return wildcards;
    }

    public synchronized long getMedals() {
        return medals;
    }

    public synchronized int getLevel() {
        return level;
    }

    public synchronized int getXp() {
        return xp;
    }

    public synchronized MultiCurrencyValue getCurrency() {
        return new MultiCurrencyValue(
                coins,
                food,
                keys,
                relics,
                diamonds,
                starpower,
                medals,
                shards,
                wildcards
        );
    }

    public synchronized ProfileDisplay getProfileDisplay() {
        return profileDisplay;
    }

    public synchronized String getFriendCode() {
        return friendCode;
    }

    public synchronized List<Island> getIslands() {
        return islands.values().stream().toList();
    }

    public Island getIsland(IslandType islandType) {
        return getIslands().stream().filter(i -> i.getIslandType() == islandType)
                .findFirst().orElse(null);
    }

    public Island getIsland(long userIslandId) {
        return getIslands().stream().filter(i -> i.getUserIslandId() == userIslandId)
                .findFirst().orElse(null);
    }

    public Monster getMonster(long userMonsterId) {
        for (Island island : getIslands()) {
            Monster monster = island.getMonster(userMonsterId);
            if (monster != null) return monster;
        }
        return null;
    }

    public synchronized List<Friend> getFriends() {
        return Collections.unmodifiableList(friends);
    }

    public synchronized Friend getFriend(String friendCode) {
        String formatted = StringUtil.formatFriendCode(friendCode);
        return friends.stream().filter(f -> f.getFriendCode().equals(formatted)).findFirst().orElse(null);
    }

    public synchronized Friend getFriend(int userId) {
        return friends.stream().filter(f -> f.getUserId() == userId).findFirst().orElse(null);
    }

    public synchronized List<FriendRequest> getPendingOutgoingFriendRequests() {
        return Collections.unmodifiableList(pendingOutgoingFriendRequests);
    }

    public Island buyIsland(int typeId, @NotNull String name) throws InterruptedException, ClientException {
        return eventHandlerManager.getBuyIslandHandler().request(typeId, name);
    }

    public Island buyIsland(int typeId) throws InterruptedException, ClientException {
        return buyIsland(typeId, "");
    }

    public Island buyIsland(IslandType islandType, String name) throws InterruptedException, ClientException {
        return buyIsland(islandType.getId(), name);
    }

    public Island buyIsland(IslandType islandType) throws InterruptedException, ClientException {
        return buyIsland(islandType.getId());
    }

    public SendFriendRequestResult sendFriendRequest(String friendCode) throws InterruptedException, ClientException {
        return eventHandlerManager.getRequestFriendHandler().request(friendCode);
    }

    public VisitData visitUser(long userId) throws InterruptedException, ClientException {
        return eventHandlerManager.getVisitFriendHandler().request(userId);
    }

    public VisitData visitUser(User user) throws InterruptedException, ClientException {
        return visitUser(user.getUserId());
    }

}