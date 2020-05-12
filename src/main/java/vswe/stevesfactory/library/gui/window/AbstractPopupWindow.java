package vswe.stevesfactory.library.gui.window;

import vswe.stevesfactory.library.gui.Render2D;
import vswe.stevesfactory.library.gui.debug.ITextReceiver;

/**
 * Simple base class for a draggable popup. For more complicated usages see {@link AbstractDockableWindow}.
 */
public abstract class AbstractPopupWindow extends AbstractWindow implements IPopupWindow {

    private int initialDragX = -1;
    private int initialDragY = -1;
    private boolean alive = true;
    private int order;

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClickSubtree(mouseX, mouseY, button)) {
            return true;
        }
        if (isInside(mouseX, mouseY)) {
            setFocusedWidget(null);
            initialDragX = (int) mouseX - getX();
            initialDragY = (int) mouseY - getY();
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (super.mouseDraggedSubtree(mouseX, mouseY, button, deltaX, deltaY)) {
            return true;
        }
        if (isDragging()) {
            int x = (int) mouseX - initialDragX;
            int y = (int) mouseY - initialDragY;
            setPosition(x, y);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (super.mouseReleasedSubtree(mouseX, mouseY, button)) {
            return true;
        }
        if (isInside(mouseX, mouseY)) {
            initialDragX = -1;
            initialDragY = -1;
            return true;
        }
        return false;
    }

    private boolean isDragging() {
        return initialDragX != -1 && initialDragY != -1;
    }

    public void discard() {
        alive = false;
    }

    @Override
    public boolean shouldDiscard() {
        return !alive;
    }

    @Override
    public float getZLevel() {
        return Render2D.POPUP_WINDOW_Z;
    }

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public void provideInformation(ITextReceiver receiver) {
        super.provideInformation(receiver);
        receiver.line("Order=${order}");
    }
}
