package vswe.stevesfactory.library.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;

import java.util.List;

public class VanillaTextRenderer implements TextRenderer {

    private float fontHeight = getDefaultFontHeight();
    private float scaleFactor = 1F;
    private int textColor = 0xffffff;

    VanillaTextRenderer() {
    }

    @Override
    public void renderText(String text, int x, int y, float z) {
        RenderSystem.pushMatrix();
        RenderSystem.translatef(x, y, z + 0.1F);
        RenderSystem.scalef(scaleFactor, scaleFactor, 1F);
        Minecraft.getInstance().fontRenderer.drawString(text, 0, 0, textColor);
        RenderSystem.popMatrix();
    }

    @Override
    public void renderLines(List<String> text, int x, int y, float z) {
        RenderSystem.pushMatrix();
        RenderSystem.translatef(x, y, z + 0.1F);
        RenderSystem.scalef(scaleFactor, scaleFactor, 1F);
        for (String line : text) {
            Minecraft.getInstance().fontRenderer.drawString(line, 0, 0, textColor);
            RenderSystem.translatef(0F, fontHeight, 0F);
        }
        RenderSystem.popMatrix();
    }

    @Override
    public int calculateWidth(String text) {
        return (int) (Minecraft.getInstance().fontRenderer.getStringWidth(text) * scaleFactor);
    }

    @Override
    public String trimToWidth(String text, int width) {
        return Minecraft.getInstance().fontRenderer.trimStringToWidth(text, (int) (width * getInverseScaleFactor()));
    }

    @Override
    public float getFontHeight() {
        return fontHeight;
    }

    @Override
    public void setFontHeight(float fontHeight) {
        this.fontHeight = fontHeight;
        this.scaleFactor = fontHeight / getDefaultFontHeight();
    }

    public float getScaleFactor() {
        return scaleFactor;
    }

    public float getInverseScaleFactor() {
        return 1F / scaleFactor;
    }

    public void useDefaultFontHeight() {
        setFontHeight(getDefaultFontHeight());
    }

    public float getDefaultFontHeight() {
        return Minecraft.getInstance().fontRenderer.FONT_HEIGHT;
    }

    @Override
    public int getTextColor() {
        return textColor;
    }

    @Override
    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public void useDefaultTextColor() {
        this.textColor = 0xffffff;
    }

    // TODO
    @Override
    public void useItalics(boolean italics) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void useBold(boolean bold) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void useStrikeThrough(boolean strikeThrough) {
        throw new UnsupportedOperationException();
    }
}
