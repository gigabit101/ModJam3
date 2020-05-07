package vswe.stevesfactory.ui.manager;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;
import vswe.stevesfactory.Config;
import vswe.stevesfactory.api.network.INetworkController;
import vswe.stevesfactory.library.gui.Render2D;
import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.screen.WidgetScreen;
import vswe.stevesfactory.library.gui.widget.IWidget;
import vswe.stevesfactory.library.gui.window.AbstractWindow;
import vswe.stevesfactory.ui.manager.editor.ConnectionsPanel;
import vswe.stevesfactory.ui.manager.editor.EditorPanel;
import vswe.stevesfactory.ui.manager.selection.SelectionPanel;
import vswe.stevesfactory.ui.manager.tool.ToolHolderPanel;
import vswe.stevesfactory.ui.manager.tool.group.GroupDataModel;
import vswe.stevesfactory.ui.manager.toolbox.ToolboxPanel;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.glCallList;

public class FactoryManagerGUI extends WidgetScreen<FactoryManagerContainer> {

    public static FactoryManagerGUI get() {
        return (FactoryManagerGUI) Minecraft.getInstance().currentScreen;
    }

    public static final int FIXED_WIDTH = 256;
    public static final int FIXED_HEIGHT = 180;
    public static final float WIDTH_PROPORTION = 2F / 3F;
    public static final float HEIGHT_PROPORTION = 3F / 4F;

    public FactoryManagerGUI(FactoryManagerContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    protected void init() {
        super.init();
        setPrimaryWindow(new PrimaryWindow());
    }

    @Override
    public void removed() {
        getPrimaryWindow().editorPanel.saveAll();
        super.removed();
    }

    public INetworkController getController() {
        return container.controller;
    }

    @Override
    public PrimaryWindow getPrimaryWindow() {
        return (PrimaryWindow) super.getPrimaryWindow();
    }

    public static class PrimaryWindow extends AbstractWindow {

        public final SelectionPanel selectionPanel;
        public final EditorPanel editorPanel;
        public final ConnectionsPanel connectionsPanel;
        public final ToolHolderPanel toolHolderPanel;
        public final ToolboxPanel toolboxPanel;
        private final List<DynamicWidthWidget<?>> children = new ArrayList<>();

        private boolean fullscreen = false;
        private int backgroundDL;

        private PrimaryWindow() {
            asWindow();

            this.selectionPanel = new SelectionPanel();
            this.editorPanel = new EditorPanel();
            this.connectionsPanel = new ConnectionsPanel();
            this.toolHolderPanel = new ToolHolderPanel();
            this.toolboxPanel = new ToolboxPanel();
            addChildren(selectionPanel);
            // Let connections panel receive events first
            addChildren(connectionsPanel);
            addChildren(editorPanel);
            addChildren(toolHolderPanel);
            addChildren(toolboxPanel);

            reflow();
        }

        public void reflow() {
            int h = getContentHeight();
            for (DynamicWidthWidget<?> child : children) {
                child.setHeight(h);
                child.reflow();
            }
//            selectionPanel.setHeight(h);
//            editorPanel.setHeight(h);
//            connectionsPanel.setHeight(h);
//            toolHolderPanel.setHeight(h);
//            toolboxPanel.setHeight(h);
//
//            selectionPanel.reflow();
//            editorPanel.reflow();
//            connectionsPanel.reflow();
//            toolHolderPanel.reflow();
//            toolboxPanel.reflow();
            DynamicWidthWidget.resizeAll(this.getContentWidth(), children);
        }

        @Override
        public void render(int mouseX, int mouseY, float partialTicks) {
            RenderEventDispatcher.onPreRender(this, mouseX, mouseY);
            if (fullscreen && !Config.CLIENT.useBackgroundOnFullscreen.get()) {
                Point pos = getPosition();
                Dimension border = getBorder();
                Render2D.beginColoredQuad();
                Render2D.coloredRect(pos.x, pos.y, pos.x + border.width, pos.y + border.height, 0xffc6c6c6);
                Render2D.draw();
            } else {
                glCallList(backgroundDL);
            }

            for (DynamicWidthWidget<?> child : children) {
                child.render(mouseX, mouseY, partialTicks);
            }
            RenderEventDispatcher.onPostRender(this, mouseX, mouseY);
        }

        private void setScreenBounds(int width, int height) {
            setBorder(width, height);
            centralize();
            backgroundDL = createVanillaStyleDL();
            FactoryManagerGUI.get().xSize = width;
            FactoryManagerGUI.get().ySize = height;
        }

        private void asWindow() {
            int width, height;
            if (Config.CLIENT.useFixedSizeScreen.get()) {
                width = FIXED_WIDTH;
                height = FIXED_HEIGHT;
            } else {
                width = (int) (screenWidth() * WIDTH_PROPORTION);
                height = (int) (screenHeight() * HEIGHT_PROPORTION);
            }
            setScreenBounds(width, height);
        }

        private void asFullscreen() {
            setScreenBounds(screenWidth(), screenHeight());
        }

        public void toggleFullscreen() {
            fullscreen = !fullscreen;
            if (fullscreen) {
                asFullscreen();
            } else {
                asWindow();
            }
        }

        private void addChildren(DynamicWidthWidget<?> child) {
            children.add(child);
            child.attachWindow(this);
        }

        @Override
        public int getBorderSize() {
            return 4;
        }

        @Override
        public List<? extends IWidget> getChildren() {
            return children;
        }
    }

    public final GroupDataModel groupModel = new GroupDataModel();
}
