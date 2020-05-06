package vswe.stevesfactory.library.gui.contextmenu;

import net.minecraft.util.ResourceLocation;
import vswe.stevesfactory.library.gui.Render2D;
import vswe.stevesfactory.library.gui.screen.WidgetScreen;
import vswe.stevesfactory.library.gui.window.IControllableAppearance;

import javax.annotation.Nullable;

import static vswe.stevesfactory.library.gui.Render2D.*;

public class ExpandableEntry<T extends ContextMenu & IControllableAppearance> extends DefaultEntry {

    public static final ResourceLocation ARROW_TEX = Render2D.RIGHT_ARROW_SHORT;
    public static final int ARROW_DIM = 16;

    private final T target;
    private boolean deployed = false;

    /**
     * Counter for how many ticks has passed since the last time that this entry is hovered.
     */
    private int offCounter = -1;

    public ExpandableEntry(@Nullable ResourceLocation icon, String translationKey, T target) {
        super(icon, translationKey);
        this.target = target;
    }

    @Override
    protected void renderContents(int mouseX, int mouseY, float partialTicks) {
        super.renderContents(mouseX, mouseY, partialTicks);
        int x = getAbsoluteXRight() - MARGIN_SIDES - RENDERED_ICON_WIDTH;
        int y = getAbsoluteY() + MARGIN_SIDES;
        bindTexture(ARROW_TEX);
        beginTexturedQuad();
        completeTexture(x, y, x + RENDERED_ICON_WIDTH, y + RENDERED_ICON_HEIGHT, getZLevel());
        draw();
    }

    @Override
    protected int computeWidth() {
        return super.computeWidth() + ARROW_DIM + MARGIN_SIDES;
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        if (isInside(mouseX, mouseY)) {
            // Control flow limiting to ensure the target is only revived once
            if (offCounter == 0) {
                return;
            }
            offCounter = 0;

            // Player might move his cursor back before the timer runs out
            // or if the submenu prevent itself from being closed in the second branch
            if (!target.isAlive()) {
                target.revive();
                WidgetScreen.assertActive().addPopupWindow(target);
                tryDeployTarget();
            }
        } else {
            offCounter++;
            // Arbitrary minimum time for this entry to be not hovered to close the submenu; the submenu can have custom
            // logic to prevent itself from closed
            // Try to kill the submenu if after the delay, so that if the cursor moves out after timer runs out, it will still be closed
            if (offCounter >= 10) {
                target.kill();
            }
        }
    }

    private void tryDeployTarget() {
        if (deployed) {
            return;
        }

        ContextMenu menu = getContextMenu();
        int distLeft = menu.getX();
        int distRight = windowWidth() - menu.getXRight();
        int distTop = this.getAbsoluteY();
        int distBottom = windowHeight() - this.getAbsoluteYBottom();

        int x = distLeft < distRight
                ? menu.getXRight()
                : computeRightX(menu.getX(), target.getWidth());
        int y = distTop < distBottom
                ? this.getAbsoluteY()
                : computeBottomY(this.getAbsoluteYBottom(), target.getHeight());
        target.setPosition(x, y);
        target.reflow();

        deployed = true;
    }

    @Override
    public boolean forceAlive() {
        return target.isAlive();
    }
}
