package vswe.stevesfactory.library.gui.widget.slot;

import com.google.common.base.MoreObjects;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.item.ItemStack;
import vswe.stevesfactory.library.gui.*;
import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.widget.AbstractWidget;
import vswe.stevesfactory.library.gui.widget.mixin.LeafWidgetMixin;

import static vswe.stevesfactory.library.gui.Render2D.*;

public abstract class AbstractItemSlot extends AbstractWidget implements LeafWidgetMixin {

    public static final Texture BASE = Texture.complete(Render2D.ITEM_SLOT, 18, 18);

    public AbstractItemSlot() {
        this.setDimensions(18, 18);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        RenderEventDispatcher.onPreRender(this, mouseX, mouseY);
        renderBase();
        if (isInside(mouseX, mouseY)) {
            renderHoveredOverlay();
        }
        renderStack();
        RenderEventDispatcher.onPostRender(this, mouseX, mouseY);
    }

    public void renderStack() {
        ItemStack stack = getRenderedStack();
        ItemRenderer ir = minecraft().getItemRenderer();
        FontRenderer fr = MoreObjects.firstNonNull(stack.getItem().getFontRenderer(stack), minecraft().fontRenderer);
        int x = getAbsoluteX() + 2;
        int y = getAbsoluteY() + 2;
        ir.renderItemAndEffectIntoGUI(stack, x, y);
        ir.renderItemOverlayIntoGUI(fr, stack, x, y, null);
    }

    public void renderBase() {
        BASE.render(getAbsoluteX(), getAbsoluteY(), getAbsoluteXRight(), getAbsoluteYBottom());
    }

    public void renderHoveredOverlay() {
        useBlendingGLStates();
        beginColoredQuad();
        coloredRect(getAbsoluteX(), getAbsoluteY(), getAbsoluteXRight(), getAbsoluteYBottom(), 0xaac4c4c4);
        draw();
        useTextureGLStates();
    }

    public abstract ItemStack getRenderedStack();
}
