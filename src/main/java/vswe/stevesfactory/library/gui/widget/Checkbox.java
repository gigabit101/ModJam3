package vswe.stevesfactory.library.gui.widget;

import com.mojang.blaze3d.platform.GlStateManager;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import vswe.stevesfactory.library.gui.debug.ITextReceiver;
import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.widget.mixin.LeafWidgetMixin;

import static vswe.stevesfactory.library.gui.Render2D.*;

public class Checkbox extends AbstractWidget implements LeafWidgetMixin {

    public static final int NORMAL_BORDER = 0x4d4d4d;
    public static final int UNCHECKED = 0xc3c3c3;
    public static final int CHECKED = 0x5c9e2d;
    public static final int HOVERED_BORDER = 0x8d8d8d;
    public static final int HOVERED_UNCHECKED = 0xd7d6d6;
    public static final int HOVERED_CHECKED = 0x96bf79;

    private boolean checked = false;

    public BooleanConsumer onStateChange = b -> {
    };

    public Checkbox() {
        this.setDimensions(9, 9);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        RenderEventDispatcher.onPreRender(this, mouseX, mouseY);
        int x1 = getAbsoluteX();
        int y1 = getAbsoluteY();
        int x2 = getAbsoluteXRight();
        int y2 = getAbsoluteYBottom();
        boolean hovered = isInside(mouseX, mouseY);
        int borderColor = hovered ? HOVERED_BORDER : NORMAL_BORDER;
        int contentColor = hovered
                ? (checked ? HOVERED_CHECKED : HOVERED_UNCHECKED)
                : (checked ? CHECKED : UNCHECKED);

        GlStateManager.disableAlphaTest();
        GlStateManager.disableTexture();
        beginColoredQuad();
        coloredRect(x1, y1, x2, y2, getZLevel(), borderColor);
        coloredRect(x1 + 1, y1 + 1, x2 - 1, y2 - 1, getZLevel(), contentColor);
        draw();
        RenderEventDispatcher.onPostRender(this, mouseX, mouseY);
    }

    @Override
    public boolean onMouseClicked(double mouseX, double mouseY, int button) {
        setFocused(true);
        toggle();
        return true;
    }

    public void toggle() {
        checked = !checked;
        onStateChange.accept(checked);
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
        onStateChange.accept(checked);
    }

    public boolean isChecked() {
        return checked;
    }

    @Override
    public void provideInformation(ITextReceiver receiver) {
        super.provideInformation(receiver);
        receiver.line("Checked=" + checked);
    }
}
