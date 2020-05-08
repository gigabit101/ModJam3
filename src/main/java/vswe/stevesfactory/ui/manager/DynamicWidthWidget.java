package vswe.stevesfactory.ui.manager;

import vswe.stevesfactory.library.gui.layout.properties.BoxSizing;
import vswe.stevesfactory.library.gui.widget.AbstractContainer;
import vswe.stevesfactory.library.gui.widget.IWidget;

import java.util.List;

public abstract class DynamicWidthWidget<T extends IWidget> extends AbstractContainer<T> implements IWidget {

    @SuppressWarnings("UnusedReturnValue")
    public static List<DynamicWidthWidget<?>> resizeAll(int width, List<DynamicWidthWidget<?>> widgets) {
        int amountMaxWidth = 0;
        for (DynamicWidthWidget<?> widget : widgets) {
            if (!BoxSizing.shouldIncludeWidget(widget)) {
                continue;
            }
            switch (widget.getWidthOccupier()) {
                case MIN_WIDTH:
                    break;
                case MAX_WIDTH:
                    amountMaxWidth++;
                    break;
            }
        }

        int usable = width;
        for (DynamicWidthWidget<?> widget : widgets) {
            if (!BoxSizing.shouldIncludeWidget(widget)) {
                continue;
            }
            if (widget.getWidthOccupier() == WidthOccupierType.MIN_WIDTH) {
                usable -= widget.getFullWidth();
            }
        }

        int unit = usable / amountMaxWidth;
        int nextX = 0;
        for (DynamicWidthWidget<?> widget : widgets) {
            if (!BoxSizing.shouldIncludeWidget(widget)) {
                continue;
            }
            if (widget.getWidthOccupier() == WidthOccupierType.MAX_WIDTH) {
                widget.setWidth(unit);
            }
            widget.setX(nextX);
            nextX += widget.getFullWidth();
        }

        for (DynamicWidthWidget<?> widget : widgets) {
            widget.reflowAfter();
        }

        return widgets;
    }

    public enum WidthOccupierType {
        MIN_WIDTH, MAX_WIDTH
    }

    private WidthOccupierType widthOccupier;

    public DynamicWidthWidget(WidthOccupierType widthOccupier) {
        this.widthOccupier = widthOccupier;
    }

//    @Override
//    public void onParentPositionChanged() {
//        super.onParentPositionChanged();
//        setHeight(getParentHeight());
//    }

    public WidthOccupierType getWidthOccupier() {
        return widthOccupier;
    }

    protected void reflowAfter() {
    }
}
