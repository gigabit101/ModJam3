package vswe.stevesfactory.blocks;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import vswe.stevesfactory.tiles.TileEntityClusterElement;
import vswe.stevesfactory.tiles.TileEntityIntake;

public class BlockCableIntake extends BlockCableDirectionAdvanced
{
    @Override
    public TileEntity createNewTileEntity(World world, int meta)
    {
        return new TileEntityIntake();
    }

    @Override
    protected Class<? extends TileEntityClusterElement> getTeClass()
    {
        return TileEntityIntake.class;
    }

}
