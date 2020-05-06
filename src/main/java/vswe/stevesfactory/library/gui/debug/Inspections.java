package vswe.stevesfactory.library.gui.debug;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import vswe.stevesfactory.Config;
import vswe.stevesfactory.library.gui.Render2D;
import vswe.stevesfactory.library.gui.widget.IWidget;
import vswe.stevesfactory.library.gui.window.IWindow;

import static org.lwjgl.opengl.GL11.GL_QUADS;
import static vswe.stevesfactory.library.gui.Render2D.*;

public abstract class Inspections implements IRenderEventListener {

    public interface IInfoProvider {

        void provideInformation(ITextReceiver receiver);
    }

    public interface IHighlightRenderer {

        void renderHighlight();
    }

    public static final Inspections INSTANCE = new Inspections() {
        @Override
        public void onPreRender(IWidget widget, int mx, int my) {
        }

        @Override
        public void onPreRender(IWindow window, int mx, int my) {
        }

        @Override
        public void onPostRender(IWidget widget, int mx, int my) {
            tryRender(widget, mx, my);
        }

        @Override
        public void onPostRender(IWindow window, int mx, int my) {
            tryRender(window, mx, my);
        }
    };

    private static final ITextReceiver DEFAULT_INFO_RENDERER = new ITextReceiver() {
        private static final int STARTING_X = 1;
        private static final int STARTING_Y = 1;
        private int x;
        private int y;

        {
            reset();
        }

        @Override
        public void reset() {
            x = STARTING_X;
            y = STARTING_Y;
        }

        @Override
        public void string(String text) {
            fontRenderer().drawStringWithShadow(text, x, y, 0xffffff);
            x += fontRenderer().getStringWidth(text);
        }

        @Override
        public void line(String line) {
            fontRenderer().drawStringWithShadow(line, STARTING_X, y, 0xffffff);
            nextLine();
        }

        @Override
        public void nextLine() {
            x = STARTING_X;
            y += Render2D.fontHeight() + 2;
        }
    };

    public static final int CONTENTS = 0x662696ff;
    public static final int BORDER = 0x88e38a42;

    // Mark these final to enforce the master switch on subclasses

    @SuppressWarnings("UnusedReturnValue")
    public final boolean tryRender(IWidget widget, int mx, int my) {
        if (!Config.CLIENT.enableInspections.get()) {
            return false;
        }
        if (widget.isInside(mx, my) && shouldRender(widget, mx, my)) {
            renderBox(widget);
            if (Screen.hasControlDown()) {
                renderOverlayInfo(widget);
            }
            return true;
        }
        return false;
    }

    @SuppressWarnings("UnusedReturnValue")
    public final boolean tryRender(IWindow window, int mx, int my) {
        if (!Config.CLIENT.enableInspections.get()) {
            return false;
        }
        if (window.isInside(mx, my) && shouldRender(window, mx, my)) {
            renderBox(window);
            if (Screen.hasControlDown()) {
                renderOverlayInfo(window);
            }
            return true;
        }
        return false;
    }

    public boolean shouldRender(IWidget widget, int mx, int my) {
        return true;
    }

    public boolean shouldRender(IWindow window, int mx, int my) {
        return true;
    }

    public void renderBox(IWidget widget) {
        if (widget instanceof IHighlightRenderer) {
            ((IHighlightRenderer) widget).renderHighlight();
        } else {
            fontRenderer().drawStringWithShadow("(Widget does not support highlight)", 0, windowHeight() - Render2D.fontHeight(), 0xffffff);
        }
    }

    public void renderBox(IWindow window) {
        if (window instanceof IHighlightRenderer) {
            ((IHighlightRenderer) window).renderHighlight();
        } else {
            fontRenderer().drawStringWithShadow("(Window does not support highlight)", 0, windowHeight() - Render2D.fontHeight(), 0xffffff);
        }
    }

    public void renderOverlayInfo(IWidget widget) {
        GlStateManager.pushMatrix();
        GlStateManager.scalef(0.5F, 0.5F, 1F);
        DEFAULT_INFO_RENDERER.reset();
        if (widget instanceof IInfoProvider) {
            ((IInfoProvider) widget).provideInformation(DEFAULT_INFO_RENDERER);
        } else {
            DEFAULT_INFO_RENDERER.line("(Widget does not support overlay info)");
        }
        GlStateManager.popMatrix();
    }

    public void renderOverlayInfo(IWindow window) {
        GlStateManager.pushMatrix();
        GlStateManager.scalef(0.5F, 0.5F, 1.0F);
        DEFAULT_INFO_RENDERER.reset();
        if (window instanceof IInfoProvider) {
            ((IInfoProvider) window).provideInformation(DEFAULT_INFO_RENDERER);
        } else {
            DEFAULT_INFO_RENDERER.line("(Window does not support overlay info)");
        }
        GlStateManager.popMatrix();
    }

    public static void renderHighlight(int x, int y, int width, int height) {
        useBlendingGLStates();
        beginColoredQuad();
        coloredRect(x, y, x + width, y + height, CONTENTS);
        draw();
        useTextureGLStates();
    }

    public static void renderBorderedHighlight(int x1, int y1, int ix1, int iy1, int width, int height, int fullWidth, int fullHeight) {
        int x2 = x1 + fullWidth;
        int y2 = y1 + fullHeight;
        int ix2 = ix1 + width;
        int iy2 = iy1 + height;

        useBlendingGLStates();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        // Can't just do two rectangles because they are transparent

        // 1------4
        // |      |
        // 2------3
        quad(buffer, x1, y1, ix1, iy1, ix2, iy1, x2, y1, 0F, BORDER); // Top border
        quad(buffer, ix2, iy1, ix2, iy2, x2, y2, x2, y1, 0F, BORDER); // Right border
        quad(buffer, ix1, iy2, x1, y2, x2, y2, ix2, iy2, 0F, BORDER); // Bottom border
        quad(buffer, x1, y1, x1, y2, ix1, iy2, ix1, iy1, 0F, BORDER); // Left border
        coloredRect(ix1, iy1, ix2, iy2, CONTENTS);

        tessellator.draw();
        useTextureGLStates();
    }
}
