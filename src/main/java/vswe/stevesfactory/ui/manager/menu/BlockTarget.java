package vswe.stevesfactory.ui.manager.menu;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import vswe.stevesfactory.library.gui.Render2D;
import vswe.stevesfactory.library.gui.contextmenu.CallbackEntry;
import vswe.stevesfactory.library.gui.contextmenu.ContextMenuBuilder;
import vswe.stevesfactory.library.gui.contextmenu.Section;
import vswe.stevesfactory.library.gui.debug.ITextReceiver;
import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.screen.WidgetScreen;
import vswe.stevesfactory.library.gui.widget.AbstractWidget;
import vswe.stevesfactory.library.gui.widget.button.IButton;
import vswe.stevesfactory.library.gui.widget.mixin.LeafWidgetMixin;
import vswe.stevesfactory.render.BlockHighlight;

import java.util.function.IntConsumer;

public class BlockTarget extends AbstractWidget implements IButton, IStringSerializable, LeafWidgetMixin {

    private boolean hovered = false;
    private boolean clicked = false;
    private boolean selected = false;

    public final BlockPos pos;

    private BlockState state;
    private ItemStack cachedItemStack;
    private IntConsumer action = DUMMY;

    public BlockTarget(BlockPos pos) {
        this(pos, 16);
    }

    public BlockTarget(BlockPos pos, int size) {
        this.setDimensions(size, size);
        this.pos = pos;
        this.setBlockState(Minecraft.getInstance().world.getBlockState(pos));
    }

    @Override
    public boolean onMouseClicked(double mouseX, double mouseY, int button) {
        clicked = true;
        setSelected(!selected);
        action.accept(button);
        return true;
    }

    @Override
    protected void buildContextMenu(ContextMenuBuilder builder) {
        Section section = builder.obtainSection("");
        section.addChildren(new CallbackEntry(null, "menu.sfm.BlockTarget.Highlight", b -> BlockHighlight.createHighlight(pos, 80)));
        super.buildContextMenu(builder);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        clicked = false;
        return true;
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        hovered = isInside(mouseX, mouseY);
    }

    @Override
    public boolean isHovered() {
        return hovered;
    }

    @Override
    public boolean isClicked() {
        return clicked;
    }

    @Override
    public boolean hasClickAction() {
        return false;
    }

    @Override
    public IntConsumer getClickAction() {
        return action;
    }

    @Override
    public void setClickAction(IntConsumer action) {
        this.action = action;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public BlockState getBlockState() {
        return state;
    }

    public void setBlockState(BlockState state) {
        this.state = state;
        this.cachedItemStack = new ItemStack(state.getBlock());
    }

    public ItemStack getCachedItemStack() {
        return cachedItemStack;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        RenderEventDispatcher.onPreRender(this, mouseX, mouseY);

        int x = getAbsoluteX();
        int y = getAbsoluteY();

        int color = selected
                ? (hovered ? 0xff62c93a : 0xffadcfa0)
                : (hovered ? 0xffd4d4d4 : 0xffa4a4a4);
        Render2D.beginColoredQuad();
        Render2D.coloredRect(x, y, getAbsoluteXRight(), getAbsoluteYBottom(), color);
        Render2D.draw();

        // No depth test so that popups get correctly rendered
        RenderSystem.disableDepthTest();
        RenderSystem.enableTexture();
        RenderHelper.enableStandardItemLighting();
        // 16 is the standard item size
        Minecraft.getInstance().getItemRenderer().renderItemIntoGUI(cachedItemStack, x + (getWidth() - 16) / 2, y + (getHeight() - 16) / 2);

        if (hovered) {
            WidgetScreen.assertActive().scheduleTooltip(new ItemStack(state.getBlock()), mouseX, mouseY);
        }

        RenderEventDispatcher.onPostRender(this, mouseX, mouseY);
    }

    @Override
    public String getName() {
        return I18n.format(state.getBlock().getTranslationKey());
    }

    @Override
    public void provideInformation(ITextReceiver receiver) {
        super.provideInformation(receiver);
        receiver.line("BlockState=" + state);
    }
}
