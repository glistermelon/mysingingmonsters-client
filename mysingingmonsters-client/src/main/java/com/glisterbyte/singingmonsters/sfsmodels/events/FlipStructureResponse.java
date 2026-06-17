package com.glisterbyte.singingmonsters.sfsmodels.events;

import com.glisterbyte.singingmonsters.sfsmapping.SfsCmd;
import com.glisterbyte.singingmonsters.sfsmodels.SfsResultResponse;

/**
 * @additionalResponse On success, UpdateStructureEvent is also sent.
 */
@SfsCmd("gs_flip_structure")
public class FlipStructureResponse extends SfsResultResponse { }