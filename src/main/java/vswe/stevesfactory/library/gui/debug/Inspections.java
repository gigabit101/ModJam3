package vswe.stevesfactory.library.gui.debug;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;
import vswe.stevesfactory.library.gui.widget.IWidget;
import vswe.stevesfactory.library.gui.window.IWindow;
import vswe.stevesfactory.utils.RenderingHelper;

import java.awt.*;

import static vswe.stevesfactory.utils.RenderingHelper.*;

public abstract class Inspections implements IRenderEventListener {

    public interface IInspectionInfoProvider {

        void provideInformation(ITextReceiver receiver);
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
            fontRenderer().drawString(text, x, y, Color.WHITE.getRGB());
            x += fontRenderer().getStringWidth(text);
        }

        @Override
        public void line(String line) {
            fontRenderer().drawString(line, STARTING_X, y, Color.WHITE.getRGB());
            nextLine();
        }

        @Override
        public void nextLine() {
            x = STARTING_X;
            y += fontHeight() + 2;
        }
    };

    /**
     * Master switch for enabling/disabling inspection. Used for SFM setting 'InspectionBoxHighlighting'.
     */
    public static boolean enabled;

    public static final int CONTENTS = 0x662696ff;
    public static final int BORDER = 0x88e38a42;
    public static final int BORDER_A = BORDER >> 24 & 255;
    public static final int BORDER_R = BORDER >> 16 & 255;
    public static final int BORDER_G = BORDER >> 8 & 255;
    public static final int BORDER_B = BORDER & 255;

    // Mark these final to enforce the master switch on subclasses

    @SuppressWarnings("UnusedReturnValue")
    public final boolean tryRender(IWidget widget, int mx, int my) {
        if (!enabled) {
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
        if (!enabled) {
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
        int ax = widget.getAbsoluteX();
        int ay = widget.getAbsoluteY();
        useBlendingGLStates();
        drawRect(ax, ay, ax + widget.getWidth(), ay + widget.getHeight(), CONTENTS);
        useTextureGLStates();
    }

    public void renderBox(IWindow window) {
        // Can't just do two rectangles because they are transparent
        useBlendingGLStates();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        {
            int x = window.getX();
            int y = window.getY();
            int x2 = x + window.getWidth();
            int y2 = y + window.getHeight();
            int bs = window.getBorderSize();

            RenderingHelper.rectVertices(x, y, x2 - bs, y + bs, BORDER_R, BORDER_G, BORDER_B, BORDER_A);
            RenderingHelper.rectVertices(x2 - bs, y, x2, y2 - bs, BORDER_R, BORDER_G, BORDER_B, BORDER_A);
            RenderingHelper.rectVertices(x + bs, y2 - bs, x2, y2, BORDER_R, BORDER_G, BORDER_B, BORDER_A);
            RenderingHelper.rectVertices(x, y + bs, x + bs, y2, BORDER_R, BORDER_G, BORDER_B, BORDER_A);
        }
        {
            int cx = window.getContentX();
            int cy = window.getContentY();
            RenderingHelper.rectVertices(cx, cy, cx + window.getContentWidth(), cy + window.getContentHeight(), CONTENTS);
        }
        tessellator.draw();
        useTextureGLStates();
    }

    public void renderOverlayInfo(IWidget widget) {
        GlStateManager.pushMatrix();
        GlStateManager.scalef(0.5F, 0.5F, 1F);
        DEFAULT_INFO_RENDERER.reset();
        if (widget instanceof IInspectionInfoProvider) {
            ((IInspectionInfoProvider) widget).provideInformation(DEFAULT_INFO_RENDERER);
        } else {
            defaultOverlayInfo(widget);
        }
        GlStateManager.popMatrix();
    }

    public void renderOverlayInfo(IWindow window) {
        GlStateManager.pushMatrix();
        GlStateManager.scalef(0.5F, 0.5F, 1.0F);
        DEFAULT_INFO_RENDERER.reset();
        if (window instanceof IInspectionInfoProvider) {
            ((IInspectionInfoProvider) window).provideInformation(DEFAULT_INFO_RENDERER);
        } else {
            defaultOverlayInfo(window);
        }
        GlStateManager.popMatrix();
    }

    protected void defaultOverlayInfo(IWidget widget) {
        DEFAULT_INFO_RENDERER.line("(default inspection info)");
        DEFAULT_INFO_RENDERER.line(widget.toString());
        DEFAULT_INFO_RENDERER.line("X=" + widget.getX());
        DEFAULT_INFO_RENDERER.line("Y=" + widget.getY());
        DEFAULT_INFO_RENDERER.line("AbsX=" + widget.getAbsoluteX());
        DEFAULT_INFO_RENDERER.line("AbsY=" + widget.getAbsoluteY());
        DEFAULT_INFO_RENDERER.line("Width=" + widget.getWidth());
        DEFAULT_INFO_RENDERER.line("Height=" + widget.getHeight());
    }

    protected void defaultOverlayInfo(IWindow window) {
        DEFAULT_INFO_RENDERER.line("(default inspection info)");
        DEFAULT_INFO_RENDERER.line(window.toString());
        DEFAULT_INFO_RENDERER.line("X=" + window.getX());
        DEFAULT_INFO_RENDERER.line("Y=" + window.getY());
        DEFAULT_INFO_RENDERER.line("Width=" + window.getWidth());
        DEFAULT_INFO_RENDERER.line("Height=" + window.getHeight());
        DEFAULT_INFO_RENDERER.line("ContentX=" + window.getContentX());
        DEFAULT_INFO_RENDERER.line("ContentY=" + window.getContentY());
        DEFAULT_INFO_RENDERER.line("ContentWidth=" + window.getContentWidth());
        DEFAULT_INFO_RENDERER.line("ContentHeight=" + window.getContentHeight());
        DEFAULT_INFO_RENDERER.line("BorderSize=" + window.getBorderSize());
    }
}
