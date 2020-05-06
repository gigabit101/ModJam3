package vswe.stevesfactory.library.gui.window.mixin;

import vswe.stevesfactory.library.gui.window.IWindow;

public interface WindowPropertiesMixin extends IWindow {

    @Override
    default int getX() {
        return getPosition().x;
    }

    @Override
    default int getY() {
        return getPosition().y;
    }

    @Override
    default int getXRight() {
        return getPosition().x + getBorder().width;
    }

    @Override
    default int getYBottom() {
        return getPosition().y + getBorder().height;
    }

    @Override
    default int getContentX() {
        return getPosition().x + getBorderSize();
    }

    @Override
    default int getContentY() {
        return getPosition().y + getBorderSize();
    }

    @Override
    default int getContentXRight() {
        return getPosition().x + getBorder().width - getBorderSize();
    }

    @Override
    default int getContentYBottom() {
        return getPosition().y + getBorder().height - getBorderSize();
    }

    @Override
    default int getWidth() {
        return getBorder().width;
    }

    @Override
    default int getHeight() {
        return getBorder().height;
    }

    @Override
    default int getContentWidth() {
        return getBorder().width - getBorderSize() * 2;
    }

    @Override
    default int getContentHeight() {
        return getBorder().height - getBorderSize() * 2;
    }

    @Override
    default void setPosition(int x, int y) {
        getPosition().x = x;
        getPosition().y = y;
    }
}
