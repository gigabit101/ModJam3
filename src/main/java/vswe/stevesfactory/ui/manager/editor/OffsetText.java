package vswe.stevesfactory.ui.manager.editor;

import net.minecraft.client.gui.screen.Screen;
import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.widget.AbstractWidget;
import vswe.stevesfactory.library.gui.widget.NumberField;
import vswe.stevesfactory.library.gui.widget.mixin.LeafWidgetMixin;
import vswe.stevesfactory.library.gui.window.Dialog;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static vswe.stevesfactory.library.gui.Render2D.fontRenderer;

class OffsetText extends AbstractWidget implements LeafWidgetMixin {

    private final String prefix;
    private String text = "";
    private int value;

    public int rightX;

    public OffsetText(String prefix, int xRight, int y) {
        this.setLocation(xRight, y);
        this.setDimensions(0, fontRenderer().FONT_HEIGHT);
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
    public void render(int mouseX, int mouseY, float partialTicks) {
        RenderEventDispatcher.onPreRender(this, mouseX, mouseY);
        int color = isInside(mouseX, mouseY) ? 0xffff00 : 0xffffff;
        fontRenderer().drawStringWithShadow(text, getAbsoluteX(), getAbsoluteY(), color);
        RenderEventDispatcher.onPostRender(this, mouseX, mouseY);
    }

    @Override
    public boolean onMouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW_MOUSE_BUTTON_LEFT) {
            if (Screen.hasControlDown()) {
                set(0);
            } else {
                NumberField<Integer> field = NumberField.integerField(0, 16);
                field.setValue(value);

                Dialog prompt = Dialog.createPrompt(
                        "gui.sfm.FactoryManager.Editor.EditOffset",
                        () -> field,
                        "gui.sfm.ok", "gui.sfm.cancel",
                        (b, s) -> {
                        },
                        (b, s) -> set(field.getValue()));
                prompt.tryAddSelfToActiveGUI();
            }
            return true;
        }
        return false;
    }
}
