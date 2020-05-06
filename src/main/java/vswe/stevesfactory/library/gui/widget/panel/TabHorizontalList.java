package vswe.stevesfactory.library.gui.widget.panel;

public class TabHorizontalList extends HorizontalList<Tab> {

    public TabHorizontalList(int width, int height) {
        super(width, height);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public int getBarHeight() {
        return 0;
    }
}
