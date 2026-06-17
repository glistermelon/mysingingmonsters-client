package com.glisterbyte.singingmonsters.sfsmodels.events;

import com.glisterbyte.singingmonsters.sfsmapping.SfsCmd;
import com.glisterbyte.singingmonsters.sfsmapping.SfsOptional;
import com.glisterbyte.singingmonsters.sfsmodels.SfsCorrelatedResultResponse;

import java.util.Optional;

/**
 * @additionalResponse On success, UpdateMonsterEvent is also sent.
 */
@SfsCmd("gs_collect_monster")
public class CollectMonsterEvent extends SfsCorrelatedResultResponse {

    // I've only seen 'coins'; I'm hypothesizing these other values are possible and have these types.

    @SfsOptional
    public Integer coins;

    @SfsOptional
    public Integer food;

    @SfsOptional
    public Integer keys;

    @SfsOptional
    public Integer relics;

    @SfsOptional
    public Integer diamonds;

    @SfsOptional
    public Integer starpower;

    @SfsOptional
    public Integer medals;

    @SfsOptional
    public Integer ethereal_currency;

    @SfsOptional
    public Integer egg_wildcards;

    public Long user_monster_id;

    @Override
    public Optional<Long> getCorrelationId() {
        return Optional.ofNullable(user_monster_id);
    }

}