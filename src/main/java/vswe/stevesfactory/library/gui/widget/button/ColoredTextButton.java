package vswe.stevesfactory.library.gui.widget.button;

import net.minecraft.client.resources.I18n;
import net.minecraft.util.IStringSerializable;
import vswe.stevesfactory.library.gui.Render2D;
import vswe.stevesfactory.library.gui.debug.ITextReceiver;
import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.widget.AbstractWidget;
import vswe.stevesfactory.library.gui.widget.mixin.LeafWidgetMixin;

import java.util.function.IntConsumer;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static vswe.stevesfactory.library.gui.Render2D.*;

public class ColoredTextButton extends AbstractWidget implements IButton, LeafWidgetMixin, IStringSerializable {

    public static ColoredTextButton of(String text) {
        ColoredTextButton button = new ColoredTextButton();
        button.setText(text);
        button.expandToTextWidth();
        return button;
    }

    public static ColoredTextButton of(String text, IntConsumer action) {
        ColoredTextButton button = new ColoredTextButton();
        button.setText(text);
        button.expandToTextWidth();
        button.onClick = action;
        return button;
    }

    private IntConsumer onClick = DUMMY;
    private String text;

    private boolean hovered = false;
    private boolean clicked = false;

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        RenderEventDispatcher.onPreRender(this, mouseX, mouseY);

        int x1 = getAbsoluteX();
        int y1 = getAbsoluteY();
        int x2 = getAbsoluteXRight();
        int y2 = getAbsoluteYBottom();
        boolean hovered = isInside(mouseX, mouseY);

        usePlainColorGLStates();
        beginColoredQuad();
        coloredRect(x1, y1, x2, y2, getZLevel(), hovered ? getHoveredBorderColor() : getNormalBorderColor());
        coloredRect(x1 + 1, y1 + 1, x2 - 1, y2 - 1, getZLevel(), hovered ? getHoveredBackgroundColor() : getNormalBackgroundColor());
        draw();
        useTextureGLStates();

        renderText();

        RenderEventDispatcher.onPostRender(this, mouseX, mouseY);
    }

    public int getNormalBorderColor() {
        return 0xff737373;
    }

    public int getHoveredBorderColor() {
        return 0xffc9c9c9;
    }

    public int getNormalBackgroundColor() {
        return 0xff8c8c8c;
    }

    public int getHoveredBackgroundColor() {
        return 0xff8c8c8c;
    }

    public int getTextColor() {
        return 0xffffffff;
    }

    protected void renderText() {
        Render2D.renderCenteredText(getText(), getAbsoluteY(), getAbsoluteYBottom(), getAbsoluteX(), getAbsoluteXRight(), getZLevel(), getTextColor());
    }

    @Override
    public boolean onMouseClicked(double mouseX, double mouseY, int button) {
        if (button != GLFW_MOUSE_BUTTON_LEFT) {
            return false;
        }
        clicked = true;
        onClick.accept(button);
        return true;
    }

    @Override
    public boolean onMouseReleased(double mouseX, double mouseY, int button) {
        clicked = false;
        return true;
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        hovered = isInside(mouseX, mouseY);
        if (!hovered) {
            clicked = false;
        }
    }

    public void expandToTextWidth() {
        setWidth(Math.max(getFullWidth(), 4 + fontRenderer().getStringWidth(text) + 4));
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
        setDimensions(fontRenderer().getStringWidth(text), 3 + fontHeight() + 2);
    }

    public void setTextRaw(String text) {
        this.text = text;
    }

    public void translate(String translationKey) {
        setText(I18n.format(translationKey));
    }

    public void translate(String translationKey, Object... args) {
        setText(I18n.format(translationKey, args));
    }

    public void translateRaw(String translationKey) {
        setTextRaw(I18n.format(translationKey));
    }

    public void translateRaw(String translationKey, Object... args) {
        setTextRaw(I18n.format(translationKey, args));
    }

    @Override
    public boolean hasClickAction() {
        return onClick != DUMMY;
    }

    @Override
    public IntConsumer getClickAction() {
        return onClick;
    }

    @Override
    public void setClickAction(IntConsumer action) {
        onClick = action;
    }

    @Override
    public boolean isHovered() {
        return hovered;
    }

    @Override
    public boolean isClicked() {
        return clicked;
    }

    @Override
    public String getName() {
        return text;
    }

    @Override
    public void provideInformation(ITextReceiver receiver) {
        super.provideInformation(receiver);
        receiver.line("Hovered=${hovered}");
        receiver.line("Clicked=${clicked}");
    }
}
