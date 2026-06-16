package com.glisterbyte.singingmonsters.handling.implementations;

import com.glisterbyte.singingmonsters.exceptions.ClientException;
import com.glisterbyte.singingmonsters.handling.EventHandlerInitArg;
import com.glisterbyte.singingmonsters.handling.Request;
import com.glisterbyte.singingmonsters.handling.UncorrelatedResultResponseHandler;
import com.glisterbyte.singingmonsters.main.monster.Monster;
import com.glisterbyte.singingmonsters.sfsmodels.requests.FeedMonsterRequest;
import com.glisterbyte.singingmonsters.sfsmodels.events.FeedMonsterResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FeedMonsterHandler extends UncorrelatedResultResponseHandler<Long, FeedMonsterResponse, Void> {

    private static final Logger logger = LoggerFactory.getLogger(FeedMonsterHandler.class);

    public FeedMonsterHandler(EventHandlerInitArg arg) {
        super(arg, FeedMonsterResponse.class);
    }

    @Override
    public Void handleSuccess(FeedMonsterResponse event, Long userMonsterId) throws InterruptedException, ClientException {
        /*
            Event doesn't actually do anything besides signal success or failure,
            but MonsterImpl does some approximated updates anyway
         */
        if (userMonsterId != null) client.getMonster(userMonsterId).getEventHandler().handleFeedEvent(event);
        else logger.error("FeedMonsterHandler request data (user monster id) is null");
        return null;
    }

    public void request(Monster monster) throws InterruptedException, ClientException {

        long userMonsterId = monster.getUserMonsterId();

        FeedMonsterRequest model = new FeedMonsterRequest();
        model.user_monster_id = userMonsterId;

        Request request = new Request(client);
        var updateFuture = request.expectResponse(handlerManager.getUpdateMonsterHandler(), userMonsterId);
        request.expectResponse(this, userMonsterId)
                .whenComplete((x, ex) -> {
                    if (ex != null) updateFuture.completeExceptionally(ex);
                });

        request.run(model, monster.getIsland());

    }

}