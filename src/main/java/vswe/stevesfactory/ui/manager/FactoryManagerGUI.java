package vswe.stevesfactory.ui.manager;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import vswe.stevesfactory.Config;
import vswe.stevesfactory.api.network.INetworkController;
import vswe.stevesfactory.library.gui.RenderingHelper;
import vswe.stevesfactory.library.gui.TextureWrapper;
import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.layout.StrictTableLayout;
import vswe.stevesfactory.library.gui.layout.StrictTableLayout.GrowDirection;
import vswe.stevesfactory.library.gui.screen.DisplayListCaches;
import vswe.stevesfactory.library.gui.screen.WidgetScreen;
import vswe.stevesfactory.library.gui.widget.AbstractContainer;
import vswe.stevesfactory.library.gui.widget.IWidget;
import vswe.stevesfactory.library.gui.window.AbstractWindow;
import vswe.stevesfactory.ui.manager.editor.ConnectionsPanel;
import vswe.stevesfactory.ui.manager.editor.EditorPanel;
import vswe.stevesfactory.ui.manager.selection.SelectionPanel;
import vswe.stevesfactory.ui.manager.tool.ToolHolderPanel;
import vswe.stevesfactory.ui.manager.tool.group.GroupDataModel;
import vswe.stevesfactory.ui.manager.toolbox.ToolboxPanel;

import java.awt.*;
import java.util.List;

import static org.lwjgl.opengl.GL11.glCallList;

public class FactoryManagerGUI extends WidgetScreen<FactoryManagerContainer> {

    public static FactoryManagerGUI get() {
        return (FactoryManagerGUI) Minecraft.getInstance().currentScreen;
    }

    public static final StrictTableLayout DOWN_RIGHT_4_STRICT_TABLE = new StrictTableLayout(GrowDirection.DOWN, GrowDirection.RIGHT, 4);

    public static final ResourceLocation DELETE_ICON = RenderingHelper.linkTexture("gui/actions/delete.png");
    public static final ResourceLocation COPY_ICON = RenderingHelper.linkTexture("gui/actions/copy.png");
    public static final ResourceLocation CUT_ICON = RenderingHelper.linkTexture("gui/actions/cut.png");
    public static final ResourceLocation PASTE_ICON = RenderingHelper.linkTexture("gui/actions/paste.png");

    public static final TextureWrapper CLOSE_ICON = TextureWrapper.ofFlowComponent(18, 36, 9, 9);
    public static final TextureWrapper CLOSE_ICON_HOVERED = CLOSE_ICON.toRight(1);
    public static final TextureWrapper SETTINGS_ICON = TextureWrapper.ofFlowComponent(18, 106, 18, 18);
    public static final TextureWrapper SETTINGS_ICON_HOVERED = SETTINGS_ICON.toRight(1);
    public static final TextureWrapper ADD_ENTRY_ICON = TextureWrapper.ofFlowComponent(18, 125, 8, 8);
    public static final TextureWrapper ADD_ENTRY_HOVERED_ICON = ADD_ENTRY_ICON.toRight(1);
    public static final TextureWrapper REMOVE_ENTRY_ICON = TextureWrapper.ofFlowComponent(34, 125, 8, 8);
    public static final TextureWrapper REMOVE_ENTRY_HOVERED_ICON = REMOVE_ENTRY_ICON.toRight(1);

    ///////////////////////////////////////////////////////////////////////////
    // GUI code
    ///////////////////////////////////////////////////////////////////////////

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
        PrimaryWindow w = new PrimaryWindow();
        initializePrimaryWindow(w);
        w.init();
    }

    @Override
    public void removed() {
        getPrimaryWindow().topLevel.editorPanel.saveAll();
        super.removed();
    }

    public INetworkController getController() {
        return container.controller;
    }

    @Override
    public PrimaryWindow getPrimaryWindow() {
        return (PrimaryWindow) super.getPrimaryWindow();
    }

    public TopLevelWidget getTopLevel() {
        return getPrimaryWindow().topLevel;
    }

    public static class PrimaryWindow extends AbstractWindow {

        public final TopLevelWidget topLevel;

        // The location and dimensions will be set in the constructor
        private Rectangle screenBounds = new Rectangle();
        private boolean fullscreen = false;

        private int backgroundDL;

        private PrimaryWindow() {
            this.topLevel = new TopLevelWidget(this);
        }

        public void init() {
            topLevel.init();
            setFocusedWidget(topLevel);
            asProportional();
        }

        @Override
        public int getBorderSize() {
            return 4;
        }

        @Override
        public List<? extends IWidget> getChildren() {
            return ImmutableList.of(topLevel);
        }

        @Override
        public void render(int mouseX, int mouseY, float particleTicks) {
            RenderEventDispatcher.onPreRender(this, mouseX, mouseY);
            if (fullscreen && !Config.CLIENT.useBackgroundOnFullscreen.get()) {
                RenderingHelper.drawRect(getPosition(), getBorder(), 0xffc6c6c6);
            } else {
                glCallList(backgroundDL);
            }
            topLevel.render(mouseX, mouseY, particleTicks);
            RenderEventDispatcher.onPostRender(this, mouseX, mouseY);
        }

        public Rectangle getScreenBounds() {
            return screenBounds;
        }

        public void setScreenBounds(int width, int height) {
            // Borders
            this.screenBounds.width = width;
            this.screenBounds.height = height;
            // Centering
            this.screenBounds.x = screenWidth() / 2 - width / 2;
            this.screenBounds.y = screenHeight() / 2 - height / 2;

            setPosition(screenBounds.x, screenBounds.y);
            setBorder(width, height);
            updateBackgroundDL();
        }

        private void updateBackgroundDL() {
            backgroundDL = DisplayListCaches.createVanillaStyleBackground(new Rectangle(screenBounds), 0F);
        }

        private void asProportional() {
            int width, height;
            if (Config.CLIENT.useFixedSizeScreen.get()) {
                width = FIXED_WIDTH;
                height = FIXED_HEIGHT;
            } else {
                width = (int) (screenWidth() * WIDTH_PROPORTION);
                height = (int) (screenHeight() * HEIGHT_PROPORTION);
            }
            setScreenBounds(width, height);
            topLevel.reflow();

            get().xSize = width;
            get().ySize = height;
        }

        private void asFullscreen() {
            setScreenBounds(screenWidth(), screenHeight());
            topLevel.reflow();
        }

        public void toggleFullscreen() {
            fullscreen = !fullscreen;
            if (fullscreen) {
                asFullscreen();
            } else {
                asProportional();
            }
        }
    }

    public static class TopLevelWidget extends AbstractContainer<DynamicWidthWidget<?>> {

        public final SelectionPanel selectionPanel;
        public final EditorPanel editorPanel;
        public final ConnectionsPanel connectionsPanel;
        public final ToolHolderPanel toolHolderPanel;
        public final ToolboxPanel toolboxPanel;
        private final ImmutableList<DynamicWidthWidget<?>> children;

        private TopLevelWidget(PrimaryWindow window) {
            super(window);
            this.selectionPanel = new SelectionPanel();
            this.editorPanel = new EditorPanel();
            this.connectionsPanel = new ConnectionsPanel();
            this.toolHolderPanel = new ToolHolderPanel();
            this.toolboxPanel = new ToolboxPanel();
            // Let connections panel receive events first
            this.children = ImmutableList.of(selectionPanel, connectionsPanel, editorPanel, toolHolderPanel, toolboxPanel);
        }

        public void init() {
            attachChildren();
            editorPanel.readProcedures();
        }

        @Override
        public IWidget getParentWidget() {
            return null;
        }

        @Override
        public void setParentWidget(IWidget newParent) {
            Preconditions.checkArgument(newParent == null);
        }

        @Override
        public void onParentPositionChanged() {
            // Do nothing because we don't really have a parent widget
        }

        @Override
        public List<DynamicWidthWidget<?>> getChildren() {
            return children;
        }

        @Override
        public void render(int mouseX, int mouseY, float particleTicks) {
            // No render events for this object because it is technically internal for the window, and it has the exact size as the window
            for (DynamicWidthWidget<?> child : children) {
                child.render(mouseX, mouseY, particleTicks);
            }
        }

        private int prevWidth, prevHeight;

        @Override
        public void reflow() {
            fillWindow();
            if (getWidth() != prevWidth || getHeight() != prevHeight) {
                prevWidth = getWidth();
                prevHeight = getHeight();
                selectionPanel.setHeight(prevHeight);
                editorPanel.setHeight(prevHeight);
                connectionsPanel.setHeight(prevHeight);
                toolHolderPanel.setHeight(prevHeight);
                toolboxPanel.setHeight(prevHeight);
            }

            selectionPanel.reflow();
            editorPanel.reflow();
            connectionsPanel.reflow();
            toolHolderPanel.reflow();
            toolboxPanel.reflow();
            DynamicWidthWidget.reflowDynamicWidth(getDimensions(), children);
        }
    }

    public final GroupDataModel groupModel = new GroupDataModel(this);
}
