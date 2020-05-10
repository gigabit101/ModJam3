package vswe.stevesfactory.library.gui;

import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.val;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import vswe.stevesfactory.api.StevesFactoryManagerAPI;
import vswe.stevesfactory.library.gui.layout.StrictTableLayout;
import vswe.stevesfactory.library.gui.layout.StrictTableLayout.GrowDirection;
import vswe.stevesfactory.library.gui.widget.AbstractWidget;
import vswe.stevesfactory.library.gui.widget.IWidget;

import java.awt.*;

import static org.lwjgl.opengl.GL11.*;

public final class Render2D {

    private Render2D() {
    }

    public static final float REGULAR_WINDOW_Z = 0F;
    public static final float POPUP_WINDOW_Z = 128F;
    public static final float CONTEXT_MENU_Z = 200F;

    public static final ResourceLocation INVALID_TEXTURE = new ResourceLocation(StevesFactoryManagerAPI.MODID, "textures/gui/invalid.png");
    public static final ResourceLocation COMPONENTS = new ResourceLocation(StevesFactoryManagerAPI.MODID, "textures/gui/flow_components.png");
    public static final ResourceLocation DELETE = new ResourceLocation(StevesFactoryManagerAPI.MODID, "textures/gui/actions/delete.png");
    public static final ResourceLocation CUT = new ResourceLocation(StevesFactoryManagerAPI.MODID, "textures/gui/actions/cut.png");
    public static final ResourceLocation COPY = new ResourceLocation(StevesFactoryManagerAPI.MODID, "textures/gui/actions/copy.png");
    public static final ResourceLocation PASTE = new ResourceLocation(StevesFactoryManagerAPI.MODID, "textures/gui/actions/paste.png");
    public static final ResourceLocation BACK = new ResourceLocation(StevesFactoryManagerAPI.MODID, "textures/gui/actions/back.png");
    public static final ResourceLocation CLOSE = new ResourceLocation(StevesFactoryManagerAPI.MODID, "textures/gui/actions/close.png");
    public static final ResourceLocation ITEM_SLOT = new ResourceLocation(StevesFactoryManagerAPI.MODID, "textures/gui/icon/item_slot.png");
    public static final ResourceLocation RIGHT_ARROW_SHORT = new ResourceLocation(StevesFactoryManagerAPI.MODID, "textures/gui/icon/right_arrow_short.png");

    public static final Texture CLOSE_ICON = Render2D.ofFlowComponent(18, 36, 9, 9);
    public static final Texture CLOSE_ICON_HOVERED = CLOSE_ICON.moveRight(1);
    public static final Texture SETTINGS_ICON = Render2D.ofFlowComponent(18, 106, 18, 18);
    public static final Texture SETTINGS_ICON_HOVERED = SETTINGS_ICON.moveRight(1);
    public static final Texture ADD_ENTRY_ICON = Render2D.ofFlowComponent(18, 125, 8, 8);
    public static final Texture ADD_ENTRY_HOVERED_ICON = ADD_ENTRY_ICON.moveRight(1);
    public static final Texture REMOVE_ENTRY_ICON = Render2D.ofFlowComponent(34, 125, 8, 8);
    public static final Texture REMOVE_ENTRY_HOVERED_ICON = REMOVE_ENTRY_ICON.moveRight(1);

    public static final StrictTableLayout DOWN_RIGHT_4_STRICT_TABLE = new StrictTableLayout(GrowDirection.DOWN, GrowDirection.RIGHT, 4);

    public static int mouseX() {
        return (int) Minecraft.getInstance().mouseHelper.getMouseX();
    }

    public static int mouseY() {
        return (int) Minecraft.getInstance().mouseHelper.getMouseY();
    }

    public static Texture ofFlowComponent(int x, int y, int width, int height) {
        return Texture.portion256x256(COMPONENTS, x, y, width, height);
    }

    public static boolean isInside(int x, int y, int mx, int my) {
        return isInside(x, y, 0, 0, mx, my);
    }

    public static boolean isInside(int x, int y, int bx1, int by1, int bx2, int by2) {
        return x >= bx1 && x < bx2 && y >= by1 && y < by2;
    }

    public static Minecraft minecraft() {
        return Minecraft.getInstance();
    }

    public static int windowWidth() {
        return Minecraft.getInstance().getMainWindow().getScaledWidth();
    }

    public static int windowHeight() {
        return Minecraft.getInstance().getMainWindow().getScaledHeight();
    }

    public static FontRenderer fontRenderer() {
        return Minecraft.getInstance().fontRenderer;
    }

    public static int fontHeight() {
        return Minecraft.getInstance().fontRenderer.FONT_HEIGHT;
    }

    public static void color(int color) {
        int alpha = (color >> 24) & 255;
        int red = (color >> 16) & 255;
        int green = (color >> 8) & 255;
        int blue = color & 255;
        RenderSystem.color4f(red / 255F, green / 255F, blue / 255F, alpha / 255F);
    }

    public static void bindTexture(ResourceLocation texture) {
        Minecraft.getInstance().getTextureManager().bindTexture(texture);
    }

    public static void beginColoredQuad() {
        Tessellator.getInstance().getBuffer().begin(GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
    }

    public static void beginTexturedQuad() {
        Tessellator.getInstance().getBuffer().begin(GL_QUADS, DefaultVertexFormats.POSITION_TEX);
    }

    public static void draw() {
        Tessellator.getInstance().draw();
    }

    public static void quad(BufferBuilder buffer, int x1, int y1, int x2, int y2, int x3, int y3, int x4, int y4, float z, int color) {
        int alpha = (color >> 24) & 255;
        int red = (color >> 16) & 255;
        int green = (color >> 8) & 255;
        int blue = color & 255;
        buffer.pos(x1, y1, z).color(red, green, blue, alpha).endVertex();
        buffer.pos(x2, y2, z).color(red, green, blue, alpha).endVertex();
        buffer.pos(x3, y3, z).color(red, green, blue, alpha).endVertex();
        buffer.pos(x4, y4, z).color(red, green, blue, alpha).endVertex();
    }

    public static void coloredRect(Point position, Dimension dimensions, float z, int color) {
        coloredRect(position.x, position.y, dimensions.width, dimensions.height, z, color);
    }

    public static void coloredRect(int x, int y, Dimension dimensions, float z, int color) {
        coloredRect(x, y, dimensions.width, dimensions.height, z, color);
    }

    public static void coloredRect(Point position, int width, int height, float z, int color) {
        coloredRect(position.x, position.y, width, height, z, color);
    }

    public static void coloredRect(int x1, int y1, int x2, int y2, float z, int color) {
        int alpha = (color >> 24) & 255;
        int red = (color >> 16) & 255;
        int green = (color >> 8) & 255;
        int blue = color & 255;
        val renderer = Tessellator.getInstance().getBuffer();
        renderer.pos(x1, y1, z).color(red, green, blue, alpha).endVertex();
        renderer.pos(x1, y2, z).color(red, green, blue, alpha).endVertex();
        renderer.pos(x2, y2, z).color(red, green, blue, alpha).endVertex();
        renderer.pos(x2, y1, z).color(red, green, blue, alpha).endVertex();
    }

    public static void coloredRect(int x1, int y1, int x2, int y2, int color) {
        coloredRect(x1, y1, x2, y2, 0F, color);
    }

    public static void verticalGradientRect(int x1, int y1, int x2, int y2, float z, int color1, int color2) {
        int a1 = (color1 >> 24) & 255;
        int r1 = (color1 >> 16) & 255;
        int g1 = (color1 >> 8) & 255;
        int b1 = color1 & 255;
        int a2 = (color2 >> 24) & 255;
        int r2 = (color2 >> 16) & 255;
        int g2 = (color2 >> 8) & 255;
        int b2 = color2 & 255;

        val buffer = Tessellator.getInstance().getBuffer();
        buffer.pos(x2, y1, z).color(r1, g1, b1, a1).endVertex();
        buffer.pos(x1, y1, z).color(r1, g1, b1, a1).endVertex();
        buffer.pos(x1, y2, z).color(r2, g2, b2, a2).endVertex();
        buffer.pos(x2, y2, z).color(r2, g2, b2, a2).endVertex();
    }

    public static void horizontalGradientRect(int x1, int y1, int x2, int y2, float z, int color1, int color2) {
        int a1 = (color1 >> 24) & 255;
        int r1 = (color1 >> 16) & 255;
        int g1 = (color1 >> 8) & 255;
        int b1 = color1 & 255;
        int a2 = (color2 >> 24) & 255;
        int r2 = (color2 >> 16) & 255;
        int g2 = (color2 >> 8) & 255;
        int b2 = color2 & 255;

        val buffer = Tessellator.getInstance().getBuffer();
        buffer.pos(x1, y1, z).color(r1, g1, b1, a1).endVertex();
        buffer.pos(x1, y2, z).color(r1, g1, b1, a1).endVertex();
        buffer.pos(x2, y2, z).color(r2, g2, b2, a2).endVertex();
        buffer.pos(x2, y1, z).color(r2, g2, b2, a2).endVertex();
    }

    public static void borderedRect(int x1, int y1, int x2, int y2) {
        coloredRect(x1, y1, x2, y2, 0xff404040);
        coloredRect(x1 + 1, y1 + 1, x2 - 1, y2 - 1, 0xff5e5e5e);
    }

    public static void thickBeveledBox(int x1, int y1, int x2, int y2, float z, int thickness, int topLeftColor, int bottomRightColor, int fillColor) {
        coloredRect(x1, y1, x2, y2, z, bottomRightColor);
        coloredRect(x1, y1, x2 - thickness, y2 - thickness, z, topLeftColor);
        coloredRect(x1 + thickness, y1 + thickness, x2 - thickness, y2 - thickness, z, fillColor);
    }

    public static void textureVertices(int x1, int y1, int x2, int y2, float z, float u1, float v1, float u2, float v2) {
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.pos(x1, y1, z).tex(u1, v1).endVertex();
        buffer.pos(x1, y2, z).tex(u1, v2).endVertex();
        buffer.pos(x2, y2, z).tex(u2, v2).endVertex();
        buffer.pos(x2, y1, z).tex(u2, v1).endVertex();
    }

    public static void verticalLine(int x, int y1, int y2, float z) {
        glVertex3f(x, y1, z);
        glVertex3f(x, y2, z);
    }

    public static void horizontalLine(int x1, int x2, int y, float z) {
        glVertex3f(x1, y, z);
        glVertex3f(x2, y, z);
    }

    public static void completeTexture(int x1, int y1, int x2, int y2, float z) {
        textureVertices(x1, y1, x2, y2, z, 0.0F, 0.0F, 1.0F, 1.0F);
    }

    public static int computeCenterY(int top, int bottom, int height) {
        return top + (bottom - top) / 2 - height / 2;
    }

    public static int computeCenterY(IWidget widget) {
        int top = widget.getOuterAbsoluteY();
        int height = widget.getFullHeight();
        return computeCenterY(top, top + height, height);
    }

    public static int computeBottomY(int bottom, int height) {
        return bottom - height;
    }

    public static int computeBottomY(IWidget widget) {
        int height = widget.getFullHeight();
        return computeBottomY(widget.getOuterAbsoluteY() + height, height);
    }

    public static int computeCenterX(int left, int right, int width) {
        return left + (right - left) / 2 - width / 2;
    }

    public static int computeCenterX(IWidget widget) {
        int left = widget.getOuterAbsoluteX();
        int width = widget.getFullWidth();
        return computeCenterX(left, left + width, width);
    }

    public static int computeRightX(int right, int width) {
        return right - width;
    }

    public static int computeRightX(IWidget widget) {
        return computeRightX(widget.getOuterAbsoluteX(), widget.getFullWidth());
    }

    public static int getXForHorizontallyCenteredText(String text, int left, int right) {
        return getXForHorizontallyCenteredText(TextRenderer.vanilla(), text, left, right);
    }

    public static int getXForHorizontallyCenteredText(TextRenderer textRenderer, String text, int left, int right) {
        int textWidth = textRenderer.calculateWidth(text);
        return computeCenterX(left, right, textWidth);
    }

    public static int getYForVerticallyCenteredText(int top, int bottom) {
        return getYForVerticallyCenteredText(TextRenderer.vanilla(), top, bottom);
    }

    public static int getYForVerticallyCenteredText(TextRenderer textRenderer, int top, int bottom) {
        return computeCenterY(top, bottom, (int) textRenderer.getFontHeight());
    }

    public static void renderVerticallyCenteredText(String text, int leftX, int top, int bottom, float z, int color) {
        int y = getYForVerticallyCenteredText(top, bottom);
        val tr = TextRenderer.vanilla();
        tr.setFontHeight(9);
        tr.setTextColor(color);
        tr.renderText(text, leftX, y, z);
    }

    public static void renderVerticallyCenteredText(TextRenderer textRenderer, String text, int leftX, int top, int bottom, float z) {
        int y = getYForVerticallyCenteredText(textRenderer, top, bottom);
        textRenderer.renderText(text, leftX, y, z);
    }

    public static void renderHorizontallyCenteredText(String text, int left, int right, int topY, float z, int color) {
        int x = getXForHorizontallyCenteredText(text, left, right);
        val tr = TextRenderer.vanilla();
        tr.setFontHeight(9);
        tr.setTextColor(color);
        tr.renderText(text, x, topY, z);
    }

    public static void renderHorizontallyCenteredText(TextRenderer textRenderer, String text, int left, int right, int topY, float z) {
        int x = getXForHorizontallyCenteredText(textRenderer, text, left, right);
        textRenderer.renderText(text, x, topY, z);
    }

    public static void renderCenteredText(String text, int top, int bottom, int left, int right, float z, int color) {
        int x = getXForHorizontallyCenteredText(text, left, right);
        int y = getYForVerticallyCenteredText(top, bottom);
        VanillaTextRenderer tr = TextRenderer.vanilla();
        tr.setFontHeight(9);
        tr.setTextColor(color);
        tr.renderText(text, x, y, z);
    }

    public static void renderCenteredText(TextRenderer textRenderer, String text, int top, int bottom, int left, int right, float z) {
        int x = getXForHorizontallyCenteredText(textRenderer, text, left, right);
        int y = getYForVerticallyCenteredText(textRenderer, top, bottom);
        textRenderer.renderText(text, x, y, z);
    }

    public static void renderVerticalText(String text, int x, int y, float z, int fontHeight, int color) {
        val fr = fontRenderer();
        float k = (float) fontHeight / fr.FONT_HEIGHT;
        RenderSystem.pushMatrix();
        RenderSystem.translatef(x + fontHeight, y, z);
        RenderSystem.rotatef(90F, 0F, 0F, 1F);
        RenderSystem.scalef(k, k, 1F);
        fr.drawString(text, 0, 0, color);
        RenderSystem.popMatrix();
    }

    public static void useGradientGLStates() {
        RenderSystem.disableTexture();
        RenderSystem.disableAlphaTest();
        RenderSystem.enableBlend();
        RenderSystem.shadeModel(GL_SMOOTH);
        RenderSystem.blendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
    }

    public static void useBlendingGLStates() {
        RenderSystem.disableTexture();
        RenderSystem.disableAlphaTest();
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
    }

    public static void usePlainColorGLStates() {
        RenderSystem.disableTexture();
        RenderSystem.disableBlend();
    }

    public static void useTextureGLStates() {
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
        RenderSystem.color3f(1F, 1F, 1F);
    }

    private static int translationX = 0;
    private static int translationY = 0;
    private static boolean active = false;

    public static int getTranslationX() {
        return active ? translationX : 0;
    }

    public static int getTranslationY() {
        return active ? translationY : 0;
    }

    public static int getPrevTranslationX() {
        return translationX;
    }

    public static int getPrevTranslationY() {
        return translationY;
    }

    public static void translate(int x, int y) {
        translationX = x;
        translationY = y;
        active = true;
    }

    public static void clearTranslation() {
        active = false;
    }

    public static final int LEFT_BORDER = 2;

    public static void renderSideLine(AbstractWidget widget) {
        RenderSystem.disableTexture();
        Tessellator.getInstance().getBuffer().begin(GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        int x1 = widget.getOuterAbsoluteX();
        int x2 = x1 + LEFT_BORDER;
        int y1 = widget.getAbsoluteY() - 1;
        int y2 = widget.getAbsoluteYBottom() + 1;
        coloredRect(x1, y1, x2, y2, 0xff797979);
        coloredRect(x1 + 1, y1, x2, y2, 0xffffffff);
        Tessellator.getInstance().draw();
        RenderSystem.enableTexture();
    }
}
