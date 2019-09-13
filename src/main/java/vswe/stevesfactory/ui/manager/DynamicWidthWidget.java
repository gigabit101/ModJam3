package vswe.stevesfactory.ui.manager;

import vswe.stevesfactory.library.gui.IWidget;
import vswe.stevesfactory.library.gui.widget.AbstractContainer;
import vswe.stevesfactory.library.gui.widget.mixin.ResizableWidgetMixin;

import java.awt.*;
import java.util.Comparator;
import java.util.List;

public abstract class DynamicWidthWidget<T extends IWidget> extends AbstractContainer<T> implements ResizableWidgetMixin {

    @SuppressWarnings("UnusedReturnValue")
    public static List<DynamicWidthWidget<?>> reflowDynamicWidth(Dimension bounds, List<DynamicWidthWidget<?>> widgets) {
        int amountMinWidth = 0;
        int amountMaxWidth = 0;
        for (DynamicWidthWidget<?> widget : widgets) {
            switch (widget.getWidthOccupier()) {
                case MIN_WIDTH:
                    amountMinWidth++;
                    break;
                case MAX_WIDTH:
                    amountMaxWidth++;
                    break;
            }
        }

        int usable = bounds.width;
        for (DynamicWidthWidget widget : widgets) {
            if (widget.getWidthOccupier() == WidthOccupierType.MIN_WIDTH) {
                int w = calculateWidthMin(widget);
                widget.setWidth(w);
                usable -= w;
            }
        }

        int unit = usable / amountMaxWidth;
        int nextX = 0;
        for (DynamicWidthWidget<?> widget : widgets) {
            if (widget.getWidthOccupier() == WidthOccupierType.MAX_WIDTH) {
                widget.setWidth(unit);
            }
            widget.setX(nextX);
            nextX += widget.getWidth();
        }

        return widgets;
    }

    private static int calculateWidthMin(DynamicWidthWidget<?> widget) {
        IWidget furthest = widget.getChildren().stream()
                .max(Comparator.comparingInt(IWidget::getX))
                .orElseThrow(RuntimeException::new);
        int x = furthest.getX();
        int w = furthest.getWidth();
        return x + w;
    }

    public enum WidthOccupierType {
        MIN_WIDTH, MAX_WIDTH
    }

    private WidthOccupierType widthOccupier;

    public DynamicWidthWidget(WidthOccupierType widthOccupier) {
        super(0, 0, 0, 0);
        setLocation(0, 0);
        this.widthOccupier = widthOccupier;
    }

    @Override
    public void onParentPositionChanged() {
        super.onParentPositionChanged();
        setHeight(getParentHeight());
    }

    public WidthOccupierType getWidthOccupier() {
        return widthOccupier;
    }
}
