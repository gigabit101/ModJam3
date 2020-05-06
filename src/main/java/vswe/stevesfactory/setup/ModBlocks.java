package vswe.stevesfactory.setup;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import vswe.stevesfactory.api.StevesFactoryManagerAPI;
import vswe.stevesfactory.blocks.*;
import vswe.stevesfactory.render.WorkingAreaRenderer;

@SuppressWarnings("ConstantConditions")
@EventBusSubscriber(modid = StevesFactoryManagerAPI.MODID, bus = Bus.MOD)
public final class ModBlocks {

    private ModBlocks() {
    }

    public static DeferredRegister<Block> blocks = new DeferredRegister<>(ForgeRegistries.BLOCKS, StevesFactoryManagerAPI.MODID);

    public static final RegistryObject<FactoryManagerBlock> factoryManagerBlock = blocks.register(
            "factory_manager",
            () -> new FactoryManagerBlock(Block.Properties
                    .create(Material.IRON)
                    .hardnessAndResistance(3F, 10F)));
    public static final RegistryObject<CableBlock> cableBlock = blocks.register(
            "cable",
            () -> new CableBlock(Block.Properties
                    .create(Material.IRON)
                    .hardnessAndResistance(1F, 10F)));
    public static final RegistryObject<RedstoneEmitterBlock> redstoneEmitterBlock = blocks.register(
            "redstone_emitter",
            () -> new RedstoneEmitterBlock(Block.Properties
                    .create(Material.IRON)
                    .hardnessAndResistance(1.8F, 10F)));
    public static final RegistryObject<RedstoneInputBlock> redstoneInputBlock = blocks.register(
            "redstone_input",
            () -> new RedstoneInputBlock(Block.Properties
                    .create(Material.IRON)
                    .hardnessAndResistance(1.8F, 10F)));
    public static final RegistryObject<ItemIntakeBlock> itemIntakeBlock = blocks.register(
            "item_intake",
            () -> new ItemIntakeBlock(ItemIntakeTileEntity::regular, Block.Properties
                    .create(Material.IRON)
                    .hardnessAndResistance(1.8F, 10F)));
    public static final RegistryObject<ItemIntakeBlock> instantItemIntakeBlock = blocks.register(
            "instant_item_intake",
            () -> new ItemIntakeBlock(ItemIntakeTileEntity::instant, Block.Properties
                    .create(Material.IRON)
                    .hardnessAndResistance(1.8F, 10F)));
    public static final RegistryObject<BUDBlock> budBlock = blocks.register(
            "bud",
            () -> new BUDBlock(Block.Properties
                    .create(Material.IRON)
                    .hardnessAndResistance(1.8F, 10F)));
    public static final RegistryObject<BlockInteractorBlock> blockInteractorBlock = blocks.register(
            "block_interactor",
            () -> new BlockInteractorBlock(Block.Properties
                    .create(Material.IRON)
                    .hardnessAndResistance(1.8F, 10F)));
    public static final RegistryObject<WorldInteractorBlock> worldInteractorBlock = blocks.register(
            "world_interactor",
            () -> new WorldInteractorBlock(Block.Properties
                    .create(Material.IRON)
                    .hardnessAndResistance(1.8F, 10F)));
    public static final RegistryObject<SignUpdaterBlock> signUpdaterBlock = blocks.register(
            "sign_updater",
            () -> new SignUpdaterBlock(Block.Properties
                    .create(Material.IRON)
                    .hardnessAndResistance(1.8F, 10F)));

    public static DeferredRegister<TileEntityType<?>> tiles = new DeferredRegister<>(ForgeRegistries.TILE_ENTITIES, StevesFactoryManagerAPI.MODID);

    public static final RegistryObject<TileEntityType<FactoryManagerTileEntity>> factoryManagerTileEntity = tiles.register(
            "factory_manager",
            () -> TileEntityType.Builder
                    .create(FactoryManagerTileEntity::new, factoryManagerBlock.get())
                    .build(null));
    public static final RegistryObject<TileEntityType<CableTileEntity>> cableTileEntity = tiles.register(
            "cable",
            () -> TileEntityType.Builder
                    .create(CableTileEntity::new, cableBlock.get())
                    .build(null));
    public static final RegistryObject<TileEntityType<RedstoneEmitterTileEntity>> redstoneEmitterTileEntity = tiles.register(
            "redstone_emitter",
            () -> TileEntityType.Builder
                    .create(RedstoneEmitterTileEntity::new, redstoneEmitterBlock.get())
                    .build(null));
    public static final RegistryObject<TileEntityType<RedstoneInputTileEntity>> redstoneInputTileEntity = tiles.register(
            "redstone_input",
            () -> TileEntityType.Builder
                    .create(RedstoneInputTileEntity::new, redstoneInputBlock.get())
                    .build(null));
    public static final RegistryObject<TileEntityType<ItemIntakeTileEntity>> itemIntakeTileEntity = tiles.register(
            "item_intake",
            () -> TileEntityType.Builder
                    .create(ItemIntakeTileEntity::regular, itemIntakeBlock.get())
                    .build(null));
    public static final RegistryObject<TileEntityType<ItemIntakeTileEntity>> instantItemIntakeTileEntity = tiles.register(
            "instant_item_intake",
            () -> TileEntityType.Builder
                    .create(ItemIntakeTileEntity::instant, instantItemIntakeBlock.get())
                    .build(null));
    public static final RegistryObject<TileEntityType<BUDTileEntity>> budTileEntity = tiles.register(
            "bud",
            () -> TileEntityType.Builder
                    .create(BUDTileEntity::new, budBlock.get())
                    .build(null));
    public static final RegistryObject<TileEntityType<BlockInteractorTileEntity>> blockInteractorTileEntity = tiles.register(
            "block_interactor",
            () -> TileEntityType.Builder
                    .create(BlockInteractorTileEntity::new, blockInteractorBlock.get())
                    .build(null));
    public static final RegistryObject<TileEntityType<WorldInteractorTileEntity>> worldInteractorTileEntity = tiles.register(
            "world_interactor",
            () -> TileEntityType.Builder
                    .create(WorldInteractorTileEntity::new, worldInteractorBlock.get())
                    .build(null));
    public static final RegistryObject<TileEntityType<SignUpdaterTileEntity>> signUpdaterTileEntity = tiles.register(
            "sign_updater",
            () -> TileEntityType.Builder
                    .create(SignUpdaterTileEntity::new, signUpdaterBlock.get())
                    .build(null));

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        ClientRegistry.bindTileEntityRenderer(itemIntakeTileEntity.get(), WorkingAreaRenderer::new);
        ClientRegistry.bindTileEntityRenderer(instantItemIntakeTileEntity.get(), WorkingAreaRenderer::new);
    }
}
