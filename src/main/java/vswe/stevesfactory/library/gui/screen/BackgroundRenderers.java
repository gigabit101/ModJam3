package vswe.stevesfactory.library.gui.screen;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;
import vswe.stevesfactory.StevesFactoryManager;

import static vswe.stevesfactory.utils.RenderingHelper.*;

@OnlyIn(Dist.CLIENT)
public final class BackgroundRenderers {

    private BackgroundRenderers() {
    }

    ///////////////////////////////////////////////////////////////////////////
    // Flat style
    ///////////////////////////////////////////////////////////////////////////

    public static final int LIGHT_BORDER_COLOR = 0xffffff;
    public static final int DARK_BORDER_COLOR = 0x606060;
    public static final int BACKGROUND_COLOR = 0xc6c6c6;

    /**
     * Draw a flat style GUI background on the given position with the given width and height.
     * <p>
     * The background has a border of 2 pixels, therefore the background cannot have a dimension less than 4x4 pixels. It shares the same
     * bottom color as the vanilla background but has a simpler border (plain color).
     * <p>
     * Note that {@link GL11#GL_ALPHA_TEST} needs to be disabled in order for this method to function, if the background is drawn in a
     * standard Minecraft GUI setting (has a dark gradient overlay on the world renderer).
     * <p>
     * See {@link #drawVanillaStyle(int, int, int, int, float)} for parameter information.
     *
     * @see #LIGHT_BORDER_COLOR
     * @see #DARK_BORDER_COLOR
     * @see #BACKGROUND_COLOR
     */
    public static void drawFlatStyle(int x, int y, int width, int height, float z) {
        Preconditions.checkArgument(width >= 4 && height >= 4);

        int x2 = x + width;
        int y2 = y + height;

        drawRect(x, y, x2, y2, BACKGROUND_COLOR);
        usePlainColorGLStates();
        getRenderer().begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        rectVertices(x, y, x2, y2, DARK_BORDER_COLOR);
        rectVertices(x, y, x2 - 2, y2 - 2, LIGHT_BORDER_COLOR);
        rectVertices(x + 2, y + 2, x2 - 2, y2 - 2, BACKGROUND_COLOR);

        Tessellator.getInstance().draw();
        GlStateManager.enableTexture();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Vanilla style
    ///////////////////////////////////////////////////////////////////////////

    private static final ResourceLocation TEXTURE = new ResourceLocation(StevesFactoryManager.MODID, "textures/gui/generic_components.png");
    private static final int UNIT_LENGTH = 4;
    private static final float UV_MULTIPLIER = 1f / 256f;

    private static float zLevel = 0F;

    /**
     * Draw a vanilla styled GUI background on the given position with the given width and height.
     * <p>
     * {@code x} and {@code y} includes the top/left border; {@code width} and {@code height} also includes the borders. Since a border is 4
     * pixels wide, {@code width} and {@code height} must be greater than 8.
     * <p>
     * The background will be drawn in 9 parts max: 4 corners, 4 borders, and a body piece. Only the 4 corners are mandatory, the rest is
     * optional depending on the size of the background to be drawn.
     *
     * @param x      Left x of the result, including border
     * @param y      Top y of the result, including border
     * @param width  Width of the result, including both borders and must be larger than 8
     * @param height Height of the result, including both borders and must be larger than 8
     * @param z      Z level that will be used for drawing and put into the depth buffer
     */
    public static void drawVanillaStyle(int x, int y, int width, int height, float z) {
        Preconditions.checkArgument(width >= 8 && height >= 8);

        useTextureGLStates();
        zLevel = z;

        getRenderer().begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        bindTexture(TEXTURE);

        int cornerXRight = x + width - UNIT_LENGTH;
        int cornerYBottom = y + height - UNIT_LENGTH;
        CornerPiece.drawTopLeft(x, y);
        CornerPiece.drawTopRight(cornerXRight, y);
        CornerPiece.drawBottomLeft(x, cornerYBottom);
        CornerPiece.drawBottomRight(cornerXRight, cornerYBottom);

        int bodyWidth = width - UNIT_LENGTH * 2;
        int bodyHeight = height - UNIT_LENGTH * 2;
        int bodyX = x + UNIT_LENGTH;
        int bodyY = y + UNIT_LENGTH;

        if (bodyWidth > 0) {
            EdgePiece.drawTop(bodyX, y, bodyWidth);
            EdgePiece.drawBottom(bodyX, bodyY + bodyHeight, bodyWidth);
        }
        if (bodyHeight > 0) {
            EdgePiece.drawLeft(x, bodyY, bodyHeight);
            EdgePiece.drawRight(bodyX + bodyWidth, bodyY, bodyHeight);
        }

        Tessellator.getInstance().draw();

        if (bodyWidth > 0 && bodyHeight > 0) {
            drawRect(bodyX, bodyY, bodyX + bodyWidth, bodyY + bodyHeight, 198, 198, 198, 255);
            GlStateManager.enableTexture();
        }
    }

    /**
     * All methods assume {@link #TEXTURE} is already bond with {@link net.minecraft.client.renderer.texture.TextureManager#bindTexture(ResourceLocation)}.
     */
    private static final class CornerPiece {

        private static final int TX_TOP_LEFT = 0;
        private static final int TX_TOP_RIGHT = TX_TOP_LEFT + UNIT_LENGTH;
        private static final int TX_BOTTOM_LEFT = TX_TOP_LEFT + UNIT_LENGTH * 2;
        private static final int TX_BOTTOM_RIGHT = TX_TOP_LEFT + UNIT_LENGTH * 3;
        private static final int TY = 0;

        private static void drawTopLeft(int x, int y) {
            plotVertexesTex(x, y, UNIT_LENGTH, UNIT_LENGTH, TX_TOP_LEFT, TY);
        }

        private static void drawTopRight(int x, int y) {
            plotVertexesTex(x, y, UNIT_LENGTH, UNIT_LENGTH, TX_TOP_RIGHT, TY);
        }

        private static void drawBottomLeft(int x, int y) {
            plotVertexesTex(x, y, UNIT_LENGTH, UNIT_LENGTH, TX_BOTTOM_LEFT, TY);
        }

        private static void drawBottomRight(int x, int y) {
            plotVertexesTex(x, y, UNIT_LENGTH, UNIT_LENGTH, TX_BOTTOM_RIGHT, TY);
        }
    }

    /**
     * All methods assume {@link #TEXTURE} is already bond with {@link net.minecraft.client.renderer.texture.TextureManager#bindTexture(ResourceLocation)}.
     */
    private static final class EdgePiece {

        private static final int TX_TOP = UNIT_LENGTH * 4;
        private static final int TX_BOTTOM = TX_TOP + UNIT_LENGTH;
        private static final int TX_LEFT = TX_TOP + UNIT_LENGTH * 2;
        private static final int TX_RIGHT = TX_TOP + UNIT_LENGTH * 3;
        private static final int TY = 0;

        private static void drawTop(int x, int y, int width) {
            plotVertexesTex(x, y, width, UNIT_LENGTH, TX_TOP, TY);
        }

        private static void drawBottom(int x, int y, int width) {
            plotVertexesTex(x, y, width, UNIT_LENGTH, TX_BOTTOM, TY);
        }

        private static void drawLeft(int x, int y, int height) {
            plotVertexesTex(x, y, UNIT_LENGTH, height, TX_LEFT, TY);
        }

        private static void drawRight(int x, int y, int height) {
            plotVertexesTex(x, y, UNIT_LENGTH, height, TX_RIGHT, TY);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Util methods
    ///////////////////////////////////////////////////////////////////////////

    private static void plotVertexesTex(int x1, int y1, int width, int height, int tx, int ty) {
        int x2 = x1 + width;
        int y2 = y1 + height;
        int tx2 = tx + UNIT_LENGTH;
        int ty2 = ty + UNIT_LENGTH;

        float u1 = tx * UV_MULTIPLIER;
        float u2 = tx2 * UV_MULTIPLIER;
        float v1 = ty * UV_MULTIPLIER;
        float v2 = ty2 * UV_MULTIPLIER;

        // Bottom Left -> Top Left -> Top Right -> Bottom Right
        getRenderer().pos(x2, y1, zLevel).tex(u2, v1).endVertex();
        getRenderer().pos(x1, y1, zLevel).tex(u1, v1).endVertex();
        getRenderer().pos(x1, y2, zLevel).tex(u1, v2).endVertex();
        getRenderer().pos(x2, y2, zLevel).tex(u2, v2).endVertex();
    }
}