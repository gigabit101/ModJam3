/* Code adapted from McJtyLib mcjty.lib.gui.widgets.Slider
 */

package vswe.stevesfactory.library.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.math.MathHelper;
import vswe.stevesfactory.library.gui.debug.ITextReceiver;
import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.widget.mixin.LeafWidgetMixin;
import vswe.stevesfactory.utils.Utils;

import java.util.function.IntConsumer;

import static org.lwjgl.opengl.GL11.*;
import static vswe.stevesfactory.library.gui.Render2D.*;

public class Slider extends AbstractWidget implements LeafWidgetMixin {

    public static final int TOP_LEFT_COLOR = 0xff2b2b2b;
    public static final int BOTTOM_RIGHT_COLOR = 0xffffffff;
    public static final int FILL_COLOR = 0xffc6c6c6;
    public static final int MARKER_LINE_COLOR = 0xff4e4e4e;

    public static final int DEFAULT_KNOB_SIZE = 20;
    public static final int MINIMUM_KNOB_SIZE = 4;
    public static final int LINE_WIDTH = 1;

    private boolean horizontal = false;
    private int min = -1;
    private int max = -1;
    private int value = -1;
    private int knobSize = DEFAULT_KNOB_SIZE;
    public IntConsumer onValueChanged = i -> {
    };

    private int offset = 0;
    private boolean hovered = false;
    private boolean dragging = false;
    private int initialDragOffset = -1;

    public Slider(int width, int height) {
        this.setDimensions(width, height);
        this.setValueRange(0, 20);
    }

    public boolean isHorizontal() {
        return horizontal;
    }

    public boolean isVertical() {
        return !horizontal;
    }

    public Slider setMinimumKnobSize(int m) {
        return this;
    }

    public Slider setHorizontal() {
        this.horizontal = true;
        return this;
    }

    public Slider setVertical() {
        this.horizontal = false;
        return this;
    }

    public int getMinValue() {
        return min;
    }

    public int getMaxValue() {
        return max;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = MathHelper.clamp(value, min, max);
        onValueChanged.accept(this.value);
    }

    public void setValueRange(int min, int max) {
        this.min = Math.min(min, max);
        this.max = Math.max(min, max);
        value = this.min;
    }

    public void setKnobSize(int size) {
        this.knobSize = Utils.lowerBound(size, MINIMUM_KNOB_SIZE);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        if (!isEnabled()) {
            return;
        }
        RenderEventDispatcher.onPreRender(this, mouseX, mouseY);
        RenderSystem.disableTexture();
        beginColoredQuad();

        int x1 = getAbsoluteX();
        int y1 = getAbsoluteY();
        int x2 = getAbsoluteXRight();
        int y2 = getAbsoluteYBottom();

        coloredRect(x1, y1, x2, y2, getZLevel(), 0xff000000);
        thickBeveledBox(x1, y1, x2, y2, getZLevel(), 1, TOP_LEFT_COLOR, BOTTOM_RIGHT_COLOR, FILL_COLOR);

        int topLeft = dragging ? 0xff5c669d
                : hovered ? 0xffa5aac5
                : 0xffeeeeee;
        int bottomRight = dragging ? 0xffbcc5ff
                : hovered ? 0xff777c99
                : 0xff333333;
        int fill = dragging ? 0xff7f89bf
                : hovered ? 0xff858aa5
                : 0xff8b8b8b;
        if (horizontal) {
            int left = x1 + 1 + offset;
            thickBeveledBox(left, y1 + 2, left + knobSize - 1, y2 - 2, getZLevel(), 1, topLeft, bottomRight, fill);
            draw();

            if (knobSize >= 8) {
                color(MARKER_LINE_COLOR);
                glLineWidth(LINE_WIDTH);
                glBegin(GL_LINES);
                int mx = left + knobSize / 2 - 1;
                int my1 = y1 + 4;
                int my2 = y2 - 4;
                verticalLine(mx, my1, my2, getZLevel());
                if (knobSize >= 10) {
                    verticalLine(mx - 2, my1, my2, getZLevel());
                    verticalLine(mx + 2, my1, my2, getZLevel());
                }
                glEnd();
            }
        } else {
            int top = y1 + 1 + offset;
            thickBeveledBox(x1 + 1, top, x2 - 1, top + knobSize - 1, getZLevel(), 1, topLeft, bottomRight, fill);
            draw();

            if (knobSize >= 8) {
                color(MARKER_LINE_COLOR);
                glLineWidth(LINE_WIDTH);
                glBegin(GL_LINES);
                int mx1 = x1 + 3;
                int mx2 = x2 - 3;
                int my = top + knobSize / 2 - 1;
                horizontalLine(mx1, mx2, my, getZLevel());
                if (knobSize >= 10) {
                    horizontalLine(mx1, mx2, my + 2, getZLevel());
                }
                glEnd();
            }
        }
        RenderSystem.color3f(1F, 1F, 1F);
        RenderSystem.enableTexture();
        RenderEventDispatcher.onPostRender(this, mouseX, mouseY);
    }

    @Override
    public boolean onMouseScrolled(double mouseX, double mouseY, double scroll) {
        int amount = Screen.hasControlDown()
                ? (int) -scroll
                : (int) -scroll * 5;
        if (horizontal) {
            offset = MathHelper.clamp(offset + amount, 0, getUsableWidth() - knobSize);
        } else {
            offset = MathHelper.clamp(offset + amount, 0, getUsableHeight() - knobSize);
        }
        updateValue();
        return true;
    }

    @Override
    public boolean onMouseClicked(double mouseX, double mouseY, int button) {
        dragging = true;
        initialDragOffset = (int) (horizontal
                ? mouseX - getAbsoluteX() - offset
                : mouseY - getAbsoluteY() - offset);
        setFocused(true);
        return true;
    }

    @Override
    public boolean onMouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragging) {
            if (horizontal) {
                offset = MathHelper.clamp((int) mouseX - getAbsoluteX() - initialDragOffset, 0, getUsableWidth() - knobSize);
            } else {
                offset = MathHelper.clamp((int) mouseY - getAbsoluteY() - initialDragOffset, 0, getUsableHeight() - knobSize);
            }
            updateValue();
        }
        return true;
    }

    // Use the event without mouseover check so that even if the mouse left the slider, dragging is still getting updated
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        boolean initialState = dragging;
        dragging = false;
        initialDragOffset = -1;
        setFocused(false);
        return initialState;
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        hovered = isInside(mouseX, mouseY);
    }

    private void updateValue() {
        float factor = (float) offset / (getUsableBoundSize() - knobSize);
        value = (int) Utils.map(factor, 0F, 1F, min, max);
        onValueChanged.accept(value);
    }

    public int getUsableBoundSize() {
        return horizontal ? getUsableWidth() : getUsableHeight();
    }

    public int getUsableWidth() {
        return getWidth() - 2;
    }

    public int getUsableHeight() {
        return getHeight() - 2;
    }

    @Override
    public void provideInformation(ITextReceiver receiver) {
        super.provideInformation(receiver);
        receiver.line("Type=" + (horizontal ? "horizontal" : "vertical"));
        receiver.line("Dragging=" + dragging);
        receiver.line("Hovered=" + hovered);
        receiver.line("Offset=" + offset);
        receiver.line("Value=" + value);
    }
}
