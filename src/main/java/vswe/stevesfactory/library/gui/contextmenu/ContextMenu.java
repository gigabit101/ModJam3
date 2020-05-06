package vswe.stevesfactory.library.gui.contextmenu;

import com.google.common.collect.Iterables;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHelper;
import vswe.stevesfactory.Config;
import vswe.stevesfactory.library.gui.Render2D;
import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.widget.IWidget;
import vswe.stevesfactory.library.gui.window.IPopupWindow;
import vswe.stevesfactory.library.gui.window.mixin.WindowEventHandlerMixin;
import vswe.stevesfactory.library.gui.window.mixin.WindowOverlayInfoMixin;
import vswe.stevesfactory.library.gui.window.mixin.WindowPropertiesMixin;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static vswe.stevesfactory.library.gui.Render2D.*;

public class ContextMenu implements IPopupWindow, WindowEventHandlerMixin, WindowOverlayInfoMixin, WindowPropertiesMixin {

    public static ContextMenu atCursor() {
        MouseHelper m = Minecraft.getInstance().mouseHelper;
        double scale = Minecraft.getInstance().getMainWindow().getGuiScaleFactor();
        double mouseX = m.getMouseX() / scale;
        double mouseY = m.getMouseY() / scale;
        return new ContextMenu((int) mouseX, (int) mouseY);
    }

    private final Point position;
    private final Dimension border;
    private final List<Section> sections;
    private IEntry focusedEntry;

    protected boolean alive = true;
    private int order = -1;

    public ContextMenu(int x, int y) {
        this(new Point(x, y));
    }

    public ContextMenu(Point position) {
        this.position = position;
        this.sections = new ArrayList<>();
        this.border = new Dimension();
    }

    public void reflow() {
        int width = 0;
        int height = 0;
        int y = 0;
        for (Section section : sections) {
            section.setLocation(0, y);
            section.reflow();
            y += section.getFullHeight();

            width = Math.max(width, section.getFullWidth());
            height += section.getFullHeight();
        }
        int border = getBorderSize();
        this.border.width = border + width + border;
        this.border.height = border + height + border;

        for (Section section : sections) {
            section.setWidth(width);
        }

        adjustForBorders(Config.CLIENT.minBorderDistance.get());
    }

    public void adjustForBorders(int minDistance) {
        int xOff = 0, yOff = 0;

        // Prefer to have the top left corner inside (if the context menu is too large to fit in the whole screen)
        int left = getX() - minDistance;
        if (left < 0) {
            xOff = left;
        } else {
            int right = getX() + getWidth() + minDistance;
            if (right > windowWidth()) {
                xOff = right - windowWidth();
            }
        }

        int top = getY() - minDistance;
        if (top < 0) {
            yOff = top;
        } else {
            int bottom = getY() + getHeight() + minDistance;
            if (bottom > windowHeight()) {
                yOff = bottom - windowHeight();
            }
        }

        position.x -= xOff;
        position.y -= yOff;
        for (Section section : sections) {
            section.onParentPositionChanged();
        }
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        RenderEventDispatcher.onPreRender(this, mouseX, mouseY);

        GlStateManager.disableTexture();
        beginColoredQuad();
        int x = getX();
        int y = getY();
        coloredRect(x, y, x + getWidth(), y + getHeight(), getZLevel(), 0xff4b4b4b);
        int cx = getContentX();
        int cy = getContentY();
        coloredRect(cx, cy, cx + getContentWidth(), cy + getContentHeight(), getZLevel(), 0xff3d3d3d);
        draw();
        GlStateManager.enableTexture();

        for (Section section : sections) {
            section.render(mouseX, mouseY, partialTicks);
        }
        RenderEventDispatcher.onPostRender(this, mouseX, mouseY);
    }

    @Nullable
    @Override
    public IWidget getFocusedWidget() {
        return focusedEntry;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isInside(mouseX, mouseY)) {
            discard();
        }
        WindowEventHandlerMixin.super.mouseClicked(mouseX, mouseY, button);
        return false;
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

    public boolean isLastSection(Section section) {
        return Iterables.getLast(sections) == section;
    }

    @Override
    public Dimension getBorder() {
        return border;
    }

    @Override
    public List<? extends Section> getChildren() {
        return sections;
    }

    @Override
    public Point getPosition() {
        return position;
    }

    @Override
    public float getZLevel() {
        return Render2D.CONTEXT_MENU_Z;
    }

    @Override
    public int getBorderSize() {
        return 1;
    }

    public void discard() {
        alive = false;
    }

    @Override
    public boolean shouldDiscard() {
        return !alive;
    }

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public void setOrder(int order) {
        // Ensure high order by setting the 31th bit (highest in i32) to 1
        this.order = order | (0b1 << 30);
    }

    public void addSection(Section section) {
        sections.add(section);
        section.attach(this);
        reflow();
    }

    void addSectionNoReflow(Section section) {
        sections.add(section);
        section.attach(this);
    }
}
