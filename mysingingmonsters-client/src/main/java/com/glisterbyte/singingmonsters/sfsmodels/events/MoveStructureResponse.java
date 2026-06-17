package com.glisterbyte.singingmonsters.sfsmodels.events;

import com.glisterbyte.singingmonsters.sfsmapping.SfsCmd;
import com.glisterbyte.singingmonsters.sfsmodels.SfsResultResponse;

/**
 * @additionalResponse On success, UpdateStructureEvent is also sent.
 */
@SfsCmd("gs_move_structure")
public class MoveStructureResponse extends SfsResultResponse { }