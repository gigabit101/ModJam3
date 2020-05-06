package vswe.stevesfactory.library.gui.widget;

import vswe.stevesfactory.library.gui.widget.panel.VerticalList;

public class MinimumVerticalList<T extends IWidget> extends VerticalList<T> {

    @Override
    protected boolean isDrawingScrollBar() {
        return false;
    }

    @Override
    public int getBorder() {
        return 0;
    }

    @Override
    public int getMarginMiddle() {
        return 0;
    }
}