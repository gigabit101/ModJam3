package vswe.stevesfactory.library.gui.window;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import vswe.stevesfactory.library.gui.*;
import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.widget.IWidget;
import vswe.stevesfactory.library.gui.widget.button.SimpleIconButton;
import vswe.stevesfactory.library.gui.widget.slot.*;

import java.util.List;
import java.util.function.Function;

public class PlayerInventoryWindow extends AbstractPopupWindow {

    private static final Texture CLOSE = Texture.portion(Render2D.CLOSE, 16, 16, 0, 0, 16, 16);

    private final List<IWidget> children;

    public PlayerInventoryWindow() {
        this(0, 0, ItemSlot::new);
    }

    public PlayerInventoryWindow(int x, int y, Function<ItemStack, AbstractItemSlot> factory) {
        setPosition(x, y);

        PlayerInventory playerInventory = Minecraft.getInstance().player.inventory;
        ItemSlotPanel inventory = new ItemSlotPanel(9, 3, playerInventory.mainInventory.subList(9, playerInventory.mainInventory.size()), factory);
        inventory.setBorderTop(8 + 2);
        inventory.setBorderBottom(4);
        inventory.setLocation(0, 0);
        ItemSlotPanel hotbar = new ItemSlotPanel(9, 1, playerInventory.mainInventory.subList(0, 9), factory);
        hotbar.setLocation(0, inventory.getYBottom());
        SimpleIconButton close = new SimpleIconButton(CLOSE, CLOSE);
        close.setLocation(inventory.getWidth() - 8, 0);
        close.setClickAction(b -> discard());
        children = ImmutableList.of(close, inventory, hotbar);

        setContents(inventory.getWidth(), inventory.getFullHeight() + hotbar.getFullHeight());
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
