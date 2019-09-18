package vswe.stevesfactory.ui.manager.menu;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.BlockPos;
import vswe.stevesfactory.api.logic.IProcedure;
import vswe.stevesfactory.api.logic.IProcedureClientData;
import vswe.stevesfactory.api.network.INetworkController;
import vswe.stevesfactory.library.gui.screen.WidgetScreen;
import vswe.stevesfactory.library.gui.widget.box.WrappingList;
import vswe.stevesfactory.logic.procedure.IInventoryTarget;
import vswe.stevesfactory.ui.manager.FactoryManagerGUI;
import vswe.stevesfactory.ui.manager.editor.FlowComponent;
import vswe.stevesfactory.ui.manager.editor.Menu;

import java.util.*;

public class InventorySelectionMenu<P extends IInventoryTarget & IProcedure & IProcedureClientData> extends Menu<P> {

    private final int id;
    private final String name;

    private WrappingList<BlockTarget> list;

    public InventorySelectionMenu(int id) {
        this(id, I18n.format("gui.sfm.Menu.InventorySelection"));
    }

    public InventorySelectionMenu(int id, String name) {
        this.id = id;
        this.name = name;

        list = new WrappingList<>("");
        list.setLocation(4, HEADING_BOX.getPortionHeight() + 4);
        list.setItemsPerRow(5);
        list.setVisibleRows(2);
        list.getContentArea().y += list.getSearchBoxHeight() + 2;
        list.setDimensions(list.getContentArea().width, getContentHeight() - 4 * 2);
        list.getScrollUpArrow().setLocation(100, 24);
        list.alignArrows();
        FactoryManagerGUI gui = (FactoryManagerGUI) WidgetScreen.getCurrentScreen();
        INetworkController controller = Objects.requireNonNull((INetworkController) Minecraft.getInstance().world.getTileEntity(gui.controllerPos));
        for (BlockPos pos : controller.getLinkedInventories()) {
            list.addElement(new BlockTarget(pos));
        }

        // TODO add selection buttons
        addChildren(list);
    }

    @Override
    public void onLinkFlowComponent(FlowComponent<P> flowComponent) {
        super.onLinkFlowComponent(flowComponent);
        Set<BlockPos> poses = new HashSet<>(flowComponent.getLinkedProcedure().getInventories(id));
        for (BlockTarget target : list.getContents()) {
            if (poses.contains(target.pos)) {
                target.setSelected(true);
            }
        }
    }

    @Override
    public void expand() {
        super.expand();
    }

    @Override
    public String getHeadingText() {
        return name;
    }

    @Override
    protected void updateData() {
        List<BlockPos> inventories = getLinkedProcedure().getInventories(id);
        inventories.clear();
        for (BlockTarget target : list.getContents()) {
            if (target.isSelected()) {
                inventories.add(target.pos);
            }
        }
    }
}
