package vswe.stevesfactory.library.gui.widget.slot;

import com.mojang.blaze3d.systems.RenderSystem;
import lombok.*;
import net.minecraft.item.ItemStack;
import vswe.stevesfactory.library.gui.Render2D;
import vswe.stevesfactory.library.gui.contextmenu.CallbackEntry;
import vswe.stevesfactory.library.gui.screen.WidgetScreen;
import vswe.stevesfactory.library.gui.window.PlayerInventoryWindow;

import javax.xml.ws.Holder;
import java.util.function.IntConsumer;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;

@Getter
@Setter
public class ItemSlot extends AbstractItemSlot {

    private ItemStack renderedStack;
    private IntConsumer action;

    public ItemSlot(ItemStack renderedStack) {
        this(renderedStack, b -> {
        });
    }

    public ItemSlot(ItemStack renderedStack, IntConsumer action) {
        this.renderedStack = renderedStack;
        this.action = action;
    }

    @Override
    public boolean onMouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW_MOUSE_BUTTON_LEFT) {
            action.accept(button);
        }
        return true;
    }

    public void clearRenderedStack() {
        setRenderedStack(ItemStack.EMPTY);
    }

    public void createSelectItemPopup() {
        val popup = this.selectItemPopup();
        popup.centralize();
        WidgetScreen.assertActive().addPopupWindow(popup);
    }

    public PlayerInventoryWindow selectItemPopup() {
        val selected = new Holder<AbstractItemSlot>();
        return new PlayerInventoryWindow(
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

    public void setInventorySelectAction() {
        setAction(b -> {
            if (b == GLFW_MOUSE_BUTTON_LEFT) {
                createSelectItemPopup();
            }
        });
    }

    public void setCtxMenuClear() {
        addCtxMenuListener(builder -> {
            val section = builder.obtainSection("Misc");
            section.addChildren(new CallbackEntry(
                    null,
                    "gui.sfm.clear",
                    b -> this.clearRenderedStack()));
        });
    }
}
