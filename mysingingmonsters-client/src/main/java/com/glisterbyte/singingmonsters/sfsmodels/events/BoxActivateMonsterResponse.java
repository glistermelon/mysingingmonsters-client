package com.glisterbyte.singingmonsters.sfsmodels.events;

import com.glisterbyte.singingmonsters.sfsmapping.SfsCmd;
import com.glisterbyte.singingmonsters.sfsmodels.SfsCorrelatedResultResponse;

import java.util.Optional;

/**
 * @additionalResponse On success, UpdateMonsterEvent is also sent.
 */
@SfsCmd("gs_box_activate_monster")
public class BoxActivateMonsterResponse extends SfsCorrelatedResultResponse {

    public Long user_monster_id;

    @Override
    public Optional<Long> getCorrelationId() {
        return Optional.ofNullable(user_monster_id);
    }

}