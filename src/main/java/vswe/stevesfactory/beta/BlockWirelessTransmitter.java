package vswe.stevesfactory.beta;

import vswe.stevesfactory.api.ICable;
import vswe.stevesfactory.blocks.BlockSFM;

/**
 * Created by Gigabit101 on 27/11/2016.
 */
public class BlockWirelessTransmitter extends BlockSFM implements ICable
{
    public BlockWirelessTransmitter()
    {
        setUpdateInventorys(true);
        setUnlocalizedName("wirelesstrans");
    }
}
