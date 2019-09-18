package vswe.stevesfactory.library.gui.window;

import vswe.stevesfactory.library.gui.widget.IWidget;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.List;

public interface IWindow {

    Dimension getBorder();

    default int getWidth() {
        return getBorder().width;
    }

    default int getHeight() {
        return getBorder().height;
    }

    int getBorderSize();

    Dimension getContentDimensions();

    default int getContentWidth() {
        return getContentDimensions().width;
    }

    default int getContentHeight() {
        return getContentDimensions().height;
    }

    List<? extends IWidget> getChildren();

    Point getPosition();

    default void setPosition(int x, int y) {
        getPosition().x = x;
        getPosition().y = y;
    }

    default int getX() {
        return getPosition().x;
    }

    default int getY() {
        return getPosition().y;
    }

    default int getContentX() {
        return getX() + getBorderSize();
    }

    default int getContentY() {
        return getY() + getBorderSize();
    }

    void render(int mouseX, int mouseY, float particleTicks);

    @Nullable
    IWidget getFocusedWidget();

    /**
     * Change which widget is focused.
     * <p>
     * When possible, use {@link #changeFocus(IWidget, boolean)} instead.
     *
     * @implSpec This method should invoke {@link IWidget#onFocusChanged(boolean)} on both the parameter and the focused element as long as
     * they are nonnull.
     */
    void setFocusedWidget(@Nullable IWidget widget);

    /**
     * Helper method to set focus of a specific element. Notice this would cancel the originally focus element. Implementations should not
     * override this method unless it is using a special focus handler that is not compatible with the default implementation of this
     * method.
     */
    default boolean changeFocus(IWidget widget, boolean focus) {
        if (focus && widget.isEnabled()) {
            setFocusedWidget(widget);
            return true;
        } else {
            setFocusedWidget(null);
            return false;
        }
    }

    void onRemoved();

    default boolean isInside(double x, double y) {
        int selfX = getX();
        int selfY = getY();
        int selfXBR = selfX + getWidth();
        int selfYBR = selfY + getHeight();
        return x >= selfX &&
                x < selfXBR &&
                y >= selfY &&
                y < selfYBR;
    }

    /**
     * @implSpec Propagate the event and pass it on to the children.
     * @implNote This method will be invoked regardless of state (e.g. whether the cursor is inside or not). Event capture method. Return
     * {@code true} to stop propagation of the event to other widgets, otherwise the process would continue. Note that this is valid only
     * when the GUI is handling more than one windows.
     */
    boolean mouseClicked(double mouseX, double mouseY, int button);

    /**
     * @implSpec Propagate the event and pass it on to the children.
     * @implNote This method will be invoked regardless of state (e.g. whether the cursor is inside or not). Event capture method. Return
     * {@code true} to stop propagation of the event to other widgets, otherwise the process would continue. Note that this is valid only
     * when the GUI is handling more than one windows.
     */
    boolean mouseReleased(double mouseX, double mouseY, int button);

    /**
     * @implSpec Propagate the event and pass it on to the children.
     * @implNote This method will be invoked regardless of state (e.g. whether the cursor is inside or not). Event capture method. Return
     * {@code true} to stop propagation of the event to other widgets, otherwise the process would continue. Note that this is valid only
     * when the GUI is handling more than one windows.
     */
    boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY);

    /**
     * @implSpec Propagate the event and pass it on to the children.
     * @implNote This method will be invoked regardless of state (e.g. whether this window is focused or not). Event capture method. Return
     * {@code true} to stop propagation of the event to other widgets, otherwise the process would continue. Note that this is valid only
     * when the GUI is handling more than one windows.
     */
    boolean mouseScrolled(double mouseX, double mouseY, double scroll);

    /**
     * @implSpec Propagate the event and pass it on to the children.
     * @implNote This method will be invoked regardless of state (e.g. whether this window is focused or not). Event capture method. Return
     * {@code true} to stop propagation of the event to other widgets, otherwise the process would continue. Note that this is valid only
     * when the GUI is handling more than one windows.
     */
    boolean keyPressed(int keyCode, int scanCode, int modifiers);

    /**
     * @implSpec Propagate the event and pass it on to the children.
     * @implNote This method will be invoked regardless of state (e.g. whether this window is focused or not). Event capture method. Return
     * {@code true} to stop propagation of the event to other widgets, otherwise the process would continue. Note that this is valid only
     * when the GUI is handling more than one windows.
     */
    boolean keyReleased(int keyCode, int scanCode, int modifiers);

    /**
     * @implSpec Propagate the event and pass it on to the children.
     * @implNote This method will be invoked regardless of state (e.g. whether this window is focused or not). Event capture method. Return
     * {@code true} to stop propagation of the event to other widgets, otherwise the process would continue. Note that this is valid only
     * when the GUI is handling more than one windows.
     */
    boolean charTyped(char charTyped, int keyCode);

    void mouseMoved(double mouseX, double mouseY);

    void update(float particleTicks);
}
