package vswe.stevesfactory.blocks;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import vswe.stevesfactory.init.ModBlocks;
import vswe.stevesfactory.lib.ModInfo;
import vswe.stevesfactory.tiles.TileEntityCreative;

public class BlockCableCreative extends BlockSFM
{
    public BlockCableCreative()
    {
        setUnlocalizedName(ModInfo.UNLOCALIZED_START + ModBlocks.CABLE_CREATIVE_UNLOCALIZED_NAME);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta)
    {
        return new TileEntityCreative();
    }

}