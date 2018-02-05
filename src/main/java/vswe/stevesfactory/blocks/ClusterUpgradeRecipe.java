package vswe.stevesfactory.blocks;

import net.minecraft.block.Block;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.util.NonNullList;
import net.minecraftforge.oredict.RecipeSorter;
import vswe.stevesfactory.StevesFactoryManager;
import vswe.stevesfactory.init.ModBlocks;

public class ClusterUpgradeRecipe extends ShapelessRecipes
{
    private static final ItemStack RESULT;
    private static final NonNullList RECIPE;

    static
    {
        RESULT = new ItemStack(ModBlocks.blockCableCluster, 1, 8);
        RECIPE = NonNullList.create();
        RECIPE.add(new ItemStack(ModBlocks.blockCableCluster, 1, 0));
        for (int i = 0; i < 8; i++)
        {
            RECIPE.add(new ItemStack(ModBlocks.blockCable));
        }
    }

    public ClusterUpgradeRecipe()
    {
        super(StevesFactoryManager.MODID + "cluster", RESULT, RECIPE);
        RecipeSorter.register("sfm:clusterupgrade", ClusterUpgradeRecipe.class, RecipeSorter.Category.SHAPELESS, "after:minecraft:shapeless");
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv)
    {
        for (int i = 0; i < inv.getSizeInventory(); i++)
        {
            ItemStack itemStack = inv.getStackInSlot(i);

            if (!itemStack.isEmpty() && Block.getBlockFromItem(itemStack.getItem()) == ModBlocks.blockCableCluster)
            {
                ItemStack copy = itemStack.copy();
                copy.setItemDamage(8);
                return copy;
            }
        }
        return super.getCraftingResult(inv);
    }
}
