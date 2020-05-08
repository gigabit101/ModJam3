package vswe.stevesfactory.ui.manager.editor;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import vswe.stevesfactory.api.logic.IClientDataStorage;
import vswe.stevesfactory.api.logic.IErrorPopulator;
import vswe.stevesfactory.api.logic.IProcedure;
import vswe.stevesfactory.library.gui.Render2D;
import vswe.stevesfactory.library.gui.Texture;
import vswe.stevesfactory.library.gui.contextmenu.CallbackEntry;
import vswe.stevesfactory.library.gui.contextmenu.ContextMenuBuilder;
import vswe.stevesfactory.library.gui.contextmenu.IEntry;
import vswe.stevesfactory.library.gui.contextmenu.Section;
import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.layout.properties.BoxSizing;
import vswe.stevesfactory.library.gui.widget.AbstractContainer;
import vswe.stevesfactory.library.gui.widget.IWidget;
import vswe.stevesfactory.library.gui.widget.button.AbstractIconButton;
import vswe.stevesfactory.library.gui.widget.panel.VerticalList;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public abstract class Menu<P extends IProcedure & IClientDataStorage> extends AbstractContainer<IWidget> implements IErrorPopulator, IWidget {

    public enum State {
        COLLAPSED(Render2D.ofFlowComponent(0, 40, 9, 9),
                Render2D.ofFlowComponent(9, 40, 9, 9)) {
            @Override
            public void toggleStateFor(Menu<?> menu) {
                menu.expand();
            }
        },
        EXPANDED(Render2D.ofFlowComponent(0, 49, 9, 9),
                Render2D.ofFlowComponent(9, 49, 9, 9)) {
            @Override
            public void toggleStateFor(Menu<?> menu) {
                menu.collapse();
            }
        };

        public final Texture toggleStateNormalTexture;
        public final Texture toggleStateHoveringTexture;

        State(Texture toggleStateNormalTexture, Texture toggleStateHoveringTexture) {
            this.toggleStateNormalTexture = toggleStateNormalTexture;
            this.toggleStateHoveringTexture = toggleStateHoveringTexture;
        }

        public abstract void toggleStateFor(Menu<?> menu);
    }

    public static class ToggleStateButton extends AbstractIconButton {

        public ToggleStateButton(Menu<?> parent) {
            this.setLocation(109, 2);
            this.setDimensions(9, 9);
            attach(parent);
        }

        @Override
        public Texture getTextureNormal() {
            return getParent().state.toggleStateNormalTexture;
        }

        @Override
        public Texture getTextureHovered() {
            return getParent().state.toggleStateHoveringTexture;
        }

        @Override
        public boolean onMouseClicked(double mouseX, double mouseY, int button) {
            getParent().toggleState();
            return true;
        }

        @Nonnull
        @Override
        public Menu<?> getParent() {
            return Objects.requireNonNull((Menu<?>) super.getParent());
        }

        @Override
        public BoxSizing getBoxSizing() {
            return BoxSizing.PHANTOM;
        }

        @Override
        public void render(int mouseX, int mouseY, float partialTicks) {
            RenderSystem.color3f(1F, 1F, 1F);
            super.render(mouseX, mouseY, partialTicks);
        }
    }

    private static final List<Supplier<IEntry>> EMPTY_LIST = ImmutableList.of();

    public static final Texture HEADING_BOX = Render2D.ofFlowComponent(0, 0, 120, 13);
    public static final int DEFAULT_CONTENT_HEIGHT = 65;

    private FlowComponent<P> flowComponent;
    private State state = State.COLLAPSED;

    private ToggleStateButton toggleStateButton;
    private final List<IWidget> children = new ArrayList<>();

    private List<Supplier<IEntry>> actionMenuEntries = EMPTY_LIST;

    public Menu() {
        // Start at a collapsed state
        this.setDimensions(HEADING_BOX.getPortionWidth(), HEADING_BOX.getPortionHeight());
    }

    @Override
    public void onInitialAttach() {
        toggleStateButton = new ToggleStateButton(this);
        addChildren(toggleStateButton);
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
        widget.attach(this);
        return this;
    }

    @Override
    public Menu<P> addChildren(Collection<IWidget> widgets) {
        for (IWidget widget : widgets) {
            addChildren(widget);
        }
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
        for (IWidget child : children) {
            if (BoxSizing.shouldIncludeWidget(child)) {
                child.setEnabled(state);
            }
        }
    }

    public void growHeight(int growth) {
        setHeight(getHeight() + growth);
    }

    public void shrinkHeight(int shrinkage) {
        growHeight(-shrinkage);
    }

    public int getContentHeight() {
        return DEFAULT_CONTENT_HEIGHT;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        if (!isEnabled()) {
            return;
        }

        RenderEventDispatcher.onPreRender(this, mouseX, mouseY);
        RenderSystem.color3f(1F, 1F, 1F);
        HEADING_BOX.render(getAbsoluteX(), getAbsoluteY());
        renderHeadingText();

        if (state == State.EXPANDED) {
            renderContents(mouseX, mouseY, partialTicks);
        } else {
            toggleStateButton.render(mouseX, mouseY, partialTicks);
        }
        RenderEventDispatcher.onPostRender(this, mouseX, mouseY);
    }

    public void renderHeadingText() {
        int y1 = getAbsoluteY();
        int y2 = y1 + HEADING_BOX.getPortionHeight();
        Render2D.renderVerticallyCenteredText(getHeadingText(), getHeadingLeftX(), y1, y2 + 1, getZLevel(), getHeadingColor());
    }

    public int getHeadingLeftX() {
        return getAbsoluteX() + 5;
    }

    public int getHeadingColor() {
        return 0xff404040;
    }

    public abstract String getHeadingText();

    public void renderContents(int mouseX, int mouseY, float partialTicks) {
        for (IWidget child : children) {
            child.render(mouseX, mouseY, partialTicks);
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
            return true;
        }
        return false;
    }

    @Override
    protected void buildContextMenu(ContextMenuBuilder builder) {
        Section section = builder.obtainSection("FlowComponent.Menu");
        section.addChildren(new CallbackEntry(null, "gui.sfm.FactoryManager.Tool.Inspector.Props.CollapseAll", b -> flowComponent.collapseAllMenus()));
        section.addChildren(new CallbackEntry(null, "gui.sfm.FactoryManager.Tool.Inspector.Props.ExpandAll", b -> flowComponent.expandAllMenus()));
        for (Supplier<IEntry> entry : actionMenuEntries) {
            section.addChildren(entry.get());
        }
        super.buildContextMenu(builder);
    }

    protected void saveData() {
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    @Override
    public VerticalList<Menu<P>> getParent() {
        return Objects.requireNonNull((VerticalList<Menu<P>>) super.getParent());
    }

    public void onLinkFlowComponent(FlowComponent<P> flowComponent) {
        Preconditions.checkState(this.flowComponent == null);
        this.flowComponent = flowComponent;
        this.attach(flowComponent.getMenusBox());
    }

    public FlowComponent<P> getFlowComponent() {
        return flowComponent;
    }

    public P getLinkedProcedure() {
        return flowComponent.getProcedure();
    }

    void useActionList(List<Supplier<IEntry>> actions) {
        actionMenuEntries = actions;
    }

    public void injectAction(Supplier<IEntry> action) {
        if (actionMenuEntries == EMPTY_LIST) {
            actionMenuEntries = new ArrayList<>();
        }
        actionMenuEntries.add(action);
    }
}
