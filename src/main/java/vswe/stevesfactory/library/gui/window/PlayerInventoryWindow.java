package vswe.stevesfactory.library.gui.window;

import com.google.common.collect.ImmutableList;
import lombok.val;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import vswe.stevesfactory.library.gui.Render2D;
import vswe.stevesfactory.library.gui.Texture;
import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.widget.IWidget;
import vswe.stevesfactory.library.gui.widget.button.SimpleIconButton;
import vswe.stevesfactory.library.gui.widget.slot.*;

import java.util.List;
import java.util.function.Function;

public class PlayerInventoryWindow extends AbstractPopupWindow {

    private final List<IWidget> children;

    public PlayerInventoryWindow() {
        this(ItemSlot::new);
    }

    public PlayerInventoryWindow(Function<ItemStack, AbstractItemSlot> factory) {
        val inv = Minecraft.getInstance().player.inventory;
        val inventory = new ItemSlotPanel(9, 3, inv.mainInventory.subList(9, inv.mainInventory.size()), factory);
        inventory.attachWindow(this);
        inventory.setBorderTop(9 + 2);
        inventory.setBorderBottom(4);
        val hotbar = new ItemSlotPanel(9, 1, inv.mainInventory.subList(0, 9), factory);
        hotbar.attachWindow(this);
        hotbar.alignTop(inventory.getYBottom());
        val close = new SimpleIconButton(Render2D.CLOSE_ICON, Render2D.CLOSE_ICON_HOVERED);
        close.attachWindow(this);
        close.alignBottom(inventory.getInnerY() - 1);
        close.alignRight(inventory.getXRight() - 1);
        close.setClickAction(b -> discard());
        children = ImmutableList.of(close, inventory, hotbar);

        this.setContents(inventory.getWidth(), inventory.getFullHeight() + hotbar.getFullHeight());
    }

    @Override
    public int getBorderSize() {
        return 4;
    }

    @Override
    public List<? extends IWidget> getChildren() {
        return children;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        RenderEventDispatcher.onPreRender(this, mouseX, mouseY);
        renderVanillaStyleBackground();
        renderChildren(mouseX, mouseY, partialTicks);
        RenderEventDispatcher.onPostRender(this, mouseX, mouseY);
    }
}
