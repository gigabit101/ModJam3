/* Code adapted from net.minecraftforge.client.gui.ScrollPanel
 */

package vswe.stevesfactory.library.gui.widget.panel;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.client.gui.GuiUtils;
import org.lwjgl.glfw.GLFW;
import vswe.stevesfactory.Config;
import vswe.stevesfactory.library.gui.ScissorTest;
import vswe.stevesfactory.library.gui.debug.ITextReceiver;
import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.widget.AbstractContainer;
import vswe.stevesfactory.library.gui.widget.IWidget;
import vswe.stevesfactory.utils.Utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.lwjgl.opengl.GL11.GL_QUADS;
import static vswe.stevesfactory.library.gui.Render2D.coloredRect;

public class VerticalList<T extends IWidget> extends AbstractContainer<T> implements IWidget {

    private boolean scrolling;
    protected float scrollDistance;

    private final List<T> elements;

    public VerticalList() {
        this.elements = new ArrayList<>();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isInside(mouseX, mouseY)) {
            return false;
        }
        scrolling = button == GLFW.GLFW_MOUSE_BUTTON_LEFT && isInsideBar(mouseX, mouseY) && isDrawingScrollBar();
        if (scrolling) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean isInsideBar(double mouseX, double mouseY) {
        int barLeftX = getAbsBarLeft();
        return mouseX >= barLeftX && mouseX < barLeftX + getBarWidth()
                && mouseY >= getAbsoluteY() && mouseY < getAbsoluteYBottom();
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (isInside(mouseX, mouseY)) {
            boolean ret = scrolling;
            scrolling = false;
            return ret;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
            return true;
        }
        if (scrolling) {
            int maxScroll = getHeight() - getBarHeight();
            double moved = deltaY / maxScroll;
            scrollDistance += getMaxScroll() * moved;
            applyScrollLimits();
            reflow();
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scroll) {
        if (super.mouseScrolled(mouseX, mouseY, scroll)) {
            return true;
        }
        if (isInside(mouseX, mouseY) && scroll != 0) {
            scrollDistance += -scroll * getScrollAmount();
            applyScrollLimits();
            reflow();
            return true;
        }
        return false;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        if (isEnabled()) {
            RenderEventDispatcher.onPreRender(this, mouseX, mouseY);
            drawBackground();

            int left = getAbsoluteX();
            int top = getAbsoluteY();
            int right = getAbsoluteXRight();
            int bottom = getAbsoluteYBottom();
            int width = getWidth();
            int height = getHeight();

            Tessellator tess = Tessellator.getInstance();
            BufferBuilder renderer = tess.getBuffer();

            ScissorTest test = ScissorTest.scaled(left, top, width, height);

            for (T child : getChildren()) {
                child.render(mouseX, mouseY, partialTicks);
            }
            drawOverlay();

            int extraHeight = getBarExtraHeight();
            if (extraHeight > 0 && isDrawingScrollBar()) {
                int barWidth = getBarWidth();
                int barHeight = getBarHeight();
                int barTopY = Utils.lowerBound((int) scrollDistance * (height - barHeight) / extraHeight + top, top);
                int barBottomY = barTopY + barHeight;
                int barLeftX = getAbsBarLeft();
                int barRightX = barLeftX + barWidth;

                RenderSystem.disableDepthTest();
                RenderSystem.disableTexture();
                renderer.begin(GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
                coloredRect(barLeftX, top, barRightX, bottom, getShadowColor());
                coloredRect(barLeftX, barTopY, barRightX, barBottomY, getBarBorderColor());
                coloredRect(barLeftX, barTopY, barRightX - 1, barBottomY - 1, getBarBodyColor());
                tess.draw();
                RenderSystem.enableTexture();
            }

            test.destroy();
            RenderEventDispatcher.onPostRender(this, mouseX, mouseY);
        }
    }

    protected boolean isDrawingScrollBar() {
        return true;
    }

    public int getBarBodyColor() {
        return 0xffc0c0c0;
    }

    public int getBarBorderColor() {
        return 0xff808080;
    }

    public int getShadowColor() {
        return 0xff000000;
    }

    protected void drawOverlay() {
    }

    /**
     * Draw a vanilla style overlay.
     * <p>
     * If there is no world loaded, it will draw a dirt background; if a world is loaded, it will simply draw a vertical gradient rectangle
     * from {@code 0xc0101010} to {@code 0xd0101010}.
     */
    protected final void drawDefaultOverlay() {
        int left = getAbsoluteX();
        int top = getAbsoluteY();
        int right = getAbsoluteXRight();
        int bottom = getAbsoluteYBottom();
        if (Minecraft.getInstance().world != null) {
            GuiUtils.drawGradientRect(0, left, top, right, bottom, 0xc0101010, 0xd0101010);
        } else {
            // Draw dark dirt background
            RenderSystem.disableLighting();
            RenderSystem.disableFog();
            Minecraft.getInstance().getTextureManager().bindTexture(AbstractGui.BACKGROUND_LOCATION);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            float texScale = 32.0F;
            BufferBuilder renderer = Tessellator.getInstance().getBuffer();
            renderer.begin(GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
            renderer.pos(left, bottom, 0.0D).tex(left / texScale, (bottom + (int) scrollDistance) / texScale).color(0x20, 0x20, 0x20, 0xFF).endVertex();
            renderer.pos(right, bottom, 0.0D).tex(right / texScale, (bottom + (int) scrollDistance) / texScale).color(0x20, 0x20, 0x20, 0xFF).endVertex();
            renderer.pos(right, top, 0.0D).tex(right / texScale, (top + (int) scrollDistance) / texScale).color(0x20, 0x20, 0x20, 0xFF).endVertex();
            renderer.pos(left, top, 0.0D).tex(left / texScale, (top + (int) scrollDistance) / texScale).color(0x20, 0x20, 0x20, 0xFF).endVertex();
            Tessellator.getInstance().draw();
        }
    }

    protected void drawBackground() {
    }

    @Override
    public List<T> getChildren() {
        return elements;
    }

    @Override
    public void reflow() {
        int offset = (int) -scrollDistance;
        int y = getBorder();
        for (T child : getChildren()) {
            child.setY(y + offset);
            y += child.getHeight() + getMarginMiddle();
        }
    }

    @Override
    public VerticalList<T> addChildren(T widget) {
        elements.add(widget);
        widget.attach(this);
        return this;
    }

    @Override
    public VerticalList<T> addChildren(Collection<T> widgets) {
        elements.addAll(widgets);
        for (T widget : widgets) {
            widget.attach(this);
        }
        return this;
    }

    protected int getContentHeight() {
        int contentHeight = 0;
        for (T child : getChildren()) {
            contentHeight += child.getHeight() + getMarginMiddle();
        }
        // Remove last unnecessary border
        return contentHeight - getMarginMiddle();
    }

    public int getFirstRowY() {
        return getAbsoluteY() + getBorder();
    }

    public int getScrollAmount() {
        return Config.CLIENT.scrollSpeed.get();
    }

    public int getMarginMiddle() {
        return 4;
    }

    public int getBarLeft() {
        return getX() + getWidth() - getBarWidth();
    }

    public int getAbsBarLeft() {
        return getAbsoluteX() + getBarLeft();
    }

    public int getMaxScroll() {
        return getContentHeight() - (getHeight() - getBorder());
    }

    private void applyScrollLimits() {
        int max = Utils.lowerBound(getMaxScroll(), 0);
        scrollDistance = MathHelper.clamp(scrollDistance, 0.0F, max);
    }

    public int getBarWidth() {
        return 6;
    }

    public int getBarHeight() {
        int height = getHeight();
        return MathHelper.clamp((height * height) / getContentHeight(), 32, height - getBorder() * 2);
    }

    public int getAbsBarTop() {
        int top = getAbsoluteY();
        return Utils.lowerBound((int) scrollDistance * (getHeight() - getBarHeight()) / getBarExtraHeight() + top, top);
    }

    public int getAbsBarBottom() {
        return getAbsBarTop() + getBarHeight();
    }

    public int getBarExtraHeight() {
        return (getContentHeight() + getBorder()) - getHeight();
    }

    public int getBorder() {
        return 4;
    }

    public float getScrollDistance() {
        return scrollDistance;
    }

    public void setScrollDistance(float scrollDistance) {
        this.scrollDistance = scrollDistance;
        applyScrollLimits();
        reflow();
    }

    @Override
    public void provideInformation(ITextReceiver receiver) {
        super.provideInformation(receiver);
        receiver.line("Offset=" + scrollDistance);
    }
}
