package com.glisterbyte.singingmonsters.sfsmodels.requests;

import com.glisterbyte.singingmonsters.sfsmapping.SfsCmd;
import com.glisterbyte.singingmonsters.sfsmodels.SfsRequestModel;

/**
 * On islands that have nurseries, this hatches an egg that has finished incubating.
 * On islands that do not have nurseries, this is used directly to buy a monster.
 *
 * @additionalRequest After a successful response, the client should send MultiUpdateMonsterRequest.
 */
@SfsCmd("gs_hatch_egg")
public class HatchEggRequest extends SfsRequestModel {

    public int costume;
    public int flip;
    public int pos_x;
    public int pos_y;
    public boolean store_in_hotel;

    /**
     * On islands that have nurseries, the nursery egg's user egg ID.
     * On islands without nurseries, the monster species ID.
     */
    public long user_egg_id;

}