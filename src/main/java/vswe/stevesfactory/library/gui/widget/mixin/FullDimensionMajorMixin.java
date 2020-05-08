package vswe.stevesfactory.library.gui.widget.mixin;

/**
 * This mixin changes the behavior of widgets to "full dimension major" instead of "content dimension major". This class
 * simply combines both {@link FullWidthMajorMixin} and {@link FullHeightMajorMixin}.
 *
 * <ul>
 * <li>Full dimension major: when setting dimensions, the provided size is expected to be size with borders.
 * <li>Content dimension major: when setting dimensions, the provided size is expected to be the content size, i.e.
 * without the borders.
 * </ul>
 *
 * @see FullWidthMajorMixin
 * @see FullHeightMajorMixin
 */
public interface FullDimensionMajorMixin extends FullWidthMajorMixin, FullHeightMajorMixin {
}
