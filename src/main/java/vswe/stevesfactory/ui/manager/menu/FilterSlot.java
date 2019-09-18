package vswe.stevesfactory.ui.manager.menu;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import vswe.stevesfactory.library.gui.TextureWrapper;
import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.screen.WidgetScreen;
import vswe.stevesfactory.library.gui.widget.*;
import vswe.stevesfactory.library.gui.widget.mixin.LeafWidgetMixin;
import vswe.stevesfactory.library.gui.widget.slot.AbstractItemSlot;
import vswe.stevesfactory.library.gui.window.PlayerInventoryWindow;
import vswe.stevesfactory.logic.item.ItemTraitsFilter;
import vswe.stevesfactory.ui.manager.FactoryManagerGUI;
import vswe.stevesfactory.ui.manager.editor.Menu;
import vswe.stevesfactory.utils.RenderingHelper;

import javax.annotation.Nonnull;
import java.util.*;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT;

public class FilterSlot extends AbstractWidget implements INamedElement, LeafWidgetMixin {

    private static final TextureWrapper NORMAL = TextureWrapper.ofFlowComponent(36, 20, 16, 16);
    private static final TextureWrapper HOVERED = NORMAL.toDown(1);

    public ItemStack stack;

    private Editor editor;

    public FilterSlot(ItemStack stack) {
        this.stack = stack;
        this.setDimensions(16, 16);
    }

    @Override
    public void render(int mouseX, int mouseY, float particleTicks) {
        RenderEventDispatcher.onPreRender(this, mouseX, mouseY);
        GlStateManager.color3f(1F, 1F, 1F);
        int x = getAbsoluteX();
        int y = getAbsoluteY();
        if (isInside(mouseX, mouseY)) {
            HOVERED.draw(x, y);
            if (!stack.isEmpty()) {
                WidgetScreen.getCurrentScreen().setHoveringText(stack, mouseX, mouseY);
            }
        } else {
            NORMAL.draw(x, y);
        }

        GlStateManager.disableDepthTest();
        GlStateManager.enableTexture();
        RenderHelper.enableGUIStandardItemLighting();
        ItemRenderer ir = minecraft().getItemRenderer();
        ir.renderItemAndEffectIntoGUI(stack, x, y);
        ir.renderItemOverlayIntoGUI(fontRenderer(), stack, x, y, "");
        RenderHelper.disableStandardItemLighting();

        RenderEventDispatcher.onPostRender(this, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW_MOUSE_BUTTON_LEFT) {
            openInventorySelection();
            return true;
        }
        if (button == GLFW_MOUSE_BUTTON_RIGHT && !stack.isEmpty()) {
            openEditor();
            return true;
        }
        return false;
    }

    private void openInventorySelection() {
        AbstractItemSlot[] selected = new AbstractItemSlot[1];
        PlayerInventoryWindow popup = new PlayerInventoryWindow(getAbsoluteXRight() + 4, getAbsoluteY(), stack -> new AbstractItemSlot() {
            private ItemStack representative;

            @Override
            public ItemStack getRenderedStack() {
                return stack;
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                if (isSelected() || stack.isEmpty()) {
                    selected[0] = null;
                    FilterSlot.this.stack = ItemStack.EMPTY;
                } else {
                    selected[0] = this;
                    FilterSlot.this.stack = getRepresentative();
                }
                return super.mouseClicked(mouseX, mouseY, button);
            }

            @Override
            protected void renderBase() {
                super.renderBase();
                if (isSelected() && !stack.isEmpty()) {
                    RenderingHelper.useBlendingGLStates();
                    RenderingHelper.drawRect(getAbsoluteX(), getAbsoluteY(), getAbsoluteXRight(), getAbsoluteYBottom(), 0x66ffff00);
                    GlStateManager.disableBlend();
                    GlStateManager.enableTexture();
                }
            }

            private boolean isSelected() {
                return selected[0] == this;
            }

            private ItemStack getRepresentative() {
                if (representative == null) {
                    representative = stack.copy();
                    representative.setCount(1);
                }
                return representative;
            }
        });
        WidgetScreen.getCurrentScreen().addPopupWindow(popup);
    }

    private Editor getDedicatedEditor() {
        if (editor == null) {
            editor = new Editor(this);
        }
        return editor;
    }

    private void openEditor() {
        getMenu().openEditor(getDedicatedEditor());
    }

    private void closeEditor() {
        getMenu().openEditor(null);
    }

    @Nonnull
    public ItemTraitsFilterMenu<?> getMenu() {
        IWidget parentWidget = Objects.requireNonNull(super.getParentWidget());
        return (ItemTraitsFilterMenu<?>) Objects.requireNonNull(parentWidget.getParentWidget());
    }

    public ItemTraitsFilter getLinkedFiler() {
        return getMenu().getLinkedFilter();
    }

    @Override
    public String getName() {
        return I18n.format(stack.getTranslationKey());
    }

    public static class Editor extends AbstractContainer<IWidget> {

        private FilterSlot slot;

        private final NumberField<Integer> count;
        private final NumberField<Integer> damage;
        private final List<IWidget> children;

        public Editor(FilterSlot slot) {
            this.slot = slot;
            ItemTraitsFilterMenu<?> menu = slot.getMenu();
            setDimensions(menu.getWidth(), menu.getHeight() - Menu.HEADING_BOX.getPortionHeight());

            TextButton delete = new DeleteFilterButton();
            delete.translate("gui.sfm.Menu.Delete");
            delete.setDimensions(32, 11);
            delete.setLocation(getWidth() - delete.getWidth() - 2, 2);
            delete.onClick = b -> {
                slot.closeEditor();
                slot.stack = ItemStack.EMPTY;
            };
            AbstractIconButton close = new AbstractIconButton(getWidth() - 8 - 1, getHeight() - 8 - 1, 8, 8) {
                @Override
                public void render(int mouseX, int mouseY, float particleTicks) {
                    super.render(mouseX, mouseY, particleTicks);
                    if (isHovered()) {
                        WidgetScreen.getCurrentScreen().setHoveringText(I18n.format("gui.sfm.Menu.CloseEditor.Info"), mouseX, mouseY);
                    }
                }

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
                    slot.closeEditor();
                    return super.mouseClicked(mouseX, mouseY, button);
                }
            };

            int width = 33;
            int height = 12;
            int x = getWidth() - width - 4;
            count = NumberField.integerFieldRanged(width, height, 1, 1, Integer.MAX_VALUE)
                    .setBackgroundStyle(TextField.BackgroundStyle.RED_OUTLINE)
                    .setValue(slot.stack.getCount());
            count.setLocation(x, 24);
            damage = NumberField.integerField(width, height)
                    .setBackgroundStyle(TextField.BackgroundStyle.RED_OUTLINE)
                    .setValue(slot.stack.getDamage());
            damage.setLocation(x, count.getY() + count.getHeight() + 4);

            children = ImmutableList.of(close, delete, count, damage);
        }

        @Override
        public Collection<IWidget> getChildren() {
            return children;
        }

        @Override
        public void render(int mouseX, int mouseY, float particleTicks) {
            RenderEventDispatcher.onPreRender(this, mouseX, mouseY);
            super.render(mouseX, mouseY, particleTicks);
            int x = getAbsoluteX() + 2;
            fontRenderer().drawString(I18n.format("gui.sfm.Menu.FilterAmount"), x, count.getAbsoluteY() + 2, 0xff000000);
            fontRenderer().drawString(I18n.format("gui.sfm.Menu.FilterDamage"), x, damage.getAbsoluteY() + 2, 0xff000000);
            renderItem();
            RenderEventDispatcher.onPostRender(this, mouseX, mouseY);
        }

        private void renderItem() {
            GlStateManager.disableDepthTest();
            GlStateManager.enableTexture();
            RenderHelper.enableGUIStandardItemLighting();
            ItemRenderer ir = minecraft().getItemRenderer();
            int x = getAbsoluteX() + 4;
            int y = getAbsoluteY() + 4;
            ir.renderItemAndEffectIntoGUI(slot.stack, x, y);
            ir.renderItemOverlayIntoGUI(fontRenderer(), slot.stack, x, y, "");
            RenderHelper.disableStandardItemLighting();
            GlStateManager.color3f(1F, 1F, 1F);
        }

        @Override
        public void onRemoved() {
            slot.stack.setCount(count.getValue());
            slot.stack.setDamage(damage.getValue());
        }

        @Override
        public void reflow() {
        }

        @Override
        public void setParentWidget(IWidget newParent) {
            super.setParentWidget(newParent);
            ItemTraitsFilter filter = slot.getLinkedFiler();
            count.setEnabled(filter.isMatchingAmount());
            damage.setEnabled(slot.stack.isDamageable() && filter.isMatchingDamage());
        }
    }

    private static class DeleteFilterButton extends TextButton {

        @Override
        public void render(int mouseX, int mouseY, float particleTicks) {
            super.render(mouseX, mouseY, particleTicks);
            if (isHovered()) {
                WidgetScreen.getCurrentScreen().setHoveringText(I18n.format("gui.sfm.Menu.Delete.Info"), mouseX, mouseY);
            }
        }

        @Override
        protected void renderText() {
            GlStateManager.pushMatrix();
            GlStateManager.enableTexture();
            GlStateManager.translatef(getAbsoluteX() + 2, getAbsoluteY() + 2, 0F);
            GlStateManager.scalef(0.8F, 0.8F, 1F);
            fontRenderer().drawString(getText(), 0, 0, getTextColor());
            GlStateManager.popMatrix();
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
    }
}
