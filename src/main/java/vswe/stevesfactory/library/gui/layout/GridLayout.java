package vswe.stevesfactory.library.gui.layout;

import com.google.common.base.Preconditions;
import io.netty.util.internal.StringUtil;
import org.apache.commons.lang3.StringUtils;
import vswe.stevesfactory.library.gui.layout.properties.BoxSizing;
import vswe.stevesfactory.library.gui.layout.properties.IFractionalLengthHandler;
import vswe.stevesfactory.library.gui.layout.properties.Length;
import vswe.stevesfactory.library.gui.widget.IWidget;
import vswe.stevesfactory.library.gui.widget.mixin.ResizableWidgetMixin;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Layout widgets on a non-fixed dimension grid, where each row and column have their individual size. The widgets are layed on the grid so
 * that they cover a rectangle of cells.
 * <p>
 * See CSS Grid Layout. This class is meant to replicate the mechanics of it.
 */
public class GridLayout {

    private static class Resolver implements IFractionalLengthHandler {

        private int length;
        private int denominator;

        @Override
        public int getDenominator() {
            return denominator;
        }

        @Override
        public int getTotalLength() {
            return length;
        }
    }

    private Resolver widthResolver = new Resolver();
    private Resolver heightResolver = new Resolver();
    private Length<?> gridGap;
    private Length<Resolver>[] columns;
    private Length<Resolver>[] rows;

    private int[] xPx;
    private int[] yPx;

    /**
     * A map of where the child widgets should occupy. See CSS Grid's {@code grid-template-areas}. Each element is the ID (index) of the
     * child widget. Note that this array should be setup in [y][x], not [x][y].
     */
    private String[][] areas;

    @SuppressWarnings("UnusedReturnValue")
    public GridLayout gridGap(Length<?> gridGap) {
        setGridGap(gridGap);
        return this;
    }

    @SafeVarargs
    @SuppressWarnings("UnusedReturnValue")
    public final GridLayout rows(Length<Resolver>... rows) {
        setRows(rows);
        return this;
    }

    @SafeVarargs
    @SuppressWarnings("UnusedReturnValue")
    public final GridLayout columns(Length<Resolver>... columns) {
        setColumns(columns);
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    public GridLayout areas(String... areas) {
        this.setAreas(areas);
        return this;
    }

    /**
     * CSS-style template areas, where each line is a single string.
     */
    @SuppressWarnings("UnusedReturnValue")
    public GridLayout templateAreas(String... areas) {
        Preconditions.checkState(areas.length == rows.length);
        this.areas = new String[areas.length][];
        for (int i = 0; i < areas.length; i++) {
            this.areas[i] = StringUtils.split(areas[i], ' ');
        }
        return this;
    }

    // px/py stands for "Pixel-position x/y"
    // gx/gy stands for "Grid x/y"
    public <T extends IWidget & ResizableWidgetMixin> void reflow(IWidget parent, Map<String, T> widgets) {
        Set<T> initializationStats = new HashSet<>();
        reresolveLengths(parent);
        for (int gy = 0; gy < areas.length; gy++) {
            for (int gx = 0; gx < areas[gy].length; gx++) {
                String cell = areas[gy][gx];
                T widget = widgets.get(cell);
                // This handles null check
                if (!BoxSizing.shouldIncludeWidget(widget)) {
                    continue;
                }
                if (!initializationStats.contains(widget)) {
                    widget.setLocation(getPxAt(gx), getPyAt(gy));
                    widget.setDimensions(getWidth(gx), getHeight(gy));
                    initializationStats.add(widget);
                    continue;
                }

                int px = getPxAt(gx);
                int py = getPyAt(gy);
                // Expand the first vertex towards top left
                if (!widget.isInside(px, py)) {
                    widget.setLocation(Math.min(widget.getX(), px), Math.min(widget.getY(), py));
                }

                int px2 = getPx2At(gx);
                int py2 = getPy2At(gy);
                // Expand the second vertex towards bottom right
                if (!widget.isInside(px2, py2)) {
                    widget.setDimensions(
                            Math.max(widget.getFullWidth(), px2 - widget.getX()),
                            Math.max(widget.getFullHeight(), py2 - widget.getY()));
                }
            }
        }
    }

    private int getPxAt(int gx) {
        return xPx[gx];
    }

    private int getPyAt(int gy) {
        return yPx[gy];
    }

    private int getWidth(int gx) {
        return columns[gx].getInt();
    }

    private int getHeight(int gy) {
        return rows[gy].getInt();
    }

    private int getPx2At(int gx) {
        return xPx[gx] + columns[gx].getInt();
    }

    private int getPy2At(int gy) {
        return yPx[gy] + columns[gy].getInt();
    }

    public int getGridGap() {
        return gridGap.getInt();
    }

    private int getHorizontalSumGaps() {
        return (columns.length - 1) * gridGap.getInt();
    }

    private int getVerticalSumGaps() {
        return (rows.length - 1) * gridGap.getInt();
    }

    public final void setGridGap(Length<?> gridGap) {
        Preconditions.checkArgument(gridGap.getInt() >= 0);
        this.gridGap = gridGap;
    }

    @SafeVarargs
    public final void setColumns(Length<Resolver>... columns) {
        this.columns = columns;
        updateDenominator(widthResolver, columns);
        xPx = new int[columns.length];
    }

    @SafeVarargs
    public final void setRows(Length<Resolver>... rows) {
        this.rows = rows;
        updateDenominator(heightResolver, rows);
        yPx = new int[rows.length];
    }

    @SafeVarargs
    private final void updateDenominator(Resolver resolver, Length<Resolver>... lengths) {
        for (Length<Resolver> length : lengths) {
            if (length instanceof Length.Fr) {
                resolver.denominator += ((Length.Fr<Resolver>) length).getNumerator();
            }
        }
    }

    private void reresolveLengths(IWidget widget) {
        widthResolver.length = widget.getWidth();
        heightResolver.length = widget.getHeight();
        buildPrefixSum(xPx, widthResolver, rows);
        buildPrefixSum(yPx, heightResolver, columns);
    }

    @SafeVarargs
    private final void buildPrefixSum(int[] px, Resolver resolver, Length<Resolver>... lengths) {
        int len = 0;
        for (int i = 0; i < lengths.length; i++) {
            Length<Resolver> length = lengths[i];
            length.resolve(resolver);
            px[i] = len;
            len += length.getInt() + gridGap.getInt();
        }
    }

    public void setAreas(String[] areas) {
        Preconditions.checkArgument(areas.length == rows.length * columns.length);
        this.areas = new String[rows.length][columns.length];
        for (int y = 0; y < rows.length; y++) {
            System.arraycopy(areas, y * columns.length, this.areas[y], 0, rows.length);
        }
    }
}
