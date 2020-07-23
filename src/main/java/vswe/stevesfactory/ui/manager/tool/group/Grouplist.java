package vswe.stevesfactory.ui.manager.tool.group;

import net.minecraft.client.resources.I18n;
import vswe.stevesfactory.library.gui.RenderingHelper;
import vswe.stevesfactory.library.gui.widget.Spacer;
import vswe.stevesfactory.library.gui.widget.TextButton;
import vswe.stevesfactory.library.gui.widget.TextField;
import vswe.stevesfactory.library.gui.widget.box.LinearList;
import vswe.stevesfactory.library.gui.window.Dialog;
import vswe.stevesfactory.ui.manager.FactoryManagerGUI;
import vswe.stevesfactory.ui.manager.editor.FlowComponent;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

public class Grouplist extends LinearList<GroupButton> {

    public Grouplist() {
        super(64, 0);
        // No need to remove listener, because this list has the same lifetime as the whole GUI
        FactoryManagerGUI.get().groupModel.addListenerAdd(this::onGroupAdded);
        FactoryManagerGUI.get().groupModel.addListenerRemove(this::onGroupRemoved);
        FactoryManagerGUI.get().groupModel.addListenerUpdate(this::onGroupUpdated);
        // Wait for reflow to finish in this tick
        FactoryManagerGUI.get().scheduleTask(__ -> {
            for (String group : FactoryManagerGUI.get().groupModel.getGroups()) {
                this.addChildren(new GroupButton(group));
            }
            this.reflow();
        });
    }

    private static Grouplist getGroupList() {
        return FactoryManagerGUI.get().getTopLevel().toolboxPanel.getGroupList();
    }

    private void onGroupAdded(String group) {
        addChildren(new GroupButton(group));
        reflow();
    }

    private void onGroupRemoved(String group) {
        int i = 0;
        for (GroupButton child : getChildren()) {
            if (child.getGroup().equals(group)) {
                int index = i; // Effectively final index for lambda capturing
                FactoryManagerGUI.get().scheduleTask(__ -> {
                    getChildren().remove(index);
                    reflow();
                });
            }
            i++;
        }
    }

    private void onGroupUpdated(String from, String to) {
        for (GroupButton child : getChildren()) {
            if (child.getGroup().equals(from)) {
                child.setGroup(to);
            }
        }
    }

    @Override
    public Grouplist addChildren(GroupButton widget) {
        super.addChildren(widget);
        widget.setWidth(calcButtonWidth());
        return this;
    }

    @Override
    public Grouplist addChildren(Collection<GroupButton> widgets) {
        super.addChildren(widgets);
        for (GroupButton widget : widgets) {
            widget.setWidth(calcButtonWidth());
        }
        return this;
    }

    private int calcButtonWidth() {
        return getBarLeft() - 2;
    }

    @Override
    public int getMarginMiddle() {
        return 2;
    }

    public void delete(String group) {
        FactoryManagerGUI.get().groupModel.removeGroup(group);
    }

    public static Dialog createNewGroupDialog() {
        return Dialog.createPrompt(
                "gui.sfm.FactoryManager.Tool.Group.CreateGroup",
                () -> new TextField(0, 0, 0, 16),
                "gui.sfm.ok", "gui.sfm.cancel",
                (b, name) -> {
                    boolean success = FactoryManagerGUI.get().groupModel.addGroup(name);
                    if (!success) {
                        Dialog.createDialog("gui.sfm.FactoryManager.Tool.Group.CreateFailed").tryAddSelfToActiveGUI();
                    }
                },
                (b, name) -> {});
    }

    public static final int SEL_DIALOG_LIST_WIDTH = 280;
    public static final int SEL_DIALOG_LIST_HEIGHT = 160;

    public static Dialog createSelectGroupDialog(Consumer<String> onConfirm, Runnable onCancel) {
        Dialog dialog = new Dialog();

        dialog.getMessageBox().addLine(I18n.format("gui.sfm.FactoryManager.Tool.Group.SelectGroup"));
        TargetList list = new TargetList();
        dialog.insertBeforeButtons(list);

        GroupDataModel data = FactoryManagerGUI.get().groupModel;
        int addId = data.addListenerAdd(group -> {
            list.createTarget(group);
            list.reflow();
        });
        int removeId = data.addListenerRemove(group -> {
            int i = 0;
            for (Target target : list.getChildren()) {
                if (target.getGroup().equals(group)) {
                    int index = i; // Effectively final index for lambda capturing
                    FactoryManagerGUI.get().scheduleTask(__ -> list.getChildren().remove(index));
                }
                i++;
            }
        });
        int updateId = data.addListenerUpdate((from, to) -> {
            for (Target target : list.getChildren()) {
                if (target.getGroup().equals(from)) {
                    target.setGroup(to);
                }
            }
        });

        dialog.getButtons().addChildren(TextButton.of("gui.sfm.ok", b -> {
            onConfirm.accept(list.getSelectedGroup());
            data.removeListenerAdd(addId);
            data.removeListenerRemove(removeId);
            data.removeListenerUpdate(updateId);
        }));
        dialog.bindRemoveSelf2LastButton();
        dialog.getButtons().addChildren(TextButton.of("gui.sfm.cancel", b -> {
            onCancel.run();
            data.removeListenerAdd(addId);
            data.removeListenerRemove(removeId);
            data.removeListenerUpdate(updateId);
        }));
        dialog.bindRemoveSelf2LastButton();
        dialog.getButtons().addChildren(TextButton.of("gui.sfm.new", b -> createNewGroupDialog().tryAddSelfToActiveGUI()));

        dialog.insertBeforeButtons(new Spacer(0, 10));

        dialog.reflow();
        dialog.centralize();
        return dialog;
    }


    private static class TargetList extends LinearList<Target> {

        private int selected = 0;

        public TargetList() {
            super(SEL_DIALOG_LIST_WIDTH, SEL_DIALOG_LIST_HEIGHT);
            for (String group : FactoryManagerGUI.get().groupModel.getGroups()) {
                createTarget(group);
            }
            reflow();
        }

        public void createTarget(String group) {
            int index = getChildren().size();
            Target target = new Target(index, group);
            addChildren(target);
            target.setWidth(this.getBarLeft() - 2);
        }

        @Override
        public void render(int mouseX, int mouseY, float partialTicks) {
            RenderingHelper.drawRect(getAbsoluteX(), getAbsoluteY(), getAbsoluteXRight(), getAbsoluteYBottom(), 0xffb1b1b1);
            super.render(mouseX, mouseY, partialTicks);
        }

        @Override
        public int getMarginMiddle() {
            return 2;
        }

        public Target getSelected() {
            return getChildren().get(selected);
        }

        public String getSelectedGroup() {
            return getSelected().group;
        }
    }

    private static class Target extends TextButton {

        private final int index;
        private String group;

        public Target(int index, String group) {
            this.index = index;
            this.setGroup(group);
            this.setDimensions(SEL_DIALOG_LIST_WIDTH - 8, 12);
        }

        public String getGroup() {
            return group;
        }

        public void setGroup(String group) {
            this.group = group;
            this.setTextRaw(GroupButton.formatGroupName(group));
        }

        @Override
        public int getNormalBorderColor() {
            return isSelected() ? 0xffffff66 : super.getNormalBorderColor();
        }

        @Override
        public int getHoveredBorderColor() {
            return isSelected() ? 0xffffff00 : super.getHoveredBorderColor();
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            getParentWidget().selected = this.index;
            return true;
        }

        @Nonnull
        @Override
        public TargetList getParentWidget() {
            return (TargetList) Objects.requireNonNull(super.getParentWidget());
        }

        private boolean isSelected() {
            return getParentWidget().selected == this.index;
        }
    }
}
