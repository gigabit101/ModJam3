package vswe.stevesfactory.library.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.widget.mixin.LeafWidgetMixin;

import java.util.List;

import static vswe.stevesfactory.library.gui.RenderingHelper.fontHeight;
import static vswe.stevesfactory.library.gui.RenderingHelper.fontRenderer;

public class TextList extends AbstractWidget implements LeafWidgetMixin {

    private List<String> texts;
    private boolean fitContents = false;

    private int fontHeight = fontHeight();
    private float scaleFactor = (float) fontHeight / fontHeight();

    public TextList(int width, int height, List<String> texts) {
        super(0, 0, width, height);
        this.texts = texts;
    }

    @Override
    public void render(int mouseX, int mouseY, float particleTicks) {
        RenderEventDispatcher.onPreRender(this, mouseX, mouseY);
        int x = getAbsoluteX() + 1;
        int y = getAbsoluteY() + 1;
        RenderSystem.enableTexture();
        RenderSystem.pushMatrix();
        RenderSystem.translatef(x, y, 0F);
        RenderSystem.scalef(scaleFactor, scaleFactor, 1F);
        for (String text : texts) {
            fontRenderer().drawString(text, 0, 0, 0xff000000);
            RenderSystem.translatef(0F, fontHeight, 0F);
        }
        RenderSystem.popMatrix();
        RenderEventDispatcher.onPostRender(this, mouseX, mouseY);
    }

    public boolean doesFitContents() {
        return fitContents;
    }

    public void setFitContents(boolean fitContents) {
        this.fitContents = fitContents;
    }

    public List<String> getTexts() {
        return texts;
    }

    public void addLine(String newLine) {
        texts.add(newLine);
        tryExpand(newLine);
    }

    public void addLineSplit(String text) {
        addLineSplit((int) (getWidth() / scaleFactor), text);
    }

    public void addLineSplit(int maxWidth, String text) {
        int end = fontRenderer().sizeStringToWidth(text, maxWidth);
        if (end >= text.length()) {
            addLine(text);
        } else {
            String trimmed = text.substring(0, end);
            String after = text.substring(end).trim();
            addLine(trimmed);
            addLineSplit(maxWidth, after);
        }
    }

    private void tryExpand(String line) {
        if (fitContents) {
            int w = (int) (Minecraft.getInstance().fontRenderer.getStringWidth(line) * scaleFactor);
            setWidth(Math.max(getWidth(), 1 + w + 1));
            setHeight(1 + (fontHeight + 2) * texts.size() + 1);
        }
    }

    public int getFontHeight() {
        return fontHeight;
    }

    public void setFontHeight(int fontHeight) {
        this.fontHeight = fontHeight;
        this.scaleFactor = (float) fontHeight / fontHeight();
    }
}
