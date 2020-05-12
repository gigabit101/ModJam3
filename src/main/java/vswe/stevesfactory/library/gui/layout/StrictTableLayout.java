package vswe.stevesfactory.library.gui.layout;

import com.google.common.base.Preconditions;
import lombok.val;
import vswe.stevesfactory.library.gui.layout.properties.BoxSizing;
import vswe.stevesfactory.library.gui.widget.IWidget;
import vswe.stevesfactory.utils.Utils;

import java.awt.*;
import java.util.List;

import static vswe.stevesfactory.library.gui.Render2D.isInside;

public class StrictTableLayout {

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
    private static boolean isCompletelyInside(int x, int y, int width, int height, int mx, int my) {
        return isInside(x, y, mx, my) &&
                isInside(x, y + height, mx, my) &&
                isInside(x + width, y + height, mx, my) &&
                isInside(x + width, y, mx, my);
    }

    public GrowDirection stackDirection;
    public GrowDirection overflowDirection;
    public int tableGap;

    public StrictTableLayout(GrowDirection stackDirection, GrowDirection overflowDirection, int tableGap) {
        this.stackDirection = stackDirection;
        this.overflowDirection = overflowDirection;
        this.tableGap = tableGap;
    }

    @SuppressWarnings("UnusedReturnValue")
    public <T extends IWidget> List<T> reflow(Dimension bounds, List<T> widgets) {
        Preconditions.checkArgument(isWidgetDimensionsIdentical(widgets));

        int nextX = 0;
        int nextY = 0;
        int headX = nextX;
        int headY = nextY;

        for (T widget : widgets) {
            if (!BoxSizing.shouldIncludeWidget(widget)) {
                continue;
            }

            widget.setLocation(nextX, nextY);

            int width = widget.getFullWidth();
            int height = widget.getFullHeight();
            nextX = stackDirection.computeNextX(nextX, width, tableGap);
            nextY = stackDirection.computeNextY(nextY, height, tableGap);
            if (!isCompletelyInside(nextX, nextY, width, height, bounds.width, bounds.height)) {
                nextX = headX = overflowDirection.computeNextX(headX, width, tableGap);
                nextY = headY = overflowDirection.computeNextY(headY, height, tableGap);
            }
        }
        return widgets;
    }

    /**
     * Test whether all widgets have the same dimension or not. Currently this layout implementation does not support widgets with different
     * sizes.
     */
    private boolean isWidgetDimensionsIdentical(List<? extends IWidget> widgets) {
        if (widgets.isEmpty()) {
            return true;
        }

        val first = Utils.first(widgets);
        int commonWidth = first.getFullWidth();
        int commonHeight = first.getFullHeight();
        for (val widget : widgets) {
            if (commonWidth != widget.getFullWidth() || commonHeight != widget.getFullHeight()) {
                return false;
            }
        }
        return true;
    }
}
