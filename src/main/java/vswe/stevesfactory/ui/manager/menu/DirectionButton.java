package vswe.stevesfactory.ui.manager.menu;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.Direction;
import vswe.stevesfactory.library.gui.Render2D;
import vswe.stevesfactory.library.gui.Texture;
import vswe.stevesfactory.library.gui.debug.ITextReceiver;
import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.widget.IWidget;
import vswe.stevesfactory.library.gui.widget.button.AbstractIconButton;

import javax.annotation.Nonnull;
import java.util.Objects;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;

class DirectionButton extends AbstractIconButton {

    public static final Texture NORMAL = Render2D.ofFlowComponent(0, 70, 31, 12);
    public static final Texture HOVERED = NORMAL.down(1);
    public static final Texture DISABLED = NORMAL.down(2);
    public static final Texture SELECTED_NORMAL = NORMAL.right(1);
    public static final Texture SELECTED_HOVERED = SELECTED_NORMAL.down(1);
    public static final Texture SELECTED_DISABLED = SELECTED_NORMAL.down(2);

    private boolean selected = false;
    private boolean editing = false;

    public BooleanConsumer onStateChanged = b -> {
    };
    private final String name;

    public DirectionButton(Direction direction) {
        this.setDimensions(31, 12);
        this.name = I18n.format("gui.sfm." + direction.getName());
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);
        Render2D.renderVerticallyCenteredText(name, getAbsoluteX() + 2, getAbsoluteY(), getAbsoluteYBottom(), getZLevel(), 0xff4d4d4d);
    }

    @Override
    public Texture getTextureNormal() {
        return selected ? SELECTED_NORMAL : NORMAL;
    }

    @Override
    public Texture getTextureHovered() {
        return selected ? SELECTED_HOVERED : HOVERED;
    }

    @Override
    public Texture getTextureDisabled() {
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
    public boolean onMouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW_MOUSE_BUTTON_LEFT) {
            setEditing(!editing);
            return super.onMouseClicked(mouseX, mouseY, button);
        }
        return false;
    }

    public boolean isEditing() {
        return editing;
    }

    public void setEditing(boolean editing) {
        this.editing = editing;
        for (IWidget child : getParent().getChildren()) {
            if (child instanceof DirectionButton && child != this) {
                child.setEnabled(!editing);
            }
        }
        if (editing) {
            setEnabled(true);
            getParent().editDirection(this);
        } else {
            getParent().clearEditing();
        }
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        onStateChanged.accept(selected);
    }

    public void toggleSelected() {
        setSelected(!selected);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (!enabled) {
            editing = false;
            getParent().clearEditing();
        }
    }

    @Nonnull
    @Override
    public DirectionSelectionMenu<?> getParent() {
        return Objects.requireNonNull((DirectionSelectionMenu<?>) super.getParent());
    }

    @Override
    public void provideInformation(ITextReceiver receiver) {
        super.provideInformation(receiver);
        receiver.line("Selected=" + selected);
        receiver.line("Editing=" + editing);
    }
}
