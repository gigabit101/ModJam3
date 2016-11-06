package vswe.stevesfactory.blocks;


import gigabit101.AdvancedSystemManager2.blocks.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import gigabit101.AdvancedSystemManager2.AdvancedSystemManager2;

public class ItemIntake extends ItemBlock {


    public ItemIntake(Block block) {
        super(block);
        setHasSubtypes(true);
        setMaxDamage(0);
    }

    @Override
    public String getUnlocalizedName(ItemStack item) {
        return "tile." + AdvancedSystemManager2.UNLOCALIZED_START + (gigabit101.AdvancedSystemManager2.blocks.ModBlocks.blockCableIntake.isAdvanced(item.getItemDamage()) ? ModBlocks.CABLE_INSTANT_INTAKE_UNLOCALIZED_NAME : ModBlocks.CABLE_INTAKE_UNLOCALIZED_NAME);
    }

}
