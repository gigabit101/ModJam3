package vswe.stevesfactory.library.gui.widget.slot;

import com.google.common.base.MoreObjects;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import vswe.stevesfactory.library.gui.RenderingHelper;
import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.screen.WidgetScreen;
import vswe.stevesfactory.library.gui.widget.AbstractWidget;
import vswe.stevesfactory.library.gui.widget.mixin.LeafWidgetMixin;

import static vswe.stevesfactory.library.gui.RenderingHelper.fontRenderer;

public abstract class AbstractItemSlot extends AbstractWidget implements LeafWidgetMixin {

    public AbstractItemSlot() {
        super(0, 0, 18, 18);
    }

    @Override
    public void render(int mouseX, int mouseY, float particleTicks) {
        RenderEventDispatcher.onPreRender(this, mouseX, mouseY);
        RenderSystem.color3f(1F, 1F, 1F);
        renderBase();
        if (isInside(mouseX, mouseY)) {
            renderHoveredOverlay();
        }
        renderStack();
        RenderEventDispatcher.onPostRender(this, mouseX, mouseY);
    }

    protected void renderStack() {
        ItemStack stack = getRenderedStack();
        ItemRenderer ir = Minecraft.getInstance().getItemRenderer();
        FontRenderer fr = MoreObjects.firstNonNull(stack.getItem().getFontRenderer(stack), fontRenderer());
        int x = getAbsoluteX() + 1;
        int y = getAbsoluteY() + 1;
        RenderSystem.disableDepthTest();
        RenderHelper.enableStandardItemLighting();
        ir.renderItemAndEffectIntoGUI(stack, x, y);
        ir.renderItemOverlayIntoGUI(fr, stack, x, y, null);
        RenderHelper.disableStandardItemLighting();
    }

    protected void renderBase() {
        WidgetScreen.ITEM_SLOT.draw(getAbsoluteX(), getAbsoluteY());
    }

    private void renderHoveredOverlay() {
        RenderingHelper.useBlendingGLStates();
        RenderingHelper.drawRect(getAbsoluteX() + 1, getAbsoluteY() + 1, getAbsoluteXRight() - 1, getAbsoluteYBottom() - 1, 0xaac4c4c4);
        RenderingHelper.useTextureGLStates();
    }

    public abstract ItemStack getRenderedStack();
}
