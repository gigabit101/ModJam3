package vswe.stevesfactory.ui.manager.menu;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import vswe.stevesfactory.library.gui.Render2D;
import vswe.stevesfactory.library.gui.Texture;
import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.layout.FlowLayout;
import vswe.stevesfactory.library.gui.layout.properties.BoxSizing;
import vswe.stevesfactory.library.gui.screen.WidgetScreen;
import vswe.stevesfactory.library.gui.widget.AbstractContainer;
import vswe.stevesfactory.library.gui.widget.IWidget;
import vswe.stevesfactory.library.gui.widget.NumberField;
import vswe.stevesfactory.library.gui.widget.TextField;
import vswe.stevesfactory.library.gui.widget.button.AbstractIconButton;
import vswe.stevesfactory.library.gui.widget.button.ColoredTextButton;
import vswe.stevesfactory.logic.item.ItemTraitsFilter;
import vswe.stevesfactory.ui.manager.editor.Menu;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;

import static vswe.stevesfactory.library.gui.Render2D.fontRenderer;

// TODO popup configuration menu
public class FilterSlot extends ConfigurableSlot<FilterSlot.Editor> {

    private final int index;
    private final ItemTraitsFilter filter;

    public FilterSlot(ItemTraitsFilter filter, int index, ItemStack stack) {
        super(stack);
        this.filter = filter;
        this.index = index;
        this.setDimensions(16, 16);
    }

    @Override
    protected boolean hasEditor() {
        return true;
    }

    @Nonnull
    @Override
    protected Editor createEditor() {
        return new Editor();
    }

    @Override
    public void openEditor() {
        super.openEditor();
        editor.update();
    }

    @Override
    protected void onSetStack() {
        filter.getItems().set(index, stack);
    }

    public class Editor extends AbstractContainer<IWidget> {

        private NumberField<Integer> count;
        private NumberField<Integer> damage;
        private List<IWidget> children;

        @Override
        public void onInitialAttach() {
            super.onInitialAttach();

            MultiLayerMenu<?> menu = getMenu();
            setDimensions(menu.getWidth(), Menu.DEFAULT_CONTENT_HEIGHT);

            ColoredTextButton delete = new DeleteFilterButton();
            delete.setText(I18n.format("menu.sfm.Delete"));
            delete.setDimensions(32, 11);
            delete.setLocation(getWidth() - delete.getWidth() - 2, 2);
            delete.setClickAction(b -> {
                closeEditor();
                stack = ItemStack.EMPTY;
                onSetStack();
            });
            AbstractIconButton close = new AbstractIconButton() {
                {
                    this.setLocation(getWidth() - 8 - 1, getHeight() - 8 - 1);
                    this.setDimensions(8, 8);
                }

                @Override
                public void render(int mouseX, int mouseY, float partialTicks) {
                    super.render(mouseX, mouseY, partialTicks);
                    if (isHovered()) {
                        WidgetScreen.assertActive().scheduleTooltip(I18n.format("menu.sfm.CloseEditor.Info"), mouseX, mouseY);
                    }
                }

                @Override
                public Texture getTextureNormal() {
                    return Render2D.CLOSE_ICON;
                }

                @Override
                public Texture getTextureHovered() {
                    return Render2D.CLOSE_ICON_HOVERED;
                }

                @Override
                public boolean onMouseClicked(double mouseX, double mouseY, int button) {
                    closeEditor();
                    return true;
                }

                @Override
                public BoxSizing getBoxSizing() {
                    return BoxSizing.PHANTOM;
                }
            };

            count = NumberField.integerFieldRanged(33, 12, 1, 1, Integer.MAX_VALUE);
            count.setValue(stack.getCount());
            count.setBackgroundStyle(TextField.BackgroundStyle.RED_OUTLINE);
            count.onValueUpdated = stack::setCount;
            damage = NumberField.integerField(33, 12);
            damage.setValue(stack.getDamage());
            damage.setBackgroundStyle(TextField.BackgroundStyle.RED_OUTLINE);
            damage.onValueUpdated = stack::setDamage;

            children = ImmutableList.of(
                    close, delete, count, damage,
                    // TODO label pos
                    count.makeLabel().translate("menu.sfm.ItemFilter.Traits.Amount"),
                    damage.makeLabel().translate("menu.sfm.ItemFilter.Traits.Damage")
            );
            reflow();
        }

        @Override
        public Collection<IWidget> getChildren() {
            return children;
        }

        @Override
        public void render(int mouseX, int mouseY, float partialTicks) {
            RenderEventDispatcher.onPreRender(this, mouseX, mouseY);
            super.renderChildren(mouseX, mouseY, partialTicks);
            renderItem();
            RenderEventDispatcher.onPostRender(this, mouseX, mouseY);
        }

        private void renderItem() {
            RenderSystem.disableDepthTest();
            RenderSystem.enableTexture();
            RenderHelper.enableStandardItemLighting();
            ItemRenderer ir = Minecraft.getInstance().getItemRenderer();
            int x = getAbsoluteX() + 4;
            int y = getAbsoluteY() + 4;
            ir.renderItemAndEffectIntoGUI(stack, x, y);
            ir.renderItemOverlayIntoGUI(fontRenderer(), stack, x, y, "");
            RenderHelper.disableStandardItemLighting();
            RenderSystem.color3f(1F, 1F, 1F);
        }

        @Override
        public void reflow() {
            FlowLayout.vertical(children, 4, 24, 4);
        }

        public void update() {
            count.setEnabled(filter.isMatchingAmount());
            damage.setEnabled(stack.isDamageable() && filter.isMatchingDamage());
        }
    }

    private static class DeleteFilterButton extends ColoredTextButton {

        @Override
        public void render(int mouseX, int mouseY, float partialTicks) {
            super.render(mouseX, mouseY, partialTicks);
            if (isHovered()) {
                WidgetScreen.assertActive().scheduleTooltip(I18n.format("menu.sfm.Delete.Info"), mouseX, mouseY);
            }
        }

        @Override
        protected void renderText() {
            RenderSystem.pushMatrix();
            RenderSystem.enableTexture();
            RenderSystem.translatef(getAbsoluteX() + 2, getAbsoluteY() + 2, 0F);
            RenderSystem.scalef(0.8F, 0.8F, 1F);
            fontRenderer().drawString(getText(), 0, 0, getTextColor());
            RenderSystem.popMatrix();
        }

        @Override
        public int getTextColor() {
            return 0xffaf2727;
        }

        @Override
        public int getNormalBorderColor() {
            return 0xffaf2727;
        }

        @Override
        public int getHoveredBorderColor() {
            return 0xff963737;
        }

        @Override
        public BoxSizing getBoxSizing() {
            return BoxSizing.PHANTOM;
        }
    }
}
