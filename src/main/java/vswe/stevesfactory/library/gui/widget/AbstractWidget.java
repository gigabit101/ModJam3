package vswe.stevesfactory.library.gui.widget;

import javafx.geometry.Side;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import vswe.stevesfactory.library.gui.IWidget;
import vswe.stevesfactory.library.gui.IWindow;
import vswe.stevesfactory.library.gui.debug.ITextReceiver;
import vswe.stevesfactory.library.gui.debug.Inspections;
import vswe.stevesfactory.library.gui.layout.ILayoutDataProvider;
import vswe.stevesfactory.library.gui.layout.properties.BoxSizing;
import vswe.stevesfactory.library.gui.layout.properties.HorizontalAlignment;
import vswe.stevesfactory.library.gui.widget.mixin.ResizableWidgetMixin;
import vswe.stevesfactory.utils.RenderingHelper;

import javax.annotation.Nullable;
import java.awt.*;

public abstract class AbstractWidget implements IWidget, Inspections.IInspectionInfoProvider, ILayoutDataProvider, ResizableWidgetMixin {

    public static Minecraft minecraft() {
        return Minecraft.getInstance();
    }

    public static FontRenderer fontRenderer() {
        return Minecraft.getInstance().fontRenderer;
    }

    private Point location;
    private Dimension dimensions;
    private boolean enabled = true;

    private IWindow window;
    private IWidget parent;

    // Cached because this might reach all the up to the root node by recursion on getAbsoluteX/Y
    private int absX;
    private int absY;

    public AbstractWidget(IWindow window) {
        this(0, 0, window.getContentDimensions().width, window.getContentDimensions().height);
        this.window = window;
    }

    public AbstractWidget() {
        this(0, 0, 0, 0);
    }

    public AbstractWidget(int x, int y, int width, int height) {
        this(new Point(x, y), new Dimension(width, height));
    }

    public AbstractWidget(Point location, Dimension dimensions) {
        this.location = location;
        this.dimensions = dimensions;
    }

    @Override
    public void setParentWidget(IWidget newParent) {
        this.parent = newParent;
        this.window = newParent.getWindow();
        onParentPositionChanged();
    }

    public void setWindow(IWindow window) {
        this.window = window;
        onParentPositionChanged();
    }

    @Override
    public void onParentPositionChanged() {
        updateAbsolutePosition();
    }

    @Override
    public void onRelativePositionChanged() {
        updateAbsolutePosition();
    }

    private void updateAbsolutePosition() {
        absX = getParentAbsXSafe() + getX();
        absY = getParentAbsYSafe() + getY();
    }

    private int getParentAbsXSafe() {
        if (parent != null) {
            return parent.getAbsoluteX();
        }
        if (window != null) {
            return window.getContentX();
        }
        return 0;
    }

    private int getParentAbsYSafe() {
        if (parent != null) {
            return parent.getAbsoluteY();
        }
        if (window != null) {
            return window.getContentY();
        }
        return 0;
    }

    /**
     * A safe version of {@code getParentWidget().getHeight()} that prevents NPE.
     */
    public int getParentHeight() {
        if (parent != null) {
            return parent.getHeight();
        }
        if (window != null) {
            return window.getContentHeight();
        }
        return 0;
    }

    /**
     * A safe version of {@code getParentWidget().getWidth()} that prevents NPE.
     */
    public int getParentWidth() {
        if (parent != null) {
            return parent.getWidth();
        }
        if (window != null) {
            return window.getContentWidth();
        }
        return 0;
    }

    public void fillParentContainer() {
        setLocation(0, 0);
        setDimensions(parent.getDimensions());
    }

    public void expandHorizontally() {
        setWidth(Math.max(getWidth(), getParentWidth()));
    }

    public void expandVertically() {
        setHeight(Math.max(getHeight(), getParentHeight()));
    }

    @Override
    public boolean isFocused() {
        return getWindow().getFocusedWidget() == this;
    }

    @Override
    public Point getPosition() {
        return location;
    }

    @Override
    public int getAbsoluteX() {
        return absX;
    }

    @Override
    public int getAbsoluteY() {
        return absY;
    }

    public int getAbsoluteXBR() {
        return getAbsoluteX() + getWidth();
    }

    public int getAbsoluteYBR() {
        return getAbsoluteY() + getHeight();
    }

    public void alignTo(IWidget other, Side side, HorizontalAlignment alignment) {
        if (this.getParentWidget() != other.getParentWidget()) {
            return;
        }

        int otherLeft = other.getX();
        int otherTop = other.getY();
        int otherRight = otherLeft + other.getWidth();
        int otherBottom = otherTop + other.getHeight();

        switch (side) {
            case TOP:
                alignBottom(otherTop);
                alignHorizontally(alignment, otherLeft, otherRight);
                break;
            case BOTTOM:
                alignTop(otherBottom);
                alignHorizontally(alignment, otherLeft, otherRight);
                break;
            case LEFT:
                alignRight(otherLeft);
                alignVertically(alignment, otherTop, otherBottom);
                break;
            case RIGHT:
                alignLeft(otherRight);
                alignVertically(alignment, otherTop, otherBottom);
                break;
        }
    }

    private void alignHorizontally(HorizontalAlignment alignment, int left, int right) {
        switch (alignment) {
            case LEFT:
                alignLeft(left);
                break;
            case CENTER:
                alignCenterX(left, right);
                break;
            case RIGHT:
                alignRight(right);
                break;
        }
    }

    private void alignVertically(HorizontalAlignment alignment, int top, int bottom) {
        switch (alignment) {
            case LEFT:
                alignTop(top);
                break;
            case CENTER:
                alignCenterY(top, bottom);
                break;
            case RIGHT:
                alignBottom(bottom);
                break;
        }
    }

    public void alignLeft(int left) {
        setX(left);
    }

    public void alignCenterX(int left, int right) {
        setX(RenderingHelper.getXForAlignedCenter(left, right, getWidth()));
    }

    public void alignRight(int right) {
        setX(RenderingHelper.getXForAlignedRight(right, getWidth()));
    }

    public void alignTop(int top) {
        setY(top);
    }

    public void alignCenterY(int top, int bottom) {
        setY(RenderingHelper.getYForAlignedCenter(top, bottom, getHeight()));
    }

    public void alignBottom(int bottom) {
        setY(RenderingHelper.getYForAlignedBottom(bottom, getHeight()));
    }

    @Override
    public Dimension getDimensions() {
        return dimensions;
    }

    @Nullable
    @Override
    public IWidget getParentWidget() {
        return parent;
    }

    @Override
    public IWindow getWindow() {
        return window;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean isInside(double x, double y) {
        return getAbsoluteX() <= x &&
                getAbsoluteXBR() > x &&
                getAbsoluteY() <= y &&
                getAbsoluteYBR() > y;
    }

    @Override
    public BoxSizing getBoxSizing() {
        return BoxSizing.BORDER_BOX;
    }

    @Override
    public int getX() {
        return getPosition().x;
    }

    @Override
    public int getY() {
        return getPosition().y;
    }

    @Override
    public int getWidth() {
        return getDimensions().width;
    }

    @Override
    public int getHeight() {
        return getDimensions().height;
    }

    @Override
    public void provideInformation(ITextReceiver receiver) {
        receiver.line(this.toString());
        receiver.line("X=" + this.getX());
        receiver.line("Y=" + this.getY());
        receiver.line("AbsX=" + this.getAbsoluteX());
        receiver.line("AbsY=" + this.getAbsoluteY());
        receiver.line("Width=" + this.getWidth());
        receiver.line("Height=" + this.getHeight());
        receiver.line("Enabled=" + this.isEnabled());
    }
}
