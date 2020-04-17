package vswe.stevesfactory.ui.manager.editor;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.util.math.BlockPos;
import vswe.stevesfactory.Config;
import vswe.stevesfactory.api.logic.IProcedure;
import vswe.stevesfactory.api.network.INetworkController;
import vswe.stevesfactory.library.gui.RenderingHelper;
import vswe.stevesfactory.library.gui.ScissorTest;
import vswe.stevesfactory.library.gui.contextmenu.CallbackEntry;
import vswe.stevesfactory.library.gui.contextmenu.ContextMenu;
import vswe.stevesfactory.library.gui.debug.ITextReceiver;
import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.screen.WidgetScreen;
import vswe.stevesfactory.library.gui.window.Dialog;
import vswe.stevesfactory.ui.manager.DynamicWidthWidget;
import vswe.stevesfactory.ui.manager.FactoryManagerGUI;
import vswe.stevesfactory.ui.manager.tool.group.GroupDataModel;
import vswe.stevesfactory.utils.NetworkHelper;

import java.util.*;

import static org.lwjgl.glfw.GLFW.*;
import static vswe.stevesfactory.library.gui.RenderingHelper.fontRenderer;

public final class EditorPanel extends DynamicWidthWidget<FlowComponent<?>> {

    private Map<String, TreeSet<FlowComponent<?>>> groupMappedChildren = new HashMap<>();
    private TreeSet<FlowComponent<?>> children;
    private Collection<FlowComponent<?>> childrenView;
    private int nextZIndex = 0;

    private OffsetText xOffset;
    private OffsetText yOffset;

    public EditorPanel() {
        super(WidthOccupierType.MAX_WIDTH);

        xOffset = new OffsetText(I18n.format("gui.sfm.FactoryManager.Editor.XOff"), 0, 0);
        xOffset.setParentWidget(this);
        yOffset = new OffsetText(I18n.format("gui.sfm.FactoryManager.Editor.YOff"), 0, 0);
        yOffset.setParentWidget(this);

        children = new TreeSet<>();
        childrenView = children.descendingSet();
        groupMappedChildren.put(GroupDataModel.DEFAULT_GROUP, children);

        GroupDataModel data = FactoryManagerGUI.get().groupModel;
        data.addListenerRemove(this::onGroupRemoved);
        data.addListenerUpdate(this::onGroupUpdated);
        data.addListenerSelect(this::onGroupSelected);
    }

    private void onGroupRemoved(String group) {
        for (FlowComponent<?> component : children) {
            if (component.getGroup().equals(group)) {
                FactoryManagerGUI.get().scheduleTask(__ -> component.remove());
            }
        }
    }

    private void onGroupUpdated(String from, String to) {
        for (FlowComponent<?> component : children) {
            if (component.getGroup().equals(from)) {
                component.setGroup(to);
            }
        }
    }

    private void onGroupSelected(String current) {
        children = groupMappedChildren.computeIfAbsent(current, __ -> new TreeSet<>());
        childrenView = children.descendingSet();
    }

    public TreeSet<FlowComponent<?>> getFlowComponents() {
        return children;
    }

    @Override
    public Collection<FlowComponent<?>> getChildren() {
        return childrenView;
    }

    @Override
    public void reflow() {
    }

    @Override
    public EditorPanel addChildren(FlowComponent<?> widget) {
        widget.setParentWidget(this);
        widget.setZIndex(nextZIndex());
        children.add(widget);
        return this;
    }

    @Override
    public EditorPanel addChildren(Collection<FlowComponent<?>> widgets) {
        for (FlowComponent<?> widget : widgets) {
            widget.setParentWidget(this);
            widget.setZIndex(nextZIndex());
        }
        children.addAll(widgets);
        return this;
    }

    @Override
    public void render(int mouseX, int mouseY, float particleTicks) {
        RenderEventDispatcher.onPreRender(this, mouseX, mouseY);

        ScissorTest test = ScissorTest.scaled(getAbsoluteX(), getAbsoluteY(), getWidth(), getHeight());
        RenderSystem.pushMatrix();
        RenderSystem.translatef(xOffset.get(), yOffset.get(), 0F);
        RenderingHelper.translate(xOffset.get(), yOffset.get());
        {
            // Widgets are translated on render, which means player inputs will go at the translated positions
            // we need to translate the inputs back to the original position for logic handling, since the data position isn't changed at all
            int translatedX = mouseX - xOffset.get();
            int translatedY = mouseY - yOffset.get();

            // Iterate in ascending order for rendering as a special case
            for (FlowComponent<?> child : children) {
                child.render(translatedX, translatedY, particleTicks);
            }
        }
        RenderingHelper.clearTranslation();
        RenderSystem.popMatrix();
        test.destroy();

        xOffset.render(mouseX, mouseY, particleTicks);
        yOffset.render(mouseX, mouseY, particleTicks);

        RenderEventDispatcher.onPostRender(this, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        double translatedX = mouseX - xOffset.get();
        double translatedY = mouseY - yOffset.get();

        // All other events will be iterated in descending order
        for (FlowComponent<?> child : getChildren()) {
            if (child.mouseClicked(translatedX, translatedY, button)) {
                raiseComponentToTop(child);
                return true;
            }
        }

        if (xOffset.isInside(mouseX, mouseY)) {
            return xOffset.mouseClicked(mouseX, mouseY, button);
        }
        if (yOffset.isInside(mouseX, mouseY)) {
            return yOffset.mouseClicked(mouseX, mouseY, button);
        }
        if (isInside(mouseX, mouseY)) {
            switch (button) {
                case GLFW_MOUSE_BUTTON_LEFT:
                    getWindow().setFocusedWidget(this);
                    break;
                case GLFW_MOUSE_BUTTON_RIGHT:
                    openActionMenu();
                    break;
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (xOffset.isInside(mouseX, mouseY)) {
            return xOffset.mouseReleased(mouseX, mouseY, button);
        }
        if (yOffset.isInside(mouseX, mouseY)) {
            return yOffset.mouseReleased(mouseX, mouseY, button);
        }

        double translatedX = mouseX - xOffset.get();
        double translatedY = mouseY - yOffset.get();
        for (FlowComponent<?> child : getChildren()) {
            if (child.mouseReleased(translatedX, translatedY, button)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (isFocused()) {
            xOffset.add((int) Math.round(deltaX));
            yOffset.add((int) Math.round(deltaY));
        }
        if (xOffset.isInside(mouseX, mouseY)) {
            return xOffset.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }
        if (yOffset.isInside(mouseX, mouseY)) {
            return yOffset.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }

        for (FlowComponent<?> child : getChildren()) {
            if (child.mouseDragged(mouseX - xOffset.get(), mouseY - yOffset.get(), button, deltaX, deltaY)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scroll) {
        if (xOffset.isInside(mouseX, mouseY)) {
            return xOffset.mouseScrolled(mouseX, mouseY, scroll);
        }
        if (yOffset.isInside(mouseX, mouseY)) {
            return yOffset.mouseScrolled(mouseX, mouseY, scroll);
        }

        double translatedX = mouseX - xOffset.get();
        double translatedY = mouseY - yOffset.get();
        for (FlowComponent<?> child : getChildren()) {
            if (child.mouseScrolled(translatedX, translatedY, scroll)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        if (xOffset.isInside(mouseX, mouseY)) {
            xOffset.mouseMoved(mouseX, mouseY);
        }
        if (yOffset.isInside(mouseX, mouseY)) {
            yOffset.mouseMoved(mouseX, mouseY);
        }

        for (FlowComponent<?> child : getChildren()) {
            child.mouseMoved(mouseX - xOffset.get(), mouseY - yOffset.get());
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (FlowComponent<?> child : getChildren()) {
            if (child.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        for (FlowComponent<?> child : getChildren()) {
            if (child.keyReleased(keyCode, scanCode, modifiers)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean charTyped(char charTyped, int keyCode) {
        for (FlowComponent<?> child : getChildren()) {
            if (child.charTyped(charTyped, keyCode)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void update(float particleTicks) {
        for (FlowComponent<?> child : getChildren()) {
            child.update(particleTicks);
        }
    }

    private void openActionMenu() {
        ContextMenu contextMenu = ContextMenu.atCursor(ImmutableList.of(
                new CallbackEntry(FactoryManagerGUI.PASTE_ICON, "gui.sfm.FactoryManager.Editor.Paste", b -> actionPaste()),
                new CallbackEntry(null, "gui.sfm.FactoryManager.Editor.CleanupProcedures", b -> actionCleanup()),
                new CallbackEntry(null, "gui.sfm.FactoryManager.Generic.ToggleFullscreen", b -> FactoryManagerGUI.get().getPrimaryWindow().toggleFullscreen())
        ));
        WidgetScreen.getCurrent().addPopupWindow(contextMenu);
    }

    private void actionPaste() {
        String json = Minecraft.getInstance().keyboardListener.getClipboardString();
        CompoundNBT tag;
        try {
            tag = JsonToNBT.getTagFromJson(json);
        } catch (CommandSyntaxException e) {
            Dialog.createDialog("gui.sfm.FactoryManager.Editor.Paste.Fail").tryAddSelfToActiveGUI();
            return;
        }

        INetworkController controller = FactoryManagerGUI.get().getController();
        IProcedure procedure = NetworkHelper.retrieveProcedureAndAdd(controller, tag);

        addChildren(procedure.createFlowComponent());
    }

    private void actionCleanup() {
        // TODO implement
    }

    @Override
    public void onRelativePositionChanged() {
        super.onRelativePositionChanged();
        updateOffsets();
    }

    @Override
    public void onParentPositionChanged() {
        super.onParentPositionChanged();
        updateOffsets();
    }

    private void updateOffsets() {
        xOffset.onParentPositionChanged();
        yOffset.onParentPositionChanged();
    }

    @Override
    public void onAfterReflow() {
        int statusX = getWidth() - 4;
        int fontHeight = fontRenderer().FONT_HEIGHT;
        int yStatusY = getHeight() - 4 - fontHeight;
        xOffset.rightX = statusX;
        xOffset.setY(yStatusY - 4 - fontHeight);
        xOffset.update();
        yOffset.rightX = statusX;
        yOffset.setY(yStatusY);
        yOffset.update();
    }

    public void removeFlowComponent(FlowComponent<?> flowComponent) {
        children.remove(flowComponent);
        flowComponent.onRemoved();
    }

    private void raiseComponentToTop(FlowComponent<?> target) {
        // Move the flow component to top by setting its z-index to the maximum z-index ever given out
        target.setZIndex(nextZIndex());
        updateChild(target);
    }

    private int nextZIndex() {
        return nextZIndex++;
    }

    private int getLastDistributedZIndex() {
        return nextZIndex - 1;
    }

    private void updateChild(FlowComponent<?> child) {
        children.remove(child);
        children.add(child);
    }

    public void saveAll() {
        for (TreeSet<FlowComponent<?>> children : groupMappedChildren.values()) {
            for (FlowComponent<?> child : children) {
                child.save();
            }
        }
    }

    public void readProcedures() {
        BlockPos controllerPos = ((FactoryManagerGUI) WidgetScreen.getCurrent()).getController().getPosition();
        INetworkController controller = Objects.requireNonNull((INetworkController) Minecraft.getInstance().world.getTileEntity(controllerPos));

        FactoryManagerGUI.get().getTopLevel().connectionsPanel.disabledModification = true;
        Map<IProcedure, FlowComponent<?>> m = new HashMap<>();
        for (IProcedure procedure : controller.getPGraph().iterableValidAll()) {
            FlowComponent<?> f = procedure.createFlowComponent();
            f.setParentWidget(this);
            f.setZIndex(nextZIndex());

            m.put(procedure, f);
            groupMappedChildren.computeIfAbsent(f.getGroup(), __ -> new TreeSet<>()).add(f);
        }
        FactoryManagerGUI.get().getTopLevel().connectionsPanel.disabledModification = false;

        for (TreeSet<FlowComponent<?>> children : groupMappedChildren.values()) {
            for (FlowComponent<?> child : children) {
                child.readConnections(m);
            }
        }
    }

    public int getXOffset() {
        return xOffset.get();
    }

    public int getYOffset() {
        return yOffset.get();
    }

    public void moveGroup(String from, String to) {
        TreeSet<FlowComponent<?>> original = groupMappedChildren.computeIfAbsent(from, __ -> new TreeSet<>());
        groupMappedChildren.computeIfAbsent(to, __ -> new TreeSet<>()).addAll(original);
        for (FlowComponent<?> component : original) {
            component.setGroup(to);
        }
        original.clear();
    }

    @Override
    public void notifyChildrenForPositionChange() {
        for (TreeSet<FlowComponent<?>> children : groupMappedChildren.values()) {
            for (FlowComponent<?> child : children) {
                child.onParentPositionChanged();
            }
        }
    }

    @Override
    public void provideInformation(ITextReceiver receiver) {
        super.provideInformation(receiver);
        receiver.line("XOff=" + xOffset.get());
        receiver.line("YOff=" + yOffset.get());
    }
}
