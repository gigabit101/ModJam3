package vswe.stevesfactory.utils;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import vswe.stevesfactory.StevesFactoryManager;

import java.awt.*;

public final class RenderingHelper {

    public static final Dimension UNBOUNDED = new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);

    private RenderingHelper() {
    }

    public static BufferBuilder getRenderer() {
        return Tessellator.getInstance().getBuffer();
    }

    public static void bindTexture(ResourceLocation texture) {
        Minecraft.getInstance().getTextureManager().bindTexture(texture);
    }

    public static FontRenderer fontRenderer() {
        return Minecraft.getInstance().fontRenderer;
    }

    public static int fontHeight() {
        return fontRenderer().FONT_HEIGHT;
    }

    public static int textWidth(String text) {
        return fontRenderer().getStringWidth(text);
    }

    public static void useGradientGLStates() {
        GlStateManager.disableTexture();
        GlStateManager.disableAlphaTest();
        GlStateManager.enableBlend();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        GlStateManager.blendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
    }

    public static void useBlendingGLStates() {
        GlStateManager.disableTexture();
        GlStateManager.disableAlphaTest();
        GlStateManager.enableBlend();
        GlStateManager.blendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
    }

    public static void usePlainColorGLStates() {
        GlStateManager.disableTexture();
        GlStateManager.disableBlend();
    }

    public static void useTextureGLStates() {
        GlStateManager.enableTexture();
        GlStateManager.disableBlend();
        GlStateManager.color3f(1.0F, 1.0F, 1.0F);
    }

    public static void drawRect(Rectangle rect, int color) {
        drawRect(rect.x, rect.y, rect.x + rect.width, rect.y + rect.height, color);
    }

    public static void drawRect(Point point, Dimension dimensions, int red, int green, int blue, int alpha) {
        drawRect(point.x, point.y, point.x + dimensions.width, point.y + dimensions.height, red, green, blue, alpha);
    }

    public static void drawRect(Point point, Dimension dimensions, int color) {
        drawRect(point.x, point.y, point.x + dimensions.width, point.y + dimensions.width, color);
    }

    public static void drawRect(int x, int y, Dimension dimensions, int red, int green, int blue, int alpha) {
        drawRect(x, y, x + dimensions.width, y + dimensions.height, red, green, blue, alpha);
    }

    public static void drawRect(int x, int y, Dimension dimensions, int color) {
        drawRect(x, y, x + dimensions.width, y + dimensions.height, color);
    }

    public static void drawRect(int x, int y, int x2, int y2, int red, int green, int blue, int alpha) {
        GlStateManager.disableTexture();
        getRenderer().begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        rectVertices(x, y, x2, y2, red, green, blue, alpha);
        Tessellator.getInstance().draw();
    }

    public static void drawRect(int x, int y, int x2, int y2, int color) {
        GlStateManager.disableTexture();
        getRenderer().begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        rectVertices(x, y, x2, y2, color);
        Tessellator.getInstance().draw();
    }

    public static void rectVertices(int x, int y, int x2, int y2, int color) {
        int alpha = (color >> 24) & 255;
        int red = (color >> 16) & 255;
        int green = (color >> 8) & 255;
        int blue = color & 255;
        rectVertices(x, y, x2, y2, red, green, blue, alpha);
    }

    public static void rectVertices(int x, int y, int x2, int y2, int red, int green, int blue, int alpha) {
        BufferBuilder renderer = getRenderer();
        renderer.pos(x, y, 0F).color(red, green, blue, alpha).endVertex();
        renderer.pos(x, y2, 0F).color(red, green, blue, alpha).endVertex();
        renderer.pos(x2, y2, 0F).color(red, green, blue, alpha).endVertex();
        renderer.pos(x2, y, 0F).color(red, green, blue, alpha).endVertex();
    }

    public static void drawColorLogic(int x, int y, int width, int height, int red, int green, int blue, GlStateManager.LogicOp logicOp) {
        GlStateManager.disableTexture();
        GlStateManager.enableColorLogicOp();
        GlStateManager.logicOp(logicOp);

        drawRect(x, y, x + width, y + height, red, green, blue, 255);

        GlStateManager.disableColorLogicOp();
        GlStateManager.enableTexture();
    }

    public static void drawThickBeveledBox(int x1, int y1, int x2, int y2, int thickness, int topLeftColor, int bottomRightColor, int fillColor) {
        GlStateManager.disableTexture();
        drawRect(x1, y1, x2, y2, bottomRightColor);
        drawRect(x1, y1, x2 - thickness, y2 - thickness, topLeftColor);
        if (fillColor != -1) {
            drawRect(x1 + thickness, y1 + thickness, x2 - thickness, y2 - thickness, fillColor);
        }
        GlStateManager.enableTexture();
    }

    public static void drawVerticalGradientRect(int x1, int y1, int x2, int y2, int color1, int color2) {
        int a1 = (color1 >> 24) & 255;
        int r1 = (color1 >> 16) & 255;
        int g1 = (color1 >> 8) & 255;
        int b1 = color1 & 255;
        int a2 = (color2 >> 24) & 255;
        int r2 = (color2 >> 16) & 255;
        int g2 = (color2 >> 8) & 255;
        int b2 = color2 & 255;

        useGradientGLStates();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(x2, y1, 0F).color(r1, g1, b1, a1).endVertex();
        buffer.pos(x1, y1, 0F).color(r1, g1, b1, a1).endVertex();
        buffer.pos(x1, y2, 0F).color(r2, g2, b2, a2).endVertex();
        buffer.pos(x2, y2, 0F).color(r2, g2, b2, a2).endVertex();
        tessellator.draw();

        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.disableBlend();
        GlStateManager.enableAlphaTest();
        GlStateManager.enableTexture();
    }

    public static void drawHorizontalGradientRect(int x1, int y1, int x2, int y2, int color1, int color2) {
        int a1 = (color1 >> 24) & 255;
        int r1 = (color1 >> 16) & 255;
        int g1 = (color1 >> 8) & 255;
        int b1 = color1 & 255;
        int a2 = (color2 >> 24) & 255;
        int r2 = (color2 >> 16) & 255;
        int g2 = (color2 >> 8) & 255;
        int b2 = color2 & 255;

        useGradientGLStates();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(x1, y1, 0F).color(r1, g1, b1, a1).endVertex();
        buffer.pos(x1, y2, 0F).color(r1, g1, b1, a1).endVertex();
        buffer.pos(x2, y2, 0F).color(r2, g2, b2, a2).endVertex();
        buffer.pos(x2, y1, 0F).color(r2, g2, b2, a2).endVertex();
        tessellator.draw();

        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.disableBlend();
        GlStateManager.enableAlphaTest();
        GlStateManager.enableTexture();
    }

    public static void drawTexture(int x1, int y1, int x2, int y2, float z, ResourceLocation texture, float u1, float v1, float u2, float v2) {
        GlStateManager.enableTexture();
        GlStateManager.disableLighting();
        GlStateManager.color3f(1.0F, 1.0F, 1.0F);
        bindTexture(texture);

        BufferBuilder buffer = getRenderer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        textureVertices(x1, y1, x2, y2, z, u1, v1, u2, v2);
        Tessellator.getInstance().draw();
    }

    public static void textureVertices(int x1, int y1, int x2, int y2, float z, float u1, float v1, float u2, float v2) {
        BufferBuilder buffer = getRenderer();
        buffer.pos(x1, y1, z).tex(u1, v1).endVertex();
        buffer.pos(x1, y2, z).tex(u1, v2).endVertex();
        buffer.pos(x2, y2, z).tex(u2, v2).endVertex();
        buffer.pos(x2, y1, z).tex(u2, v1).endVertex();
    }

    public static void drawTexture(int x1, int y1, int x2, int y2, ResourceLocation texture, float u1, float v1, float u2, float v2) {
        drawTexture(x1, y1, x2, y2, 0F, texture, u1, v1, u2, v2);
    }

    public static void drawTexturePortion(int x1, int y1, int x2, int y2, float z, ResourceLocation texture, int textureWidth, int textureHeight, int tx, int ty, int portionWidth, int portionHeight) {
        float uFactor = 1.0F / (float) textureWidth;
        float vFactor = 1.0F / (float) textureHeight;
        int tx2 = tx + portionWidth;
        int ty2 = ty + portionHeight;
        drawTexture(x1, y1, x2, y2, z, texture, tx * uFactor, ty * vFactor, tx2 * uFactor, ty2 * vFactor);
    }

    public static void drawTexturePortion(int x1, int y1, int x2, int y2, ResourceLocation texture, int textureWidth, int textureHeight, int tx, int ty, int portionWidth, int portionHeight) {
        drawTexturePortion(x1, y1, x2, y2, 0F, texture, textureWidth, textureHeight, tx, ty, portionWidth, portionHeight);
    }

    public static void drawCompleteTexture(int x1, int y1, int x2, int y2, float z, ResourceLocation texture) {
        drawTexture(x1, y1, x2, y2, z, texture, 0.0F, 0.0F, 1.0F, 1.0F);
    }

    public static void drawCompleteTexture(int x1, int y1, int x2, int y2, ResourceLocation texture) {
        drawCompleteTexture(x1, y1, x2, y2, 0F, texture);
    }

    public static void drawTexture256x256(int x1, int y1, int x2, int y2, ResourceLocation texture, int tx, int ty, int portionWidth, int portionHeight) {
        drawTexturePortion(x1, y1, x2, y2, texture, 256, 256, tx, ty, portionWidth, portionHeight);
    }

    public static int getXForAlignedRight(int right, int width) {
        return right - width;
    }

    public static int getXForAlignedCenter(int left, int right, int width) {
        return left + (right - left) / 2 - width / 2;
    }

    public static int getYForAlignedCenter(int top, int bottom, int height) {
        return top + (bottom - top) / 2 - height / 2;
    }

    public static int getYForAlignedBottom(int bottom, int height) {
        return bottom - height;
    }

    public static int getXForHorizontallyCenteredText(String text, int left, int right) {
        int textWidth = fontRenderer().getStringWidth(text);
        return getXForAlignedCenter(left, right, textWidth);
    }

    public static int getYForVerticallyCenteredText(int top, int bottom) {
        return getYForAlignedCenter(top, bottom, fontHeight());
    }

    public static void drawTextCenteredVertically(String text, int leftX, int top, int bottom, int color) {
        int y = getYForVerticallyCenteredText(top, bottom);
        GlStateManager.enableTexture();
        fontRenderer().drawString(text, leftX, y, color);
    }

    public static void drawTextCenteredHorizontally(String text, int left, int right, int topY, int color) {
        int x = getXForHorizontallyCenteredText(text, left, right);
        GlStateManager.enableTexture();
        fontRenderer().drawString(text, x, topY, color);
    }

    public static void drawTextCentered(String text, int top, int bottom, int left, int right, int color) {
        int x = getXForHorizontallyCenteredText(text, left, right);
        int y = getYForVerticallyCenteredText(top, bottom);
        GlStateManager.enableTexture();
        fontRenderer().drawString(text, x, y, color);
    }

    public static Dimension toBorder(Dimension contents, int borderSize) {
        return new Dimension(contents.width + borderSize * 2, contents.height + borderSize * 2);
    }

    public static ResourceLocation linkTexture(String path) {
        return new ResourceLocation(StevesFactoryManager.MODID, "textures/" + path);
    }

    public static ResourceLocation linkTexture(String seg1, String seg2) {
        return new ResourceLocation(StevesFactoryManager.MODID, "textures/" + seg1 + "/" + seg2);
    }

    public static ResourceLocation linkTexture(String seg1, String seg2, String seg3) {
        return new ResourceLocation(StevesFactoryManager.MODID, "textures/" + seg1 + "/" + seg2 + "/" + seg3);
    }

    public static ResourceLocation linkTexture(String... segments) {
        StringBuilder path = new StringBuilder("textures/");
        for (String segment : segments) {
            path.append(segment).append("/");
        }
        return new ResourceLocation(StevesFactoryManager.MODID, path.toString());
    }
}
