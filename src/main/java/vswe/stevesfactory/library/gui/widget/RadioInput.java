package vswe.stevesfactory.library.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import vswe.stevesfactory.library.gui.Render2D;
import vswe.stevesfactory.library.gui.Texture;
import vswe.stevesfactory.library.gui.debug.ITextReceiver;
import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.widget.button.IButton;
import vswe.stevesfactory.library.gui.widget.mixin.LeafWidgetMixin;

import java.util.function.IntConsumer;

public class RadioInput extends AbstractWidget implements IButton, IRadioInput, LeafWidgetMixin {

    private static final Texture UNCHECKED = Texture.portion(Render2D.COMPONENTS, 256, 256, 0, 12, 8, 8);
    private static final Texture CHECKED = UNCHECKED.moveRight(1);
    private static final Texture HOVERED_UNCHECKED = UNCHECKED.moveDown(1);
    private static final Texture HOVERED_CHECKED = CHECKED.moveDown(1);

    private final RadioController controller;
    private final int index;

    private IntConsumer onClick = DUMMY;
    private boolean hovered;
    private boolean checked;

    public RadioInput(RadioController controller) {
        this.setDimensions(8, 8);
        this.controller = controller;
        this.index = controller.add(this);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        RenderEventDispatcher.onPreRender(this, mouseX, mouseY);
        RenderSystem.color3f(1F, 1F, 1F);
        Texture texture = hovered
                ? (checked ? HOVERED_CHECKED : HOVERED_UNCHECKED)
                : (checked ? CHECKED : UNCHECKED);
        texture.render(getAbsoluteX(), getAbsoluteY(), getAbsoluteXRight(), getAbsoluteYBottom(), getZLevel());
        RenderEventDispatcher.onPostRender(this, mouseX, mouseY);
    }

    @Override
    public boolean onMouseClicked(double mouseX, double mouseY, int button) {
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

    @Override
    public boolean hasClickAction() {
        return onClick != DUMMY;
    }

    @Override
    public IntConsumer getClickAction() {
        return onClick;
    }

    @Override
    public void setClickAction(IntConsumer action) {
        onClick = action;
    }

    public void setCheckAction(Runnable action) {
        setClickAction(__ -> {
            if (checked) {
                action.run();
            }
        });
    }

    @Override
    public boolean isHovered() {
        return hovered;
    }

    @Override
    public boolean isChecked() {
        return checked;
    }

    @Override
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

    @Override
    public void check(boolean checked) {
        setChecked(checked);
        if (checked) {
            controller.checkRadioButton(index);
        }
    }

    public Label label(String translationKey) {
        return new Label(this).translate(translationKey);
    }

    public Label textLabel(String text) {
        return new Label(this).text(text);
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
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
