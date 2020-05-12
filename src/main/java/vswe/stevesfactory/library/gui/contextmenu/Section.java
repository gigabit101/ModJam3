package vswe.stevesfactory.library.gui.contextmenu;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.systems.RenderSystem;
import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.widget.AbstractContainer;
import vswe.stevesfactory.library.gui.window.IWindow;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static vswe.stevesfactory.library.gui.contextmenu.DefaultEntry.HALF_MARGIN_SIDES;
import static vswe.stevesfactory.library.gui.contextmenu.DefaultEntry.MARGIN_SIDES;

public class Section extends AbstractContainer<IEntry> {

    private static final float LINE_COLOR = 125F / 256F;

    private List<IEntry> entries = new ArrayList<>();

    // Relative position is (0,0) by default

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        RenderEventDispatcher.onPreRender(this, mouseX, mouseY);
        renderChildren(mouseX, mouseY, partialTicks);
        if (!getContextMenu().isLastSection(this)) {
            renderLine();
        }
        RenderEventDispatcher.onPostRender(this, mouseX, mouseY);
    }

    private void renderLine() {
        int bx = getAbsoluteX() + HALF_MARGIN_SIDES;
        int bx2 = getAbsoluteXRight() - HALF_MARGIN_SIDES;
        int by = getAbsoluteYBottom() + 1;
        RenderSystem.disableTexture();
        RenderSystem.color3f(LINE_COLOR, LINE_COLOR, LINE_COLOR);
        RenderSystem.lineWidth(1F);
        glBegin(GL_LINES);
        glVertex3f(bx, by, getZLevel());
        glVertex3f(bx2, by, getZLevel());
        glEnd();
        RenderSystem.enableTexture();
    }

    public void attach(ContextMenu contextMenu) {
        super.attachWindow(contextMenu);
        Dimension bounds = getDimensions();
        bounds.width = contextMenu.getWidth() - MARGIN_SIDES * 2;
        bounds.height = MARGIN_SIDES;
    }

    @Deprecated
    @Override
    public void attachWindow(IWindow window) {
        throw new UnsupportedOperationException("Use #attach(ContextMenu) instead!");
    }

    @Override
    public void reflow() {
        int width = 0;
        int height = 0;
        int y = 0;
        for (IEntry entry : entries) {
            width = Math.max(width, entry.getFullWidth());
            height += entry.getFullHeight();
            entry.setLocation(0, y);
            y += entry.getFullHeight();
        }
        setDimensions(width, height);
        if (!getContextMenu().isLastSection(this)) {
            setBorderBottom(3);
        }
    }

    @Override
    public Collection<IEntry> getChildren() {
        return entries;
    }

    @Override
    public Section addChildren(IEntry widget) {
        Preconditions.checkState(isValid());
        entries.add(widget);
        widget.attach(this);
        return this;
    }

    @Override
    public Section addChildren(Collection<IEntry> widgets) {
        Preconditions.checkState(isValid());
        entries.addAll(widgets);
        for (IEntry widget : widgets) {
            widget.attach(this);
        }
        return this;
    }

    public ContextMenu getContextMenu() {
        return (ContextMenu) super.getWindow();
    }

    @Override
    public void setWidth(int width) {
        super.setWidth(width);
        for (IEntry entry : entries) {
            entry.setWidth(width);
        }
    }
}
