package vswe.stevesfactory.ui.manager.editor;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Getter;
import lombok.val;
import vswe.stevesfactory.api.logic.IClientDataStorage;
import vswe.stevesfactory.api.logic.IErrorPopulator;
import vswe.stevesfactory.api.logic.IProcedure;
import vswe.stevesfactory.library.gui.Render2D;
import vswe.stevesfactory.library.gui.Texture;
import vswe.stevesfactory.library.gui.contextmenu.CallbackEntry;
import vswe.stevesfactory.library.gui.contextmenu.ContextMenuBuilder;
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
import java.util.function.Consumer;

/**
 * A collapsible menu that's shown inside {@link vswe.stevesfactory.ui.manager.tool.inspector.Inspector inspector}'s
 * {@link vswe.stevesfactory.ui.manager.tool.inspector.PropertiesPanel properties panel}.
 * <p>
 * See {@link #onLinkFlowComponent(FlowComponent)} for more docs about additional widget lifecycle for menu widgets.
 */
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

    private static final List<Consumer<ContextMenuBuilder>> EMPTY_LIST = ImmutableList.of();

    public static final Texture HEADING_BOX = Render2D.ofFlowComponent(0, 0, 120, 13);
    public static final int DEFAULT_CONTENT_HEIGHT = 57;
    public static final int SIDE_MARGINS = 4;

    @Getter
    private FlowComponent<P> flowComponent;
    private State state = State.EXPANDED;
    private int contentHeight = -1;

    private final ToggleStateButton toggleStateButton;
    private final List<IWidget> children = new ArrayList<>();

    private List<Consumer<ContextMenuBuilder>> actionMenuEntries = EMPTY_LIST;

    public Menu() {
        this.setBorders(SIDE_MARGINS);
        this.setBorderTop(HEADING_BOX.getPortionHeight() + SIDE_MARGINS);
        this.setDimensions(HEADING_BOX.getPortionWidth() - SIDE_MARGINS * 2, DEFAULT_CONTENT_HEIGHT);
        this.toggleStateButton = new ToggleStateButton(this);
        this.toggleStateButton.setLocation(109 - 4, 2 - 4 - HEADING_BOX.getPortionHeight());
        this.toggleStateButton.setDimensions(9, 9);
    }

    @Override
    public void onInitialAttach() {
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
        for (val widget : widgets) {
            addChildren(widget);
        }
        return this;
    }

    public void toggleState() {
        state.toggleStateFor(this);
    }

    // TODO get rid of this border resizing hack and use a proper child widget to contain the custom contents
    public void expand() {
        if (state == State.COLLAPSED) {
            state = State.EXPANDED;
            growHeight(contentHeight);

            setBorderTop(HEADING_BOX.getPortionHeight() + SIDE_MARGINS);
            setBorderBottom(SIDE_MARGINS);
            toggleStateButton.moveY(-SIDE_MARGINS);
            updateChildrenEnableState(true);
        }
    }

    public void collapse() {
        if (state == State.EXPANDED) {
            state = State.COLLAPSED;
            contentHeight = this.getHeight();
            shrinkHeight(contentHeight);

            setBorderTop(HEADING_BOX.getPortionHeight());
            setBorderBottom(0);
            toggleStateButton.moveY(SIDE_MARGINS);
            updateChildrenEnableState(false);
        }
    }

    private void updateChildrenEnableState(boolean state) {
        for (val child : children) {
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

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        if (!isEnabled()) {
            return;
        }

        RenderEventDispatcher.onPreRender(this, mouseX, mouseY);
        RenderSystem.color3f(1F, 1F, 1F);
        HEADING_BOX.render(getOuterAbsoluteX(), getOuterAbsoluteY());
        renderHeadingText();

        if (state == State.EXPANDED) {
            renderContents(mouseX, mouseY, partialTicks);
        } else {
            toggleStateButton.render(mouseX, mouseY, partialTicks);
        }
        RenderEventDispatcher.onPostRender(this, mouseX, mouseY);
    }

    public void renderHeadingText() {
        int x = getOuterAbsoluteX() + 5;
        int y1 = getOuterAbsoluteY();
        int y2 = y1 + HEADING_BOX.getPortionHeight();
        Render2D.renderVerticallyCenteredText(getHeadingText(), x, y1, y2, getZLevel(), getHeadingColor());
    }

    public int getHeadingLeftX() {
        return getOuterAbsoluteX() + 5;
    }

    public int getHeadingColor() {
        return 0xff404040;
    }

    public abstract String getHeadingText();

    public void renderContents(int mouseX, int mouseY, float partialTicks) {
        for (val child : children) {
            child.render(mouseX, mouseY, partialTicks);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        switch (state) {
            case COLLAPSED:
                if (toggleStateButton.mouseClicked(mouseX, mouseY, button)) {
                    return true;
                }
                if (this.isInside(mouseX, mouseY)) {
                    getWindow().setFocusedWidget(this);
                    return true;
                }
                return false;
            case EXPANDED:
                return super.mouseClicked(mouseX, mouseY, button);
            default:
                throw new IllegalStateException();
        }
    }

    @Override
    protected void buildContextMenu(ContextMenuBuilder builder) {
        val section = builder.obtainSection("FlowComponent.Menu");
        section.addChildren(new CallbackEntry(null, "gui.sfm.FactoryManager.Tool.Inspector.Props.CollapseAll", b -> flowComponent.collapseAllMenus()));
        section.addChildren(new CallbackEntry(null, "gui.sfm.FactoryManager.Tool.Inspector.Props.ExpandAll", b -> flowComponent.expandAllMenus()));

        for (val entry : actionMenuEntries) {
            entry.accept(builder);
        }
        super.buildContextMenu(builder);
    }

    protected void saveData() {
    }

    @Override
    public void onDimensionChanged() {
        val parent = getParent();
        if (parent != null) {
            @SuppressWarnings("unchecked") val list = (VerticalList<Menu<P>>) parent;
            list.reflow();
        }
    }

    /**
     * An additional lifecycle event that is gardened to happen after {@link #onInitialAttach()}. Any operations related
     * to flow component or the procedure should be done here or after here. Generally, child widgets are created in the
     * constructor, attached in {@link #onInitialAttach()}, and data bindings are created here.
     */
    public void onLinkFlowComponent(FlowComponent<P> flowComponent) {
        Preconditions.checkState(this.flowComponent == null);
        this.flowComponent = flowComponent;
        this.attach(flowComponent.getMenusBox());
    }

    public P getLinkedProcedure() {
        return flowComponent.getProcedure();
    }

    public void injectAction(Consumer<ContextMenuBuilder> action) {
        if (actionMenuEntries == EMPTY_LIST) {
            actionMenuEntries = new ArrayList<>();
        }
        actionMenuEntries.add(action);
    }
}
