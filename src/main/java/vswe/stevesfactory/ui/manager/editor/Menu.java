package vswe.stevesfactory.ui.manager.editor;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.GlStateManager;
import org.lwjgl.glfw.GLFW;
import vswe.stevesfactory.api.logic.IProcedure;
import vswe.stevesfactory.api.logic.IProcedureClientData;
import vswe.stevesfactory.library.gui.TextureWrapper;
import vswe.stevesfactory.library.gui.actionmenu.ActionMenu;
import vswe.stevesfactory.library.gui.actionmenu.CallbackEntry;
import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.layout.properties.BoxSizing;
import vswe.stevesfactory.library.gui.screen.WidgetScreen;
import vswe.stevesfactory.library.gui.widget.*;
import vswe.stevesfactory.library.gui.widget.box.LinearList;
import vswe.stevesfactory.library.gui.widget.mixin.ResizableWidgetMixin;
import vswe.stevesfactory.utils.RenderingHelper;

import javax.annotation.Nonnull;
import java.util.*;

public abstract class Menu<P extends IProcedure & IProcedureClientData> extends AbstractContainer<IWidget> implements ResizableWidgetMixin {

    public enum State {
        COLLAPSED(TextureWrapper.ofFlowComponent(0, 40, 9, 9),
                TextureWrapper.ofFlowComponent(9, 40, 9, 9)) {
            @Override
            public void toggleStateFor(Menu menu) {
                menu.expand();
            }
        },
        EXPANDED(TextureWrapper.ofFlowComponent(0, 49, 9, 9),
                TextureWrapper.ofFlowComponent(9, 49, 9, 9)) {
            @Override
            public void toggleStateFor(Menu menu) {
                menu.collapse();
            }
        };

        public final TextureWrapper toggleStateNormalTexture;
        public final TextureWrapper toggleStateHoveringTexture;

        State(TextureWrapper toggleStateNormalTexture, TextureWrapper toggleStateHoveringTexture) {
            this.toggleStateNormalTexture = toggleStateNormalTexture;
            this.toggleStateHoveringTexture = toggleStateHoveringTexture;
        }

        public abstract void toggleStateFor(Menu menu);
    }

    public static class ToggleStateButton extends AbstractIconButton {

        public ToggleStateButton(Menu parent) {
            super(109, 2, 9, 9);
            setParentWidget(parent);
        }

        @Override
        public TextureWrapper getTextureNormal() {
            return getParentWidget().state.toggleStateNormalTexture;
        }

        @Override
        public TextureWrapper getTextureHovered() {
            return getParentWidget().state.toggleStateHoveringTexture;
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            getParentWidget().toggleState();
            return true;
        }

        @Nonnull
        @Override
        public Menu getParentWidget() {
            return Objects.requireNonNull((Menu) super.getParentWidget());
        }

        @Override
        public BoxSizing getBoxSizing() {
            return BoxSizing.PHANTOM;
        }

        @Override
        public void render(int mouseX, int mouseY, float particleTicks) {
            GlStateManager.color3f(1F, 1F, 1F);
            super.render(mouseX, mouseY, particleTicks);
        }
    }

    public static final TextureWrapper HEADING_BOX = TextureWrapper.ofFlowComponent(66, 152, 120, 13);
    public static final int DEFAULT_CONTENT_HEIGHT = 65;

    private FlowComponent<P> flowComponent;

    private State state = State.COLLAPSED;

    private ToggleStateButton toggleStateButton;
    private final List<IWidget> children;

    public Menu() {
        // Start at a collapsed state
        super(0, 0, HEADING_BOX.getPortionWidth(), HEADING_BOX.getPortionHeight());
        this.toggleStateButton = new ToggleStateButton(this);
        this.children = new ArrayList<>();
        {
            children.add(toggleStateButton);
        }
    }

    @Override
    public List<IWidget> getChildren() {
        return children;
    }

    @Override
    public void reflow() {
    }

    public ToggleStateButton getToggleStateButton() {
        return toggleStateButton;
    }

    @Override
    public Menu<P> addChildren(IWidget widget) {
        children.add(widget);
        return this;
    }

    @Override
    public Menu<P> addChildren(Collection<IWidget> widgets) {
        children.addAll(widgets);
        return this;
    }

    public void toggleState() {
        state.toggleStateFor(this);
    }

    public void expand() {
        if (state == State.COLLAPSED) {
            state = State.EXPANDED;
            growHeight(getContentHeight());
            updateChildrenEnableState(true);
        }
    }

    public void collapse() {
        if (state == State.EXPANDED) {
            state = State.COLLAPSED;
            shrinkHeight(getContentHeight());
            updateChildrenEnableState(false);
        }
    }

    private void updateChildrenEnableState(boolean state) {
        for (int i = 1; i < children.size(); i++) {
            IWidget child = children.get(i);
            child.setEnabled(state);
        }
    }

    public void growHeight(int growth) {
        setHeight(getHeight() + growth);
    }

    public void shrinkHeight(int shrinkage) {
        growHeight(-shrinkage);
    }

    @Override
    public void setHeight(int height) {
        super.setHeight(height);
        getParentWidget().reflow();
    }

    @Override
    public void setWidth(int width) {
        throw new UnsupportedOperationException();
    }

    public int getContentHeight() {
        return DEFAULT_CONTENT_HEIGHT;
    }

    @Override
    public void render(int mouseX, int mouseY, float particleTicks) {
        if (!isEnabled()) {
            return;
        }

        RenderEventDispatcher.onPreRender(this, mouseX, mouseY);
        GlStateManager.color3f(1F, 1F, 1F);
        HEADING_BOX.draw(getAbsoluteX(), getAbsoluteY());
        renderHeadingText();

        if (state == State.EXPANDED) {
            renderContents(mouseX, mouseY, particleTicks);
        } else {
            toggleStateButton.render(mouseX, mouseY, particleTicks);
        }
        RenderEventDispatcher.onPostRender(this, mouseX, mouseY);
    }

    public void renderHeadingText() {
        int y1 = getAbsoluteY();
        int y2 = y1 + HEADING_BOX.getPortionHeight();
        RenderingHelper.drawTextCenteredVertically(getHeadingText(), getHeadingLeftX(), y1, y2 + 1, getHeadingColor());
    }

    public int getHeadingLeftX() {
        return getAbsoluteX() + 5;
    }

    public int getHeadingColor() {
        return 0x404040;
    }

    public abstract String getHeadingText();

    public void renderContents(int mouseX, int mouseY, float particleTicks) {
        for (IWidget child : children) {
            child.render(mouseX, mouseY, particleTicks);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isEnabled()) {
            return false;
        }
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return state != State.COLLAPSED || toggleStateButton.isInside(mouseX, mouseY);
        }
        if (isInside(mouseX, mouseY)) {
            getWindow().setFocusedWidget(this);
            if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                ActionMenu actionMenu = ActionMenu.atCursor(mouseX, mouseY, ImmutableList.of(
                        new CallbackEntry(null, "gui.sfm.ActionMenu.Menus.CollapseAll", b -> flowComponent.collapseAllMenus()),
                        new CallbackEntry(null, "gui.sfm.ActionMenu.Menus.ExpandAll", b -> flowComponent.expandAllMenus())
                ));
                WidgetScreen.getCurrentScreen().addPopupWindow(actionMenu);
            }
            return true;
        }
        return false;
    }

    @Override
    public void onFocusChanged(boolean focus) {
        updateData();
    }

    @Override
    public void onRemoved() {
        updateData();
    }

    protected void updateData() {
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    @Override
    public LinearList<Menu<P>> getParentWidget() {
        return Objects.requireNonNull((LinearList<Menu<P>>) super.getParentWidget());
    }

    public void onLinkFlowComponent(FlowComponent<P> flowComponent) {
        Preconditions.checkState(this.flowComponent == null);
        this.flowComponent = flowComponent;
        this.setParentWidget(flowComponent.getMenusBox());
    }

    public FlowComponent<P> getFlowComponent() {
        return flowComponent;
    }

    public P getLinkedProcedure() {
        return flowComponent.getLinkedProcedure();
    }
}
