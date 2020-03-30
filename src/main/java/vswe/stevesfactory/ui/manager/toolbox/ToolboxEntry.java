package vswe.stevesfactory.ui.manager.toolbox;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import vswe.stevesfactory.library.gui.RenderingHelper;
import vswe.stevesfactory.library.gui.TextureWrapper;
import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.screen.WidgetScreen;
import vswe.stevesfactory.library.gui.widget.AbstractWidget;
import vswe.stevesfactory.library.gui.widget.IWidget;
import vswe.stevesfactory.library.gui.widget.mixin.LeafWidgetMixin;
import vswe.stevesfactory.ui.manager.FactoryManagerGUI;

import java.util.function.Supplier;

import static org.lwjgl.opengl.GL11.GL_QUADS;
import static vswe.stevesfactory.library.gui.RenderingHelper.rectVertices;
import static vswe.stevesfactory.library.gui.RenderingHelper.textWidth;

public class ToolboxEntry<T extends IWidget> extends AbstractWidget implements IToolType, LeafWidgetMixin {

    public static final int NORMAL_BORDER_COLOR = 0xff8c8c8c;
    public static final int HOVERED_BORDER_COLOR = 0xff8c8c8c;
    public static final int NORMAL_FILLER_COLOR = 0xff737373;
    public static final int HOVERED_FILLER_COLOR = 0xffc9c9c9;

    public static final int FONT_HEIGHT = 5;
    public static final int LABEL_VERTICAL_GAP = 3;

    private TextureWrapper tex;
    private String name = "";

    private Supplier<T> toolWindowConstructor;
    private T cachedToolWindow;

    public ToolboxEntry(TextureWrapper tex, Supplier<T> toolWindowConstructor) {
        super(0, 0, Math.max(tex.getPortionWidth() / 2, 8), tex.getPortionHeight() / 2);
        this.tex = tex;
        this.toolWindowConstructor = toolWindowConstructor;
    }

    @Override
    public void render(int mouseX, int mouseY, float particleTicks) {
        RenderEventDispatcher.onPreRender(this, mouseX, mouseY);

        RenderSystem.disableAlphaTest();
        RenderSystem.disableTexture();
        Tessellator.getInstance().getBuffer().begin(GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        int x1 = getAbsoluteX();
        int y1 = getAbsoluteY();
        int x2 = getAbsoluteXRight();
        int y2 = getAbsoluteYBottom();
        boolean hovered = isInside(mouseX, mouseY);
        rectVertices(x1, y1, x2, y2, hovered ? HOVERED_BORDER_COLOR : NORMAL_BORDER_COLOR);
        rectVertices(x1 + 1, y1 + 1, x2 - 1, y2 - 1, hovered ? HOVERED_FILLER_COLOR : NORMAL_FILLER_COLOR);
        Tessellator.getInstance().draw();
        RenderSystem.enableTexture();
        RenderSystem.enableAlphaTest();

        int textureSize = getWidth();
        tex.draw(x1, y1, textureSize, textureSize);
        RenderingHelper.drawVerticalText(name, x1 + 1, y1 + textureSize + LABEL_VERTICAL_GAP, FONT_HEIGHT, 0xffffffff);

        if (hovered && !name.isEmpty()) {
            WidgetScreen.getCurrent().setHoveringText(name, mouseX, mouseY);
        }

        RenderEventDispatcher.onPreRender(this, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        FactoryManagerGUI.get().getTopLevel().toolHolderPanel.setActivePanel(getToolWindow());
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
        this.setHeight(getHeight() + LABEL_VERTICAL_GAP + textWidth(name, FONT_HEIGHT) + LABEL_VERTICAL_GAP);
        return this;
    }
}
