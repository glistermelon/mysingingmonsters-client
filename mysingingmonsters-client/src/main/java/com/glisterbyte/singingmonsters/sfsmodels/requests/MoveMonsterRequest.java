package com.glisterbyte.singingmonsters.sfsmodels.requests;

import com.glisterbyte.singingmonsters.sfsmapping.SfsCmd;
import com.glisterbyte.singingmonsters.sfsmodels.SfsRequestModel;

/**
 * Moves a monster and sets its volume.
 *
 * @additionalRequest After a successful response, the client should send MultiUpdateMonsterRequest.
 */
@SfsCmd("gs_move_monster")
public class MoveMonsterRequest extends SfsRequestModel {
    public long user_monster_id;
    public int pos_x;
    public int pos_y;
    public double volume;
}