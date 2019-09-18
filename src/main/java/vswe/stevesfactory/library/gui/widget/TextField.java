/* Code is adapted from McJtyLib
 * https://github.com/McJtyMods/McJtyLib/blob/1.12/src/main/java/mcjty/lib/gui/widgets/TextField.java
 */

package vswe.stevesfactory.library.gui.widget;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;
import vswe.stevesfactory.library.gui.debug.ITextReceiver;
import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.widget.mixin.LeafWidgetMixin;
import vswe.stevesfactory.utils.RenderingHelper;
import vswe.stevesfactory.utils.Utils;

import java.awt.*;

public class TextField extends AbstractWidget implements LeafWidgetMixin {

    public static TextField DUMMY = new TextField(0, 0, 0, 0) {
        @Override
        public boolean isEditable() {
            return false;
        }

        @Override
        public void render(int mouseX, int mouseY, float particleTicks) {
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            return false;
        }

        @Override
        public boolean charTyped(char typedChar, int keyCode) {
            return false;
        }

        @Override
        public void setX(int x) {
        }

        @Override
        public void setY(int y) {
        }

        @Override
        public void onParentPositionChanged() {
        }

        @Override
        public boolean isFocused() {
            return false;
        }
    };

    public enum BackgroundStyle implements IBackgroundRenderer {
        NONE(0xff000000, 0xff333333) {
            @Override
            public void render(int x1, int y1, int x2, int y2, boolean hovered, boolean focused) {
            }
        },
        THICK_BEVELED(0xff000000, 0xff333333) {
            @Override
            public void render(int x1, int y1, int x2, int y2, boolean hovered, boolean focused) {
                int color = focused ? 0xffeeeeee
                        : hovered ? 0xffdadada
                        : 0xffc6c6c6;
                RenderingHelper.drawThickBeveledBox(x1, y1, x2 - 1, y2 - 1, 1, 0xff2b2b2b, 0xffffffff, color);
            }
        },
        RED_OUTLINE(0xffffffff, 0xffcccccc) {
            @Override
            public void render(int x1, int y1, int x2, int y2, boolean hovered, boolean focused) {
                if (focused) {
                    RenderingHelper.drawRect(x1, y1, x2, y2, 0xffcf191f);
                    RenderingHelper.drawVerticalGradientRect(x1 + 1, y1 + 1, x2 - 1, y2 - 1, 0xff191919, 0xff313131);
                } else {
                    RenderingHelper.drawRect(x1, y1, x2, y2, 0xff6d0b0e);
                    RenderingHelper.drawRect(x1 + 1, y1 + 1, x2 - 1, y2 - 1, 0xff1c1c1c);
                }
            }
        };

        public final int textColor;
        public final int textColorUneditable;

        BackgroundStyle(int textColor, int textColorUneditable) {
            this.textColor = textColor;
            this.textColorUneditable = textColorUneditable;
        }
    }

    private IBackgroundRenderer backgroundStyle = BackgroundStyle.THICK_BEVELED;

    private String text = "";
    private int cursor = 0;
    /**
     * Index of the first character that will be drawn.
     */
    private int startOffset = 0;
    /**
     * One end of the selected region. If nothing is selected, is should be -1.
     */
    private int selection = -1;
    private boolean editable = true;

    private int textColor = 0xff000000;
    private int textColorUneditable = 0xff333333;
    private int fontHeight = fontRenderer().FONT_HEIGHT;
    private float scaleFactor = 1.0F;

    public TextField(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    public TextField(Point location, Dimension dimensions) {
        super(location, dimensions);
    }

    public boolean isEditable() {
        return editable;
    }

    @SuppressWarnings("UnusedReturnValue")
    public TextField setEditable(boolean editable) {
        this.editable = editable;
        return this;
    }

    public String getText() {
        return text;
    }

    @SuppressWarnings("UnusedReturnValue")
    public TextField setText(String text) {
        updateText(text);
        cursor = text.length();
        if (startOffset >= cursor) {
            startOffset = Utils.lowerBound(cursor - 1, 0);
        }
        return this;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isEnabled() && editable) {
            getWindow().setFocusedWidget(this);
            if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                setText("");
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (Screen.hasControlDown() && !Screen.hasShiftDown() && !Screen.hasAltDown()) {
            switch (keyCode) {
                case GLFW.GLFW_KEY_C: {
                    copyText();
                    break;
                }
                case GLFW.GLFW_KEY_V: {
                    pasteText();
                    break;
                }
                case GLFW.GLFW_KEY_X: {
                    cutText();
                    break;
                }
                case GLFW.GLFW_KEY_A: {
                    selectAll();
                    break;
                }
            }
        } else {
            switch (keyCode) {
                case GLFW.GLFW_KEY_ESCAPE:
                    getWindow().changeFocus(this, false);
                case GLFW.GLFW_KEY_ENTER:
                case GLFW.GLFW_KEY_DOWN:
                case GLFW.GLFW_KEY_UP:
                case GLFW.GLFW_KEY_TAB: {
                    return false;
                }
                case GLFW.GLFW_KEY_HOME: {
                    updateSelection();
                    cursor = 0;
                    break;
                }
                case GLFW.GLFW_KEY_END: {
                    updateSelection();
                    cursor = text.length();
                    break;
                }
                case GLFW.GLFW_KEY_LEFT: {
                    updateSelection();
                    if (cursor > 0) {
                        cursor--;
                    }
                    break;
                }
                case GLFW.GLFW_KEY_RIGHT: {
                    updateSelection();
                    if (cursor < text.length()) {
                        cursor++;
                    }
                    break;
                }
                case GLFW.GLFW_KEY_BACKSPACE: {
                    if (isRegionSelected()) {
                        replaceSelectedRegion("");
                    } else if (!text.isEmpty() && cursor > 0) {
                        if (removeTextAt(cursor - 1, cursor)) {
                            cursor--;
                        }
                    }
                    break;
                }
                case GLFW.GLFW_KEY_DELETE: {
                    if (isRegionSelected()) {
                        replaceSelectedRegion("");
                    } else if (cursor < text.length()) {
                        removeTextAt(cursor, cursor + 1);
                    }
                    break;
                }
            }
        }
        return true;
    }

    @Override
    public boolean charTyped(char typedChar, int keyCode) {
        // e.g. F1~12, insert
        // Char code of 0 will appear to be nothing
        if ((int) typedChar != 0) {
            String replacement = String.valueOf(typedChar);
            if (isRegionSelected()) {
                replaceSelectedRegion(replacement);
            } else {
                insertTextAtCursor(replacement);
            }
            return true;
        }
        return false;
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean insertTextAtCursor(String in) {
        if (insertTextAt(cursor, in)) {
            cursor += in.length();
            return true;
        }
        return false;
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean insertTextAt(int index, String in) {
        return updateText(text.substring(0, index) + in + text.substring(index));
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean removeTextAtCursor(int length) {
        int a = cursor + length;
        int b = cursor;
        if (removeTextAt(Math.min(a, b), Math.max(a, b))) {
            cursor -= length;
            return true;
        }
        return false;
    }

    /**
     * Remove text in range of {@code [start, end)}.
     *
     * @param start Inclusive index
     * @param end   Exclusive index
     */
    @SuppressWarnings("UnusedReturnValue")
    public boolean removeTextAt(int start, int end) {
        return updateText(text.substring(0, start) + text.substring(end));
    }

    /**
     * Update the text reference for internal usages. This method is meant to be overridden for validation purposes.
     */
    @SuppressWarnings("UnusedReturnValue")
    protected boolean updateText(String text) {
        this.text = text;
        return true;
    }

    private void copyText() {
        if (isRegionSelected()) {
            minecraft().keyboardListener.setClipboardString(getSelectedText());
        }
    }

    private void pasteText() {
        String text = minecraft().keyboardListener.getClipboardString();
        if (isRegionSelected()) {
            replaceSelectedRegion(text);
        } else {
            insertTextAtCursor(text);
        }
    }

    private void cutText() {
        if (isRegionSelected()) {
            minecraft().keyboardListener.setClipboardString(getSelectedText());
            replaceSelectedRegion("");
        }
    }

    public int getCursor() {
        return cursor;
    }

    protected void setCursor(int cursor) {
        this.cursor = MathHelper.clamp(cursor, 0, text.length());
    }

    @SuppressWarnings("UnusedReturnValue")
    public TextField scrollToFront() {
        cursor = 0;
        startOffset = 0;
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    public TextField selectAll() {
        return setSelection(0, text.length());
    }

    @SuppressWarnings("UnusedReturnValue")
    public TextField setSelection(int start, int end) {
        selection = start;
        cursor = end;
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    public TextField clearSelection() {
        selection = -1;
        return this;
    }

    public boolean isRegionSelected() {
        return selection != -1;
    }

    /**
     * Inclusive text index indicating start of the selected region. If nothing is selected, it will return -1.
     */
    public int getSelectionStart() {
        return Math.min(cursor, selection);
    }

    /**
     * Exclusive text index indicating end of the selecred region, If nothing is selected, it will return {@link #cursor}.
     */
    public int getSelectionEnd() {
        return Math.max(cursor, selection);
    }

    public String getSelectedText() {
        return text.substring(getSelectionStart(), getSelectionEnd());
    }

    @SuppressWarnings("UnusedReturnValue")
    public TextField replaceSelectedRegion(String replacement) {
        int selectionStart = getSelectionStart();
        if (updateText(text.substring(0, selectionStart) + replacement + text.substring(getSelectionEnd()))) {
            cursor = selectionStart + replacement.length();
        }
        clearSelection();
        return this;
    }

    private void updateSelection() {
        if (Screen.hasShiftDown()) {
            // Don't clear selection as long as shift is pressed
            if (!isRegionSelected()) {
                selection = cursor;
            }
        } else {
            clearSelection();
        }
    }

    private int calculateVerticalOffset() {
        return (getDimensions().height - fontHeight) / 2;
    }

    private void ensureVisible() {
        if (cursor < startOffset) {
            startOffset = cursor;
        } else {
            int w = fontRenderer().getStringWidth(text.substring(startOffset, cursor));
            while (w > getDimensions().width - 12) {
                startOffset++;
                w = fontRenderer().getStringWidth(text.substring(startOffset, cursor));
            }
        }
    }

    public int getFontHeight() {
        return fontHeight;
    }

    @SuppressWarnings("UnusedReturnValue")
    public TextField setFontHeight(int fontHeight) {
        this.fontHeight = fontHeight;
        this.scaleFactor = (float) fontHeight / getDefaultFontHeight();
        return this;
    }

    public int getDefaultFontHeight() {
        return fontRenderer().FONT_HEIGHT;
    }

    @Override
    public void render(int mouseX, int mouseY, float particleTicks) {
        RenderEventDispatcher.onPreRender(this, mouseX, mouseY);

        ensureVisible();

        int x = getAbsoluteX();
        int y = getAbsoluteY();
        int x2 = getAbsoluteXRight();
        int y2 = getAbsoluteYBottom();

        backgroundStyle.render(x, y, x2, y2, isInside(mouseX, mouseY), isFocused());

        int width = (int) ((getDimensions().width - 4) * (1F / scaleFactor));
        String renderedText = fontRenderer().trimStringToWidth(this.text.substring(startOffset), width);
        int textX = x + 2;
        int textY = y + calculateVerticalOffset();
        GlStateManager.enableTexture();
        if (isEnabled()) {
            if (isEditable()) {
                drawString(renderedText, textX, textY, textColor);
            } else {
                drawString(renderedText, textX, textY, textColor);
            }

            if (isRegionSelected()) {
                int selectionStart = getSelectionStart();
                int selectionEnd = getSelectionEnd();

                // Text: abcdefghijklmn
                // Rendered: abcdefg, length=7
                //                 ^6
                int renderedStart = MathHelper.clamp(selectionStart - startOffset, 0, renderedText.length());
                int renderedEnd = MathHelper.clamp(selectionEnd - startOffset, 0, renderedText.length());

                String renderedSelection = renderedText.substring(renderedStart, renderedEnd);
                String renderedPreSelection = renderedText.substring(0, renderedStart);
                int selectionX = textX + fontRenderer().getStringWidth(renderedPreSelection);
                int selectionWidth = fontRenderer().getStringWidth(renderedSelection);
                RenderingHelper.drawColorLogic(selectionX - 1, textY, selectionWidth + 1, fontRenderer().FONT_HEIGHT, 60, 147, 242, GlStateManager.LogicOp.OR_REVERSE);
            }
        } else {
            drawString(renderedText, textX, textY, 0xffa0a0a0);
        }

        if (isFocused()) {
            int w = (int) (fontRenderer().getStringWidth(text.substring(startOffset, cursor)) * scaleFactor);
            int cx = x + 2 + w;
            RenderingHelper.drawRect(cx, y + 2, cx + 1, y2 - 3, 0xff000000);
            GlStateManager.enableTexture();
        }

        RenderEventDispatcher.onPostRender(this, mouseX, mouseY);
    }

    private void drawString(String text, int textX, int textY, int color) {
        GlStateManager.pushMatrix();
        GlStateManager.translatef(textX, textY, 0F);
        GlStateManager.scalef(scaleFactor, scaleFactor, 1F);
        fontRenderer().drawString(text, 0, 0, color);
        GlStateManager.popMatrix();
    }

    public IBackgroundRenderer getBackgroundStyle() {
        return backgroundStyle;
    }

    @SuppressWarnings("UnusedReturnValue")
    public TextField setBackgroundStyle(IBackgroundRenderer backgroundStyle) {
        this.backgroundStyle = backgroundStyle;
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    public TextField setBackgroundStyle(BackgroundStyle backgroundStyle) {
        this.backgroundStyle = backgroundStyle;
        this.setTextColor(backgroundStyle.textColor, backgroundStyle.textColorUneditable);
        return this;
    }

    public int getTextColor() {
        return textColor;
    }

    public int getTextColorUneditable() {
        return textColorUneditable;
    }

    @SuppressWarnings("UnusedReturnValue")
    public TextField setTextColor(int textColor, int textColorUneditable) {
        this.textColor = textColor;
        this.textColorUneditable = textColorUneditable;
        return this;
    }

    private String getSelectedTextSafe() {
        if (isRegionSelected()) {
            return getSelectedText();
        }
        return "";
    }

    @Override
    public void provideInformation(ITextReceiver receiver) {
        super.provideInformation(receiver);
        receiver.line("Editable=" + editable);
        receiver.line("Text=" + text);
        receiver.line("StartOffset=" + startOffset);
        receiver.line("Cursor=" + cursor);
        receiver.line("SelectionStart=" + getSelectionStart());
        receiver.line("SelectionEnd=" + getSelectionEnd());
        receiver.line("SelectedText=" + getSelectedTextSafe());
    }
}
