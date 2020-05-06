package vswe.stevesfactory.library.gui.widget;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import vswe.stevesfactory.library.gui.debug.ITextReceiver;
import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.widget.mixin.LeafWidgetMixin;

import static vswe.stevesfactory.library.gui.Render2D.*;

public class Switch extends AbstractWidget implements LeafWidgetMixin {

    private boolean active = false;

    public BooleanConsumer onStateChange = b -> {
    };

    public Switch() {
        this.setDimensions(16, 12);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        RenderEventDispatcher.onPreRender(this, mouseX, mouseY);
        beginColoredQuad();
        renderSlot();
        renderSwitch();
        draw();
        RenderEventDispatcher.onPostRender(this, mouseX, mouseY);
    }

    private void renderSwitch() {
        int x1, x2;
        int y1 = getAbsoluteY();
        int y2 = getAbsoluteYBottom();
        if (active) {
            x2 = getAbsoluteXRight() - 1;
            x1 = x2 - 5;
        } else {
            x1 = getAbsoluteX() + 1;
            x2 = x1 + 5;
        }
        coloredRect(x1, y1, x2, y2, getZLevel(), getSwitchBorder());
        coloredRect(x1 + 1, y1 + 1, x2 - 1, y2 - 1, getZLevel(), getSwitchFiller());
    }

    private void renderSlot() {
        int x1 = getAbsoluteX();
        int y1 = getAbsoluteY() + 1;
        int x2 = getAbsoluteXRight();
        int y2 = getAbsoluteYBottom() - 1;
        coloredRect(x1, y1, x2, y2, getZLevel(), getSlotBorder());
        coloredRect(x1 + 1, y1 + 1, x2 - 1, y2 - 1, getZLevel(), active ? getSlotFillerActive() : getSlotFillerInactive());
    }

    public int getSlotBorder() {
        return 0xff373737;
    }

    public int getSlotFillerActive() {
        return 0xff5c9e2d;
    }

    public int getSlotFillerInactive() {
        return 0xff979797;
    }

    public int getSwitchBorder() {
        return 0xffa4a4a4;
    }

    public int getSwitchFiller() {
        return 0xffc3c3c3;
    }

    @Override
    public boolean onMouseClicked(double mouseX, double mouseY, int button) {
        setFocused(true);
        toggle();
        return true;
    }

    public void toggle() {
        active = !active;
        onStateChange.accept(active);
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        boolean oldActive = this.active;
        this.active = active;
        if (oldActive != active) {
            onStateChange.accept(active);
        }
    }

    @Override
    public void provideInformation(ITextReceiver receiver) {
        super.provideInformation(receiver);
        receiver.line("Active=" + active);
    }
}
