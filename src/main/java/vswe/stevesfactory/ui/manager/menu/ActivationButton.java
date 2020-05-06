package vswe.stevesfactory.ui.manager.menu;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.resources.I18n;
import vswe.stevesfactory.library.gui.widget.button.ColoredTextButton;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static vswe.stevesfactory.library.gui.Render2D.fontRenderer;

class ActivationButton extends ColoredTextButton {

    private DirectionButton target;

    public ActivationButton(DirectionButton target) {
        this.setTarget(target);
        this.setDimensions(42, 12);
    }

    public void setTarget(DirectionButton target) {
        this.target = target;
        updateText();
    }

    @Override
    protected void renderText() {
        // 0.7F, 3, and 3 are magic numbers
        // It would require a lot of effort (including rewriting FontRenderer#getStringWidth) to get rid of those magic numbers
        // 0.7F can be seen as fontHeight=7.3
        RenderSystem.enableTexture();
        RenderSystem.pushMatrix();
        RenderSystem.translatef(getAbsoluteX() + 3, getAbsoluteY() + 3, 0F);
        RenderSystem.scalef(0.7F, 0.7F, 1F);
        fontRenderer().drawString(getText(), 0F, 0F, 0xff4d4d4d);
        RenderSystem.popMatrix();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isEnabled() && button == GLFW_MOUSE_BUTTON_LEFT) {
            target.toggleSelected();
            updateText();
            return super.mouseClicked(mouseX, mouseY, button);
        }
        return false;
    }

    private void updateText() {
        setText(target.isSelected() ? I18n.format("menu.sfm.Deactivate") : I18n.format("menu.sfm.Activate"));
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        if (isEnabled()) {
            super.render(mouseX, mouseY, partialTicks);
        }
    }

    public void setEditingState(boolean state) {
        super.setEnabled(state);
    }

    @Override
    public void setEnabled(boolean enabled) {
        // Do nothing because we want to limit state changing through only setEditingState(boolean)
        super.setEnabled(false);
    }

    @Override
    public int getNormalBorderColor() {
        return 0xff4b4b4b;
    }

    @Override
    public int getHoveredBorderColor() {
        return 0xff4b4b4b;
    }

    @Override
    public int getNormalBackgroundColor() {
        return 0xffa3a3a3;
    }

    @Override
    public int getHoveredBackgroundColor() {
        return 0xffb4b4b4;
    }
}
