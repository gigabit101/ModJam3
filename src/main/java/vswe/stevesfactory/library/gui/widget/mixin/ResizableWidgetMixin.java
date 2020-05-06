package vswe.stevesfactory.library.gui.widget.mixin;

import vswe.stevesfactory.library.gui.widget.IWidget;

import java.awt.*;

public interface ResizableWidgetMixin extends IWidget {

    default void setDimensions(Dimension dimensions) {
        setDimensions(dimensions.width, dimensions.height);
    }

    default void setDimensions(int width, int height) {
        setWidth(width);
        setHeight(height);
    }

    default void setWidth(int width) {
        getDimensions().width = width;
        onDimensionChanged();
    }

    default void setHeight(int height) {
        getDimensions().height = height;
        onDimensionChanged();
    }

    default int getBorderTop() {
        return getBorders().top;
    }

    default int getBorderRight() {
        return getBorders().right;
    }

    default int getBorderBottom() {
        return getBorders().bottom;
    }

    default int getBorderLeft() {
        return getBorders().left;
    }

    default void setBorderTop(int top) {
        getBorders().top = top;
        onBordersChanged();
    }

    default void setBorderRight(int right) {
        getBorders().right = right;
        onBordersChanged();
    }

    default void setBorderBottom(int bottom) {
        getBorders().bottom = bottom;
        onBordersChanged();
    }

    default void setBorderLeft(int left) {
        getBorders().left = left;
        onBordersChanged();
    }

    default void setBorders(int top, int right, int bottom, int left) {
        getBorders().top = top;
        getBorders().right = right;
        getBorders().bottom = bottom;
        getBorders().left = left;
        onBordersChanged();
    }

    default void setBorders(int borders) {
        setBorders(borders, borders, borders, borders);
    }

    default void onDimensionChanged() {
    }

    default void onBordersChanged() {
    }
}
