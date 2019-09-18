package vswe.stevesfactory.library.gui.widget;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.resources.I18n;
import vswe.stevesfactory.library.gui.TextureWrapper;
import vswe.stevesfactory.library.gui.debug.ITextReceiver;
import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.widget.mixin.LeafWidgetMixin;
import vswe.stevesfactory.utils.RenderingHelper;

public class RadioButton extends AbstractWidget implements IButton, LeafWidgetMixin {

    private static final TextureWrapper UNCHECKED = TextureWrapper.ofFlowComponent(18, 20, 8, 8);
    private static final TextureWrapper CHECKED = UNCHECKED.toRight(1);
    private static final TextureWrapper HOVERED_UNCHECKED = UNCHECKED.toDown(1);
    private static final TextureWrapper HOVERED_CHECKED = CHECKED.toDown(1);

    private final RadioController controller;
    private final int index;
    private String label = "";

    private boolean hovered;
    private boolean checked;

    public RadioButton(RadioController controller) {
        this.controller = controller;
        this.index = controller.add(this);
        this.setDimensions(8, 8);
    }

    @Override
    public void render(int mouseX, int mouseY, float particleTicks) {
        RenderEventDispatcher.onPreRender(this, mouseX, mouseY);
        GlStateManager.color3f(1F, 1F, 1F);
        TextureWrapper texture = hovered
                ? (checked ? HOVERED_CHECKED : HOVERED_UNCHECKED)
                : (checked ? CHECKED : UNCHECKED);
        int x1 = getAbsoluteX();
        int x2 = getAbsoluteXRight() + 2;
        int y1 = getAbsoluteY();
        int y2 = getAbsoluteYBottom();
        texture.draw(x1, y1);

        if (!label.isEmpty()) {
            RenderingHelper.drawTextCenteredVertically(label, x2, y1, y2, 0xff404040);
        }
        RenderEventDispatcher.onPostRender(this, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!checked) {
            check(true);
        }
        return true;
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        hovered = isInside(mouseX, mouseY);
    }

    protected void onStateUpdate(boolean oldValue) {
    }

    protected void onCheck() {
    }

    protected void onUncheck() {
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
    public boolean isHovered() {
        return hovered;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        boolean oldValue = this.checked;
        this.checked = checked;
        onStateUpdate(oldValue);
        if (checked) {
            onCheck();
        } else {
            onUncheck();
        }
    }

    public void check(boolean checked) {
        setChecked(checked);
        if (checked) {
            controller.checkRadioButton(index);
        }
    }

    public int getIndex() {
        return index;
    }

    public RadioController getRadioController() {
        return controller;
    }

    /**
     * Same as {@link #isChecked()} because it a radio button doesn't really need a clicked style. Prefer to use the mentioned method for
     * its semantic advantages.
     */
    @Override
    public boolean isClicked() {
        return checked;
    }

    @Override
    public void provideInformation(ITextReceiver receiver) {
        super.provideInformation(receiver);
        receiver.line("Hovered=" + hovered);
        receiver.line("Checked=" + checked);
        receiver.line("Index=" + index);
    }
}
