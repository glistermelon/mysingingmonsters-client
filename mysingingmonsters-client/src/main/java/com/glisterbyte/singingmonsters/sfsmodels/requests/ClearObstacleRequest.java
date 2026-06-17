package com.glisterbyte.singingmonsters.sfsmodels.requests;

import com.glisterbyte.singingmonsters.sfsmapping.SfsCmd;
import com.glisterbyte.singingmonsters.sfsmodels.SfsRequestModel;

/**
 * Finishes clearing of an obstacle.
 * Fails if the removal timer hasn't finished yet.
 *
 * @additionalRequest After a successful response, the client should send MultiUpdateMonsterRequest.
 */
@SfsCmd("gs_clear_obstacle")
public class ClearObstacleRequest extends SfsRequestModel {
    public long user_structure_id;
}