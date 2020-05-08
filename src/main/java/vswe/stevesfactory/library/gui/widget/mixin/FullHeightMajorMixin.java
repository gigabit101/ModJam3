package vswe.stevesfactory.library.gui.widget.mixin;

import vswe.stevesfactory.library.gui.widget.IWidget;

/**
 * @see FullDimensionMajorMixin
 */
public interface FullHeightMajorMixin extends IWidget, ResizableWidgetMixin {

    @Override
    default void setHeight(int height) {
        ResizableWidgetMixin.super.setHeight(height - getBorderTop() - getBorderBottom());
    }
}
