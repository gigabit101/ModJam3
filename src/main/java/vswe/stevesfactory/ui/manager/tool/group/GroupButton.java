package vswe.stevesfactory.ui.manager.tool.group;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.resources.I18n;
import vswe.stevesfactory.library.gui.contextmenu.CallbackEntry;
import vswe.stevesfactory.library.gui.contextmenu.ContextMenu;
import vswe.stevesfactory.library.gui.screen.WidgetScreen;
import vswe.stevesfactory.library.gui.widget.TextButton;
import vswe.stevesfactory.library.gui.widget.TextField;
import vswe.stevesfactory.library.gui.window.Dialog;
import vswe.stevesfactory.ui.manager.FactoryManagerGUI;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT;

public class GroupButton extends TextButton {

    public static String formatGroupName(String group) {
        return group.isEmpty() ? I18n.format("gui.sfm.FactoryManager.Tool.Group.DefaultGroup") : group;
    }

    private String group;

    public GroupButton(String name) {
        this.setGroup(name);
        this.setHeight(12);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        switch (button) {
            case GLFW_MOUSE_BUTTON_LEFT:
                actionSwitchGroup();
                return true;
            case GLFW_MOUSE_BUTTON_RIGHT:
                openContextMenu();
                return true;
        }
        return false;
    }

    private void openContextMenu() {
        ContextMenu contextMenu = ContextMenu.atCursor(ImmutableList.of(
                new CallbackEntry(FactoryManagerGUI.DELETE_ICON, "gui.sfm.FactoryManager.Tool.Group.Delete", b -> actionDelete()),
                new CallbackEntry(null, "gui.sfm.FactoryManager.Tool.Group.RenameGroup", b -> actionRename()),
                new CallbackEntry(null, "gui.sfm.FactoryManager.Tool.Group.MoveContent", b -> actionMoveContent())
        ));
        WidgetScreen.getCurrent().addPopupWindow(contextMenu);
    }

    private void actionSwitchGroup() {
        FactoryManagerGUI.get().groupModel.setCurrentGroup(group);
    }

    private void actionDelete() {
        getGroupList().delete(group);
    }

    private void actionRename() {
        Dialog.createPrompt(
                "gui.sfm.FactoryManager.Tool.Group.RenameGroup.Prompt",
                () -> new TextField(0, 0, 0, 16),
                "gui.sfm.ok", "gui.sfm.cancel",
                (b, newName) -> {
                    for (String group : FactoryManagerGUI.get().groupModel.getGroups()) {
                        if (newName.equals(group)) {
                            Dialog.createDialog(I18n.format("gui.sfm.FactoryManager.Tool.Group.RenameGroup.Failed", newName)).tryAddSelfToActiveGUI();
                            return;
                        }
                    }
                    FactoryManagerGUI.get().groupModel.updateGroup(group, newName);
                },
                (b, newName) -> {}).tryAddSelfToActiveGUI();
    }

    private void actionMoveContent() {
        Grouplist.createSelectGroupDialog(
                toGroup -> {
                    if (!this.group.equals(toGroup)) {
                        FactoryManagerGUI.get().getTopLevel().editorPanel.moveGroup(this.group, toGroup);
                        FactoryManagerGUI.get().getTopLevel().connectionsPanel.moveGroup(this.group, toGroup);
                    }
                },
                () -> {}).tryAddSelfToActiveGUI();
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
        this.setTextRaw(formatGroupName(group));
    }

    @Override
    public int getNormalBorderColor() {
        return isSelected() ? 0xffffff66 : super.getNormalBorderColor();
    }

    @Override
    public int getHoveredBorderColor() {
        return isSelected() ? 0xffffff00 : super.getNormalBorderColor();
    }

    private Grouplist getGroupList() {
        return FactoryManagerGUI.get().getTopLevel().toolboxPanel.getGroupList();
    }

    private boolean isSelected() {
        return group.equals(FactoryManagerGUI.get().groupModel.getCurrentGroup());
    }
}
