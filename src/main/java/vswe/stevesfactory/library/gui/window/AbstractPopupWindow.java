package vswe.stevesfactory.library.gui.window;

public abstract class AbstractPopupWindow extends AbstractWindow implements IPopupWindow {

    private int initialDragLocalX = -1, initialDragLocalY = -1;
    public boolean alive = true;

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (isInside(mouseX, mouseY) && shouldDrag()) {
            setFocusedWidget(null);
            initialDragLocalX = (int) mouseX - position.x;
            initialDragLocalY = (int) mouseY - position.y;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (super.mouseReleased(mouseX, mouseY, button)) {
            return true;
        }
        if (isInside(mouseX, mouseY)) {
            initialDragLocalX = -1;
            initialDragLocalY = -1;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
            return true;
        }
        if (isInside(mouseX, mouseY) && isDragging()) {
            int x = (int) mouseX - initialDragLocalX;
            int y = (int) mouseY - initialDragLocalY;
            setPosition(x, y);
            return true;
        }
        return false;
    }

    private boolean isDragging() {
        return initialDragLocalX != -1 && initialDragLocalY != -1;
    }

    public boolean shouldDrag() {
        return true;
    }

    @Override
    public boolean shouldDiscard() {
        return !alive;
    }
}
