package vswe.stevesfactory.ui.manager.tool.group;

import net.minecraft.client.resources.I18n;
import vswe.stevesfactory.library.gui.Render2D;
import vswe.stevesfactory.library.gui.contextmenu.CallbackEntry;
import vswe.stevesfactory.library.gui.contextmenu.ContextMenuBuilder;
import vswe.stevesfactory.library.gui.contextmenu.Section;
import vswe.stevesfactory.library.gui.widget.TextField;
import vswe.stevesfactory.library.gui.widget.button.ColoredTextButton;
import vswe.stevesfactory.library.gui.window.Dialog;
import vswe.stevesfactory.ui.manager.FactoryManagerGUI;
import vswe.stevesfactory.ui.manager.FactoryManagerGUI.PrimaryWindow;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;

public class GroupButton extends ColoredTextButton {

    public static String formatGroupName(String group) {
        return group.isEmpty() ? I18n.format("gui.sfm.FactoryManager.Tool.Group.DefaultGroup") : group;
    }

    private String group;

    public GroupButton(String name) {
        this.setGroup(name);
        this.setHeight(12);
    }

    @Override
    public boolean onMouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW_MOUSE_BUTTON_LEFT) {
            actionSwitchGroup();
            onParentPositionChanged();
            return true;
        }
        return false;
    }

    @Override
    protected void buildContextMenu(ContextMenuBuilder builder) {
        Section section = builder.obtainSection("Grouplist.Entry");
        section.addChildren(new CallbackEntry(Render2D.DELETE, "gui.sfm.FactoryManager.Tool.Group.Delete", b -> actionDelete()));
        section.addChildren(new CallbackEntry(null, "gui.sfm.FactoryManager.Tool.Group.RenameGroup", b -> actionRename()));
        section.addChildren(new CallbackEntry(null, "gui.sfm.FactoryManager.Tool.Group.MoveContent", b -> actionMoveContent()));
        super.buildContextMenu(builder);
    }

    private void actionSwitchGroup() {
        FactoryManagerGUI.get().groupModel.setCurrentGroup(group);
    }

    private void actionDelete() {
        getGroupList().delete(group);
    }

    private void actionRename() {
        Dialog.createPrompt(
                I18n.format("gui.sfm.FactoryManager.Tool.Group.RenameGroup.Prompt"),
                () -> new TextField(0, 16),
                I18n.format("gui.sfm.ok"),
                I18n.format("gui.sfm.cancel"),
                (b, newName) -> {
                    for (String group : FactoryManagerGUI.get().groupModel.getGroups()) {
                        if (newName.equals(group)) {
                            Dialog.createDialog(
                                    I18n.format("gui.sfm.FactoryManager.Tool.Group.RenameGroup.Failed", newName)
                            ).tryAddSelfToActiveGUI();
                            return;
                        }
                    }
                    FactoryManagerGUI.get().groupModel.updateGroup(group, newName);
                },
                (b, newName) -> {}
        ).tryAddSelfToActiveGUI();
    }

    private void actionMoveContent() {
        Grouplist.createSelectGroupDialog(
                toGroup -> {
                    if (!this.group.equals(toGroup)) {
                        PrimaryWindow window = FactoryManagerGUI.get().getPrimaryWindow();
                        window.editorPanel.moveGroup(this.group, toGroup);
                        window.connectionsPanel.moveGroup(this.group, toGroup);
                    }
                },
                () -> {
                }).tryAddSelfToActiveGUI();
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
        return FactoryManagerGUI.get().getPrimaryWindow().toolboxPanel.getGroupList();
    }

    private boolean isSelected() {
        return group.equals(FactoryManagerGUI.get().groupModel.getCurrentGroup());
    }
}
