package vswe.stevesfactory.init;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import vswe.stevesfactory.beta.BlockWirelessReciver;
import vswe.stevesfactory.beta.BlockWirelessTransmitter;
import vswe.stevesfactory.beta.ItemBlockBeta;
import vswe.stevesfactory.blocks.*;
import vswe.stevesfactory.items.itemblocks.ItemBlockCamouflage;
import vswe.stevesfactory.items.itemblocks.ItemBlockCluster;
import vswe.stevesfactory.items.itemblocks.ItemBlockIntake;
import vswe.stevesfactory.items.itemblocks.ItemBlockRelay;
import vswe.stevesfactory.tiles.*;

public final class ModBlocks
{
    //TODO move all to lib
    public static final byte NBT_CURRENT_PROTOCOL_VERSION = 13;
    public static final String NBT_PROTOCOL_VERSION = "ProtocolVersion";

    private static final String MANAGER_TILE_ENTITY_TAG = "TileEntityMachineManagerName";
    public static final String MANAGER_NAME_TAG = "BlockMachineManagerName";
    public static final String MANAGER_UNLOCALIZED_NAME = "BlockMachineManager";

    public static final String CABLE_NAME_TAG = "BlockCableName";
    public static final String CABLE_UNLOCALIZED_NAME = "BlockCable";


    private static final String CABLE_RELAY_TILE_ENTITY_TAG = "TileEntityCableRelayName";
    public static final String CABLE_RELAY_NAME_TAG = "BlockCableRelayName";
    public static final String CABLE_RELAY_UNLOCALIZED_NAME = "BlockCableRelay";
    public static final String CABLE_ADVANCED_RELAY_UNLOCALIZED_NAME = "BlockAdvancedCableRelay";

    private static final String CABLE_OUTPUT_TILE_ENTITY_TAG = "TileEntityCableOutputName";
    public static final String CABLE_OUTPUT_NAME_TAG = "BlockCableOutputName";
    public static final String CABLE_OUTPUT_UNLOCALIZED_NAME = "BlockCableOutput";

    private static final String CABLE_INPUT_TILE_ENTITY_TAG = "TileEntityCableInputName";
    public static final String CABLE_INPUT_NAME_TAG = "BlockCableInputName";
    public static final String CABLE_INPUT_UNLOCALIZED_NAME = "BlockCableInput";

    private static final String CABLE_CREATIVE_TILE_ENTITY_TAG = "TileEntityCableCreativeName";
    public static final String CABLE_CREATIVE_NAME_TAG = "BlockCableCreativeName";
    public static final String CABLE_CREATIVE_UNLOCALIZED_NAME = "BlockCableCreative";

    private static final String CABLE_INTAKE_TILE_ENTITY_TAG = "TileEntityCableIntakeName";
    public static final String CABLE_INTAKE_NAME_TAG = "BlockCableIntakeName";
    public static final String CABLE_INTAKE_UNLOCALIZED_NAME = "BlockCableIntake";
    public static final String CABLE_INSTANT_INTAKE_UNLOCALIZED_NAME = "BlockInstantCableIntake";

    private static final String CABLE_BUD_TILE_ENTITY_TAG = "TileEntityCableBUDName";
    public static final String CABLE_BUD_NAME_TAG = "BlockCableBUDName";
    public static final String CABLE_BUD_UNLOCALIZED_NAME = "BlockCableBUD";

    private static final String CABLE_BREAKER_TILE_ENTITY_TAG = "TileEntityCableBreakerName";
    public static final String CABLE_BREAKER_NAME_TAG = "BlockCableBreakerName";
    public static final String CABLE_BREAKER_UNLOCALIZED_NAME = "BlockCableBreaker";

    private static final String CABLE_CLUSTER_TILE_ENTITY_TAG = "TileEntityCableClusterName";
    public static final String CABLE_CLUSTER_NAME_TAG = "BlockCableClusterName";
    public static final String CABLE_CLUSTER_UNLOCALIZED_NAME = "BlockCableCluster";
    public static final String CABLE_ADVANCED_CLUSTER_UNLOCALIZED_NAME = "BlockAdvancedCableCluster";

    private static final String CABLE_CAMOUFLAGE_TILE_ENTITY_TAG = "TileEntityCableCamouflageName";
    public static final String CABLE_CAMOUFLAGE_NAME_TAG = "BlockCableCamouflageName";

    private static final String CABLE_SIGN_TILE_ENTITY_TAG = "TileEntityCableSignName";
    public static final String CABLE_SIGN_NAME_TAG = "BlockCableSignName";
    public static final String CABLE_SIGN_UNLOCALIZED_NAME = "BlockCableSign";


    public static BlockManager blockManager;
    public static BlockCable blockCable;
    public static BlockCableRelay blockCableRelay;
    public static BlockCableOutput blockCableOutput;
    public static BlockCableInput blockCableInput;
    public static BlockCableCreative blockCableCreative;
    public static BlockCableIntake blockCableIntake;
    public static BlockCableBUD blockCableBUD;
    public static BlockCableBreaker blockCableBreaker;
    public static BlockCableCluster blockCableCluster;
    public static BlockCableCamouflages blockCableCamouflage;
    public static BlockCableSign blockCableSign;
    public static BlockWirelessTransmitter blockWirelessTransmitter;
    public static BlockWirelessReciver blockWirelessReciver;


    //TODO move to register
    public static void init()
    {
        blockManager = new BlockManager();
        GameRegistry.registerBlock(blockManager, MANAGER_NAME_TAG);
        GameRegistry.registerTileEntity(TileEntityManager.class, MANAGER_TILE_ENTITY_TAG);

        blockCable = new BlockCable();
        GameRegistry.registerBlock(blockCable, CABLE_NAME_TAG);

        blockCableRelay = new BlockCableRelay();
        GameRegistry.registerBlock(blockCableRelay, ItemBlockRelay.class, CABLE_RELAY_NAME_TAG);
        GameRegistry.registerTileEntity(TileEntityRelay.class, CABLE_RELAY_TILE_ENTITY_TAG);
        ClusterRegistry.register(new ClusterRegistry.ClusterRegistryAdvancedSensitive(TileEntityRelay.class, blockCableRelay, new ItemStack(blockCableRelay, 1, 0)));
        ClusterRegistry.register(new ClusterRegistry.ClusterRegistryAdvancedSensitive(TileEntityRelay.class, blockCableRelay, new ItemStack(blockCableRelay, 1, 8)));

        blockCableOutput = new BlockCableOutput();
        GameRegistry.registerBlock(blockCableOutput, CABLE_OUTPUT_NAME_TAG);
        GameRegistry.registerTileEntity(TileEntityOutput.class, CABLE_OUTPUT_TILE_ENTITY_TAG);
        ClusterRegistry.register(TileEntityOutput.class, blockCableOutput);

        blockCableInput = new BlockCableInput();
        GameRegistry.registerBlock(blockCableInput, CABLE_INPUT_NAME_TAG);
        GameRegistry.registerTileEntity(TileEntityInput.class, CABLE_INPUT_TILE_ENTITY_TAG);
        ClusterRegistry.register(TileEntityInput.class, blockCableInput);

        blockCableCreative = new BlockCableCreative();
        GameRegistry.registerBlock(blockCableCreative, CABLE_CREATIVE_NAME_TAG);
        GameRegistry.registerTileEntity(TileEntityCreative.class, CABLE_CREATIVE_TILE_ENTITY_TAG);
        ClusterRegistry.register(TileEntityCreative.class, blockCableCreative);

        blockCableIntake = new BlockCableIntake();
        GameRegistry.registerBlock(blockCableIntake, ItemBlockIntake.class, CABLE_INTAKE_NAME_TAG);
        GameRegistry.registerTileEntity(TileEntityIntake.class, CABLE_INTAKE_TILE_ENTITY_TAG);
        ClusterRegistry.register(new ClusterRegistry.ClusterRegistryAdvancedSensitive(TileEntityIntake.class, blockCableIntake, new ItemStack(blockCableIntake, 1, 0)));
        ClusterRegistry.register(new ClusterRegistry.ClusterRegistryAdvancedSensitive(TileEntityIntake.class, blockCableIntake, new ItemStack(blockCableIntake, 1, 8)));

        blockCableBUD = new BlockCableBUD();
        GameRegistry.registerBlock(blockCableBUD, CABLE_BUD_NAME_TAG);
        GameRegistry.registerTileEntity(TileEntityBUD.class, CABLE_BUD_TILE_ENTITY_TAG);
        ClusterRegistry.register(TileEntityBUD.class, blockCableBUD);

        blockCableBreaker = new BlockCableBreaker();
        GameRegistry.registerBlock(blockCableBreaker, CABLE_BREAKER_NAME_TAG);
        GameRegistry.registerTileEntity(TileEntityBreaker.class, CABLE_BREAKER_TILE_ENTITY_TAG);
        ClusterRegistry.register(TileEntityBreaker.class, blockCableBreaker);

        blockCableCluster = new BlockCableCluster();
        GameRegistry.registerBlock(blockCableCluster, ItemBlockCluster.class, CABLE_CLUSTER_NAME_TAG);
        GameRegistry.registerTileEntity(TileEntityCluster.class, CABLE_CLUSTER_TILE_ENTITY_TAG);

        blockCableCamouflage = new BlockCableCamouflages();
        GameRegistry.registerBlock(blockCableCamouflage, ItemBlockCamouflage.class, CABLE_CAMOUFLAGE_NAME_TAG);
        GameRegistry.registerTileEntity(TileEntityCamouflage.class, CABLE_CAMOUFLAGE_TILE_ENTITY_TAG);

        ClusterRegistry.register(new ClusterRegistry.ClusterRegistryMetaSensitive(TileEntityCamouflage.class, blockCableCamouflage, new ItemStack(blockCableCamouflage, 1, 0)));
        ClusterRegistry.register(new ClusterRegistry.ClusterRegistryMetaSensitive(TileEntityCamouflage.class, blockCableCamouflage, new ItemStack(blockCableCamouflage, 1, 1)));
        ClusterRegistry.register(new ClusterRegistry.ClusterRegistryMetaSensitive(TileEntityCamouflage.class, blockCableCamouflage, new ItemStack(blockCableCamouflage, 1, 2)));

        blockCableSign = new BlockCableSign();
        GameRegistry.registerBlock(blockCableSign, CABLE_SIGN_NAME_TAG);
        GameRegistry.registerTileEntity(TileEntitySignUpdater.class, CABLE_SIGN_TILE_ENTITY_TAG);
        ClusterRegistry.register(TileEntitySignUpdater.class, blockCableSign);

        blockWirelessTransmitter = new BlockWirelessTransmitter();
        GameRegistry.registerBlock(blockWirelessTransmitter, ItemBlockBeta.class, "wirelesstransmitter");

        blockWirelessReciver = new BlockWirelessReciver();
        GameRegistry.registerBlock(blockWirelessReciver, ItemBlockBeta.class, "wirelessreciver");
    }
}
