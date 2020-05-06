package vswe.stevesfactory.ui.manager.menu;

import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import org.apache.commons.lang3.tuple.Pair;
import vswe.stevesfactory.api.logic.IClientDataStorage;
import vswe.stevesfactory.api.logic.IProcedure;
import vswe.stevesfactory.library.gui.widget.TextField;
import vswe.stevesfactory.library.gui.widget.panel.FilteredList;
import vswe.stevesfactory.library.gui.widget.panel.WrappingList;
import vswe.stevesfactory.logic.procedure.IInventoryTarget;
import vswe.stevesfactory.ui.manager.FactoryManagerGUI;
import vswe.stevesfactory.ui.manager.editor.FlowComponent;
import vswe.stevesfactory.ui.manager.editor.Menu;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InventorySelectionMenu<P extends IInventoryTarget & IProcedure & IClientDataStorage> extends Menu<P> {

    private final int id;
    private final String name;
    private final String errorMessage;

    private TextField searchBox;
    private WrappingList<BlockTarget> list;

    public InventorySelectionMenu(int id, Capability<?> cap) {
        this(id, I18n.format("menu.sfm.InventorySelection"), I18n.format("error.sfm.ItemIO.NoInv"), cap);
    }

    public InventorySelectionMenu(int id, String name, String errorMessage, Capability<?> cap) {
        this.id = id;
        this.name = name;
        this.errorMessage = errorMessage;

        Pair<WrappingList<BlockTarget>, TextField> constructionResult = FilteredList.createSearchableList(new ArrayList<>(), "");
        searchBox = constructionResult.getRight();
        searchBox.setLocation(4, 4);

        list = constructionResult.getLeft();
        list.setLocation(4, HEADING_BOX.getPortionHeight() + 4 + searchBox.getHeight() + 2);
        list.setItemsPerRow(5);
        list.setVisibleRows(2);
        list.getScrollUpArrow().setLocation(100, 24);
        list.alignArrows();
        for (BlockPos pos : FactoryManagerGUI.get().getController().getLinkedInventories(cap)) {
            list.addElement(new BlockTarget(pos));
        }

        // TODO add selection buttons
        addChildren(searchBox);
        addChildren(list);
    }

    @Override
    public void onLinkFlowComponent(FlowComponent<P> flowComponent) {
        super.onLinkFlowComponent(flowComponent);
        Set<BlockPos> poses = new HashSet<>(getLinkedProcedure().getInventories(id));
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
    protected void saveData() {
        P procedure = getLinkedProcedure();
        List<BlockPos> inventories = procedure.getInventories(id);
        inventories.clear();
        for (BlockTarget target : list.getContents()) {
            if (target.isSelected()) {
                inventories.add(target.pos);
            }
        }
        procedure.markDirty();
    }

    @Override
    public List<String> populateErrors(List<String> errors) {
        if (!hasAnythingSelected()) {
            errors.add(errorMessage);
        }
        return errors;
    }

    private boolean hasAnythingSelected() {
        for (BlockTarget target : list.getContents()) {
            if (target.isSelected()) {
                return true;
            }
        }
        return false;
    }
}
