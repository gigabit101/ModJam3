package vswe.stevesfactory.blocks;


import gigabit101.AdvancedSystemManager2.blocks.TileEntityIntake;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import gigabit101.AdvancedSystemManager2.blocks.BlockCableDirectionAdvanced;
import gigabit101.AdvancedSystemManager2.blocks.TileEntityClusterElement;

//This is indeed not a subclass to the cable, you can't relay signals through this block
public class BlockCableIntake extends BlockCableDirectionAdvanced {

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new gigabit101.AdvancedSystemManager2.blocks.TileEntityIntake();
    }

    @Override
    protected Class<? extends TileEntityClusterElement> getTeClass() {
        return TileEntityIntake.class;
    }

}
