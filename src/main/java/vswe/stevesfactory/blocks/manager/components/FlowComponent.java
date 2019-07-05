package vswe.stevesfactory.blocks.manager.components;

import com.google.common.base.Preconditions;
import vswe.stevesfactory.library.gui.TextureWrapper;
import vswe.stevesfactory.library.gui.core.*;
import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.layout.BoxSizing;
import vswe.stevesfactory.library.gui.layout.FlowLayout;
import vswe.stevesfactory.library.gui.widget.TextField;
import vswe.stevesfactory.library.gui.widget.*;
import vswe.stevesfactory.library.gui.widget.mixin.ContainerWidgetMixin;
import vswe.stevesfactory.library.gui.widget.mixin.RelocatableContainerMixin;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.List;
import java.util.*;

public abstract class FlowComponent extends AbstractWidget implements IContainer<IWidget>, ContainerWidgetMixin<IWidget>, RelocatableContainerMixin<IWidget> {

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
            onParentChanged(parent);
        }

        @Override
        public TextureWrapper getTextureNormal() {
            return getParentWidget().getState().toggleStateNormal;
        }

        @Override
        public TextureWrapper getTextureHovering() {
            return getParentWidget().getState().toggleStateHovered;
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            getParentWidget().toggleState();
            return true;
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

        public static final TextureWrapper NORMAL = TextureWrapper.ofFlowComponent(32, 188, 9, 9);
        public static final TextureWrapper HOVERING = TextureWrapper.ofFlowComponent(41, 188, 9, 9);

        public RenameButton(FlowComponent parent) {
            super(-1, -1, 9, 9);
            onParentChanged(parent);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (isEnabled()) {
                setEnabled(false);
                FlowComponent parent = getParentWidget();
                parent.submitButton.setEnabled(true);
                parent.cancelButton.setEnabled(true);
                return true;
            }
            return false;
        }

        @Override
        public TextureWrapper getTextureNormal() {
            return NORMAL;
        }

        @Override
        public TextureWrapper getTextureHovering() {
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

        public static final TextureWrapper NORMAL = TextureWrapper.ofFlowComponent(32, 197, 7, 7);
        public static final TextureWrapper HOVERING = TextureWrapper.ofFlowComponent(39, 197, 7, 7);

        public SubmitButton(FlowComponent parent) {
            super(-1, -1, 7, 7);
            onParentChanged(parent);
            setEnabled(false);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (isEnabled()) {
                setEnabled(false);
                FlowComponent parent = getParentWidget();
                parent.cancelButton.setEnabled(false);
                parent.renameButton.setEnabled(true);
                return true;
            }
            return false;
        }

        @Override
        public TextureWrapper getTextureNormal() {
            return NORMAL;
        }

        @Override
        public TextureWrapper getTextureHovering() {
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

        public static final TextureWrapper NORMAL = TextureWrapper.ofFlowComponent(32, 204, 7, 7);
        public static final TextureWrapper HOVERING = TextureWrapper.ofFlowComponent(39, 204, 7, 7);

        private String previousName;

        public CancelButton(FlowComponent parent) {
            super(-1, -1, 7, 7);
            onParentChanged(parent);
            setEnabled(false);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (isEnabled()) {
                setEnabled(false);
                FlowComponent parent = getParentWidget();
                parent.submitButton.setEnabled(false);
                parent.renameButton.setEnabled(true);
                parent.setName(previousName);
                previousName = "";
                return true;
            }
            return false;
        }

        @Override
        public void setEnabled(boolean enabled) {
            super.setEnabled(enabled);
            if (enabled) {
                previousName = getParentWidget().getName();
            }
        }

        @Override
        public TextureWrapper getTextureNormal() {
            return NORMAL;
        }

        @Override
        public TextureWrapper getTextureHovering() {
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

    // TODO decided whether I want other widget to hold all the menus or not

    // Even though flow control components might have multiple parents, it is not important to the execution flow
    private FlowComponent parentComponent;
    // Use array here because it would always have a fixed size
    private FlowComponent[] childComponents;

    private ToggleStateButton toggleStateButton;
    private RenameButton renameButton;
    private SubmitButton submitButton;
    private CancelButton cancelButton;
    private TextField name;
    private List<Menu> menuComponents;
    private final List<IWidget> children;

    private State state;

    public FlowComponent(EditorPanel parent) {
        super(0, 0);
        onParentChanged(parent);
        this.toggleStateButton = new ToggleStateButton(this);
        this.renameButton = new RenameButton(this);
        this.submitButton = new SubmitButton(this);
        this.cancelButton = new CancelButton(this);
        // TODO exact number
        this.name = new TextField(9, 9, 80, 16);
        this.name.onParentChanged(this);
        this.menuComponents = new ArrayList<>();
        this.children = new AbstractList<IWidget>() {
            @Override
            public IWidget get(int i) {
                switch (i) {
                    case 0: return toggleStateButton;
                    case 1: return renameButton;
                    case 2: return submitButton;
                    case 3: return cancelButton;
                    case 4: return name;
                    default: return menuComponents.get(i);
                }
            }

            @Override
            public int size() {
                return 5 + menuComponents.size();
            }
        };

        this.state = State.COLLAPSED;
        this.updateChildStates();
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

    public void expand() {
        Preconditions.checkState(state == State.COLLAPSED);
        state = State.EXPANDED;
        updateChildStates();
    }

    public void collapse() {
        Preconditions.checkState(state == State.EXPANDED);
        state = State.COLLAPSED;
        updateChildStates();
    }

    private void updateChildStates() {
        toggleStateButton.updateTo(state);
        renameButton.updateTo(state);
        submitButton.updateTo(state);
        cancelButton.updateTo(state);
    }

    public void toggleState() {
        state.changeState(this);
    }

    public String getName() {
        return name.getText();
    }

    public void setName(String name) {
        this.name.setText(name);
    }

    @Override
    public List<IWidget> getChildren() {
        return children;
    }

    public FlowComponent addChildren(Menu menu) {
        // TODO remove this limit by adding a scrolling list to the menus
        if (menuComponents.size() >= 5) {
            throw new IllegalStateException();
        }
        menuComponents.add(menu);
        return this;
    }

    @Override
    public void reflow() {
        // We ignore the buttons here on purpose, since their position are directly defined as coordinates
        FlowLayout.INSTANCE.reflow(getDimensions(), menuComponents);
    }

    @Override
    public FlowComponent addChildren(IWidget widget) {
        if (widget instanceof Menu) {
            return addChildren(widget);
        } else {
            throw new IllegalArgumentException("Flow components do not accept new child widgets with type other than Menu");
        }
    }

    @Override
    public FlowComponent addChildren(Collection<IWidget> widgets) {
        for (IWidget widget : widgets) {
            addChildren(widget);
        }
        return this;
    }

    @Override
    public void render(int mouseX, int mouseY, float particleTicks) {
        RenderEventDispatcher.onPreRender(this, mouseX, mouseY);

        getBackgroundTexture().draw(getAbsoluteX(), getAbsoluteY());

        // Renaming state (showing different buttons at different times) is handled inside the widgets' render method
        toggleStateButton.render(mouseX, mouseY, particleTicks);
        renameButton.render(mouseX, mouseY, particleTicks);
        submitButton.render(mouseX, mouseY, particleTicks);
        cancelButton.render(mouseX, mouseY, particleTicks);
        name.render(mouseX, mouseY, particleTicks);

        if (state == State.EXPANDED) {
            for (Menu menu : menuComponents) {
                menu.render(mouseX, mouseY, particleTicks);
            }
        }

        RenderEventDispatcher.onPostRender(this, mouseX, mouseY);
    }

    public FlowComponent getParentComponent() {
        return parentComponent;
    }

    public FlowComponent[] getChildComponents() {
        return childComponents;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (ContainerWidgetMixin.super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        getWindow().changeFocus(this, true);
        return false;
    }
}