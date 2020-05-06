package vswe.stevesfactory.ui.manager.menu;

import net.minecraft.client.resources.I18n;
import vswe.stevesfactory.library.gui.Render2D;
import vswe.stevesfactory.library.gui.Texture;
import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.layout.FlowLayout;
import vswe.stevesfactory.library.gui.layout.properties.BoxSizing;
import vswe.stevesfactory.library.gui.screen.WidgetScreen;
import vswe.stevesfactory.library.gui.widget.AbstractContainer;
import vswe.stevesfactory.library.gui.widget.Checkbox;
import vswe.stevesfactory.library.gui.widget.IWidget;
import vswe.stevesfactory.library.gui.widget.NumberField;
import vswe.stevesfactory.library.gui.widget.button.AbstractIconButton;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public final class SettingsEditor extends AbstractContainer<IWidget> {

    private final List<IWidget> children = new ArrayList<>();

    public SettingsEditor(MultiLayerMenu<?> menu) {
        setDimensions(menu.getWidth(), menu.getContentHeight());

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
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                menu.openEditor(null);
                return super.mouseClicked(mouseX, mouseY, button);
            }

            @Override
            public BoxSizing getBoxSizing() {
                return BoxSizing.PHANTOM;
            }
        };
        close.attach(this);
        children.add(close);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        RenderEventDispatcher.onPreRender(this, mouseX, mouseY);
        super.renderChildren(mouseX, mouseY, partialTicks);
        RenderEventDispatcher.onPostRender(this, mouseX, mouseY);
    }

    public Checkbox addOption(boolean value, String translationKey) {
        Checkbox checkbox = new Checkbox();
        checkbox.setDimensions(8, 8);
        checkbox.setChecked(value);
        // TODO label pos
        children.add(checkbox);
        children.add(checkbox.makeLabel().translate(translationKey));
        reflow();
        return checkbox;
    }

    public NumberField<Integer> addIntegerInput(int defaultValue, int lowerBound, int upperBound) {
        NumberField<Integer> field = NumberField.integerFieldRanged(33, 12, defaultValue, lowerBound, upperBound);
        children.add(field);
        reflow();
        return field;
    }

    public NumberField<Integer> addIntegerInput(int defaultValue, int lowerBound, int upperBound, String translationKey) {
        NumberField<Integer> field = NumberField.integerFieldRanged(33, 12, defaultValue, lowerBound, upperBound);
        children.add(field);
        children.add(field.makeLabel().translate(translationKey));
        reflow();
        return field;
    }

    public void addLine(IWidget widget) {
        children.add(widget);
    }

    @Override
    public Collection<IWidget> getChildren() {
        return children;
    }

    @Override
    public void reflow() {
        FlowLayout.vertical(children, 4, 4, 4);
    }

    @Nonnull
    @Override
    public MultiLayerMenu<?> getParent() {
        return (MultiLayerMenu<?>) Objects.requireNonNull(super.getParent());
    }
}
