package vswe.stevesfactory.ui.manager.toolbox;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.resources.I18n;
import vswe.stevesfactory.library.gui.RenderingHelper;
import vswe.stevesfactory.library.gui.TextureWrapper;
import vswe.stevesfactory.library.gui.contextmenu.CallbackEntry;
import vswe.stevesfactory.library.gui.contextmenu.ContextMenu;
import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.layout.FlowLayout;
import vswe.stevesfactory.library.gui.layout.properties.BoxSizing;
import vswe.stevesfactory.library.gui.screen.WidgetScreen;
import vswe.stevesfactory.library.gui.widget.AbstractIconButton;
import vswe.stevesfactory.library.gui.widget.IWidget;
import vswe.stevesfactory.ui.manager.DynamicWidthWidget;
import vswe.stevesfactory.ui.manager.FactoryManagerGUI;
import vswe.stevesfactory.ui.manager.tool.ToolHolderPanel;
import vswe.stevesfactory.ui.manager.tool.group.Grouplist;
import vswe.stevesfactory.ui.manager.tool.inspector.Inspector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT;

public final class ToolboxPanel extends DynamicWidthWidget<IWidget> {

    public static final TextureWrapper GROUP_LIST_ICON = TextureWrapper.ofGUITexture("tool_icon/group.png", 16, 16, 0, 0, 16, 16);
    public static final TextureWrapper INSPECTOR_ICON = TextureWrapper.ofGUITexture("tool_icon/inspector.png", 16, 16, 0, 0, 16, 16);

    private final ToolboxEntry<Grouplist> groupList;
    private final ToolboxEntry<Inspector> inspector;
    private final AbstractIconButton close;
    private final List<IWidget> children = new ArrayList<>();

    public ToolboxPanel() {
        super(WidthOccupierType.MIN_WIDTH);
        this.setWidth(8 + RenderingHelper.LEFT_BORDER);

        addChildOnly(groupList = new ToolboxEntry<>(GROUP_LIST_ICON, Grouplist::new).setName(I18n.format("gui.sfm.FactoryManager.Tool.Group.Name")));
        addChildOnly(inspector = new ToolboxEntry<>(INSPECTOR_ICON, Inspector::new).setName(I18n.format("gui.sfm.FactoryManager.Tool.Inspector.Name")));
        addChildOnly(close = new AbstractIconButton(0, 0, 8, 8) {
            @Override
            public void render(int mouseX, int mouseY, float particleTicks) {
                super.render(mouseX, mouseY, particleTicks);
                if (isInside(mouseX, mouseY)) {
                    WidgetScreen.getCurrent().setHoveringText(I18n.format("gui.sfm.FactoryManager.Toolbox.CloseToolPanel"), mouseX, mouseY);
                }
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                ToolHolderPanel panel = FactoryManagerGUI.get().getPrimaryWindow().topLevel.toolHolderPanel;
                panel.setActivePanel(null);
                return true;
            }

            @Override
            public TextureWrapper getTextureNormal() {
                return FactoryManagerGUI.CLOSE_ICON;
            }

            @Override
            public TextureWrapper getTextureHovered() {
                return FactoryManagerGUI.CLOSE_ICON_HOVERED;
            }

            @Override
            public BoxSizing getBoxSizing() {
                return BoxSizing.PHANTOM;
            }
        });
    }

    @Override
    public List<IWidget> getChildren() {
        return children;
    }

    private void addChildOnly(IWidget widget) {
        children.add(widget);
        widget.setParentWidget(this);
    }

    @Override
    public ToolboxPanel addChildren(IWidget widget) {
        addChildOnly(widget);
        reflow();
        return this;
    }

    @Override
    public ToolboxPanel addChildren(Collection<IWidget> widgets) {
        children.addAll(widgets);
        for (IWidget widget : widgets) {
            widget.setParentWidget(this);
        }
        reflow();
        return this;
    }

    @Override
    public void reflow() {
        FlowLayout.vertical(children, RenderingHelper.LEFT_BORDER, 0, 0);
        close.setX(RenderingHelper.getXForAlignedCenter(RenderingHelper.LEFT_BORDER, getWidth(), close.getWidth()));
        close.alignBottom(getHeight());
    }

    @Override
    public void render(int mouseX, int mouseY, float particleTicks) {
        RenderEventDispatcher.onPreRender(this, mouseX, mouseY);
        RenderingHelper.renderSideLine(this);
        super.render(mouseX, mouseY, particleTicks);
        RenderEventDispatcher.onPostRender(this, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (isInside(mouseX, mouseY)) {
            if (button == GLFW_MOUSE_BUTTON_LEFT) {
                getWindow().setFocusedWidget(this);
            } else if (button == GLFW_MOUSE_BUTTON_RIGHT) {
                openActionMenu();
            }
            return true;
        }
        return false;
    }

    private void openActionMenu() {
        ContextMenu contextMenu = ContextMenu.atCursor(ImmutableList.of(
                new CallbackEntry(null, "gui.sfm.FactoryManager.Generic.ToggleFullscreen", b -> FactoryManagerGUI.get().getPrimaryWindow().toggleFullscreen())
        ));
        WidgetScreen.getCurrent().addPopupWindow(contextMenu);
    }

    public Grouplist getGroupList() {
        return groupList.getToolWindow();
    }

    public Inspector getInspector() {
        return inspector.getToolWindow();
    }
}
