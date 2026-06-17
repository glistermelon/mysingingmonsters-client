package com.glisterbyte.singingmonsters.sfsmodels.requests;

import com.glisterbyte.singingmonsters.sfsmapping.SfsCmd;
import com.glisterbyte.singingmonsters.sfsmodels.SfsRequestModel;

/**
 * Moves and scales a structure and sets its volume.
 *
 * @additionalRequest After a successful response, the client should send MultiUpdateMonsterRequest.
 */
@SfsCmd("gs_move_structure")
public class MoveStructureRequest extends SfsRequestModel {
    public long user_structure_id;
    public int pos_x;
    public int pos_y;
    public double scale;
    public float volume;
}