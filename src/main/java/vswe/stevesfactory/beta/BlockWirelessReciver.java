package vswe.stevesfactory.beta;

import vswe.stevesfactory.api.ICable;
import vswe.stevesfactory.blocks.BlockSFM;

/**
 * Created by Gigabit101 on 27/11/2016.
 */
public class BlockWirelessReciver extends BlockSFM implements ICable
{
    public BlockWirelessReciver()
    {
        setUpdateInventorys(true);
        setUnlocalizedName("wirelessreciver");
    }
}
