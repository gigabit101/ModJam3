package vswe.stevesfactory.library.gui.widget.panel;

import lombok.val;
import net.minecraft.client.resources.I18n;
import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.layout.FlowLayout;
import vswe.stevesfactory.library.gui.widget.AbstractContainer;
import vswe.stevesfactory.library.gui.widget.Checkbox;
import vswe.stevesfactory.library.gui.widget.IWidget;
import vswe.stevesfactory.library.gui.widget.NumberField;
import vswe.stevesfactory.library.gui.widget.button.ColoredTextButton;
import vswe.stevesfactory.library.gui.widget.mixin.ResizableWidgetMixin;

import java.util.ArrayList;
import java.util.List;

public final class SettingsEditor extends AbstractContainer<IWidget> {

    private final List<IWidget> children = new ArrayList<>();

    @Override
    public void onInitialAttach() {
        super.onInitialAttach();

        for (IWidget child : children) {
            child.attach(this);
        }
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        RenderEventDispatcher.onPreRender(this, mouseX, mouseY);
        super.renderChildren(mouseX, mouseY, partialTicks);
        RenderEventDispatcher.onPostRender(this, mouseX, mouseY);
    }

    public Checkbox addOption(boolean value, String translationKey) {
        val checkbox = new Checkbox();
        checkbox.setDimensions(8, 8);
        checkbox.setChecked(value);

        addChildrenInternal(checkbox);
        children.add(checkbox.makeLabel().translate(translationKey));
        return checkbox;
    }

    public NumberField<Integer> addIntegerInput(String translationKey) {
        val field = NumberField.integerField(33, 12);
        addChildrenInternal(field);
        children.add(field.makeLabel().translate(translationKey));
        return field;
    }

    public NumberField<Integer> addIntegerInput(int defaultValue, int lowerBound, int upperBound, String translationKey) {
        val field = NumberField.integerFieldRanged(33, 12, defaultValue, lowerBound, upperBound);
        addChildrenInternal(field);
        children.add(field.makeLabel().translate(translationKey));
        return field;
    }

    public ColoredTextButton addButton(String translationKey) {
        val button = ColoredTextButton.of(I18n.format(translationKey));
        addChildrenInternal(button);
        return button;
    }

    public <W extends IWidget & ResizableWidgetMixin> void addLine(W widget) {
        addChildrenInternal(widget);
    }

    @Override
    public List<IWidget> getChildren() {
        return children;
    }

    private int nextY = 0;

    private <W extends IWidget & ResizableWidgetMixin> void addChildrenInternal(W child) {
        children.add(child);
        child.attach(this);

        child.setY(nextY);
        nextY += child.getFullHeight() + 4;
    }

    @Override
    public void reflow() {
        FlowLayout.vertical(children, 0, 0, 4);
        adjustMinHeight();
    }
}
