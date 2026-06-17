package com.glisterbyte.singingmonsters.sfsmodels.events;

import com.glisterbyte.singingmonsters.sfsmapping.SfsCmd;
import com.glisterbyte.singingmonsters.sfsmapping.SfsOptional;
import com.glisterbyte.singingmonsters.sfsmodels.SfsCorrelatedResultResponse;

import java.util.Optional;

/**
 * @additionalResponse On success, UpdateMonsterEvent is also sent.
 */
@SfsCmd("gs_box_add_egg")
public class BoxEggResponse extends SfsCorrelatedResultResponse {

    public Long user_egg_id;
    public long user_monster_id;
    public boolean underling;
    public long dest_island_id;
    public int egg_type;
    public boolean isWublin;

    @SfsOptional
    public Boolean has_all;

    @Override
    public Optional<Long> getCorrelationId() {
        return Optional.ofNullable(user_egg_id);
    }

}