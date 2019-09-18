package vswe.stevesfactory.library.gui.widget;

import com.mojang.blaze3d.platform.GlStateManager;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import org.lwjgl.opengl.GL11;
import vswe.stevesfactory.library.gui.debug.ITextReceiver;
import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.widget.mixin.LeafWidgetMixin;
import vswe.stevesfactory.utils.RenderingHelper;

import static vswe.stevesfactory.utils.RenderingHelper.getRenderer;
import static vswe.stevesfactory.utils.RenderingHelper.rectVertices;

public class Checkbox extends AbstractWidget implements LeafWidgetMixin {

    public static final int NORMAL_BORDER = 0x4d4d4d;
    public static final int UNCHECKED = 0xc3c3c3;
    public static final int CHECKED = 0x5c9e2d;
    public static final int HOVERED_BORDER = 0x8d8d8d;
    public static final int HOVERED_UNCHECKED = 0xd7d6d6;
    public static final int HOVERED_CHECKED = 0x96bf79;

    private boolean checked = false;
    private String label = "";

    public BooleanConsumer onStateChange = b -> {};

    public Checkbox() {
        this(0, 0, 9, 9);
    }

    public Checkbox(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    @Override
    public void render(int mouseX, int mouseY, float particleTicks) {
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
        getRenderer().begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        rectVertices(x1, y1, x2, y2, borderColor);
        rectVertices(x1 + 1, y1 + 1, x2 - 1, y2 - 1, contentColor);
        Tessellator.getInstance().draw();

        if (!label.isEmpty()) {
            RenderingHelper.drawTextCenteredVertically(label, x2 + 2, y1, y2, 0xff404040);
        }
        RenderEventDispatcher.onPostRender(this, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        getWindow().setFocusedWidget(this);
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

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void translateLabel(String translationKey) {
        label = I18n.format(translationKey);
    }

    public void translateLabel(String translationKey, Object... args) {
        label = I18n.format(translationKey, args);
    }

    @Override
    public void provideInformation(ITextReceiver receiver) {
        super.provideInformation(receiver);
        receiver.line("Checked=" + checked);
    }
}
