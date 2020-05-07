package vswe.stevesfactory.ui.manager.menu;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.resources.I18n;
import vswe.stevesfactory.api.logic.IClientDataStorage;
import vswe.stevesfactory.api.logic.IProcedure;
import vswe.stevesfactory.library.gui.Render2D;
import vswe.stevesfactory.library.gui.Texture;
import vswe.stevesfactory.library.gui.screen.WidgetScreen;
import vswe.stevesfactory.library.gui.widget.IWidget;
import vswe.stevesfactory.library.gui.widget.button.AbstractIconButton;
import vswe.stevesfactory.ui.manager.editor.Menu;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public abstract class MultiLayerMenu<P extends IProcedure & IClientDataStorage> extends Menu<P> {

    private IWidget openEditor;

    @Override
    public void renderContents(int mouseX, int mouseY, float partialTicks) {
        RenderSystem.color3f(1F, 1F, 1F);
        if (openEditor != null) {
            getToggleStateButton().render(mouseX, mouseY, partialTicks);
            openEditor.render(mouseX, mouseY, partialTicks);
        } else {
            super.renderContents(mouseX, mouseY, partialTicks);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (openEditor != null) {
            if (getToggleStateButton().isInside(mouseX, mouseY)) {
                return getToggleStateButton().mouseClicked(mouseX, mouseY, button);
            }
            return openEditor.mouseClicked(mouseX, mouseY, button);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (openEditor != null) {
            if (getToggleStateButton().isInside(mouseX, mouseY)) {
                return getToggleStateButton().mouseReleased(mouseX, mouseY, button);
            }
            return openEditor.mouseReleased(mouseX, mouseY, button);
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (openEditor != null) {
            return openEditor.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scroll) {
        if (openEditor != null) {
            return openEditor.mouseScrolled(mouseX, mouseY, scroll);
        }
        return super.mouseScrolled(mouseX, mouseY, scroll);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (openEditor != null) {
            return openEditor.keyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (openEditor != null) {
            return openEditor.keyReleased(keyCode, scanCode, modifiers);
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char charTyped, int keyCode) {
        if (openEditor != null) {
            return openEditor.charTyped(charTyped, keyCode);
        }
        return super.charTyped(charTyped, keyCode);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        if (openEditor != null) {
            openEditor.mouseMoved(mouseX, mouseY);
        } else {
            super.mouseMoved(mouseX, mouseY);
        }
    }

    @Override
    public void update(float partialTicks) {
        if (openEditor != null) {
            openEditor.update(partialTicks);
        } else {
            super.update(partialTicks);
        }
    }

    @Override
    public void notifyChildrenForPositionChange() {
        super.notifyChildrenForPositionChange();
        if (openEditor != null) {
            openEditor.onParentPositionChanged();
        }
    }

    public IWidget getOpenEditor() {
        return openEditor;
    }

    public void openEditor(@Nullable IWidget editor) {
        this.openEditor = editor;
        if (editor != null) {
            editor.attach(this);
            editor.setLocation(0, HEADING_BOX.getPortionHeight());
        }
    }

    public abstract IWidget getEditor();

    public static class OpenSettingsButton extends AbstractIconButton {

        public OpenSettingsButton() {
            this.setDimensions(8, 8);
        }

        @Override
        public Texture getTextureNormal() {
            return Render2D.SETTINGS_ICON;
        }

        @Override
        public Texture getTextureHovered() {
            return Render2D.SETTINGS_ICON_HOVERED;
        }

        @Override
        public void render(int mouseX, int mouseY, float partialTicks) {
            super.render(mouseX, mouseY, partialTicks);
            if (isHovered()) {
                WidgetScreen.assertActive().scheduleTooltip(I18n.format("menu.sfm.ItemFilter.Traits.Settings"), mouseX, mouseY);
            }
        }

        @Override
        public boolean onMouseClicked(double mouseX, double mouseY, int button) {
            MultiLayerMenu<?> parent = getParent();
            parent.openEditor(parent.getEditor());
            return true;
        }

        @Nonnull
        @Override
        public MultiLayerMenu<?> getParent() {
            return (MultiLayerMenu<?>) Objects.requireNonNull(super.getParent());
        }
    }
}
