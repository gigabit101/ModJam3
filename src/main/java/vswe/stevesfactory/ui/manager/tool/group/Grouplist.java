package vswe.stevesfactory.ui.manager.tool.group;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.resources.I18n;
import vswe.stevesfactory.library.gui.Render2D;
import vswe.stevesfactory.library.gui.widget.Paragraph;
import vswe.stevesfactory.library.gui.widget.TextField;
import vswe.stevesfactory.library.gui.widget.button.ColoredTextButton;
import vswe.stevesfactory.library.gui.widget.panel.VerticalList;
import vswe.stevesfactory.library.gui.window.Dialog;
import vswe.stevesfactory.ui.manager.FactoryManagerGUI;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Consumer;

public class Grouplist extends VerticalList<GroupButton> {

    public Grouplist() {
        this.setDimensions(64, 0);
        // No need to remove listener, because this list has the same lifetime as the whole GUI
        FactoryManagerGUI.get().groupModel.addListenerAdd(this::onGroupAdded);
        FactoryManagerGUI.get().groupModel.addListenerRemove(this::onGroupRemoved);
        FactoryManagerGUI.get().groupModel.addListenerUpdate(this::onGroupUpdated);

    }

    @Override
    public void onInitialAttach() {
        super.onInitialAttach();
        // Wait for reflow to finish in this tick
        // TODO remove this
        FactoryManagerGUI.get().defer(() -> {
            for (String group : FactoryManagerGUI.get().groupModel.getGroups()) {
                this.addChildren(new GroupButton(group));
            }
            this.reflow();
        });
    }

    private static Grouplist getGroupList() {
        return FactoryManagerGUI.get().getPrimaryWindow().toolboxPanel.getGroupList();
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
                FactoryManagerGUI.get().defer(() -> {
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
                I18n.format("gui.sfm.FactoryManager.Tool.Group.CreateGroup"),
                () -> new TextField(0, 16),
                "gui.sfm.ok", "gui.sfm.cancel",
                (b, name) -> {
                    boolean success = FactoryManagerGUI.get().groupModel.addGroup(name);
                    if (!success) {
                        Dialog.createDialog(
                                I18n.format("gui.sfm.FactoryManager.Tool.Group.CreateFailed")
                        ).tryAddSelfToActiveGUI();
                    }
                },
                (b, name) -> {});
    }

    public static final int SEL_DIALOG_LIST_WIDTH = 280;
    public static final int SEL_DIALOG_LIST_HEIGHT = 160;

    public static Dialog createSelectGroupDialog(Consumer<String> onConfirm, Runnable onCancel) {
        Dialog dialog = new Dialog();

        Paragraph messageBox = dialog.getMessageBox();
        messageBox.addLine(I18n.format("gui.sfm.FactoryManager.Tool.Group.SelectGroup"));
        messageBox.setBorderTop(5);
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
                    FactoryManagerGUI.get().defer(() -> list.getChildren().remove(index));
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

        dialog.getButtons().addChildren(ColoredTextButton.of(I18n.format("gui.sfm.ok"), b2 -> {
            onConfirm.accept(list.getSelectedGroup());
            data.removeListenerAdd(addId);
            data.removeListenerRemove(removeId);
            data.removeListenerUpdate(updateId);
        }));
        dialog.bindRemoveSelf2LastButton();
        dialog.getButtons().addChildren(ColoredTextButton.of(I18n.format("gui.sfm.cancel"), b1 -> {
            onCancel.run();
            data.removeListenerAdd(addId);
            data.removeListenerRemove(removeId);
            data.removeListenerUpdate(updateId);
        }));
        dialog.bindRemoveSelf2LastButton();
        dialog.getButtons().addChildren(ColoredTextButton.of(I18n.format("gui.sfm.new"), b -> createNewGroupDialog().tryAddSelfToActiveGUI()));

        dialog.getButtons().setBorderTop(10);

        dialog.reflow();
        dialog.centralize();
        return dialog;
    }


    private static class TargetList extends VerticalList<Target> {

        private int selected = 0;

        public TargetList() {
            this.setDimensions(SEL_DIALOG_LIST_WIDTH, SEL_DIALOG_LIST_HEIGHT);
        }

        @Override
        public void onInitialAttach() {
            super.onInitialAttach();

            for (String group : FactoryManagerGUI.get().groupModel.getGroups()) {
                createTarget(group);
            }
            reflow();
        }

        public Target createTarget(String group) {
            int index = getChildren().size();
            Target target = new Target(index, group);
            target.setWidth(this.getBarLeft() - 2);
            addChildren(target);
            return target;
        }

        @Override
        public void render(int mouseX, int mouseY, float partialTicks) {
            RenderSystem.disableTexture();
            Render2D.beginColoredQuad();
            Render2D.coloredRect(getAbsoluteX(), getAbsoluteY(), getAbsoluteXRight(), getAbsoluteYBottom(), getZLevel(), 0xffb1b1b1);
            Render2D.draw();
            RenderSystem.enableTexture();
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

    private static class Target extends ColoredTextButton {

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
        public boolean onMouseClicked(double mouseX, double mouseY, int button) {
            getParent().selected = this.index;
            return true;
        }

        @Nonnull
        @Override
        public TargetList getParent() {
            return (TargetList) Objects.requireNonNull(super.getParent());
        }

        private boolean isSelected() {
            return getParent().selected == this.index;
        }
    }
}
