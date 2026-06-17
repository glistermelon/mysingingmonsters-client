package com.glisterbyte.singingmonsters.sfsmodels.requests;

import com.glisterbyte.singingmonsters.sfsmapping.SfsCmd;
import com.glisterbyte.singingmonsters.sfsmodels.SfsRequestModel;

/**
 * Buys and places a structure.
 *
 * @additionalRequest After a successful response, the client should send MultiUpdateMonsterRequest.
 */
@SfsCmd("gs_buy_structure")
public class BuyStructureRequest extends SfsRequestModel {

    public int flip;
    public int pos_x;
    public int pos_y;
    public long quest_claim_id;
    public double scale;
    public boolean starpower_purchase;

    /**
     * Structure type ID
     */
    public int structure_id;

}