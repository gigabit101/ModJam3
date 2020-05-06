package vswe.stevesfactory.ui.manager.menu;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import vswe.stevesfactory.library.gui.Render2D;
import vswe.stevesfactory.library.gui.Texture;
import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.screen.WidgetScreen;
import vswe.stevesfactory.library.gui.widget.AbstractWidget;
import vswe.stevesfactory.library.gui.widget.IWidget;
import vswe.stevesfactory.library.gui.widget.mixin.LeafWidgetMixin;
import vswe.stevesfactory.library.gui.widget.slot.AbstractItemSlot;
import vswe.stevesfactory.library.gui.window.PlayerInventoryWindow;
import vswe.stevesfactory.ui.manager.FactoryManagerGUI;

import javax.annotation.Nonnull;
import javax.xml.ws.Holder;
import java.util.Objects;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT;
import static vswe.stevesfactory.library.gui.Render2D.fontRenderer;

public abstract class ConfigurableSlot<E extends IWidget> extends AbstractWidget implements IStringSerializable, LeafWidgetMixin {

    public static final Texture NORMAL = Render2D.ofFlowComponent(36, 20, 16, 16);
    public static final Texture HOVERED = NORMAL.down(1);

    protected ItemStack stack;
    protected E editor;

    public ConfigurableSlot(ItemStack stack) {
        this.stack = stack;
        this.setDimensions(NORMAL.getPortionWidth(), NORMAL.getPortionHeight());
    }

    public ItemStack getStack() {
        return stack;
    }

    public void setStack(ItemStack stack) {
        this.stack = stack;
        this.onSetStack();
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        RenderEventDispatcher.onPreRender(this, mouseX, mouseY);
        RenderSystem.color3f(1F, 1F, 1F);
        int x = getAbsoluteX();
        int y = getAbsoluteY();
        if (isInside(mouseX, mouseY)) {
            HOVERED.render(x, y);
            if (!stack.isEmpty()) {
                FactoryManagerGUI.get().scheduleTooltip(stack, mouseX, mouseY);
            }
        } else {
            NORMAL.render(x, y);
        }

        RenderSystem.disableDepthTest();
        RenderSystem.enableTexture();
        RenderHelper.enableStandardItemLighting();
        ItemRenderer ir = Minecraft.getInstance().getItemRenderer();
        ir.renderItemAndEffectIntoGUI(stack, x, y);
        ir.renderItemOverlayIntoGUI(fontRenderer(), stack, x, y, "");
        RenderHelper.disableStandardItemLighting();

        RenderEventDispatcher.onPostRender(this, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW_MOUSE_BUTTON_LEFT) {
            onLeftClick();
            return true;
        }
        if (button == GLFW_MOUSE_BUTTON_RIGHT) {
            onRightClick();
            return true;
        }
        return false;
    }

    protected void onLeftClick() {
        openInventoryPopup();
    }

    protected void onRightClick() {
        if (!stack.isEmpty()) {
            openEditor();
        }
    }

    protected abstract boolean hasEditor();

    protected abstract E createEditor();

    public void openEditor() {
        if (!hasEditor()) {
            return;
        }
        if (editor == null) {
            editor = Preconditions.checkNotNull(createEditor());
        }
        getMenu().openEditor(editor);
    }

    public void closeEditor() {
        getMenu().openEditor(null);
    }

    public void openInventoryPopup() {
        Holder<AbstractItemSlot> selected = new Holder<>();
        PlayerInventoryWindow popup = new PlayerInventoryWindow(
                Render2D.mouseX(), Render2D.mouseY(),
                in -> new AbstractItemSlot() {
                    private ItemStack representative;

                    @Override
                    public ItemStack getRenderedStack() {
                        return in;
                    }

                    @Override
                    public boolean mouseClicked(double mouseX, double mouseY, int button) {
                        if (isSelected() || in.isEmpty()) {
                            // Unselect slot
                            selected.value = null;
                            stack = ItemStack.EMPTY;
                            onSetStack();
                        } else {
                            // Select and set slot content
                            selected.value = this;
                            stack = getRepresentative();
                            onSetStack();
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
        WidgetScreen.assertActive().addPopupWindow(popup);
    }

    protected void onSetStack() {
    }

    @Nonnull
    public MultiLayerMenu<?> getMenu() {
        IWidget parentWidget = Objects.requireNonNull(super.getParent());
        return (MultiLayerMenu<?>) Objects.requireNonNull(parentWidget.getParent());
    }

    @Override
    public String getName() {
        return I18n.format(stack.getTranslationKey());
    }
}
