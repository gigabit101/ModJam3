package vswe.stevesfactory.library.gui.window.mixin;

import vswe.stevesfactory.library.gui.widget.IWidget;
import vswe.stevesfactory.library.gui.window.IWindow;

public interface WindowEventHandlerMixin extends IWindow {

    @Override
    default boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (IWidget child : getChildren()) {
            if (child.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        return false;
    }

    @Override
    default boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (IWidget child : getChildren()) {
            if (child.mouseReleased(mouseX, mouseY, button)) {
                return true;
            }
        }
        return false;
    }

    @Override
    default boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        for (IWidget child : getChildren()) {
            if (child.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
                return true;
            }
        }
        return false;
    }

    @Override
    default boolean mouseScrolled(double mouseX, double mouseY, double scroll) {
        for (IWidget child : getChildren()) {
            if (child.mouseScrolled(mouseX, mouseY, scroll)) {
                return true;
            }
        }
        return false;
    }

    @Override
    default boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (IWidget child : getChildren()) {
            if (child.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
        }
        return false;
    }

    @Override
    default boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        for (IWidget child : getChildren()) {
            if (child.keyReleased(keyCode, scanCode, modifiers)) {
                return true;
            }
        }
        return false;
    }

    @Override
    default boolean charTyped(char charTyped, int keyCode) {
        for (IWidget child : getChildren()) {
            if (child.charTyped(charTyped, keyCode)) {
                return true;
            }
        }
        return false;
    }

    @Override
    default void mouseMoved(double mouseX, double mouseY) {
        for (IWidget child : getChildren()) {
            child.mouseMoved(mouseX, mouseY);
        }
    }

    @Override
    default void update(float partialTicks) {
        for (IWidget child : getChildren()) {
            child.update(partialTicks);
        }
    }

    @Override
    default void onRemoved() {
        for (IWidget child : getChildren()) {
            child.onRemoved();
        }
    }
}
