package vswe.stevesfactory.library.gui.widget.mixin;

import vswe.stevesfactory.library.gui.widget.IWidget;

/**
 * @see FullDimensionMajorMixin
 */
public interface FullWidthMajorMixin extends IWidget, ResizableWidgetMixin {

    @Override
    default void setWidth(int width) {
        ResizableWidgetMixin.super.setWidth(width - getBorderLeft() - getBorderRight());
    }
}
