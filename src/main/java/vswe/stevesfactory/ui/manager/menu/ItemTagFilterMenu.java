package vswe.stevesfactory.ui.manager.menu;

import com.google.common.collect.ImmutableList;
import lombok.val;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;
import vswe.stevesfactory.api.logic.IClientDataStorage;
import vswe.stevesfactory.api.logic.IProcedure;
import vswe.stevesfactory.library.gui.Render2D;
import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.layout.properties.Side;
import vswe.stevesfactory.library.gui.layout.properties.VerticalAlignment;
import vswe.stevesfactory.library.gui.widget.*;
import vswe.stevesfactory.library.gui.widget.TextField.BackgroundStyle;
import vswe.stevesfactory.library.gui.widget.button.AbstractIconButton;
import vswe.stevesfactory.library.gui.widget.button.SimpleIconButton;
import vswe.stevesfactory.library.gui.widget.panel.SettingsEditor;
import vswe.stevesfactory.library.gui.widget.panel.VerticalList;
import vswe.stevesfactory.logic.FilterType;
import vswe.stevesfactory.logic.item.ItemTagFilter;
import vswe.stevesfactory.logic.procedure.IItemFilterTarget;
import vswe.stevesfactory.ui.manager.editor.FlowComponent;
import vswe.stevesfactory.ui.manager.editor.Menu;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;

public class ItemTagFilterMenu<P extends IProcedure & IClientDataStorage & IItemFilterTarget> extends Menu<P> {

    private final int id;
    private final String name;

    private final RadioInput whitelist, blacklist;
    private final VerticalList<Entry> fields;
    private final AbstractIconButton addEntryButton;
    private final SettingsEditor settings;

    public ItemTagFilterMenu(int id) {
        this(id, I18n.format("menu.sfm.ItemFilter.Tag"));
    }

    public ItemTagFilterMenu(int id, String name) {
        this.id = id;
        this.name = name;

        val filterTypeController = new RadioController();
        whitelist = new RadioInput(filterTypeController);
        blacklist = new RadioInput(filterTypeController);
        blacklist.setX(this.getWidth() / 2);

        fields = new VerticalList<>();

        addEntryButton = new SimpleIconButton(Render2D.ADD_ENTRY_ICON, Render2D.REMOVE_ENTRY_HOVERED_ICON);
        addEntryButton.alignRight(this.getWidth() - 2);
        addEntryButton.alignTop(whitelist.getYBottom() + 4);
        addEntryButton.setClickAction(b -> {
            if (b != GLFW_MOUSE_BUTTON_LEFT) {
                return;
            }
            fields.addChildren(new Entry());
            fields.reflow();
        });

        fields.alignTop(whitelist.getYBottom() + 4);
        fields.setDimensions(addEntryButton.getX() - 2, DEFAULT_CONTENT_HEIGHT - whitelist.getYBottom() - 2);

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
        addChildren(fields);
        addChildren(addEntryButton);
        addChildren(settings);
    }

    @Override
    public void onLinkFlowComponent(FlowComponent<P> flowComponent) {
        super.onLinkFlowComponent(flowComponent);
        val filter = getLinkedFilter();
        val tags = filter.getTags();
        if (tags.isEmpty()) {
            for (int i = 0; i < 2; i++) {
                fields.addChildren(new Entry());
            }
        } else {
            for (Tag<Item> tag : tags) {
                Entry entry = new Entry();
                entry.readTag(tag);
                fields.addChildren(entry);
            }
        }
        fields.reflow();

        switch (filter.type) {
            case WHITELIST:
                whitelist.setCheckedAndUpdate(true);
                break;
            case BLACKLIST:
                blacklist.setCheckedAndUpdate(true);
                break;
        }
        whitelist.setCheckAction(() -> filter.type = FilterType.WHITELIST);
        blacklist.setCheckAction(() -> filter.type = FilterType.BLACKLIST);

        val stackLimitInput = settings.addIntegerInput(1, 0, Integer.MAX_VALUE, "menu.sfm.ItemFilter.Traits.Amount");
        stackLimitInput.setValue(filter.stackLimit);
        stackLimitInput.setBackgroundStyle(BackgroundStyle.RED_OUTLINE);
        stackLimitInput.onValueUpdated = i -> filter.stackLimit = i;
        val checkbox = settings.addOption(filter.isMatchingAmount(), "menu.sfm.ItemFilter.Traits.MatchAmount");
        checkbox.onStateChange = b -> {
            filter.setMatchingAmount(b);
            stackLimitInput.setEnabled(b);
        };

        settings.adjustMinHeight();
        this.growHeight(2 + settings.getFullHeight());
    }

    @Override
    protected void saveData() {
        super.saveData();
        val filter = getLinkedFilter();
        filter.getTags().clear();
        for (val entry : fields.getChildren()) {
            val tag = entry.createTag();
            if (tag != null) {
                filter.getTags().add(tag);
            }
        }
    }

    public ItemTagFilter getLinkedFilter() {
        return (ItemTagFilter) getLinkedProcedure().getFilter(id);
    }

    @Override
    public String getHeadingText() {
        return name;
    }

    @Override
    public List<String> populateErrors(List<String> errors) {
        return errors;
    }

    private static class Entry extends AbstractContainer<IWidget> {

        private final TextField tag;
        private final List<IWidget> children;

        public Entry() {
            setDimensions(90, 14);

            tag = new TextField();
            tag.setDimensions(75, getHeight());
            tag.setBackgroundStyle(BackgroundStyle.RED_OUTLINE);
            tag.getTextRenderer().setFontHeight(7);

            val removeEntry = new SimpleIconButton(Render2D.CLOSE_ICON, Render2D.CLOSE_ICON_HOVERED);
            removeEntry.alignTo(tag, Side.RIGHT, VerticalAlignment.CENTER.asUnion());
            removeEntry.moveX(3);
            removeEntry.setClickAction(b -> {
                if (b != GLFW_MOUSE_BUTTON_LEFT) {
                    return;
                }

                val list = this.getParent();
                list.getChildren().remove(this);
                list.reflow();
            });

            children = ImmutableList.of(tag, removeEntry);
        }

        @Override
        public void onInitialAttach() {
            super.onInitialAttach();

            for (val child : children) {
                child.attach(this);
            }
        }

        @Override
        public void render(int mouseX, int mouseY, float partialTicks) {
            RenderEventDispatcher.onPreRender(this, mouseX, mouseY);
            super.renderChildren(mouseX, mouseY, partialTicks);
            RenderEventDispatcher.onPostRender(this, mouseX, mouseY);
        }

        @Override
        public List<IWidget> getChildren() {
            return children;
        }

        @Override
        public void reflow() {
        }

        public void readTag(Tag<Item> tag) {
            this.tag.setText(tag.getId().toString());
        }

        @Nullable
        public Tag<Item> createTag() {
            ResourceLocation id = new ResourceLocation(tag.getText());
            if (ItemTags.getCollection().get(id) == null) {
                return null;
            }
            return new ItemTags.Wrapper(id);
        }

        @Nonnull
        @Override
        @SuppressWarnings("unchecked")
        public VerticalList<Entry> getParent() {
            return (VerticalList<Entry>) Objects.requireNonNull(super.getParent());
        }
    }
}
