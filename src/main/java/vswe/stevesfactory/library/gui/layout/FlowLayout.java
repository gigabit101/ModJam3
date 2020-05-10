package vswe.stevesfactory.library.gui.layout;

import lombok.val;
import vswe.stevesfactory.library.gui.Render2D;
import vswe.stevesfactory.library.gui.layout.properties.BoxSizing;
import vswe.stevesfactory.library.gui.layout.properties.HorizontalAlignment;
import vswe.stevesfactory.library.gui.layout.properties.VerticalAlignment;
import vswe.stevesfactory.library.gui.widget.IWidget;
import vswe.stevesfactory.utils.Utils;

import java.awt.*;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class FlowLayout {

    private FlowLayout() {
    }

    public static <T extends IWidget> Collection<T> vertical(Collection<T> widgets, int x, int y, int gap) {
        for (T widget : widgets) {
            if (BoxSizing.shouldIncludeWidget(widget)) {
                widget.setLocation(x, y);
                y += widget.getFullHeight() + gap;
            }
        }
        return widgets;
    }

    public static <T extends IWidget> Collection<T> vertical(Dimension bounds, HorizontalAlignment alignment, Collection<T> widgets, int y, int gap) {
        for (T widget : widgets) {
            if (BoxSizing.shouldIncludeWidget(widget)) {
                switch (alignment) {
                    case LEFT: {
                        widget.setLocation(0, y);
                        break;
                    }
                    case CENTER: {
                        int x = Render2D.computeCenterX(0, bounds.width, widget.getFullWidth());
                        widget.setLocation(x, y);
                        break;
                    }
                    case RIGHT: {
                        int x = Render2D.computeRightX(bounds.width, widget.getFullWidth());
                        widget.setLocation(Utils.lowerBound(x, 0), y);
                        break;
                    }
                }
                y += widget.getFullHeight() + gap;
            }
        }
        return widgets;
    }

    public static <T extends IWidget> Collection<T> horizontal(Collection<T> widgets, int x, int y, int gap) {
        for (T widget : widgets) {
            if (BoxSizing.shouldIncludeWidget(widget)) {
                widget.setLocation(x, y);
                x += widget.getFullWidth() + gap;
            }
        }
        return widgets;
    }

    public static <T extends IWidget> Collection<T> horizontal(Dimension bounds, VerticalAlignment alignment, Collection<T> widgets, int x, int gap) {
        for (T widget : widgets) {
            if (BoxSizing.shouldIncludeWidget(widget)) {
                switch (alignment) {
                    case TOP: {
                        widget.setLocation(x, 0);
                        break;
                    }
                    case CENTER: {
                        int y = Render2D.computeCenterY(0, bounds.height, widget.getFullHeight());
                        widget.setLocation(x, y);
                        break;
                    }
                    case BOTTOM: {
                        int y = Render2D.computeBottomY(bounds.height, widget.getFullHeight());
                        widget.setLocation(Utils.lowerBound(x, 0), y);
                        break;
                    }
                }
                x += widget.getFullWidth() + gap;
            }
        }
        return widgets;
    }

    public static <T extends IWidget> List<T> reverseHorizontal(List<T> widgets, int x, int y, int gap) {
        for (T widget : widgets) {
            if (BoxSizing.shouldIncludeWidget(widget)) {
                x -= widget.getFullWidth() + gap;
                widget.setLocation(x, y);
            }
        }
        return widgets;
    }

    // Simpler version of StrictTableLayout
    // TODO merge?
    public static <T, W extends IWidget> void table(int initialX, int initialY, int width, Collection<W> widgets) {
        int x = initialX;
        int y = initialY;
        int i = 1;
        for (val widget : widgets) {
            widget.setLocation(x, y);
            if (i % 2 == 0) {
                x = initialX;
                y += 10;
            } else {
                x = width / 2;
            }
            i++;
        }
    }
}
