package vswe.stevesfactory.ui.manager.editor;

import org.lwjgl.glfw.GLFW;
import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.widget.AbstractWidget;
import vswe.stevesfactory.library.gui.widget.mixin.LeafWidgetMixin;
import vswe.stevesfactory.library.gui.window.Dialog;

class OffsetText extends AbstractWidget implements LeafWidgetMixin {

    private final String prefix;
    private String text = "";
    private float value;

    public OffsetText(String prefix, int xRight, int y) {
        super(xRight, y, 0, fontRenderer().FONT_HEIGHT);
        this.prefix = prefix;
        set(0F);
    }

    public String getText() {
        return text;
    }

    public float get() {
        return value;
    }

    public void set(float value) {
        this.value = value;
        this.text = prefix + Math.round(value * 10) / 10F;

        int oldWidth = getWidth();
        int newWidth = fontRenderer().getStringWidth(text);
        setWidth(newWidth);
        moveX(-(newWidth - oldWidth));
    }

    public void add(float offset) {
        set(value + offset);
    }

    public void subtract(float offset) {
        set(value - offset);
    }

    @Override
    public void render(int mouseX, int mouseY, float particleTicks) {
        RenderEventDispatcher.onPreRender(this, mouseX, mouseX);
        int color = isInside(mouseX, mouseY) ? 0xffff00 : 0xffffff;
        fontRenderer().drawStringWithShadow(text, getAbsoluteX(), getAbsoluteY(), color);
        RenderEventDispatcher.onPostRender(this, mouseX, mouseX);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            Dialog.createPrompt("gui.sfm.Editor.EditOffset", (b, s) -> {
                float f;
                try {
                    f = Float.parseFloat(s);
                } catch (NumberFormatException e) {
                    Dialog.createDialog("gui.sfm.Editor.InvalidNumberFormat").tryAddSelfToActiveGUI();
                    return;
                }

                set(f);
            }).tryAddSelfToActiveGUI();
            return true;
        }
        return false;
    }
}
