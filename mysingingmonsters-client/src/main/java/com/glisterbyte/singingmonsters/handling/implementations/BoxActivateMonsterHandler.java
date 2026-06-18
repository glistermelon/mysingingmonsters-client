package com.glisterbyte.singingmonsters.handling.implementations;

import com.glisterbyte.singingmonsters.common.StringUtil;
import com.glisterbyte.singingmonsters.exceptions.ClientException;
import com.glisterbyte.singingmonsters.exceptions.ClientHandlingException;
import com.glisterbyte.singingmonsters.exceptions.ClientIllegalArgumentException;
import com.glisterbyte.singingmonsters.handling.CorrelatedResultResponseHandler;
import com.glisterbyte.singingmonsters.handling.EventHandlerInitArg;
import com.glisterbyte.singingmonsters.handling.Request;
import com.glisterbyte.singingmonsters.main.monster.eggbox.EggBoxMonster;
import com.glisterbyte.singingmonsters.main.monster.Monster;
import com.glisterbyte.singingmonsters.sfsmodels.requests.BoxActivateMonsterRequest;
import com.glisterbyte.singingmonsters.sfsmodels.events.BoxActivateMonsterResponse;

/*
    TODO: Successful activation has not been tested.
 */

public class BoxActivateMonsterHandler extends CorrelatedResultResponseHandler<Void, BoxActivateMonsterResponse, Void> {

    public BoxActivateMonsterHandler(EventHandlerInitArg arg) {
        super(arg, BoxActivateMonsterResponse.class);
    }

    @Override
    public Void handleSuccess(BoxActivateMonsterResponse event, Void requestData) throws InterruptedException, ClientException {
        Monster monster = client.getMonster(event.user_monster_id);
        if (monster.getSpecies().isWubbox()) {
            throw new ClientIllegalArgumentException("Activating wubboxes is not supported yet");
        }
        else if (monster instanceof EggBoxMonster eggBoxMonster) {
            eggBoxMonster.getEventHandler().handleActivateEvent(event);
        }
        else {
            throw new ClientHandlingException(StringUtil.format(
                    "Received activate monster event for non-egg-box-monster {}", monster
            ));
        }
        return null;
    }

    public void request(Monster monster) throws InterruptedException, ClientException {

        long userMonsterId = monster.getUserMonsterId();

        BoxActivateMonsterRequest model = new BoxActivateMonsterRequest();
        model.user_monster_id = userMonsterId;
        model.validate_only = 0;

        Request request = new Request(client);
        var activateFuture = request.expectResponse(this, userMonsterId);
        var updateFuture = request.expectResponse(handlerManager.getUpdateMonsterHandler(), userMonsterId);

        activateFuture.whenComplete((x, ex) -> {
            if (ex != null) updateFuture.completeExceptionally(ex);
        });

        request.run(model, monster.getIsland());

        handlerManager.getMultiMonsterUpdateHandler().consider(monster, null);

    }

}