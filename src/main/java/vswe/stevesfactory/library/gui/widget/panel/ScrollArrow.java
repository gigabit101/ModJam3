package vswe.stevesfactory.library.gui.widget.panel;

import vswe.stevesfactory.library.gui.Render2D;
import vswe.stevesfactory.library.gui.Texture;
import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.widget.button.AbstractIconButton;
import vswe.stevesfactory.library.gui.widget.mixin.LeafWidgetMixin;

import javax.annotation.Nonnull;
import java.util.Objects;

public abstract class ScrollArrow extends AbstractIconButton implements LeafWidgetMixin {

    public static final Texture UP_NORMAL = Render2D.ofFlowComponent(0, 58, 10, 6);
    public static final Texture UP_HOVERED = UP_NORMAL.moveRight(1);
    public static final Texture UP_CLICKED = UP_NORMAL.moveRight(2);
    public static final Texture UP_DISABLED = UP_NORMAL.moveRight(3);

    public static ScrollArrow up(int x, int y) {
        return new ScrollArrow(x, y) {
            @Override
            public Texture getTextureNormal() {
                return UP_NORMAL;
            }

            @Override
            public Texture getTextureHovered() {
                return UP_HOVERED;
            }

            @Override
            public Texture getTextureClicked() {
                return UP_CLICKED;
            }

            @Override
            public Texture getTextureDisabled() {
                return UP_DISABLED;
            }

            @Override
            protected int getScrollDirectionMask() {
                return -1;
            }
        };
    }

    private static final Texture DOWN_NORMAL = UP_NORMAL.moveDown(1);
    private static final Texture DOWN_HOVERED = UP_HOVERED.moveDown(1);
    private static final Texture DOWN_CLICKED = UP_CLICKED.moveDown(1);
    private static final Texture DOWN_DISABLED = UP_DISABLED.moveDown(1);

    public static ScrollArrow down(int x, int y) {
        return new ScrollArrow(x, y) {
            @Override
            public Texture getTextureNormal() {
                return DOWN_NORMAL;
            }

            @Override
            public Texture getTextureHovered() {
                return DOWN_HOVERED;
            }

            @Override
            public Texture getTextureClicked() {
                return DOWN_CLICKED;
            }

            @Override
            public Texture getTextureDisabled() {
                return DOWN_DISABLED;
            }

            @Override
            protected int getScrollDirectionMask() {
                return 1;
            }
        };
    }

    public ScrollArrow(int x, int y) {
        this.setLocation(x, y);
        this.setDimensions(10, 6);
    }

    @Override
    public void update(float partialTicks) {
        if (isClicked()) {
            WrappingList<?> parent = getParent();
            parent.scroll(parent.getScrollSpeed() * getScrollDirectionMask());
        }
    }

    @Override
    public boolean onMouseClicked(double mouseX, double mouseY, int button) {
        if (!isEnabled()) {
            return false;
        }
        return super.onMouseClicked(mouseX, mouseY, button);
    }

    @Nonnull
    @Override
    public WrappingList<?> getParent() {
        return Objects.requireNonNull((WrappingList<?>) super.getParent());
    }

    @Override
    protected void preRenderEvent(int mx, int my) {
        RenderEventDispatcher.onPreRender(this, mx, my);
    }

    @Override
    protected void postRenderEvent(int mx, int my) {
        RenderEventDispatcher.onPostRender(this, mx, my);
    }

    protected abstract int getScrollDirectionMask();
}
