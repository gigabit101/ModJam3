package vswe.stevesfactory;

import gigabit101.AdvancedSystemManager2.AdvancedSystemManager2;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.FMLEventChannel;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import gigabit101.AdvancedSystemManager2.GeneratedInfo;
import gigabit101.AdvancedSystemManager2.blocks.ModBlocks;
import gigabit101.AdvancedSystemManager2.components.ModItemHelper;
import gigabit101.AdvancedSystemManager2.network.FileHelper;
import gigabit101.AdvancedSystemManager2.network.PacketEventHandler;
import gigabit101.AdvancedSystemManager2.proxy.CommonProxy;

@Mod(modid = AdvancedSystemManager2.MODID, name = "Steve's Factory Manager", version = GeneratedInfo.version)
public class StevesFactoryManager {


    public static final String MODID = "StevesFactoryManager";
    public static final String RESOURCE_LOCATION = "stevesfactorymanager";
    public static final String CHANNEL = "FactoryManager";
    public static final String UNLOCALIZED_START = "sfm.";

    public static FMLEventChannel packetHandler;

    @SidedProxy(clientSide = "ClientProxy", serverSide = "CommonProxy")
    public static CommonProxy proxy;

    @Mod.Instance(MODID)
    public static AdvancedSystemManager2 instance;


    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        packetHandler = NetworkRegistry.INSTANCE.newEventDrivenChannel(CHANNEL);

        ModBlocks.init();

        proxy.preInit();

        FileHelper.setConfigDir(event.getModConfigurationDirectory());
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        packetHandler.register(new PacketEventHandler());

        ModBlocks.addRecipes();
        //new ChatListener();
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new gigabit101.AdvancedSystemManager2.GuiHandler());

        FMLInterModComms.sendMessage("Waila", "register", "Provider.callbackRegister");
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        ModItemHelper.init();
    }


}
