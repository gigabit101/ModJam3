package vswe.stevesfactory.library.gui;

import java.util.List;

public interface TextRenderer {

    static final VanillaTextRenderer VANILLA_TEXT_RENDERER = new VanillaTextRenderer();

    public static VanillaTextRenderer vanilla() {
        return VANILLA_TEXT_RENDERER;
    }

    public static VanillaTextRenderer newVanilla() {
        return new VanillaTextRenderer();
    }

    void renderText(String text, int x, int y, float z);

    void renderLines(List<String> text, int x, int y, float z);

    int calculateWidth(String text);

    String trimToWidth(String text, int width);

    float getFontHeight();

    void setFontHeight(float fontHeight);

    int getTextColor();

    void setTextColor(int textColor);

    void useItalics(boolean italics);

    void useBold(boolean bold);

    void useStrikeThrough(boolean strikeThrough);
}
