package vswe.stevesfactory.library.gui.actionmenu;

import com.google.common.base.Preconditions;
import vswe.stevesfactory.library.gui.widget.IWidget;
import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.window.AbstractPopupWindow;
import vswe.stevesfactory.utils.RenderingHelper;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;

public class ActionMenu extends AbstractPopupWindow {

    public static ActionMenu atCursor(double mouseX, double mouseY, List<? extends IEntry> entries) {
        return new ActionMenu((int) mouseX, (int) mouseY, entries);
    }

    private final List<? extends IEntry> entries;
    private IEntry focusedEntry;

    public ActionMenu(int x, int y, List<? extends IEntry> entries) {
        Preconditions.checkArgument(!entries.isEmpty());

        this.entries = entries;
        int width = entries.stream()
                .max(Comparator.comparingInt(IEntry::getWidth))
                .orElseThrow(IllegalArgumentException::new)
                .getWidth();
        int height = entries.stream()
                .mapToInt(IEntry::getHeight)
                .sum();
        setContents(width, height);
        setPosition(x, y);

        int ey = 0;
        for (IEntry e : entries) {
            e.attach(this);
            e.setLocation(0, ey);
            e.setWidth(contents.width);
            ey += e.getHeight();
        }
    }

    @Override
    public void render(int mouseX, int mouseY, float particleTicks) {
        RenderEventDispatcher.onPreRender(this, mouseX, mouseY);
        RenderingHelper.drawRect(position, border, 75, 75, 75, 255);
        RenderingHelper.drawRect(getContentX(), getContentY(), contents, 61, 61, 61, 255);
        for (IEntry entry : entries) {
            entry.render(mouseX, mouseY, particleTicks);
        }
        RenderEventDispatcher.onPostRender(this, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isInside(mouseX, mouseY)) {
            alive = false;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Nullable
    @Override
    public IWidget getFocusedWidget() {
        return focusedEntry;
    }

    @Override
    public void setFocusedWidget(@Nullable IWidget widget) {
        if (widget instanceof IEntry || widget == null) {
            if (focusedEntry != null) {
                focusedEntry.onFocusChanged(false);
            }
            focusedEntry = (IEntry) widget;
            if (widget != null) {
                widget.onFocusChanged(true);
            }
        }
    }

    @Override
    public List<? extends IEntry> getChildren() {
        return entries;
    }

    @Override
    public boolean shouldDrag() {
        return false;
    }

    @Override
    public int getBorderSize() {
        return 1;
    }
}
