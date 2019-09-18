package vswe.stevesfactory.library.gui.widget.mixin;

import vswe.stevesfactory.library.gui.widget.IWidget;

public interface LeafWidgetMixin extends IWidget {

    @Override
    default boolean mouseClicked(double mouseX, double mouseY, int button) {
        getWindow().setFocusedWidget(this);
        return false;
    }

    @Override
    default boolean mouseReleased(double mouseX, double mouseY, int button) {
        return false;
    }

    @Override
    default boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return false;
    }

    @Override
    default boolean mouseScrolled(double mouseX, double mouseY, double scroll) {
        return false;
    }

    @Override
    default boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    @Override
    default boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    @Override
    default boolean charTyped(char charTyped, int keyCode) {
        return false;
    }

    @Override
    default void mouseMoved(double mouseX, double mouseY) {
    }

    @Override
    default void update(float particleTicks) {
    }
}
