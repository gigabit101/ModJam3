package vswe.stevesfactory.ui.manager.editor;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;
import vswe.stevesfactory.api.logic.IProcedure;
import vswe.stevesfactory.api.logic.IProcedureClientData;
import vswe.stevesfactory.library.gui.IWidget;
import vswe.stevesfactory.library.gui.TextureWrapper;
import vswe.stevesfactory.library.gui.actionmenu.ActionMenu;
import vswe.stevesfactory.library.gui.actionmenu.CallbackEntry;
import vswe.stevesfactory.library.gui.debug.ITextReceiver;
import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.layout.properties.BoxSizing;
import vswe.stevesfactory.library.gui.screen.WidgetScreen;
import vswe.stevesfactory.library.gui.widget.TextField;
import vswe.stevesfactory.library.gui.widget.*;
import vswe.stevesfactory.library.gui.widget.box.LinearList;
import vswe.stevesfactory.library.gui.window.Dialog;
import vswe.stevesfactory.ui.manager.editor.ControlFlow.Node;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.List;
import java.util.*;

import static vswe.stevesfactory.ui.manager.FactoryManagerGUI.*;

public class FlowComponent<P extends IProcedure & IProcedureClientData> extends AbstractContainer<IWidget> implements Comparable<IZIndexProvider>, IZIndexProvider {

    public enum State {
        COLLAPSED(TextureWrapper.ofFlowComponent(0, 0, 64, 20),
                TextureWrapper.ofFlowComponent(0, 20, 9, 10),
                TextureWrapper.ofFlowComponent(0, 30, 9, 10),
                54, 5,
                43, 6,
                45, 3,
                45, 11) {
            @Override
            public void changeState(FlowComponent flowComponent) {
                flowComponent.expand();
            }
        },
        EXPANDED(TextureWrapper.ofFlowComponent(64, 0, 124, 152),
                TextureWrapper.ofFlowComponent(9, 20, 9, 10),
                TextureWrapper.ofFlowComponent(9, 30, 9, 10),
                114, 5,
                103, 6,
                105, 3,
                105, 11) {
            @Override
            public void changeState(FlowComponent flowComponent) {
                flowComponent.collapse();
            }
        };

        public final TextureWrapper background;
        public final TextureWrapper toggleStateNormal;
        public final TextureWrapper toggleStateHovered;

        public final int toggleStateButtonX;
        public final int toggleStateButtonY;
        public final int renameButtonX;
        public final int renameButtonY;
        public final int submitButtonX;
        public final int submitButtonY;
        public final int cancelButtonX;
        public final int cancelButtonY;

        public final Dimension dimensions;

        State(TextureWrapper background, TextureWrapper toggleStateNormal, TextureWrapper toggleStateHovered, int toggleStateButtonX, int toggleStateButtonY, int renameButtonX, int renameButtonY, int submitButtonX, int submitButtonY, int cancelButtonX, int cancelButtonY) {
            this.background = background;
            this.toggleStateNormal = toggleStateNormal;
            this.toggleStateHovered = toggleStateHovered;
            this.toggleStateButtonX = toggleStateButtonX;
            this.toggleStateButtonY = toggleStateButtonY;
            this.renameButtonX = renameButtonX;
            this.renameButtonY = renameButtonY;
            this.submitButtonX = submitButtonX;
            this.submitButtonY = submitButtonY;
            this.cancelButtonX = cancelButtonX;
            this.cancelButtonY = cancelButtonY;

            this.dimensions = new Dimension(componentWidth(), componentHeight());
        }

        public int componentWidth() {
            return background.getPortionWidth();
        }

        public int componentHeight() {
            return background.getPortionHeight();
        }

        public abstract void changeState(FlowComponent flowComponent);
    }

    public static class ToggleStateButton extends AbstractIconButton {

        public ToggleStateButton(FlowComponent parent) {
            super(-1, -1, 9, 10);
            setParentWidget(parent);
        }

        @Override
        public TextureWrapper getTextureNormal() {
            return getParentWidget().getState().toggleStateNormal;
        }

        @Override
        public TextureWrapper getTextureHovered() {
            return getParentWidget().getState().toggleStateHovered;
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                getParentWidget().toggleState();
                return true;
            }
            return false;
        }

        @Nonnull
        @Override
        public FlowComponent getParentWidget() {
            return Objects.requireNonNull((FlowComponent) super.getParentWidget());
        }

        @Override
        public BoxSizing getBoxSizing() {
            return BoxSizing.PHANTOM;
        }

        public void updateTo(State state) {
            setLocation(state.toggleStateButtonX, state.toggleStateButtonY);
        }
    }

    public static class RenameButton extends AbstractIconButton {

        public static final TextureWrapper NORMAL = TextureWrapper.ofFlowComponent(0, 120, 9, 9);
        public static final TextureWrapper HOVERING = NORMAL.toRight(1);

        public RenameButton(FlowComponent parent) {
            super(-1, -1, 9, 9);
            setParentWidget(parent);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (isEnabled() && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                setEnabled(false);
                FlowComponent parent = getParentWidget();
                parent.submitButton.setEnabled(true);
                parent.cancelButton.setEnabled(true);
                parent.nameBox.setEditable(true);
                getWindow().setFocusedWidget(parent.nameBox);
                return true;
            }
            return false;
        }

        @Override
        public TextureWrapper getTextureNormal() {
            return NORMAL;
        }

        @Override
        public TextureWrapper getTextureHovered() {
            return HOVERING;
        }

        @Nonnull
        @Override
        public FlowComponent getParentWidget() {
            return Objects.requireNonNull((FlowComponent) super.getParentWidget());
        }

        @Override
        public BoxSizing getBoxSizing() {
            return BoxSizing.PHANTOM;
        }

        public void updateTo(State state) {
            setLocation(state.renameButtonX, state.renameButtonY);
        }
    }

    public static class SubmitButton extends AbstractIconButton {

        public static final TextureWrapper NORMAL = TextureWrapper.ofFlowComponent(0, 129, 7, 7);
        public static final TextureWrapper HOVERING = NORMAL.toRight(1);

        public SubmitButton(FlowComponent parent) {
            super(-1, -1, 7, 7);
            setParentWidget(parent);
            setEnabled(false);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (isEnabled() && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                setEnabled(false);
                FlowComponent parent = getParentWidget();
                parent.cancelButton.setEnabled(false);
                parent.renameButton.setEnabled(true);
                parent.nameBox.scrollToFront();
                parent.nameBox.setEditable(false);
                parent.getDataHandler().setName(parent.getName());
                getWindow().changeFocus(parent.nameBox, false);
                return true;
            }
            return false;
        }

        @Override
        public TextureWrapper getTextureNormal() {
            return NORMAL;
        }

        @Override
        public TextureWrapper getTextureHovered() {
            return HOVERING;
        }

        @Nonnull
        @Override
        public FlowComponent getParentWidget() {
            return Objects.requireNonNull((FlowComponent) super.getParentWidget());
        }

        @Override
        public BoxSizing getBoxSizing() {
            return BoxSizing.PHANTOM;
        }

        public void updateTo(State state) {
            setLocation(state.submitButtonX, state.submitButtonY);
        }
    }

    public static class CancelButton extends AbstractIconButton {

        public static final TextureWrapper NORMAL = TextureWrapper.ofFlowComponent(0, 136, 7, 7);
        public static final TextureWrapper HOVERING = NORMAL.toRight(1);

        private String previousName;

        public CancelButton(FlowComponent parent, String previousName) {
            super(-1, -1, 7, 7);
            setParentWidget(parent);
            setEnabled(false);
            this.previousName = previousName;
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (isEnabled() && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                setEnabled(false);
                FlowComponent parent = getParentWidget();
                parent.submitButton.setEnabled(false);
                parent.renameButton.setEnabled(true);
                parent.setName(previousName);
                parent.nameBox.scrollToFront();
                parent.nameBox.setEditable(false);
                getWindow().changeFocus(parent.nameBox, false);
                previousName = "";
                return true;
            }
            return false;
        }

        @Override
        public void setEnabled(boolean enabled) {
            super.setEnabled(enabled);
            if (enabled) {
                this.previousName = getParentWidget().getName();
            }
        }

        @Override
        public TextureWrapper getTextureNormal() {
            return NORMAL;
        }

        @Override
        public TextureWrapper getTextureHovered() {
            return HOVERING;
        }

        @Nonnull
        @Override
        public FlowComponent getParentWidget() {
            return Objects.requireNonNull((FlowComponent) super.getParentWidget());
        }

        @Override
        public BoxSizing getBoxSizing() {
            return BoxSizing.PHANTOM;
        }

        public void updateTo(State state) {
            setLocation(state.cancelButtonX, state.cancelButtonY);
        }
    }

    public static <P extends IProcedure & IProcedureClientData> FlowComponent<P> of(P procedure, int inputNodes, int outputNodes) {
        if (!procedure.isNameInitialized()) {
            procedure.setName(I18n.format("logic.sfm." + procedure.getRegistryName().getPath()));
        }
        return new FlowComponent<>(procedure, inputNodes, outputNodes);
    }

    public static <P extends IProcedure & IProcedureClientData> FlowComponent<P> of(P procedure) {
        return of(procedure, 1, 1);
    }

    private P procedure;

    private int id;

    private final ToggleStateButton toggleStateButton;
    private final RenameButton renameButton;
    private final SubmitButton submitButton;
    private final CancelButton cancelButton;
    private final TextField nameBox;
    private final ControlFlow inputNodes;
    private final ControlFlow outputNodes;
    private final LinearList<Menu<P>> menus;
    // A list that refers to all the widgets above
    private final List<IWidget> children;

    private State state;
    private ActionMenu openedActionMenu;
    private int zIndex;

    // Temporary data
    private int initialDragLocalX;
    private int initialDragLocalY;

    public FlowComponent(P procedure, int inputNodes, int outputNodes) {
        super(0, 0, 0, 0);
        String name = procedure.getName();
        this.toggleStateButton = new ToggleStateButton(this);
        this.renameButton = new RenameButton(this);
        this.submitButton = new SubmitButton(this);
        this.cancelButton = new CancelButton(this, name);
        // The cursor looks a bit to short (and cute) with these numbers, might want change them?
        this.nameBox = new TextField(6, 8, 35, 10)
                .setBackgroundStyle(TextField.BackgroundStyle.NONE)
                .setText(name)
                .setTextColor(0xff303030, 0xff303030)
                .setEditable(false)
                .setFontHeight(6);
        this.inputNodes = ControlFlow.inputNodes(inputNodes);
        this.outputNodes = ControlFlow.outputNodes(outputNodes);
        this.menus = new MenusList<>(120, 130);
        this.menus.setLocation(2, 20);
        this.children = ImmutableList.of(toggleStateButton, renameButton, submitButton, cancelButton, nameBox, this.inputNodes, this.outputNodes, menus);
        this.setLinkedProcedure(procedure);

        this.collapse();
        this.reflow();
    }

    @Override
    public Dimension getDimensions() {
        return state.dimensions;
    }

    public TextureWrapper getBackgroundTexture() {
        return state.background;
    }

    public State getState() {
        return state;
    }

    public void toggleState() {
        state.changeState(this);
    }

    public void expand() {
        state = State.EXPANDED;

        nameBox.setWidth(95);
        updateMenusEnableState(true);
        reflow();
    }

    public void collapse() {
        state = State.COLLAPSED;

        nameBox.setWidth(35);
        updateMenusEnableState(false);
        reflow();
    }

    private void updateMenusEnableState(boolean enabled) {
        menus.setEnabled(enabled);
        for (Menu<P> menu : menus.getChildren()) {
            menu.setEnabled(enabled);
        }
    }

    public String getName() {
        return nameBox.getText();
    }

    public void setName(String name) {
        this.nameBox.setText(name);
        this.cancelButton.previousName = getName();
        this.procedure.setName(name);
    }

    @Override
    public List<IWidget> getChildren() {
        return children;
    }

    public LinearList<Menu<P>> getMenusBox() {
        return menus;
    }

    public void collapseAllMenus() {
        for (Menu<P> menu : menus.getChildren()) {
            menu.collapse();
        }
        menus.setScrollDistance(0F);
    }

    public void expandAllMenus() {
        for (Menu<P> menu : menus.getChildren()) {
            menu.expand();
        }
    }

    @Override
    public void reflow() {
        toggleStateButton.updateTo(state);
        renameButton.updateTo(state);
        submitButton.updateTo(state);
        cancelButton.updateTo(state);
        inputNodes.setWidth(state.dimensions.width);
        inputNodes.setY(-Node.HEIGHT);
        inputNodes.reflow();
        outputNodes.setWidth(state.dimensions.width);
        outputNodes.setY(getHeight());
        outputNodes.reflow();
        nameBox.scrollToFront();
        menus.reflow();
    }

    public FlowComponent<P> addMenu(Menu<P> menu) {
        menus.addChildren(menu);
        menu.onLinkFlowComponent(this);
        return this;
    }

    @Override
    public FlowComponent<P> addChildren(IWidget widget) {
        if (widget instanceof Menu) {
            @SuppressWarnings("unchecked") Menu<P> menu = (Menu<P>) widget;
            return addMenu(menu);
        } else {
            throw new IllegalArgumentException("Flow components do not accept new child widgets with type other than Menu");
        }
    }

    @Override
    public FlowComponent<P> addChildren(Collection<IWidget> widgets) {
        for (IWidget widget : widgets) {
            addChildren(widget);
        }
        return this;
    }

    @Override
    public void render(int mouseX, int mouseY, float particleTicks) {
        RenderEventDispatcher.onPreRender(this, mouseX, mouseY);

        GlStateManager.color3f(1F, 1F, 1F);
        getBackgroundTexture().draw(getAbsoluteX(), getAbsoluteY());

        // Renaming state (showing different buttons at different times) is handled inside the widgets' render method
        toggleStateButton.render(mouseX, mouseY, particleTicks);
        renameButton.render(mouseX, mouseY, particleTicks);
        submitButton.render(mouseX, mouseY, particleTicks);
        cancelButton.render(mouseX, mouseY, particleTicks);
        nameBox.render(mouseX, mouseY, particleTicks);
        inputNodes.render(mouseX, mouseY, particleTicks);
        outputNodes.render(mouseX, mouseY, particleTicks);
        menus.render(mouseX, mouseY, particleTicks);

        if (nameBox.isInside(mouseX, mouseY)) {
            WidgetScreen.getCurrentScreen().setHoveringText(getName(), mouseX, mouseY);
        }

        RenderEventDispatcher.onPostRender(this, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            clearDrag();
            return true;
        }
        if (!isInside(mouseX, mouseY)) {
            clearDrag();
            return false;
        }

        getWindow().setFocusedWidget(this);
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            initialDragLocalX = (int) mouseX - getAbsoluteX();
            initialDragLocalY = (int) mouseY - getAbsoluteY();
        } else {
            clearDrag();
        }
        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            openActionMenu(mouseX, mouseY);
        }
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (!isEnabled()) {
            return false;
        }
        if (super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
            return true;
        }
        if (isFocused() && isDragging()) {
            EditorPanel parent = getParentWidget();
            int x = (int) mouseX - parent.getAbsoluteX() - initialDragLocalX;
            int y = (int) mouseY - parent.getAbsoluteY() - initialDragLocalY;
            setLocation(x, y);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (!isEnabled()) {
            return false;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scroll) {
        if (!isEnabled()) {
            return false;
        }
        return super.mouseScrolled(mouseX, mouseY, scroll);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!isEnabled()) {
            return false;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (!isEnabled()) {
            return false;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char charTyped, int keyCode) {
        if (!isEnabled()) {
            return false;
        }
        return super.charTyped(charTyped, keyCode);
    }

    private void clearDrag() {
        initialDragLocalX = -1;
        initialDragLocalY = -1;
    }

    private boolean isDragging() {
        return initialDragLocalX != -1 && initialDragLocalY != -1;
    }

    public void setParentWidget(EditorPanel parent) {
        this.setParentWidget((IWidget) parent);
        id = parent.nextID();
    }

    private void openActionMenu(double mouseX, double mouseY) {
        openedActionMenu = ActionMenu.atCursor(mouseX, mouseY, ImmutableList.of(
                new CallbackEntry(DELETE_ICON, "gui.sfm.ActionMenu.Delete", b -> actionDelete()),
                new CallbackEntry(CUT_ICON, "gui.sfm.ActionMenu.Cut", b -> actionCut()),
                new CallbackEntry(COPY_ICON, "gui.sfm.ActionMenu.Copy", b -> actionCopy())
        ));
        WidgetScreen.getCurrentScreen().addPopupWindow(openedActionMenu);
    }

    private void actionDelete() {
        if (Screen.hasShiftDown()) {
            Dialog.createBiSelectionDialog(
                    "gui.sfm.ActionMenu.DeleteAll.ConfirmMsg",
                    "gui.sfm.yes",
                    "gui.sfm.no",
                    b -> removeGraph(), b -> {}).tryAddSelfToActiveGUI();
        } else {
            removeSelf();
        }
    }

    private void actionCopy() {
        save();
        CompoundNBT tag = procedure.serialize();
        minecraft().keyboardListener.setClipboardString(tag.toString());
    }

    private void actionCut() {
        actionCopy();
        removeSelf();
    }

    public void save() {
        for (Menu<?> menu : menus.getChildren()) {
            menu.updateData();
        }
    }

    public void removeGraph() {
        Set<FlowComponent<?>> children = getParentWidget().getFlowComponents();
        for (FlowComponent<?> flowComponent : children) {
            flowComponent.removeLinkedProcedure();
        }
        children.clear();
    }

    public void removeSelf() {
        removeLinkedProcedure();
        getParentWidget().getFlowComponents().remove(this);
    }

    private void removeLinkedProcedure() {
        inputNodes.removeAllConnections();
        outputNodes.removeAllConnections();
        procedure.remove();
    }

    public ControlFlow getInputNodes() {
        return inputNodes;
    }

    public ControlFlow getOutputNodes() {
        return outputNodes;
    }

    @Nonnull
    @Override
    public EditorPanel getParentWidget() {
        return Objects.requireNonNull((EditorPanel) super.getParentWidget());
    }

    public int getID() {
        return id;
    }

    @Override
    public int getZIndex() {
        return zIndex;
    }

    @Override
    public void setZIndex(int z) {
        this.zIndex = z;
    }

    @Override
    public int compareTo(IZIndexProvider that) {
        return this.getZIndex() - that.getZIndex();
    }

    public P getLinkedProcedure() {
        return procedure;
    }

    public IProcedure getProcedure() {
        return procedure;
    }

    public IProcedureClientData getDataHandler() {
        return procedure;
    }

    public void setLinkedProcedure(P procedure) {
        this.procedure = procedure;
        setName(procedure.getName());
        setLocation(procedure.getComponentX(), procedure.getComponentY());
    }

    void readConnections(Map<IProcedure, FlowComponent<?>> m) {
        this.inputNodes.readConnections(m, procedure);
        this.outputNodes.readConnections(m, procedure);
    }

    @Override
    public void setLocation(int x, int y) {
        @Nullable IWidget parent = super.getParentWidget();
        if (parent != null) {
            Dimension bounds = parent.getDimensions();
            x = MathHelper.clamp(x, 0, bounds.width);
            y = MathHelper.clamp(y, 0, bounds.height);
        }
        super.setLocation(x, y);
        procedure.setComponentX(x);
        procedure.setComponentY(y);
    }

    @Override
    public void provideInformation(ITextReceiver receiver) {
        super.provideInformation(receiver);
        receiver.line("Z=" + this.getZIndex());
    }

    public static class MenusList<T extends IWidget> extends LinearList<T> {

        public MenusList(int width, int height) {
            super(width, height);
        }

        @Override
        protected boolean isDrawingScrollBar() {
            return false;
        }

        @Override
        public int getBorder() {
            return 0;
        }

        @Override
        public int getMarginMiddle() {
            return 0;
        }
    }
}
