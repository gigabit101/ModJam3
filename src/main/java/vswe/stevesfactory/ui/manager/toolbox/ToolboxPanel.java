package vswe.stevesfactory.ui.manager.toolbox;

import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import vswe.stevesfactory.api.StevesFactoryManagerAPI;
import vswe.stevesfactory.library.gui.Render2D;
import vswe.stevesfactory.library.gui.Texture;
import vswe.stevesfactory.library.gui.contextmenu.CallbackEntry;
import vswe.stevesfactory.library.gui.contextmenu.ContextMenuBuilder;
import vswe.stevesfactory.library.gui.contextmenu.Section;
import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.layout.FlowLayout;
import vswe.stevesfactory.library.gui.layout.properties.BoxSizing;
import vswe.stevesfactory.library.gui.widget.IWidget;
import vswe.stevesfactory.library.gui.widget.button.AbstractIconButton;
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

    public static final Texture GROUP_LIST_ICON = Texture.complete(new ResourceLocation(StevesFactoryManagerAPI.MODID, "textures/gui/tool_icon/group.png"), 16, 16);
    public static final Texture INSPECTOR_ICON = Texture.complete(new ResourceLocation(StevesFactoryManagerAPI.MODID, "textures/gui/tool_icon/inspector.png"), 16, 16);

    private final ToolboxEntry<Grouplist> groupList;
    private final ToolboxEntry<Inspector> inspector;
    private final AbstractIconButton close;
    private final List<IWidget> children = new ArrayList<>();

    public ToolboxPanel() {
        super(WidthOccupierType.MIN_WIDTH);
        this.setWidth(8 + Render2D.LEFT_BORDER);

        addChildOnly(groupList = new ToolboxEntry<>(GROUP_LIST_ICON, Grouplist::new).setName(I18n.format("gui.sfm.FactoryManager.Tool.Group.Name")));
        addChildOnly(inspector = new ToolboxEntry<>(INSPECTOR_ICON, Inspector::new).setName(I18n.format("gui.sfm.FactoryManager.Tool.Inspector.Name")));
        addChildOnly(close = new AbstractIconButton() {
            {
                this.setDimensions(8, 8);
            }

            @Override
            public void render(int mouseX, int mouseY, float partialTicks) {
                super.render(mouseX, mouseY, partialTicks);
                if (isInside(mouseX, mouseY)) {
                    FactoryManagerGUI.get().scheduleTooltip(I18n.format("gui.sfm.FactoryManager.Toolbox.CloseToolPanel"), mouseX, mouseY);
                }
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                ToolHolderPanel panel = FactoryManagerGUI.get().getPrimaryWindow().topLevel.toolHolderPanel;
                panel.setActivePanel(null);
                return true;
            }

            @Override
            public Texture getTextureNormal() {
                return Render2D.CLOSE_ICON;
            }

            @Override
            public Texture getTextureHovered() {
                return Render2D.CLOSE_ICON_HOVERED;
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
        widget.attach(this);
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
            widget.attach(this);
        }
        reflow();
        return this;
    }

    @Override
    public void reflow() {
        FlowLayout.vertical(children, Render2D.LEFT_BORDER, 0, 0);
        close.setX(Render2D.computeCenterX(Render2D.LEFT_BORDER, getWidth(), close.getWidth()));
        close.alignBottom(getHeight());
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
            if (button == GLFW_MOUSE_BUTTON_LEFT) {
                getWindow().setFocusedWidget(this);
            } else if (button == GLFW_MOUSE_BUTTON_RIGHT) {
                createContextMenu(mouseX, mouseY);
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

    public Grouplist getGroupList() {
        return groupList.getToolWindow();
    }

    public Inspector getInspector() {
        return inspector.getToolWindow();
    }
}
