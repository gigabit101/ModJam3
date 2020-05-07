package vswe.stevesfactory.ui.manager.toolbox;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import vswe.stevesfactory.library.gui.Render2D;
import vswe.stevesfactory.library.gui.TextRenderer;
import vswe.stevesfactory.library.gui.Texture;
import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.widget.AbstractWidget;
import vswe.stevesfactory.library.gui.widget.IWidget;
import vswe.stevesfactory.library.gui.widget.mixin.LeafWidgetMixin;
import vswe.stevesfactory.library.gui.widget.mixin.ResizableWidgetMixin;
import vswe.stevesfactory.ui.manager.FactoryManagerGUI;

import java.util.function.Supplier;

import static org.lwjgl.opengl.GL11.GL_QUADS;
import static vswe.stevesfactory.library.gui.Render2D.coloredRect;

public class ToolboxEntry<T extends IWidget & ResizableWidgetMixin> extends AbstractWidget implements IToolType, LeafWidgetMixin {

    public static final int NORMAL_BORDER_COLOR = 0xff8c8c8c;
    public static final int HOVERED_BORDER_COLOR = 0xff8c8c8c;
    public static final int NORMAL_FILLER_COLOR = 0xff737373;
    public static final int HOVERED_FILLER_COLOR = 0xffc9c9c9;

    public static final int FONT_HEIGHT = 5;
    public static final int LABEL_VERTICAL_GAP = 3;

    private Texture tex;
    private String name = "";

    private Supplier<T> toolWindowConstructor;
    private T cachedToolWindow;

    public ToolboxEntry(Texture tex, Supplier<T> toolWindowConstructor) {
        this.setDimensions(Math.max(tex.getPortionWidth() / 2, 8), tex.getPortionHeight() / 2);
        this.tex = tex;
        this.toolWindowConstructor = toolWindowConstructor;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        RenderEventDispatcher.onPreRender(this, mouseX, mouseY);

        RenderSystem.disableAlphaTest();
        RenderSystem.disableTexture();
        Tessellator.getInstance().getBuffer().begin(GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        int x1 = getAbsoluteX();
        int y1 = getAbsoluteY();
        int x2 = getAbsoluteXRight();
        int y2 = getAbsoluteYBottom();
        boolean hovered = isInside(mouseX, mouseY);
        coloredRect(x1, y1, x2, y2, hovered ? HOVERED_BORDER_COLOR : NORMAL_BORDER_COLOR);
        coloredRect(x1 + 1, y1 + 1, x2 - 1, y2 - 1, hovered ? HOVERED_FILLER_COLOR : NORMAL_FILLER_COLOR);
        Tessellator.getInstance().draw();
        RenderSystem.enableTexture();
        RenderSystem.enableAlphaTest();

        int textureSize = getWidth();
        tex.render(x1, y1, x1 + textureSize, y1 + textureSize);
        Render2D.renderVerticalText(name, x1 + 1, y1 + textureSize + LABEL_VERTICAL_GAP, getZLevel(), FONT_HEIGHT, 0xffffffff);

        if (hovered && !name.isEmpty()) {
            FactoryManagerGUI.get().scheduleTooltip(name, mouseX, mouseY);
        }

        RenderEventDispatcher.onPreRender(this, mouseX, mouseY);
    }

    @Override
    public boolean onMouseClicked(double mouseX, double mouseY, int button) {
        FactoryManagerGUI.get().getPrimaryWindow().toolHolderPanel.setActivePanel(getToolWindow());
        return true;
    }

    @Override
    public T getToolWindow() {
        if (cachedToolWindow == null) {
            cachedToolWindow = toolWindowConstructor.get();
        }
        return cachedToolWindow;
    }

    public String getName() {
        return name;
    }

    public ToolboxEntry<T> setName(String name) {
        this.name = name;
        TextRenderer tr = TextRenderer.vanilla();
        tr.setFontHeight(FONT_HEIGHT);
        this.setHeight(getHeight() + LABEL_VERTICAL_GAP + tr.calculateWidth(name) + LABEL_VERTICAL_GAP);
        return this;
    }
}
