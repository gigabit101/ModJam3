package vswe.stevesfactory.ui.manager.menu;

import com.mojang.blaze3d.systems.RenderSystem;
import lombok.val;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import vswe.stevesfactory.api.logic.IClientDataStorage;
import vswe.stevesfactory.api.logic.IProcedure;
import vswe.stevesfactory.library.gui.Render2D;
import vswe.stevesfactory.library.gui.Texture;
import vswe.stevesfactory.library.gui.layout.properties.BoxSizing;
import vswe.stevesfactory.library.gui.layout.properties.HorizontalAlignment;
import vswe.stevesfactory.library.gui.layout.properties.Side;
import vswe.stevesfactory.library.gui.layout.properties.VerticalAlignment;
import vswe.stevesfactory.library.gui.screen.WidgetScreen;
import vswe.stevesfactory.library.gui.widget.RadioController;
import vswe.stevesfactory.library.gui.widget.RadioInput;
import vswe.stevesfactory.library.gui.widget.TextField.BackgroundStyle;
import vswe.stevesfactory.library.gui.widget.button.ColoredTextButton;
import vswe.stevesfactory.library.gui.widget.button.SimpleIconButton;
import vswe.stevesfactory.library.gui.widget.panel.SettingsEditor;
import vswe.stevesfactory.library.gui.widget.panel.WrappingList;
import vswe.stevesfactory.library.gui.widget.slot.ItemSlot;
import vswe.stevesfactory.library.gui.window.SettingsEditorWindow;
import vswe.stevesfactory.logic.FilterType;
import vswe.stevesfactory.logic.item.ItemTraitsFilter;
import vswe.stevesfactory.logic.procedure.IItemFilterTarget;
import vswe.stevesfactory.ui.manager.FactoryManagerGUI;
import vswe.stevesfactory.ui.manager.editor.FlowComponent;
import vswe.stevesfactory.ui.manager.editor.Menu;

import java.util.List;

import static vswe.stevesfactory.library.gui.Render2D.fontRenderer;

public class ItemTraitsFilterMenu<P extends IProcedure & IClientDataStorage & IItemFilterTarget> extends Menu<P> {

    public static final int DEFAULT_FILTER_SLOTS = 5;

    private final int id;
    private final String name;

    private final RadioInput whitelist, blacklist;
    private final WrappingList<ItemSlot> slots;
    private final SimpleIconButton addFilter, removeFilter;
    private final SettingsEditor settings;

    public ItemTraitsFilterMenu(int id) {
        this(id, I18n.format("menu.sfm.ItemFilter.Traits"));
    }

    public ItemTraitsFilterMenu(int id, String name) {
        this.id = id;
        this.name = name;

        val filterTypeController = new RadioController();
        whitelist = new RadioInput(filterTypeController);
        blacklist = new RadioInput(filterTypeController);
        blacklist.setX(this.getWidth() / 2);

        slots = new WrappingList<>();
        slots.setItemSize(16);
        slots.setY(whitelist.getYBottom() + 4);
        slots.setItemsPerRow(5);
        slots.setVisibleRows(2);
        slots.setDimensions(slots.getFullWidth(), DEFAULT_CONTENT_HEIGHT - whitelist.getHeight() - 4 * 2);
        slots.getScrollUpArrow().setX(slots.getFullWidth() + 3);
        slots.alignArrows();

        removeFilter = new SimpleIconButton(Render2D.REMOVE_ENTRY_ICON, Render2D.REMOVE_ENTRY_HOVERED_ICON);
        removeFilter.alignTo(slots, Side.RIGHT, VerticalAlignment.BOTTOM.asUnion());
        removeFilter.moveX(4);
        addFilter = new SimpleIconButton(Render2D.ADD_ENTRY_ICON, Render2D.ADD_ENTRY_HOVERED_ICON);
        addFilter.alignTo(removeFilter, Side.TOP, HorizontalAlignment.CENTER.asUnion());
        addFilter.moveY(-2);

        settings = new SettingsEditor();
        settings.setWidth(this.getWidth());
        settings.setY(DEFAULT_CONTENT_HEIGHT + SIDE_MARGINS);
    }

    @Override
    public void onInitialAttach() {
        super.onInitialAttach();

        addChildren(whitelist);
        addChildren(whitelist.makeLabel().translate("gui.sfm.whitelist"));
        addChildren(blacklist);
        addChildren(blacklist.makeLabel().translate("gui.sfm.blacklist"));
        addChildren(slots);
        addChildren(addFilter);
        addChildren(removeFilter);
        addChildren(settings);
    }

    @Override
    public void onLinkFlowComponent(FlowComponent<P> flowComponent) {
        super.onLinkFlowComponent(flowComponent);
        val filter = this.getLinkedFilter();
        val items = filter.getItems();
        for (int i = 0; i < DEFAULT_FILTER_SLOTS; i++) {
            ItemStack stack;
            if (i < items.size()) {
                stack = items.get(i);
            } else {
                stack = ItemStack.EMPTY;
                items.add(ItemStack.EMPTY);
            }
            slots.addElement(this.newFilterSlot(stack));
        }

        switch (filter.type) {
            case WHITELIST:
                whitelist.check(true);
                break;
            case BLACKLIST:
                blacklist.check(true);
                break;
        }
        whitelist.setCheckAction(() -> filter.type = FilterType.WHITELIST);
        blacklist.setCheckAction(() -> filter.type = FilterType.BLACKLIST);

        addFilter.setClickAction(__ -> {
            items.add(ItemStack.EMPTY);
            slots.addElement(this.newFilterSlot(ItemStack.EMPTY));
        });
        removeFilter.setClickAction(__ -> {
            // TODO
//            items.remove(items.size() - 1);
//            val children = slots.getChildren();
//            children.remove(children.size() - 1);
        });

        settings.addOption(filter.isMatchingAmount(), "menu.sfm.ItemFilter.Traits.MatchAmount").onStateChange = filter::setMatchingAmount;
        settings.addOption(filter.isMatchingDamage(), "menu.sfm.ItemFilter.Traits.MatchDamage").onStateChange = filter::setMatchingDamage;
        settings.addOption(filter.isMatchingTag(), "menu.sfm.ItemFilter.Traits.MatchTag").onStateChange = filter::setMatchingTag;

        settings.adjustMinHeight();
        this.growHeight(2 + settings.getFullHeight());
    }

    private ItemSlot newFilterSlot(ItemStack stack) {
        val slot = new FilterSlot(stack);
        slot.defaultedLeft(() -> {
            val window = new SettingsEditorWindow(this.getWidth());
            val editor = window.getEditor();

            val count = editor.addIntegerInput(1, 1, Integer.MAX_VALUE, "menu.sfm.ItemFilter.Traits.Amount");
            count.setValue(stack.getCount());
            count.setBackgroundStyle(BackgroundStyle.RED_OUTLINE);
            count.onValueUpdated = stack::setCount;

            val damage = editor.addIntegerInput("menu.sfm.ItemFilter.Traits.Damage");
            damage.setValue(stack.getDamage());
            damage.setBackgroundStyle(BackgroundStyle.RED_OUTLINE);
            damage.onValueUpdated = stack::setDamage;

            val delete = new DeleteFilterButton();
            delete.setDimensions(32, 11);
            editor.addLine(delete);
            delete.setClickAction(__ -> slot.clearRenderedStack());

            editor.adjustMinHeight();
            FactoryManagerGUI.get().addPopupWindow(window);
        });
        return slot;
    }

    public ItemTraitsFilter getLinkedFilter() {
        return (ItemTraitsFilter) getLinkedProcedure().getFilter(id);
    }

    @Override
    public String getHeadingText() {
        return name;
    }

    @Override
    public List<String> populateErrors(List<String> errors) {
        return errors;
    }

    private static class FilterSlot extends ItemSlot {

        public static final Texture BACKGROUND = Render2D.ofFlowComponent(36, 20, 16, 16);
        public static final Texture BACKGROUND_HOVERED = BACKGROUND.moveDown(1);

        public FilterSlot(ItemStack renderedStack) {
            super(renderedStack);
            this.setDimensions(BACKGROUND.getPortionWidth(), BACKGROUND.getPortionHeight());
        }

        @Override
        public void renderBase() {
            BACKGROUND.render(getAbsoluteX(), getAbsoluteY(), getAbsoluteXRight(), getAbsoluteYBottom());
        }

        @Override
        public void renderHoveredBase() {
            BACKGROUND_HOVERED.render(getAbsoluteX(), getAbsoluteY(), getAbsoluteXRight(), getAbsoluteYBottom());
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
