/* Code is adapted from McJtyLib
 * https://github.com/McJtyMods/McJtyLib/blob/1.12/src/main/java/mcjty/lib/gui/widgets/TextField.java
 */

package vswe.stevesfactory.library.gui.widget;

import com.mojang.blaze3d.platform.GlStateManager.LogicOp;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.val;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.math.MathHelper;
import vswe.stevesfactory.library.gui.Render2D;
import vswe.stevesfactory.library.gui.TextRenderer;
import vswe.stevesfactory.library.gui.contextmenu.CallbackEntry;
import vswe.stevesfactory.library.gui.contextmenu.ContextMenuBuilder;
import vswe.stevesfactory.library.gui.debug.ITextReceiver;
import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.widget.mixin.LeafWidgetMixin;
import vswe.stevesfactory.utils.Utils;

import static org.lwjgl.glfw.GLFW.*;
import static vswe.stevesfactory.library.gui.Render2D.*;

public class TextField extends AbstractWidget implements LeafWidgetMixin {

    public enum BackgroundStyle implements IBackgroundRenderer {
        NONE(0xff000000, 0xff333333, 0xff000000) {
            @Override
            public void render(int x1, int y1, int x2, int y2, float z, boolean hovered, boolean focused) {
            }
        },
        THICK_BEVELED(0xff000000, 0xff333333, 0xff000000) {
            @Override
            public void render(int x1, int y1, int x2, int y2, float z, boolean hovered, boolean focused) {
                int color = focused ? 0xffeeeeee
                        : hovered ? 0xffdadada
                        : 0xffc6c6c6;
                RenderSystem.disableTexture();
                beginColoredQuad();
                thickBeveledBox(x1, y1, x2, y2, z, 1, 0xff2b2b2b, 0xffffffff, color);
                draw();
                RenderSystem.enableTexture();
            }
        },
        BLACK_WHITE(0xffffffff, 0xffcccccc, 0xffffffff) {
            @Override
            public void render(int x1, int y1, int x2, int y2, float z, boolean hovered, boolean focused) {
                RenderSystem.disableTexture();
                beginColoredQuad();
                coloredRect(x1, y1, x2, y2, z, 0xffd0d0d0);
                coloredRect(x1 + 1, y1 + 1, x2 - 1, y2 - 1, z, 0xff000000);
                draw();
                RenderSystem.enableTexture();
            }
        },
        RED_OUTLINE(0xffffffff, 0xffcccccc, 0xffffffff) {
            @Override
            public void render(int x1, int y1, int x2, int y2, float z, boolean hovered, boolean focused) {
                RenderSystem.disableTexture();
                beginColoredQuad();
                if (focused) {
                    coloredRect(x1, y1, x2, y2, z, 0xffcf191f);
                    verticalGradientRect(x1 + 1, y1 + 1, x2 - 1, y2 - 1, z, 0xff191919, 0xff313131);
                } else {
                    coloredRect(x1, y1, x2, y2, z, 0xff6d0b0e);
                    coloredRect(x1 + 1, y1 + 1, x2 - 1, y2 - 1, z, 0xff1c1c1c);
                }
                draw();
                RenderSystem.enableTexture();
            }
        };

        public final int textColor;
        public final int textColorUneditable;
        public final int cursorColor;

        BackgroundStyle(int textColor, int textColorUneditable, int cursorColor) {
            this.textColor = textColor;
            this.textColorUneditable = textColorUneditable;
            this.cursorColor = cursorColor;
        }
    }

    private IBackgroundRenderer backgroundStyle = BackgroundStyle.BLACK_WHITE;
    private TextRenderer textRenderer = TextRenderer.newVanilla();
    private int textColor = 0xff000000;
    private int textColorUneditable = 0xff333333;

    private String text = "";
    private int cursor = 0;
    /**
     * Index of the first character that will be drawn.
     */
    private int startOffset = 0;
    /**
     * One end of the selected region. If nothing is selected, this should be -1.
     */
    private int selection = -1;
    private boolean editable = true;

    public TextField(int width, int height) {
        this.setDimensions(width, height);
        this.setBackgroundStyle(BackgroundStyle.BLACK_WHITE);
    }

    public TextField() {
        this.setBackgroundStyle(BackgroundStyle.BLACK_WHITE);
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
    public boolean onMouseClicked(double mouseX, double mouseY, int button) {
        if (isEnabled() && editable) {
            setFocused(true);
            return true;
        }
        return false;
    }

    @Override
    public boolean onKeyPressed(int keyCode, int scanCode, int modifiers) {
        boolean ctrl = Screen.hasControlDown();
        boolean alt = Screen.hasAltDown();
        if (alt) {
            return false;
        }
        if (ctrl) {
            switch (keyCode) {
                case GLFW_KEY_C: {
                    copyText();
                    break;
                }
                case GLFW_KEY_V: {
                    pasteText();
                    break;
                }
                case GLFW_KEY_X: {
                    cutText();
                    break;
                }
                case GLFW_KEY_A: {
                    selectAll();
                    break;
                }
                case GLFW_KEY_LEFT: {
                    updateSelection();
                    if (cursor > 0) {
                        cursor = findNextWord(true);
                    }
                    break;
                }
                case GLFW_KEY_RIGHT: {
                    updateSelection();
                    if (cursor < text.length()) {
                        cursor = findNextWord(false);
                    }
                    break;
                }
            }
        } else {
            switch (keyCode) {
                case GLFW_KEY_ESCAPE: {
                    setFocused(false);
                    break;
                }
                case GLFW_KEY_ENTER:
                case GLFW_KEY_DOWN:
                case GLFW_KEY_UP:
                case GLFW_KEY_TAB: {
                    return false;
                }
                case GLFW_KEY_HOME: {
                    updateSelection();
                    cursor = 0;
                    break;
                }
                case GLFW_KEY_END: {
                    updateSelection();
                    cursor = text.length();
                    break;
                }
                case GLFW_KEY_LEFT: {
                    updateSelection();
                    if (cursor > 0) {
                        cursor--;
                    }
                    break;
                }
                case GLFW_KEY_RIGHT: {
                    updateSelection();
                    if (cursor < text.length()) {
                        cursor++;
                    }
                    break;
                }
                case GLFW_KEY_BACKSPACE: {
                    if (isRegionSelected()) {
                        replaceSelectedRegion("");
                    } else if (!text.isEmpty() && cursor > 0) {
                        if (removeTextAt(cursor - 1, cursor)) {
                            cursor--;
                        }
                    }
                    break;
                }
                case GLFW_KEY_DELETE: {
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
    public boolean onCharTyped(char typedChar, int keyCode) {
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
            Render2D.minecraft().keyboardListener.setClipboardString(getSelectedText());
        }
    }

    private void pasteText() {
        String text = Render2D.minecraft().keyboardListener.getClipboardString();
        if (isRegionSelected()) {
            replaceSelectedRegion(text);
        } else {
            insertTextAtCursor(text);
        }
    }

    private void cutText() {
        if (isRegionSelected()) {
            Render2D.minecraft().keyboardListener.setClipboardString(getSelectedText());
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
        return (getDimensions().height - Render2D.fontRenderer().FONT_HEIGHT) / 2;
    }

    private void ensureVisible() {
        if (cursor < startOffset) {
            startOffset = cursor;
        } else {
            int w = textRenderer.calculateWidth(text.substring(startOffset, cursor));
            while (w > getDimensions().width - 12) {
                startOffset++;
                w = textRenderer.calculateWidth(text.substring(startOffset, cursor));
            }
        }
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        RenderEventDispatcher.onPreRender(this, mouseX, mouseY);

        ensureVisible();
        int x = getAbsoluteX();
        int y = getAbsoluteY();
        int x2 = getAbsoluteXRight();
        int y2 = getAbsoluteYBottom();

        backgroundStyle.render(x, y, x2, y2, getZLevel(), isInside(mouseX, mouseY), isFocused());

        String renderedText = textRenderer.trimToWidth(text.substring(startOffset), getWidth() - 4);
        int textX = x + 2;
        int textY = y + calculateVerticalOffset();
        if (isEnabled()) {
            textRenderer.setTextColor(isEditable() ? textColor : textColorUneditable);
            textRenderer.renderText(renderedText, textX, textY, getZLevel());

            if (isRegionSelected()) {
                int selectionStart = getSelectionStart();
                int selectionEnd = getSelectionEnd();
                int renderedStart = MathHelper.clamp(selectionStart - startOffset, 0, renderedText.length());
                int renderedEnd = MathHelper.clamp(selectionEnd - startOffset, 0, renderedText.length());

                String renderedSelection = renderedText.substring(renderedStart, renderedEnd);
                String renderedPreSelection = renderedText.substring(0, renderedStart);
                int selectionX = textX + textRenderer.calculateWidth(renderedPreSelection);
                int selectionWidth = textRenderer.calculateWidth(renderedSelection);

                RenderSystem.disableTexture();
                RenderSystem.logicOp(LogicOp.OR_REVERSE);
                beginColoredQuad();
                coloredRect(selectionX, textY, selectionX + selectionWidth, textY + (int) textRenderer.getFontHeight(), getZLevel(), 0xff3c93f2);
                draw();
                RenderSystem.enableTexture();
            }
        } else {
            textRenderer.setTextColor(0xffa0a0a0);
            textRenderer.renderText(renderedText, textX, textY, getZLevel());
        }

        if (isFocused()) {
            int w = textRenderer.calculateWidth(text.substring(startOffset, cursor));
            int cx = x + 2 + w;
            RenderSystem.disableTexture();
            beginColoredQuad();
            coloredRect(cx, y + 2, cx + 1, y2 - 3, getZLevel(), getCursorColor());
            draw();
            RenderSystem.enableTexture();
        }

        RenderEventDispatcher.onPostRender(this, mouseX, mouseY);
    }

    public int getCursorColor() {
        if (backgroundStyle instanceof BackgroundStyle) {
            return ((BackgroundStyle) backgroundStyle).cursorColor;
        }
        return 0xff000000;
    }

    /**
     * Try to match a word by that is surrounded by either whitespace or ends of the text.
     *
     * @param reversed If {@code true}, when it will search towards left, otherwise towards right.
     * @return Index, either end or beginning of a word. When {@code reversed}, it will return the beginning and otherwise the end.
     */
    private int findNextWord(boolean reversed) {
        int change = reversed ? -1 : 1;
        int i = cursor;
        char last = ' ';
        while (true) {
            i += change;
            if (i < 0 || i >= text.length()) {
                break;
            }

            char c = text.charAt(i);
            if (c == ' ' && last != ' ') {
                break;
            }
            last = c;
        }

        if (reversed) {
            return i - change;
        }
        return i;
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

    public TextRenderer getTextRenderer() {
        return textRenderer;
    }

    @SuppressWarnings("UnusedReturnValue")
    public TextField setTextRenderer(TextRenderer textRenderer) {
        this.textRenderer = textRenderer;
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
        receiver.line("Editable=${editable}");
        receiver.line("Text=${text}");
        receiver.line("StartOffset=${startOffset}");
        receiver.line("Cursor=${cursor}");
        receiver.line("SelectionStart=${getSelectionStart()}");
        receiver.line("SelectionEnd=${getSelectionEnd()}");
        receiver.line("SelectedText=${getSelectedTextSafe()}");
    }

    @Override
    protected void buildContextMenu(ContextMenuBuilder builder) {
        val section = builder.obtainSection("Misc");
        section.addChildren(new CallbackEntry(null, "gui.sfm.clear", b -> setText("")));
        super.buildContextMenu(builder);
    }
}
