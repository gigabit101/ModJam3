package vswe.stevesfactory.library.gui.screen;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.util.ResourceLocation;
import vswe.stevesfactory.api.StevesFactoryManagerAPI;
import vswe.stevesfactory.library.gui.Texture;

import static vswe.stevesfactory.library.gui.Render2D.*;

public final class BackgroundRenderers {

    private BackgroundRenderers() {
    }

    ///////////////////////////////////////////////////////////////////////////
    // Flat style
    ///////////////////////////////////////////////////////////////////////////

    public static final int LIGHT_BORDER_COLOR = 0xffffffff;
    public static final int DARK_BORDER_COLOR = 0xff606060;
    public static final int BACKGROUND_COLOR = 0xffc6c6c6;

    /**
     * Draw a flat style GUI background on the given position with the given width and height.
     * <p>
     * The background has a border of 2 pixels, therefore the background cannot have a dimension less than 4x4 pixels. It shares the same
     * bottom color as the vanilla background but has a simpler border (plain color).
     * <p>
     * See {@link #drawVanillaStyle4x4(int, int, int, int, float)} for parameter information.
     *
     * @see #LIGHT_BORDER_COLOR
     * @see #DARK_BORDER_COLOR
     * @see #BACKGROUND_COLOR
     */
    public static void drawFlatStyle(int x, int y, int width, int height, float z) {
        Preconditions.checkArgument(width >= 4 && height >= 4);

        int x2 = x + width;
        int y2 = y + height;

        RenderSystem.disableTexture();
        beginColoredQuad();
        {
            coloredRect(x, y, x2, y2, z, DARK_BORDER_COLOR);
            coloredRect(x, y, x2 - 2, y2 - 2, z, LIGHT_BORDER_COLOR);
            coloredRect(x + 2, y + 2, x2 - 2, y2 - 2, z, BACKGROUND_COLOR);
        }
        draw();
        RenderSystem.enableTexture();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Vanilla style
    ///////////////////////////////////////////////////////////////////////////

    private static final ResourceLocation GENERIC_COMPONENTS = new ResourceLocation(StevesFactoryManagerAPI.MODID, "textures/gui/generic_components.png");
    private static final int TEX_WIDTH = 256;
    private static final int TEX_HEIGHT = 256;

    public static final Texture TOP_LEFT_CORNER4x4 = Texture.portion(GENERIC_COMPONENTS, TEX_WIDTH, TEX_HEIGHT, 0, 0, 4, 4);
    public static final Texture TOP_RIGHT_CORNER4x4 = TOP_LEFT_CORNER4x4.moveRight(1);
    public static final Texture BOTTOM_LEFT_CORNER4x4 = TOP_LEFT_CORNER4x4.moveRight(2);
    public static final Texture BOTTOM_RIGHT_CORNER4x4 = TOP_LEFT_CORNER4x4.moveRight(3);

    public static final Texture TOP_EDGE4x4 = TOP_LEFT_CORNER4x4.moveRight(4);
    public static final Texture BOTTOM_EDGE4x4 = TOP_EDGE4x4.moveRight(1);
    public static final Texture LEFT_EDGE4x4 = TOP_EDGE4x4.moveRight(2);
    public static final Texture RIGHT_EDGE4x4 = TOP_EDGE4x4.moveRight(3);

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
    public static void drawVanillaStyle4x4(int x, int y, int width, int height, float z) {
        Preconditions.checkArgument(width >= 8 && height >= 8);

        int bodyWidth = width - 4 * 2;
        int bodyHeight = height - 4 * 2;
        int bodyX = x + 4;
        int bodyY = y + 4;
        int bodyXRight = bodyX + bodyWidth;
        int bodyYBottom = bodyY + bodyHeight;
        useTextureGLStates();
        beginTexturedQuad();
        bindTexture(GENERIC_COMPONENTS);
        {
            int cornerXRight = x + width - 4;
            int cornerYBottom = y + height - 4;
            TOP_LEFT_CORNER4x4.vertices(x, y, z);
            TOP_RIGHT_CORNER4x4.vertices(cornerXRight, y, z);
            BOTTOM_LEFT_CORNER4x4.vertices(x, cornerYBottom, z);
            BOTTOM_RIGHT_CORNER4x4.vertices(cornerXRight, cornerYBottom, z);

            if (bodyWidth > 0) {
                TOP_EDGE4x4.vertices(bodyX, y, bodyXRight, y + 4, z);
                BOTTOM_EDGE4x4.vertices(bodyX, bodyYBottom, bodyXRight, bodyYBottom + 4, z);
            }
            if (bodyHeight > 0) {
                LEFT_EDGE4x4.vertices(x, bodyY, x + 4, bodyYBottom, z);
                RIGHT_EDGE4x4.vertices(bodyXRight, bodyY, bodyXRight + 4, bodyYBottom, z);
            }
        }
        draw();

        if (bodyWidth > 0 && bodyHeight > 0) {
            lightGrayRect(bodyX, bodyY, bodyWidth, bodyHeight, z);
        }
    }

    public static final Texture TOP_LEFT_CORNER3x3 = Texture.portion(GENERIC_COMPONENTS, TEX_WIDTH, TEX_HEIGHT, 0, 4, 3, 3);
    public static final Texture TOP_RIGHT_CORNER3x3 = TOP_LEFT_CORNER3x3.moveRight(1);
    public static final Texture BOTTOM_LEFT_CORNER3x3 = TOP_LEFT_CORNER3x3.moveRight(2);
    public static final Texture BOTTOM_RIGHT_CORNER3x3 = TOP_LEFT_CORNER3x3.moveRight(3);

    public static final Texture TOP_EDGE3x3 = TOP_LEFT_CORNER3x3.moveRight(4);
    public static final Texture BOTTOM_EDGE3x3 = TOP_EDGE3x3.moveRight(1);
    public static final Texture LEFT_EDGE3x3 = TOP_EDGE3x3.moveRight(2);
    public static final Texture RIGHT_EDGE3x3 = TOP_EDGE3x3.moveRight(3);

    /**
     * @see #drawVanillaStyle3x3(int, int, int, int, float)
     */
    public static void drawVanillaStyle3x3(int x, int y, int width, int height, float z) {
        Preconditions.checkArgument(width >= 6 && height >= 6);

        int bodyWidth = width - 3 * 2;
        int bodyHeight = height - 3 * 2;
        int bodyX = x + 3;
        int bodyY = y + 3;
        int bodyXRight = bodyX + bodyWidth;
        int bodyYBottom = bodyY + bodyHeight;
        useTextureGLStates();
        beginTexturedQuad();
        bindTexture(GENERIC_COMPONENTS);
        {
            int cornerXRight = x + width - 3;
            int cornerYBottom = y + height - 3;
            TOP_LEFT_CORNER3x3.vertices(x, y, z);
            TOP_RIGHT_CORNER3x3.vertices(cornerXRight, y, z);
            BOTTOM_LEFT_CORNER3x3.vertices(x, cornerYBottom, z);
            BOTTOM_RIGHT_CORNER3x3.vertices(cornerXRight, cornerYBottom, z);

            if (bodyWidth > 0) {
                TOP_EDGE3x3.vertices(bodyX, y, bodyXRight, y + 3, z);
                BOTTOM_EDGE3x3.vertices(bodyX, bodyYBottom, bodyXRight, bodyYBottom + 3, z);
            }
            if (bodyHeight > 0) {
                LEFT_EDGE3x3.vertices(x, bodyY, x + 3, bodyYBottom, z);
                RIGHT_EDGE3x3.vertices(bodyXRight, bodyY, bodyXRight + 3, bodyYBottom, z);
            }
        }
        draw();

        if (bodyWidth > 0 && bodyHeight > 0) {
            lightGrayRect(bodyX, bodyY, bodyWidth, bodyHeight, z);
        }
    }

    private static void lightGrayRect(int bodyX, int bodyY, int bodyWidth, int bodyHeight, float z) {
        RenderSystem.disableTexture();
        beginColoredQuad();
        coloredRect(bodyX, bodyY, bodyX + bodyWidth, bodyY + bodyHeight, z, 0xffc6c6c6);
        draw();
        RenderSystem.enableTexture();
    }
}