package vswe.stevesfactory.library.gui.widget;

import com.mojang.blaze3d.platform.GlStateManager;
import vswe.stevesfactory.library.gui.Render2D;
import vswe.stevesfactory.library.gui.TextRenderer;
import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.widget.mixin.LeafWidgetMixin;

import java.util.Collections;
import java.util.List;

public class Paragraph extends AbstractWidget implements LeafWidgetMixin {

    private List<String> texts;
    private List<String> textView;
    private boolean fitContents = false;

    private TextRenderer textRenderer = TextRenderer.newVanilla();

    public Paragraph(int width, int height, List<String> texts) {
        this.setDimensions(width, height);
        this.setBorders(1);
        this.texts = texts;
        this.textView = Collections.unmodifiableList(texts);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        RenderEventDispatcher.onPreRender(this, mouseX, mouseY);
        int x = getAbsoluteX();
        int y = getAbsoluteY();
        GlStateManager.enableTexture();
        textRenderer.setTextColor(0x000000);
        textRenderer.renderLines(textView, x, y, getZLevel());
        RenderEventDispatcher.onPostRender(this, mouseX, mouseY);
    }

    public boolean doesFitContents() {
        return fitContents;
    }

    public void setFitContents(boolean fitContents) {
        this.fitContents = fitContents;
    }

    public List<String> getTexts() {
        return textView;
    }

    public String getLine(int line) {
        return texts.get(line);
    }

    public void addLine(String newLine) {
        texts.add(newLine);
        tryExpand(newLine);
    }

    public void addLineSplit(String text) {
        addLineSplit(getWidth(), text);
    }

    public void addLineSplit(int maxWidth, String text) {
        int end = Render2D.fontRenderer().sizeStringToWidth(text, maxWidth);
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
            int w = textRenderer.calculateWidth(line);
            setWidth(Math.max(getWidth(), 1 + w + 1));
            setHeight((int) (1 + (textRenderer.getFontHeight() + 2) * texts.size() + 1));
        }
    }

    public TextRenderer getTextRenderer() {
        return textRenderer;
    }

    @SuppressWarnings("UnusedReturnValue")
    public Paragraph setTextRenderer(TextRenderer textRenderer) {
        this.textRenderer = textRenderer;
        return this;
    }
}
