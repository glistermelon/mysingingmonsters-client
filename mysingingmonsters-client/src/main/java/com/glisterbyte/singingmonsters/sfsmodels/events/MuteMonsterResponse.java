package com.glisterbyte.singingmonsters.sfsmodels.events;

import com.glisterbyte.singingmonsters.sfsmapping.SfsCmd;
import com.glisterbyte.singingmonsters.sfsmodels.SfsResultResponse;

/**
 * @additionalResponse On success, UpdateMonsterEvent is also sent.
 */
@SfsCmd("gs_mute_monster")
public class MuteMonsterResponse extends SfsResultResponse { }