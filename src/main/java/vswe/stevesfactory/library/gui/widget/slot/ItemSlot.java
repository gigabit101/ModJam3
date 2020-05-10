package vswe.stevesfactory.library.gui.widget.slot;

import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.item.ItemStack;
import vswe.stevesfactory.library.gui.Render2D;
import vswe.stevesfactory.library.gui.screen.WidgetScreen;
import vswe.stevesfactory.library.gui.window.PlayerInventoryWindow;

import javax.xml.ws.Holder;
import java.util.function.IntConsumer;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT;

@Getter
@Setter
public class ItemSlot extends AbstractItemSlot {

    private ItemStack renderedStack;
    private IntConsumer action;

    public ItemSlot(ItemStack renderedStack) {
        this(renderedStack, b -> {});
    }

    public ItemSlot(ItemStack renderedStack, IntConsumer action) {
        this.renderedStack = renderedStack;
        this.action = action;
    }

    @Override
    public boolean onMouseClicked(double mouseX, double mouseY, int button) {
        action.accept(button);
        return true;
    }

    public void clearRenderedStack() {
        setRenderedStack(ItemStack.EMPTY);
    }

    public void defaultedLeft(Runnable rightClick) {
        setAction(b -> {
            switch (b) {
                case GLFW_MOUSE_BUTTON_LEFT:
                    createSelectItemPopup();
                    break;
                case GLFW_MOUSE_BUTTON_RIGHT:
                    rightClick.run();
                    break;
            }
        });
    }

    public void defaultedRight(Runnable leftClick) {
        setAction(b -> {
            switch (b) {
                case GLFW_MOUSE_BUTTON_LEFT:
                    leftClick.run();
                    break;
                case GLFW_MOUSE_BUTTON_RIGHT:
                    clearRenderedStack();
                    break;
            }
        });
    }

    public void defaultedBoth() {
        setAction(b -> {
            switch (b) {
                case GLFW_MOUSE_BUTTON_LEFT:
                    createSelectItemPopup();
                    break;
                case GLFW_MOUSE_BUTTON_RIGHT:
                    clearRenderedStack();
                    break;
            }
        });
    }

    public void customBoth(Runnable leftClick, Runnable rightClick) {
        setAction(b -> {
            switch (b) {
                case GLFW_MOUSE_BUTTON_LEFT:
                    leftClick.run();
                    break;
                case GLFW_MOUSE_BUTTON_RIGHT:
                    rightClick.run();
                    break;
            }
        });
    }

    public void createSelectItemPopup() {
        WidgetScreen.assertActive().addPopupWindow(this.selectItemPopup());
    }

    public PlayerInventoryWindow selectItemPopup() {
        Holder<AbstractItemSlot> selected = new Holder<>();
        return new PlayerInventoryWindow(
                Render2D.mouseX(), Render2D.mouseY(),
                in -> new AbstractItemSlot() {
                    private ItemStack representative;

                    @Override
                    public ItemStack getRenderedStack() {
                        return in;
                    }

                    @Override
                    public boolean onMouseClicked(double mouseX, double mouseY, int button) {
                        if (isSelected() || in.isEmpty()) {
                            // Unselect slot
                            selected.value = null;
                            ItemSlot.this.setRenderedStack(ItemStack.EMPTY);
                        } else {
                            // Select and set slot content
                            selected.value = this;
                            ItemSlot.this.setRenderedStack(getRepresentative());
                        }
                        return true;
                    }

                    @Override
                    public void renderBase() {
                        super.renderBase();
                        if (isSelected() && !in.isEmpty()) {
                            Render2D.useBlendingGLStates();
                            Render2D.beginColoredQuad();
                            Render2D.coloredRect(getAbsoluteX(), getAbsoluteY(), getAbsoluteXRight(), getAbsoluteYBottom(), 0x66ffff00);
                            Render2D.draw();
                            RenderSystem.disableBlend();
                            RenderSystem.enableTexture();
                        }
                    }

                    private boolean isSelected() {
                        return selected.value == this;
                    }

                    private ItemStack getRepresentative() {
                        if (representative == null) {
                            representative = in.copy();
                            representative.setCount(1);
                        }
                        return representative;
                    }
                });
    }
}
