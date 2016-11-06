package vswe.stevesfactory.components;


import gigabit101.AdvancedSystemManager2.components.FlowComponent;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import gigabit101.AdvancedSystemManager2.interfaces.ContainerManager;
import gigabit101.AdvancedSystemManager2.interfaces.GuiManager;
import gigabit101.AdvancedSystemManager2.network.DataReader;
import gigabit101.AdvancedSystemManager2.network.DataWriter;
import gigabit101.AdvancedSystemManager2.network.IComponentNetworkReader;
import gigabit101.AdvancedSystemManager2.network.PacketHandler;

import java.util.List;

public abstract class ComponentMenu implements IComponentNetworkReader {


    private gigabit101.AdvancedSystemManager2.components.FlowComponent parent;
    private int id;

    public ComponentMenu(FlowComponent parent) {
        this.parent = parent;
        id = parent.getMenus().size();
    }

    public abstract String getName();
    @SideOnly(Side.CLIENT)
    public abstract void draw(GuiManager gui, int mX, int mY);
    @SideOnly(Side.CLIENT)
    public abstract void drawMouseOver(GuiManager gui, int mX, int mY);

    public abstract void onClick(int mX, int mY, int button);
    public abstract void onDrag(int mX, int mY, boolean isMenuOpen);
    public abstract void onRelease(int mX, int mY, boolean isMenuOpen);

    @SideOnly(Side.CLIENT)
    public boolean onKeyStroke(GuiManager gui, char c, int k) {
        return false;
    }

    public FlowComponent getParent() {
        return parent;
    }

    public abstract void writeData(DataWriter dw);
    public abstract void readData(DataReader dr);

    protected DataWriter getWriterForServerComponentPacket() {
        return PacketHandler.getWriterForServerComponentPacket(getParent(), this);
    }

    protected DataWriter getWriterForClientComponentPacket(ContainerManager container) {
        return PacketHandler.getWriterForClientComponentPacket(container, getParent(), this);
    }

    public abstract void copyFrom(gigabit101.AdvancedSystemManager2.components.ComponentMenu menu);
    public abstract void refreshData(ContainerManager container, gigabit101.AdvancedSystemManager2.components.ComponentMenu newData);

    public int getId() {
        return id;
    }

    public abstract void readFromNBT(NBTTagCompound nbtTagCompound, int version, boolean pickup);
    public abstract void writeToNBT(NBTTagCompound nbtTagCompound, boolean pickup);

    public void addErrors(List<String> errors) {}

    public boolean isVisible() {
        return true;
    }

    public void update(float partial) {}

    public void doScroll(int scroll) {}

    public void onGuiClosed() {}
}
