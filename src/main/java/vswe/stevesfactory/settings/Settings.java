package vswe.stevesfactory.settings;


import net.minecraft.block.state.IBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import gigabit101.AdvancedSystemManager2.blocks.TileEntityManager;
import gigabit101.AdvancedSystemManager2.network.DataReader;
import gigabit101.AdvancedSystemManager2.network.DataWriter;
import gigabit101.AdvancedSystemManager2.network.FileHelper;
import gigabit101.AdvancedSystemManager2.network.PacketHandler;


public final class Settings {

    private static final String NAME = "StevesFactoryManagerInside";
    private static final int VERSION = 1;
    private static boolean autoCloseGroup;
    private static boolean largeOpenHitBox;
    private static boolean largeOpenHitBoxMenu;
    private static boolean quickGroupOpen;
    private static boolean commandTypes;
    private static boolean autoSide;
    private static boolean autoBlacklist;
    private static boolean enlargeInterfaces;
    private static boolean priorityMoveFirst;

    @SideOnly(Side.CLIENT)
    public static void openMenu(TileEntityManager manager) {
        manager.specialRenderer = new gigabit101.AdvancedSystemManager2.settings.SettingsScreen(manager);
    }

    public static void load() {
        DataReader dr = FileHelper.read(NAME);

        if (dr != null) {
            try {
                int version = dr.readByte();

                autoCloseGroup = dr.readBoolean();
                largeOpenHitBox = dr.readBoolean();
                largeOpenHitBoxMenu = dr.readBoolean();
                quickGroupOpen = dr.readBoolean();
                commandTypes = dr.readBoolean();
                autoSide = dr.readBoolean();
                autoBlacklist = dr.readBoolean();
                enlargeInterfaces = dr.readBoolean();
                if (version >= 1) {
                    priorityMoveFirst = dr.readBoolean();
                }
            }catch (Exception ignored){
                loadDefault();
            }finally {
                dr.close();
            }
        }else{
            loadDefault();
        }
    }

    private static void loadDefault() {
        autoCloseGroup = false;
        largeOpenHitBox = false;
        largeOpenHitBoxMenu = false;
        quickGroupOpen = false;
        commandTypes = false;
        autoSide = false;
        autoBlacklist = false;
        enlargeInterfaces = false;
    }

    private static void save() {
        DataWriter dw = FileHelper.getWriter(NAME);

        if (dw != null) {
            dw.writeByte(VERSION);

            dw.writeBoolean(autoCloseGroup);
            dw.writeBoolean(largeOpenHitBox);
            dw.writeBoolean(largeOpenHitBoxMenu);
            dw.writeBoolean(quickGroupOpen);
            dw.writeBoolean(commandTypes);
            dw.writeBoolean(autoSide);
            dw.writeBoolean(autoBlacklist);
            dw.writeBoolean(enlargeInterfaces);
            dw.writeBoolean(priorityMoveFirst);

            FileHelper.close(dw);
        }
    }

    public static boolean isAutoCloseGroup() {
        return autoCloseGroup;
    }

    public static void setAutoCloseGroup(boolean autoCloseGroup) {
        gigabit101.AdvancedSystemManager2.settings.Settings.autoCloseGroup = autoCloseGroup;
        save();
    }

    public static boolean isLargeOpenHitBox() {
        return largeOpenHitBox;
    }

    public static void setLargeOpenHitBox(boolean largeOpenHitBox) {
        gigabit101.AdvancedSystemManager2.settings.Settings.largeOpenHitBox = largeOpenHitBox;
        save();
    }

    public static boolean isLargeOpenHitBoxMenu() {
        return largeOpenHitBoxMenu;
    }

    public static void setLargeOpenHitBoxMenu(boolean largeOpenHitBoxMenu) {
        gigabit101.AdvancedSystemManager2.settings.Settings.largeOpenHitBoxMenu = largeOpenHitBoxMenu;
        save();
    }

    public static boolean isQuickGroupOpen() {
        return quickGroupOpen;
    }

    public static void setQuickGroupOpen(boolean quickGroupOpen) {
        gigabit101.AdvancedSystemManager2.settings.Settings.quickGroupOpen = quickGroupOpen;
        save();
    }

    public static boolean isCommandTypes() {
        return commandTypes;
    }

    public static void setCommandTypes(boolean commandTypes) {
        gigabit101.AdvancedSystemManager2.settings.Settings.commandTypes = commandTypes;
        save();
    }

    public static boolean isAutoSide() {
        return autoSide;
    }

    public static void setAutoSide(boolean autoSide) {
        gigabit101.AdvancedSystemManager2.settings.Settings.autoSide = autoSide;
        save();
    }

    public static boolean isAutoBlacklist() {
        return autoBlacklist;
    }

    public static void setAutoBlacklist(boolean autoBlacklist) {
        gigabit101.AdvancedSystemManager2.settings.Settings.autoBlacklist = autoBlacklist;
        save();
    }

    public static boolean isLimitless(TileEntityManager manager) {
        IBlockState state = manager.getWorld().getBlockState(manager.getPos());
        return (state.getBlock().getMetaFromState(state) & 1) != 0;
    }

    public static void setLimitless(TileEntityManager manager, boolean limitless) {
        if (manager.getWorld().isRemote) {
            DataWriter dw = PacketHandler.getWriterForServerActionPacket();
            dw.writeBoolean(limitless);
            PacketHandler.sendDataToServer(dw);
        }else{
            IBlockState state = manager.getWorld().getBlockState(manager.getPos());
            int meta = state.getBlock().getMetaFromState(state);
            if (limitless) {
                meta |= 1;
            }else{
                meta &= ~1;
            }
            manager.getWorld().setBlockState(manager.getPos(), state.getBlock().getStateFromMeta(meta), 3);
        }
    }

    public static boolean isEnlargeInterfaces() {
        return enlargeInterfaces;
    }

    public static void setEnlargeInterfaces(boolean enlargeInterfaces) {
        gigabit101.AdvancedSystemManager2.settings.Settings.enlargeInterfaces = enlargeInterfaces;
        save();
    }

    public static boolean isPriorityMoveFirst() {
        return priorityMoveFirst;
    }

    public static void setPriorityMoveFirst(boolean priorityMoveFirst) {
        gigabit101.AdvancedSystemManager2.settings.Settings.priorityMoveFirst = priorityMoveFirst;
    }

    private Settings() {}
}
