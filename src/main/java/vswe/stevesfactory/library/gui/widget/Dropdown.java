package vswe.stevesfactory.library.gui.widget;

import com.google.common.collect.ImmutableList;
import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.layout.FlowLayout;
import vswe.stevesfactory.library.gui.screen.BackgroundRenderers;
import vswe.stevesfactory.library.gui.widget.panel.VerticalList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Dropdown<B extends IWidget, L extends B, P extends B> extends AbstractContainer<B> {

    public static <T extends IWidget> Dropdown<IWidget, Paragraph, VerticalList<T>> textAndList(int width, int headerHeight, int panelHeight) {
        Paragraph header = new Paragraph(width, headerHeight, new ArrayList<>());
        VerticalList<T> panel = new VerticalList<>();
        panel.setDimensions(width, panelHeight);
        return new Dropdown<>(header, panel);
    }

    public static final IBackgroundRenderer VANILLA4x4_BACKGROUND_RENDERER = (x1, y1, x2, y2, z, hovered, focused) -> BackgroundRenderers.drawVanillaStyle4x4(x1, y1, x2, y2, z);
    public static final IBackgroundRenderer VANILLA3x3_BACKGROUND_RENDERER = (x1, y1, x2, y2, z, hovered, focused) -> BackgroundRenderers.drawVanillaStyle3x3(x1, y1, x2, y2, z);
    public static final IBackgroundRenderer FLAT_BACKGROUND_RENDERER = (x1, y1, x2, y2, z, hovered, focused) -> BackgroundRenderers.drawFlatStyle(x1, y1, x2, y2, z);

    private IBackgroundRenderer backgroundRenderer;

    private L label;
    private P panel;
    private final List<B> children;

    private boolean expanded = false;

    public Dropdown(L label, P panel) {
        this.label = label;
        this.panel = panel;
        this.children = ImmutableList.of(label, panel);
        this.setWidth(Math.max(label.getFullWidth(), panel.getFullWidth()));
        this.setHeight(label.getFullHeight());
        this.setBackgroundRenderer(3, VANILLA3x3_BACKGROUND_RENDERER);
    }

    public IBackgroundRenderer getBackgroundRenderer() {
        return backgroundRenderer;
    }

    public void setBackgroundRenderer(int borderSize, IBackgroundRenderer backgroundRenderer) {
        this.setBorders(borderSize);
        this.backgroundRenderer = backgroundRenderer;
        this.reflow();
    }

    @Override
    public void onInitialAttach() {
        label.attach(this);
        panel.attach(this);
    }

    @Override
    public Collection<B> getChildren() {
        return children;
    }

    @Override
    public void reflow() {
        FlowLayout.vertical(children, 0, 0, getBorderBottom());
    }

    public L getLabel() {
        return label;
    }

    public P getPanel() {
        return panel;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public boolean isCollapsed() {
        return !expanded;
    }

    public void toggle() {
        expanded = !expanded;
        if (expanded) {
            setHeight(label.getFullHeight() + this.getBorderBottom() + panel.getFullHeight());
        } else {
            setHeight(label.getFullHeight());
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (expanded) {
            if (super.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        } else if (label.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (isInside(mouseX, mouseY)) {
            toggle();
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (expanded) {
            return super.mouseReleased(mouseX, mouseY, button);
        }
        return label.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (expanded) {
            return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }
        return label.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scroll) {
        if (expanded) {
            return super.mouseScrolled(mouseX, mouseY, scroll);
        }
        return label.mouseScrolled(mouseX, mouseY, scroll);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (expanded) {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
        return label.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (expanded) {
            return super.keyReleased(keyCode, scanCode, modifiers);
        }
        return label.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char charTyped, int keyCode) {
        if (expanded) {
            return super.charTyped(charTyped, keyCode);
        }
        return label.charTyped(charTyped, keyCode);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        if (expanded) {
            super.mouseMoved(mouseX, mouseY);
        }
        label.mouseMoved(mouseX, mouseY);
    }

    @Override
    public void update(float partialTicks) {
        if (expanded) {
            super.update(partialTicks);
        }
        label.update(partialTicks);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        RenderEventDispatcher.onPreRender(this, mouseX, mouseY);

        int x1 = getOuterAbsoluteX();
        int y1 = getOuterAbsoluteY();
        int width = getFullWidth();
        if (expanded) {
            backgroundRenderer.render(x1, y1, width, getFullHeight(), getZLevel(), false, false);
        }
        backgroundRenderer.render(x1, y1, width, getBorderTop() + label.getFullHeight() + getBorderBottom(), getZLevel(), false, false);
        label.render(mouseX, mouseY, partialTicks);
        if (expanded) {
            panel.render(mouseX, mouseY, partialTicks);
        }

        RenderEventDispatcher.onPostRender(this, mouseX, mouseY);
    }
}
