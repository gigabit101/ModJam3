package vswe.stevesfactory.init;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;

/**
 * Created by Gigabit101 on 27/11/2016.
 */
public class ModRecipes
{
    public static void init()
    {
        GameRegistry.addRecipe(new ItemStack(ModBlocks.blockManager),
                "III",
                "IRI",
                "SPS",
                'R', Blocks.REDSTONE_BLOCK,
                'P', Blocks.PISTON,
                'I', Items.IRON_INGOT,
                'S', Blocks.STONE
        );

        GameRegistry.addRecipe(new ItemStack(ModBlocks.blockCable, 8),
                "GPG",
                "IRI",
                "GPG",
                'R', Items.REDSTONE,
                'G', Blocks.GLASS,
                'I', Items.IRON_INGOT,
                'P', Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE
        );

        GameRegistry.addShapelessRecipe(new ItemStack(ModBlocks.blockCableRelay, 1),
                ModBlocks.blockCable,
                Blocks.HOPPER
        );

        GameRegistry.addShapelessRecipe(new ItemStack(ModBlocks.blockCableOutput, 1),
                ModBlocks.blockCable,
                Items.REDSTONE,
                Items.REDSTONE,
                Items.REDSTONE
        );


        GameRegistry.addShapelessRecipe(new ItemStack(ModBlocks.blockCableInput, 1),
                ModBlocks.blockCable,
                Items.REDSTONE
        );

        GameRegistry.addShapelessRecipe(new ItemStack(ModBlocks.blockCableRelay, 1, 8),
                new ItemStack(ModBlocks.blockCableRelay, 1, 0),
                new ItemStack(Items.DYE, 1, 4)
        );

        GameRegistry.addShapelessRecipe(new ItemStack(ModBlocks.blockCableIntake, 1, 0),
                ModBlocks.blockCable,
                Blocks.HOPPER,
                Blocks.HOPPER,
                Blocks.DROPPER
        );

        GameRegistry.addShapelessRecipe(new ItemStack(ModBlocks.blockCableBUD, 1),
                ModBlocks.blockCable,
                Items.QUARTZ,
                Items.QUARTZ,
                Items.QUARTZ
        );


        GameRegistry.addShapelessRecipe(new ItemStack(ModBlocks.blockCableBreaker, 1),
                ModBlocks.blockCable,
                Items.IRON_PICKAXE,
                Blocks.DISPENSER
        );

        GameRegistry.addShapelessRecipe(new ItemStack(ModBlocks.blockCableIntake, 1, 8),
                new ItemStack(ModBlocks.blockCableIntake, 1, 0),
                Items.GOLD_INGOT
        );

        GameRegistry.addShapelessRecipe(new ItemStack(ModBlocks.blockCableCluster, 1),
                ModBlocks.blockCable,
                Items.ENDER_PEARL,
                Items.ENDER_PEARL,
                Items.ENDER_PEARL
        );

        GameRegistry.addShapelessRecipe(new ItemStack(ModBlocks.blockCableCamouflage, 1, 0),
                ModBlocks.blockCable,
                new ItemStack(Blocks.WOOL, 1, 14),
                new ItemStack(Blocks.WOOL, 1, 13),
                new ItemStack(Blocks.WOOL, 1, 11)
        );

        GameRegistry.addShapelessRecipe(new ItemStack(ModBlocks.blockCableCamouflage, 1, 1),
                new ItemStack(ModBlocks.blockCableCamouflage, 1, 0),
                new ItemStack(ModBlocks.blockCableCamouflage, 1, 0),
                Blocks.IRON_BARS,
                Blocks.IRON_BARS
        );

        GameRegistry.addShapelessRecipe(new ItemStack(ModBlocks.blockCableCamouflage, 1, 2),
                new ItemStack(ModBlocks.blockCableCamouflage, 1, 1),
                Blocks.STICKY_PISTON
        );


        GameRegistry.addShapelessRecipe(new ItemStack(ModBlocks.blockCableSign, 1),
                ModBlocks.blockCable,
                new ItemStack(Items.DYE, 0),
                Items.FEATHER
        );

        GameRegistry.addRecipe(new ClusterUpgradeRecipe());
        GameRegistry.addRecipe(new ClusterRecipe());
    }
}
