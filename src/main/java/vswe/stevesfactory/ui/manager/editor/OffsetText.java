package vswe.stevesfactory.ui.manager.editor;

import com.google.common.collect.ImmutableList;
import org.lwjgl.glfw.GLFW;
import vswe.stevesfactory.library.gui.contextmenu.CallbackEntry;
import vswe.stevesfactory.library.gui.contextmenu.ContextMenu;
import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.screen.WidgetScreen;
import vswe.stevesfactory.library.gui.widget.AbstractWidget;
import vswe.stevesfactory.library.gui.widget.mixin.LeafWidgetMixin;
import vswe.stevesfactory.library.gui.window.Dialog;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT;

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
        if (button == GLFW_MOUSE_BUTTON_LEFT) {
            Dialog.createPrompt("gui.sfm.Editor.EditOffset", (b, s) -> {
                try {
                    set(Integer.parseInt(s));
                } catch (NumberFormatException e) {
                    Dialog.createDialog("gui.sfm.Editor.InvalidNumberFormat").tryAddSelfToActiveGUI();
                }
            }).tryAddSelfToActiveGUI();
            return true;
        }
        if (button == GLFW_MOUSE_BUTTON_RIGHT) {
            ContextMenu contextMenu = ContextMenu.atCursor(ImmutableList.of(
                    new CallbackEntry(null, "gui.sfm.ActionMenu.Reset0", b -> set(0))
            ));
            WidgetScreen.getCurrentScreen().addPopupWindow(contextMenu);
            return true;
        }
        return false;
    }
}
