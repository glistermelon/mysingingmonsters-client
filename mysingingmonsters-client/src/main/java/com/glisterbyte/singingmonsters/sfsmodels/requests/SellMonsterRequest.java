package com.glisterbyte.singingmonsters.sfsmodels.requests;

import com.glisterbyte.singingmonsters.sfsmapping.SfsCmd;
import com.glisterbyte.singingmonsters.sfsmodels.SfsRequestModel;

/**
 * Sells a monster.
 *
 * @additionalRequest After a successful response, the client should send MultiUpdateMonsterRequest.
 */
@SfsCmd("gs_sell_monster")
public class SellMonsterRequest extends SfsRequestModel {
    public long user_monster_id;
    public boolean pure_destroy;
}