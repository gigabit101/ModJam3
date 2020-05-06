/* Code adapted from Steve's Factory Manager 2 by Vswe/gigabte101.
 * https://github.com/gigabit101/HarmonicsCore/blob/2.0.X/src/main/java/vswe.stevesfactory.library.components/ScrollController.java
 */

package vswe.stevesfactory.library.gui.widget.panel;

import com.google.common.base.Preconditions;
import vswe.stevesfactory.library.gui.Render2D;
import vswe.stevesfactory.library.gui.ScissorTest;
import vswe.stevesfactory.library.gui.debug.ITextReceiver;
import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.widget.AbstractContainer;
import vswe.stevesfactory.library.gui.widget.IWidget;
import vswe.stevesfactory.utils.Utils;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class WrappingList<T extends IWidget> extends AbstractContainer<IWidget> {

    // Scrolling states
    private int offset;
    private int rows;
    private boolean disabledScroll;

    // Child widgets
    private ScrollArrow scrollUpArrow;
    private ScrollArrow scrollDownArrow;
    private List<T> contents = new ArrayList<>();
    private List<IWidget> children;

    public WrappingList() {
        this(80, 80);
    }

    public WrappingList(int width, int height) {
        this.setDimensions(width, height);
        this.children = new AbstractList<IWidget>() {
            @Override
            public IWidget get(int i) {
                switch (i) {
                    case 0:
                        return scrollUpArrow;
                    case 1:
                        return scrollDownArrow;
                    default:
                        return contents.get(i - 2);
                }
            }

            @Override
            public int size() {
                return 2 + contents.size();
            }
        };
    }

    @Override
    public void onInitialAttach() {
        this.scrollUpArrow = ScrollArrow.up(0, 0);
        this.scrollUpArrow.attach(this);
        this.scrollDownArrow = ScrollArrow.down(0, 0);
        this.scrollDownArrow.attach(this);
        this.alignArrows();
        // Update arrow states
        this.scroll(1);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scroll) {
        if (!isInside(mouseX, mouseY) || !isEnabled()) {
            return false;
        }
        int x1 = getAbsoluteX();
        int y1 = getAbsoluteY();
        int x2 = x1 + getFullWidth();
        int y2 = y1 + getFullHeight();
        if (!Render2D.isInside((int) mouseX, (int) mouseY, x1, y1, x2, y2)) {
            return false;
        }
        // "Windows style scrolling": scroll wheel is controlling the page
        scroll((int) scroll * -5);
        return true;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        RenderEventDispatcher.onPreRender(this, mouseX, mouseY);

        scrollUpArrow.render(mouseX, mouseY, partialTicks);
        scrollDownArrow.render(mouseX, mouseY, partialTicks);

        ScissorTest test = ScissorTest.scaled(getAbsoluteX(), getAbsoluteY(), getWidth(), getHeight());

        int sTop = 0;
        int sBottom = getHeight();
        for (T child : contents) {
            int cy = child.getY();
            if (cy + child.getHeight() > sTop && cy < sBottom) {
                child.render(mouseX, mouseY, partialTicks);
            }
        }

        test.destroy();
        RenderEventDispatcher.onPostRender(this, mouseX, mouseY);
    }

    public void scroll(int change) {
        if (disabledScroll) {
            return;
        }
        offset += change;
        int min = 0;
        int contentHeight = rows * getItemSizeWithMargin();
        int visibleHeight = getVisibleRows() * getItemSizeWithMargin();
        int max = Utils.lowerBound(contentHeight - visibleHeight, 0);
        scrollUpArrow.setEnabled(true);
        scrollDownArrow.setEnabled(true);
        if (max == 0) {
            offset = 0;
            scrollUpArrow.setEnabled(false);
            scrollDownArrow.setEnabled(false);
        } else if (offset < min) {
            offset = min;
            scrollUpArrow.setEnabled(false);
        } else if (offset > max) {
            offset = max;
            scrollDownArrow.setEnabled(false);
        }

        reflow();
    }

    public void scrollUp(int change) {
        scroll(-change);
    }

    public void scrollUpUnit() {
        scroll(-getScrollSpeed());
    }

    public void scrollDown(int change) {
        scroll(change);
    }

    public void scrollDownUnit() {
        scroll(getScrollSpeed());
    }

    @Override
    public List<IWidget> getChildren() {
        return children;
    }

    public List<T> getContents() {
        return contents;
    }

    public WrappingList<T> addElement(T widget) {
        Preconditions.checkState(isValid());
        Preconditions.checkArgument(widget.getFullWidth() == getItemSize() && widget.getFullHeight() == getItemSize());
        widget.attach(this);
        contents.add(widget);
        reflow();
        return this;
    }

    public WrappingList<T> addElement(Collection<T> widgets) {
        Preconditions.checkState(isValid());
        for (T widget : widgets) {
            Preconditions.checkArgument(widget.getFullWidth() == getItemSize() && widget.getFullHeight() == getItemSize());
            widget.attach(this);
            contents.add(widget);
        }
        reflow();
        return this;
    }

    void setContentList(List<T> list) {
        this.contents = list;
        for (T widget : list) {
            widget.attach(this);
        }
    }

    @Override
    public void reflow() {
        int x = 0;
        int y = getFirstRowY();
        rows = 1;
        for (T child : contents) {
            child.setLocation(x, y);
            x += getItemSizeWithMargin();
            if (x > getWidth()) {
                x = 0;
                y += getItemSizeWithMargin();
                rows++;
            }
        }
    }

    private int getFirstRowY() {
        return -offset;
    }

    public void setDisabledScroll(boolean disabledScroll) {
        this.disabledScroll = disabledScroll;
    }

    public int getItemsPerRow() {
        return (int) Math.ceil((double) getWidth() / getItemSizeWithMargin());
    }

    public void setItemsPerRow(int itemsPerRow) {
        setWidth(itemsPerRow * getItemSizeWithMargin() - getMargin());
    }

    public int getVisibleRows() {
        return (int) Math.ceil((double) getHeight() / getItemSizeWithMargin());
    }

    public void setVisibleRows(int visibleRows) {
        setHeight(visibleRows * getItemSizeWithMargin() - getMargin());
    }

    public int getMargin() {
        return 4;
    }

    public int getItemSize() {
        return 16;
    }

    public int getItemSizeWithMargin() {
        return getItemSize() + getMargin();
    }

    public int getScrollSpeed() {
        return 5;
    }

    public ScrollArrow getScrollUpArrow() {
        return scrollUpArrow;
    }

    public ScrollArrow getScrollDownArrow() {
        return scrollDownArrow;
    }

    public void placeArrows(int x, int y) {
        scrollUpArrow.setLocation(x, y);
        alignArrows();
    }

    public void alignArrows() {
        scrollDownArrow.setLocation(scrollUpArrow.getX(), scrollUpArrow.getY() + scrollUpArrow.getFullHeight() + getMargin());
    }

    @Override
    public void provideInformation(ITextReceiver receiver) {
        super.provideInformation(receiver);
        receiver.line("Offset=" + offset);
    }
}