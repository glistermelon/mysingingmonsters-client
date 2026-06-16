package com.glisterbyte.singingmonsters.main.monster.eggbox;

import com.glisterbyte.singingmonsters.exceptions.ActionFailedException;
import com.glisterbyte.singingmonsters.exceptions.ClientException;
import com.glisterbyte.singingmonsters.main.island.Island;
import com.glisterbyte.singingmonsters.main.monster.Monster;
import com.glisterbyte.singingmonsters.sfsmodels.data.SfsMonster;
import com.glisterbyte.singingmonsters.sfsmodels.events.BoxActivateMonsterResponse;
import com.glisterbyte.singingmonsters.sfsmodels.events.UpdateMonsterEvent;
import org.apache.commons.lang3.Validate;

import java.time.Duration;
import java.time.Instant;

public class EggBoxMonster extends Monster implements ReadableEggBoxMonster, ControllableEggBoxMonster {

    private EggBoxMonsterData data;

    @Override
    public void internalRefresh(SfsMonster sfsMonster, boolean first) {
        super.internalRefresh(sfsMonster, first);
        data = new EggBoxMonsterData(this, sfsMonster);
    }

    public EggBoxMonster(Island island, SfsMonster sfsMonster) {
        super(island, sfsMonster);
    }

    public class EventHandler extends Monster.EventHandler {

        @Override
        public void handleUpdateEvent(UpdateMonsterEvent event) {
            super.handleUpdateEvent(event);
            synchronized (EggBoxMonster.this) {
                if (event.egg_timer_start != null) data.updateFillTimer(event.egg_timer_start);
                if (event.boxed_eggs != null) data.updateZappedEggs(event.boxed_eggs);
                if (event.random_underling_collection_min != null) {
                    Validate.isTrue(
                            event.last_collection != null,
                            "Expected update monster event to have last collection time for wublin"
                    );
                    Instant nextCollectionTime = Instant.ofEpochMilli(event.last_collection)
                            .plus(Duration.ofMinutes(event.random_underling_collection_min));
                    data.setNextCollectionTime(nextCollectionTime);
                }
            }
        }

        @Override
        public void handleActivateEvent(BoxActivateMonsterResponse event) {
            synchronized (EggBoxMonster.this) {
                EggBoxMonster.super.getEventHandler().handleActivateEvent(event);
                data.removeFillTimer();
            }
        }

    }

    @Override
    public EventHandler getEventHandler() {
        return new EventHandler();
    }

    public synchronized EggBoxMonsterData getInternalEggBoxMonsterData() {
        return data;
    }

    public void activate() throws InterruptedException, ClientException {
        getClient().getEventHandlerManager().getBoxActivateMonsterHandler().request(this);
    }

    public void sellEggs() throws InterruptedException, ClientException {
        if (isActivated()) throw new ActionFailedException("Cannot sell eggs in an activated monster");
        collect();
    }

}