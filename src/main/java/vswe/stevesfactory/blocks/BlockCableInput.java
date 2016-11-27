package vswe.stevesfactory.blocks;

import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import vswe.stevesfactory.init.ModBlocks;
import vswe.stevesfactory.lib.ModInfo;
import vswe.stevesfactory.tiles.TileEntityInput;

public class BlockCableInput extends BlockSFM
{
    public BlockCableInput()
    {
        setUnlocalizedName(ModInfo.UNLOCALIZED_START + ModBlocks.CABLE_INPUT_UNLOCALIZED_NAME);
        setUpdateInventorys(true);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta)
    {
        return new TileEntityInput();
    }

    @Override
    public boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side)
    {
        return true;
    }

    @Override
    public boolean requiresUpdates()
    {
        return super.requiresUpdates();
    }


    @Override
    public boolean getWeakChanges(IBlockAccess world, BlockPos pos)
    {
        updateRedstone(world, pos);
        return super.getWeakChanges(world, pos);
    }


    private void updateRedstone(IBlockAccess world, BlockPos pos)
    {
        TileEntityInput input = (TileEntityInput) world.getTileEntity(pos);
        if (input != null)
        {
            input.triggerRedstone();
        }
    }
}
