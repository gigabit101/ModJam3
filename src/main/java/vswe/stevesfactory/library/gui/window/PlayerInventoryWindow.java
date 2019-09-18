package vswe.stevesfactory.library.gui.window;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import vswe.stevesfactory.library.gui.*;
import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.screen.BackgroundRenderers;
import vswe.stevesfactory.library.gui.widget.AbstractIconButton;
import vswe.stevesfactory.library.gui.widget.IWidget;
import vswe.stevesfactory.library.gui.widget.slot.AbstractItemSlot;
import vswe.stevesfactory.library.gui.widget.slot.ItemSlotPanel;
import vswe.stevesfactory.ui.manager.FactoryManagerGUI;

import java.util.List;
import java.util.function.Function;

public class PlayerInventoryWindow extends AbstractPopupWindow {

    private final List<IWidget> children;

    public PlayerInventoryWindow() {
        this(0, 0, ItemSlotPanel.DefaultSlot::new);
    }

    public PlayerInventoryWindow(int x, int y, Function<ItemStack, AbstractItemSlot> factory) {
        PlayerInventory playerInventory = Minecraft.getInstance().player.inventory;

        ItemSlotPanel inventory = new ItemSlotPanel(9, 3, playerInventory.mainInventory.subList(9, playerInventory.mainInventory.size()), factory);
        inventory.setWindow(this);
        inventory.setLocation(0, 8 + 2 + 1);
        ItemSlotPanel hotbar = new ItemSlotPanel(9, 1, playerInventory.mainInventory.subList(0, 9), factory);
        hotbar.setWindow(this);
        hotbar.setLocation(0, inventory.getY() + inventory.getHeight() + 4);
        AbstractIconButton close = new AbstractIconButton(inventory.getWidth() - 8 - 1, 1, 8, 8) {
            @Override
            public TextureWrapper getTextureNormal() {
                return FactoryManagerGUI.CLOSE_ICON;
            }

            @Override
            public TextureWrapper getTextureHovered() {
                return FactoryManagerGUI.CLOSE_ICON_HOVERED;
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                alive = false;
                return super.mouseClicked(mouseX, mouseY, button);
            }
        };
        close.setWindow(this);

        children = ImmutableList.of(close, inventory, hotbar);

        setPosition(x, y);
        setContents(inventory.getWidth(), hotbar.getY() + hotbar.getHeight());
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
    public void render(int mouseX, int mouseY, float particleTicks) {
        RenderEventDispatcher.onPreRender(this, mouseX, mouseY);
        GlStateManager.enableAlphaTest();
        BackgroundRenderers.drawVanillaStyle(getX(), getY(), getWidth(), getHeight(), 0F);
        renderChildren(mouseX, mouseY, particleTicks);
        RenderEventDispatcher.onPostRender(this, mouseX, mouseY);
    }
}
