package vswe.stevesfactory.library.gui.layout;

import com.google.common.base.Preconditions;
import vswe.stevesfactory.library.gui.core.ILayout;
import vswe.stevesfactory.library.gui.core.IWidget;
import vswe.stevesfactory.library.gui.widget.mixin.RelocatableWidgetMixin;

import java.awt.*;
import java.util.List;

import static vswe.stevesfactory.utils.VectorHelper.isInside;

public class TableLayout<T extends IWidget & RelocatableWidgetMixin> implements ILayout<T> {

    public enum GrowDirection {
        UP {
            @Override
            public int computeNextX(int originalX, int width, int margin) {
                return originalX;
            }

            @Override
            public int computeNextY(int originalY, int height, int margin) {
                return originalY - margin - height;
            }
        },
        DOWN {
            @Override
            public int computeNextX(int originalX, int width, int margin) {
                return originalX;
            }

            @Override
            public int computeNextY(int originalY, int height, int margin) {
                return originalY + height + margin;
            }
        },
        LEFT {
            @Override
            public int computeNextX(int originalX, int width, int margin) {
                return originalX - margin - width;
            }

            @Override
            public int computeNextY(int originalY, int height, int margin) {
                return originalY;
            }
        },
        RIGHT {
            @Override
            public int computeNextX(int originalX, int width, int margin) {
                return originalX + width + margin;
            }

            @Override
            public int computeNextY(int originalY, int height, int margin) {
                return originalY;
            }
        };

        public abstract int computeNextX(int originalX, int width, int margin);

        public abstract int computeNextY(int originalY, int height, int margin);

    }

    /**
     * Test whether the given rectangle is completely inside the region from {@code (0,0)} to {@code (mx,my)}. Completely inside means all
     * four vertices are inside the given region.
     */
    public static boolean isCompletelyInside(int x, int y, int width, int height, int mx, int my) {
        return isInside(x, y, mx, my) &&
                isInside(x, y + height, mx, my) &&
                isInside(x + width, y + height, mx, my) &&
                isInside(x + width, y, mx, my);
    }


    public GrowDirection stackDirection;
    public GrowDirection overflowDirection;
    public int componentMargin;

    public TableLayout(GrowDirection stackDirection, GrowDirection overflowDirection, int componentMargin) {
        this.stackDirection = stackDirection;
        this.overflowDirection = overflowDirection;
        this.componentMargin = componentMargin;
    }

    @Override
    public List<T> reflow(Dimension bounds, List<T> widgets) {
        Preconditions.checkArgument(widgetDimensions(widgets));

        int width = bounds.width;
        int height = bounds.height;

        int nextX = componentMargin;
        int nextY = componentMargin;
        int headX = nextX;
        int headY = nextY;

        for (T widget : widgets) {
            widget.setLocation(nextX, nextY);
            if (isCompletelyInside(nextX, nextY, widget.getWidth(), widget.getHeight(), width, height)) {
                nextX = stackDirection.computeNextX(nextX, width, componentMargin);
                nextY = stackDirection.computeNextY(nextY, height, componentMargin);
            } else {
                nextX = headX = overflowDirection.computeNextX(headX, width, componentMargin);
                nextY = headY = overflowDirection.computeNextY(headY, height, componentMargin);
            }
        }
        return widgets;
    }

    @Override
    public LayoutType getType() {
        return LayoutType.StatedLayout;
    }

    /**
     * Test whether all widgets have the same dimension or not. Currently this layout implementation does not support widgets with different
     * sizes.
     */
    private boolean widgetDimensions(List<T> widgets) {
        if (widgets.isEmpty()) {
            return true;
        }

        T first = widgets.get(0);
        int commonWidth = first.getWidth();
        int commonHeight = first.getHeight();
        for (T widget : widgets) {
            if (commonWidth != widget.getWidth() || commonHeight != widget.getHeight()) {
                return false;
            }
        }
        return true;
    }

}