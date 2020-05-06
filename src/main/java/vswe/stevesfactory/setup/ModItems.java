package vswe.stevesfactory.setup;

import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.Item.Properties;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import vswe.stevesfactory.api.StevesFactoryManagerAPI;

public final class ModItems {

    private ModItems() {
    }

    public static final ItemGroup creativeTab = new ItemGroup(StevesFactoryManagerAPI.MODID) {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(ModBlocks.factoryManagerBlock.get());
        }
    };

    public static Properties defaultItemProperties() {
        return new Properties().group(creativeTab);
    }

    public static DeferredRegister<Item> items = new DeferredRegister<>(ForgeRegistries.ITEMS, StevesFactoryManagerAPI.MODID);

    public static RegistryObject<BlockItem> factoryManagerItem = items.register("factory_manager", () -> new BlockItem(ModBlocks.factoryManagerBlock.get(), defaultItemProperties()));
    public static RegistryObject<BlockItem> cableItem = items.register("cable", () -> new BlockItem(ModBlocks.cableBlock.get(), defaultItemProperties()));
    public static RegistryObject<BlockItem> redstoneEmitterItem = items.register("redstone_emitter", () -> new BlockItem(ModBlocks.redstoneEmitterBlock.get(), defaultItemProperties()));
    public static RegistryObject<BlockItem> redstoneInputItem = items.register("redstone_input", () -> new BlockItem(ModBlocks.redstoneInputBlock.get(), defaultItemProperties()));
    public static RegistryObject<BlockItem> itemIntakeItem = items.register("item_intake", () -> new BlockItem(ModBlocks.itemIntakeBlock.get(), defaultItemProperties()));
    public static RegistryObject<BlockItem> instantItemIntakeItem = items.register("instant_item_intake", () -> new BlockItem(ModBlocks.instantItemIntakeBlock.get(), defaultItemProperties()));
    public static RegistryObject<BlockItem> budItem = items.register("bud", () -> new BlockItem(ModBlocks.budBlock.get(), defaultItemProperties()));
    public static RegistryObject<BlockItem> blockInteractorItem = items.register("block_interactor", () -> new BlockItem(ModBlocks.blockInteractorBlock.get(), defaultItemProperties()));
    public static RegistryObject<BlockItem> worldInteractorItem = items.register("world_interactor", () -> new BlockItem(ModBlocks.worldInteractorBlock.get(), defaultItemProperties()));
    public static RegistryObject<BlockItem> signUpdaterItem = items.register("sign_updater", () -> new BlockItem(ModBlocks.signUpdaterBlock.get(), defaultItemProperties()));
}
