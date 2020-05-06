package vswe.stevesfactory.ui.manager.tool;

import com.google.common.collect.ImmutableList;
import vswe.stevesfactory.library.gui.Render2D;
import vswe.stevesfactory.library.gui.contextmenu.CallbackEntry;
import vswe.stevesfactory.library.gui.contextmenu.ContextMenu;
import vswe.stevesfactory.library.gui.contextmenu.ContextMenuBuilder;
import vswe.stevesfactory.library.gui.contextmenu.Section;
import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.screen.WidgetScreen;
import vswe.stevesfactory.library.gui.widget.AbstractContainer;
import vswe.stevesfactory.library.gui.widget.IWidget;
import vswe.stevesfactory.library.gui.widget.mixin.ResizableWidgetMixin;
import vswe.stevesfactory.ui.manager.DynamicWidthWidget;
import vswe.stevesfactory.ui.manager.FactoryManagerGUI;

import javax.annotation.Nullable;
import java.util.List;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT;

public final class ToolHolderPanel extends DynamicWidthWidget<IWidget> {

    private List<IWidget> children = ImmutableList.of();

    public ToolHolderPanel() {
        super(WidthOccupierType.MIN_WIDTH);
    }

    public <T extends IWidget & ResizableWidgetMixin> void setActivePanel(@Nullable T panel) {
        if (panel == null) {
            children = ImmutableList.of();
        } else {
            children = ImmutableList.of(panel);
            panel.attach(this);
            panel.setX(Render2D.LEFT_BORDER + 1);
            panel.setHeight(getHeight());
            getWindow().setFocusedWidget(panel);
            if (panel instanceof AbstractContainer) {
                ((AbstractContainer<?>) panel).reflow();
            }
        }
        FactoryManagerGUI.get().getTopLevel().reflow();
    }

    @Override
    public List<IWidget> getChildren() {
        return children;
    }

    @Override
    public void reflow() {
        IWidget widget = getContainedWidget();
        setWidth(widget == null ? 0 : widget.getWidth() + 2 + Render2D.LEFT_BORDER);
    }

    public IWidget getContainedWidget() {
        return children.isEmpty() ? null : children.get(0);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        RenderEventDispatcher.onPreRender(this, mouseX, mouseY);
        Render2D.renderSideLine(this);
        super.renderChildren(mouseX, mouseY, partialTicks);
        RenderEventDispatcher.onPostRender(this, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (isInside(mouseX, mouseY)) {
            switch (button) {
                case GLFW_MOUSE_BUTTON_LEFT:
                    getWindow().setFocusedWidget(this);
                    break;
                case GLFW_MOUSE_BUTTON_RIGHT:
                    createContextMenu(mouseX, mouseY);
                    break;
            }
            return true;
        }
        return false;
    }

    @Override
    protected void buildContextMenu(ContextMenuBuilder builder) {
        Section section = builder.obtainSection("");
        section.addChildren(new CallbackEntry(null, "gui.sfm.FactoryManager.Generic.ToggleFullscreen", b -> FactoryManagerGUI.get().getPrimaryWindow().toggleFullscreen()));
        super.buildContextMenu(builder);
    }
}
