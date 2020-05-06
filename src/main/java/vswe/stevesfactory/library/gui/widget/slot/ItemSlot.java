package vswe.stevesfactory.library.gui.widget.slot;

import net.minecraft.item.ItemStack;

import java.util.function.IntConsumer;

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
        action.accept(button);
        return true;
    }

    @Override
    public ItemStack getRenderedStack() {
        return renderedStack;
    }

    public void setRenderedStack(ItemStack renderedStack) {
        this.renderedStack = renderedStack;
    }
}
