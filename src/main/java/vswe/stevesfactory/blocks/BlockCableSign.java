package vswe.stevesfactory.blocks;

import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import vswe.stevesfactory.init.ModBlocks;
import vswe.stevesfactory.lib.ModInfo;
import vswe.stevesfactory.tiles.TileEntityCluster;
import vswe.stevesfactory.tiles.TileEntitySignUpdater;

public class BlockCableSign extends BlockSFM
{
    public BlockCableSign()
    {
        setUnlocalizedName(ModInfo.UNLOCALIZED_START + ModBlocks.CABLE_SIGN_UNLOCALIZED_NAME);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta)
    {
        return new TileEntitySignUpdater();
    }

    public static final IProperty FACING = PropertyDirection.create("facing");


    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, FACING);
    }

    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        return getDefaultState().withProperty(FACING, EnumFacing.getFront(meta));
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        return ((EnumFacing) state.getValue(FACING)).getIndex();
    }


    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase entity, ItemStack item)
    {
        int meta = BlockPistonBase.getFacingFromEntity(pos, entity).getIndex();

        TileEntitySignUpdater sign = TileEntityCluster.getTileEntity(TileEntitySignUpdater.class, world, pos);
        if (sign != null)
        {
            sign.setMetaData(meta);
        }
    }
}
