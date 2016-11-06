package vswe.stevesfactory.blocks;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import gigabit101.AdvancedSystemManager2.AdvancedSystemManager2;
import gigabit101.AdvancedSystemManager2.blocks.ModBlocks;


public class ItemCamouflage  extends ItemBlock {

    public ItemCamouflage(Block block) {
        super(block);
        setHasSubtypes(true);
        setMaxDamage(0);
    }

    @Override
    public String getUnlocalizedName(ItemStack item) {
        return "tile." + AdvancedSystemManager2.UNLOCALIZED_START + gigabit101.AdvancedSystemManager2.blocks.TileEntityCamouflage.CamouflageType.values()[ModBlocks.blockCableCamouflage.getId(item.getItemDamage())].getUnlocalized();
    }

}
