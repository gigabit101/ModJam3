package vswe.stevesfactory.blocks;

import gigabit101.AdvancedSystemManager2.blocks.ModBlocks;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.world.World;
import gigabit101.AdvancedSystemManager2.AdvancedSystemManager2;
import gigabit101.AdvancedSystemManager2.blocks.TileEntityCreative;


public class BlockCableCreative extends BlockContainer {
    public BlockCableCreative() {
        super(Material.IRON);
        setCreativeTab(gigabit101.AdvancedSystemManager2.blocks.ModBlocks.creativeTab);
        setSoundType(SoundType.METAL);
        setUnlocalizedName(AdvancedSystemManager2.UNLOCALIZED_START + ModBlocks.CABLE_CREATIVE_UNLOCALIZED_NAME);
        setHardness(1.2F);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityCreative();
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }
}