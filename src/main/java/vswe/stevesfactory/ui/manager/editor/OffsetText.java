package vswe.stevesfactory.ui.manager.editor;

import org.lwjgl.glfw.GLFW;
import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.widget.AbstractWidget;
import vswe.stevesfactory.library.gui.widget.mixin.LeafWidgetMixin;
import vswe.stevesfactory.library.gui.window.Dialog;
import vswe.stevesfactory.utils.RenderingHelper;

class OffsetText extends AbstractWidget implements LeafWidgetMixin {

    private final String prefix;
    private String text = "";
    private int value;

    public int rightX;

    public OffsetText(String prefix, int xRight, int y) {
        super(xRight, y, 0, fontRenderer().FONT_HEIGHT);
        this.prefix = prefix;
        set(0);
    }

    public String getText() {
        return text;
    }

    public int get() {
        return value;
    }

    public void set(int value) {
        this.value = value;
        this.text = prefix + value;
        update();
    }

    public void update() {
        int width = fontRenderer().getStringWidth(text);
        setWidth(width);
        setX(rightX - width);
    }

    public void add(int offset) {
        set(value + offset);
    }

    public void subtract(int offset) {
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
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            Dialog.createPrompt("gui.sfm.Editor.EditOffset", (b, s) -> {
                int i;
                try {
                    i = Integer.parseInt(s);
                } catch (NumberFormatException e) {
                    Dialog.createDialog("gui.sfm.Editor.InvalidNumberFormat").tryAddSelfToActiveGUI();
                    return;
                }

                set(i);
            }).tryAddSelfToActiveGUI();
            return true;
        }
        return false;
    }
}
