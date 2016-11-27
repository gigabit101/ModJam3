package vswe.stevesfactory.blocks;

import vswe.stevesfactory.api.ICable;
import vswe.stevesfactory.init.ModBlocks;
import vswe.stevesfactory.lib.ModInfo;

public class BlockCable extends BlockSFM implements ICable
{
    public BlockCable()
    {
        setUnlocalizedName(ModInfo.UNLOCALIZED_START + ModBlocks.CABLE_UNLOCALIZED_NAME);
        setUpdateInventorys(true);
    }
}
