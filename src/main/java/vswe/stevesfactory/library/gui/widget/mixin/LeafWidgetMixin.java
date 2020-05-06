package vswe.stevesfactory.library.gui.widget.mixin;

import vswe.stevesfactory.library.gui.widget.IWidget;

public interface LeafWidgetMixin extends IWidget {

    default boolean onMouseClicked(double mouseX, double mouseY, int button) {
        getWindow().setFocusedWidget(this);
        return false;
    }

    @Override
    default boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isInside(mouseX, mouseY)) {
            return onMouseClicked(mouseX, mouseY, button);
        }
        return false;
    }

    default boolean onMouseReleased(double mouseX, double mouseY, int button) {
        return false;
    }

    @Override
    default boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (isInside(mouseX, mouseY)) {
            return onMouseReleased(mouseX, mouseY, button);
        }
        return false;
    }

    default boolean onMouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return false;
    }

    @Override
    default boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (isFocused()) {
            return onMouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }
        return false;
    }

    default boolean onMouseScrolled(double mouseX, double mouseY, double scroll) {
        return false;
    }

    @Override
    default boolean mouseScrolled(double mouseX, double mouseY, double scroll) {
        if (isInside(mouseX, mouseY)) {
            return onMouseScrolled(mouseX, mouseY, scroll);
        }
        return false;
    }

    default boolean onKeyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    @Override
    default boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (isFocused()) {
            return onKeyPressed(keyCode, scanCode, modifiers);
        }
        return false;
    }

    default boolean onKeyReleased(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    @Override
    default boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (isFocused()) {
            return onKeyReleased(keyCode, scanCode, modifiers);
        }
        return false;
    }

    default boolean onCharTyped(char charTyped, int keyCode) {
        return false;
    }

    @Override
    default boolean charTyped(char charTyped, int keyCode) {
        if (isFocused()) {
            return onCharTyped(charTyped, keyCode);
        }
        return false;
    }

    @Override
    default void mouseMoved(double mouseX, double mouseY) {
    }

    @Override
    default void update(float partialTicks) {
    }
}
