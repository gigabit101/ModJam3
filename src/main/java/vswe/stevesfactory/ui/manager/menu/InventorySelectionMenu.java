package vswe.stevesfactory.ui.manager.menu;

import lombok.val;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import vswe.stevesfactory.api.logic.IClientDataStorage;
import vswe.stevesfactory.api.logic.IProcedure;
import vswe.stevesfactory.library.gui.widget.TextField;
import vswe.stevesfactory.library.gui.widget.TextField.BackgroundStyle;
import vswe.stevesfactory.library.gui.widget.panel.FilteredList;
import vswe.stevesfactory.library.gui.widget.panel.WrappingList;
import vswe.stevesfactory.logic.procedure.IInventoryTarget;
import vswe.stevesfactory.ui.manager.FactoryManagerGUI;
import vswe.stevesfactory.ui.manager.editor.FlowComponent;
import vswe.stevesfactory.ui.manager.editor.Menu;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class InventorySelectionMenu<P extends IInventoryTarget & IProcedure & IClientDataStorage> extends Menu<P> {

    private final int id;
    private final String name;
    private final String errorMessage;
    private final Capability<?> cap;

    private final TextField searchBox;
    private final WrappingList<BlockTarget> list;

    public InventorySelectionMenu(int id, Capability<?> cap) {
        this(id, I18n.format("menu.sfm.InventorySelection"), I18n.format("error.sfm.ItemIO.NoInv"), cap);
    }

    public InventorySelectionMenu(int id, String name, String errorMessage, Capability<?> cap) {
        this.id = id;
        this.name = name;
        this.errorMessage = errorMessage;
        this.cap = cap;

        // TODO add selection buttons
        val constructionResult = FilteredList.<BlockTarget>createSearchableList(new ArrayList<>(), "");
        searchBox = constructionResult.getRight();
        searchBox.setBackgroundStyle(BackgroundStyle.RED_OUTLINE);

        list = constructionResult.getLeft();
        list.setItemSize(16);
        list.setY(searchBox.getHeight() + 2);
        list.setItemsPerRow(5);
        list.setVisibleRows(2);
        list.getScrollUpArrow().setX(list.getFullWidth() + 3);
        list.alignArrows();
    }

    @Override
    public void onInitialAttach() {
        super.onInitialAttach();

        addChildren(searchBox);
        addChildren(list);
        for (BlockPos pos : FactoryManagerGUI.get().getController().getLinkedInventories(cap)) {
            list.addElement(new BlockTarget(pos));
        }
    }

    @Override
    public void onLinkFlowComponent(FlowComponent<P> flowComponent) {
        super.onLinkFlowComponent(flowComponent);
        val poses = new HashSet<>(getLinkedProcedure().getInventories(id));
        for (val target : list.getContents()) {
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
        val procedure = getLinkedProcedure();
        val inventories = procedure.getInventories(id);
        inventories.clear();
        for (val target : list.getContents()) {
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
        for (val target : list.getContents()) {
            if (target.isSelected()) {
                return true;
            }
        }
        return false;
    }
}
