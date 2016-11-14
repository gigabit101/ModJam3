package vswe.stevesfactory.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import vswe.stevesfactory.StevesFactoryManager;
import vswe.stevesfactory.init.ModBlocks;
import vswe.stevesfactory.tiles.TileEntityInput;

import java.util.Random;

public class BlockCableInput extends BlockContainer
{
    public BlockCableInput()
    {
        super(Material.IRON);
        setCreativeTab(ModBlocks.creativeTab);
        setSoundType(SoundType.METAL);
        setUnlocalizedName(StevesFactoryManager.UNLOCALIZED_START + ModBlocks.CABLE_INPUT_UNLOCALIZED_NAME);
        setHardness(1.2F);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta)
    {
        return new TileEntityInput();
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state)
    {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side)
    {
        return true;
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state)
    {
//        System.out.print("onBlockAdded");
        updateRedstone(world, pos);
        super.onBlockAdded(world, pos, state);
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn)
    {
//        System.out.print("onNeighborChange");
        updateRedstone(world, pos);
        super.neighborChanged(state, world, pos, blockIn);
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand)
    {
        super.updateTick(worldIn, pos, state, rand);
    }

    @Override
    public boolean requiresUpdates()
    {
        return super.requiresUpdates();
    }


    @Override
    public boolean getWeakChanges(IBlockAccess world, BlockPos pos)
    {
//        System.out.print("getWeakChanges");
        updateRedstone(world, pos);
        return super.getWeakChanges(world, pos);
    }

    @Override
    public boolean shouldCheckWeakPower(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side)
    {
//        System.out.print("getWeakChanges");
//        updateRedstone(world, pos);
        return super.shouldCheckWeakPower(state, world, pos, side);
    }

    private void updateRedstone(World world, BlockPos pos)
    {
        TileEntityInput input = (TileEntityInput) world.getTileEntity(pos);
//        System.out.print(" updateRedstone");
        if (input != null)
        {
            input.triggerRedstone();
        }
    }

    private void updateRedstone(IBlockAccess world, BlockPos pos)
    {
        TileEntityInput input = (TileEntityInput) world.getTileEntity(pos);
//        System.out.print(" updateRedstone");
        if (input != null)
        {
            input.triggerRedstone();
        }
    }
}
