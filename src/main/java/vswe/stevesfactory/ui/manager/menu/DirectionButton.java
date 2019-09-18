package vswe.stevesfactory.ui.manager.menu;

import net.minecraft.client.resources.I18n;
import net.minecraft.util.Direction;
import org.lwjgl.glfw.GLFW;
import vswe.stevesfactory.library.gui.widget.IWidget;
import vswe.stevesfactory.library.gui.TextureWrapper;
import vswe.stevesfactory.library.gui.debug.ITextReceiver;
import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.widget.AbstractIconButton;
import vswe.stevesfactory.utils.RenderingHelper;

import javax.annotation.Nonnull;
import java.util.Objects;

class DirectionButton extends AbstractIconButton {

    private static final TextureWrapper NORMAL = TextureWrapper.ofFlowComponent(0, 70, 31, 12);
    private static final TextureWrapper HOVERED = NORMAL.toDown(1);
    private static final TextureWrapper DISABLED = NORMAL.toDown(2);
    private static final TextureWrapper SELECTED_NORMAL = NORMAL.toRight(1);
    private static final TextureWrapper SELECTED_HOVERED = SELECTED_NORMAL.toDown(1);
    private static final TextureWrapper SELECTED_DISABLED = SELECTED_NORMAL.toDown(2);

    public boolean selected = false;
    private boolean editing = false;

    private final Direction direction;
    private final String name;

    public DirectionButton(Direction direction) {
        super(0, 0, 31, 12);
        this.direction = direction;
        this.name = I18n.format("gui.sfm." + direction.getName());
    }

    @Override
    public void render(int mouseX, int mouseY, float particleTicks) {
        super.render(mouseX, mouseY, particleTicks);
        RenderingHelper.drawTextCenteredVertically(name, getAbsoluteX() + 2, getAbsoluteY(), getAbsoluteYBottom(), 0xff4d4d4d);
    }

    @Override
    public TextureWrapper getTextureNormal() {
        return selected ? SELECTED_NORMAL : NORMAL;
    }

    @Override
    public TextureWrapper getTextureHovered() {
        return selected ? SELECTED_HOVERED : HOVERED;
    }

    @Override
    public TextureWrapper getTextureDisabled() {
        return selected ? SELECTED_DISABLED : DISABLED;
    }

    @Override
    protected void preRenderEvent(int mx, int my) {
        RenderEventDispatcher.onPreRender(this, mx, my);
    }

    @Override
    protected void postRenderEvent(int mx, int my) {
        RenderEventDispatcher.onPostRender(this, mx, my);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            setEditing(!editing);
            return super.mouseClicked(mouseX, mouseY, button);
        }
        return false;
    }

    public boolean isEditing() {
        return editing;
    }

    public void setEditing(boolean editing) {
        this.editing = editing;
        for (IWidget child : getParentWidget().getChildren()) {
            if (child instanceof DirectionButton && child != this) {
                child.setEnabled(!editing);
            }
        }
        if (editing) {
            setEnabled(true);
            getParentWidget().editDirection(this);
        } else {
            getParentWidget().clearEditing();
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (!enabled) {
            editing = false;
            getParentWidget().clearEditing();
        }
    }

    @Nonnull
    @Override
    public DirectionSelectionMenu<?> getParentWidget() {
        return Objects.requireNonNull((DirectionSelectionMenu<?>) super.getParentWidget());
    }

    @Override
    public void provideInformation(ITextReceiver receiver) {
        super.provideInformation(receiver);
        receiver.line("Selected=" + selected);
        receiver.line("Editing=" + editing);
    }
}
