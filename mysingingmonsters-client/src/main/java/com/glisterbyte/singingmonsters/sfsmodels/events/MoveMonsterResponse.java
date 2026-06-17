package com.glisterbyte.singingmonsters.sfsmodels.events;

import com.glisterbyte.singingmonsters.sfsmapping.SfsCmd;
import com.glisterbyte.singingmonsters.sfsmodels.SfsResultResponse;

/**
 * @additionalResponse On success, UpdateMonsterEvent is also sent.
 */
@SfsCmd("gs_move_monster")
public class MoveMonsterResponse extends SfsResultResponse { }