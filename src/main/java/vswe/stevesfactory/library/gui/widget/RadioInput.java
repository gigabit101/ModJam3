package vswe.stevesfactory.library.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Getter;
import lombok.Setter;
import vswe.stevesfactory.library.gui.Render2D;
import vswe.stevesfactory.library.gui.Texture;
import vswe.stevesfactory.library.gui.debug.ITextReceiver;
import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.widget.button.IButton;
import vswe.stevesfactory.library.gui.widget.mixin.LeafWidgetMixin;

import java.util.function.IntConsumer;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;

public class RadioInput extends AbstractWidget implements IButton, IRadioInput, LeafWidgetMixin {

    public static final Texture UNCHECKED = Render2D.ofFlowComponent(18, 20, 8, 8);
    public static final Texture CHECKED = UNCHECKED.moveRight(1);
    public static final Texture HOVERED_UNCHECKED = UNCHECKED.moveDown(1);
    public static final Texture HOVERED_CHECKED = CHECKED.moveDown(1);

    public static final Runnable DUMMY_RUNNABLE = () -> {};

    private final RadioController controller;
    private final int index;

    @Getter
    @Setter
    private IntConsumer clickAction = DUMMY;
    @Getter
    @Setter
    private Runnable checkAction = DUMMY_RUNNABLE;
    @Getter
    @Setter
    private Runnable uncheckAction = DUMMY_RUNNABLE;
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
        RenderSystem.enableAlphaTest();
        RenderSystem.color3f(1F, 1F, 1F);
        Texture texture = hovered
                ? (checked ? HOVERED_CHECKED : HOVERED_UNCHECKED)
                : (checked ? CHECKED : UNCHECKED);
        texture.render(getAbsoluteX(), getAbsoluteY(), getAbsoluteXRight(), getAbsoluteYBottom(), getZLevel());
        RenderEventDispatcher.onPostRender(this, mouseX, mouseY);
    }

    @Override
    public boolean onMouseClicked(double mouseX, double mouseY, int button) {
        clickAction.accept(button);
        if (button != GLFW_MOUSE_BUTTON_LEFT) {
            return false;
        }
        if (!checked) {
            setCheckedAndUpdate(true);
        }
        return true;
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        hovered = isInside(mouseX, mouseY);
    }

    protected void onCheck() {
        checkAction.run();
    }

    protected void onUncheck() {
        uncheckAction.run();
    }

    @Override
    public boolean hasClickAction() {
        return clickAction != DUMMY;
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
        this.checked = checked;
        if (checked) {
            onCheck();
        } else {
            onUncheck();
        }
    }

    @Override
    public void setCheckedAndUpdate(boolean checked) {
        setChecked(checked);
        if (checked) {
            controller.checkRadioButton(index);
        }
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
     * Same as {@link #isChecked()} because it a radio button doesn't really need a clicked style. Prefer to use the
     * mentioned method for its semantic advantages.
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
