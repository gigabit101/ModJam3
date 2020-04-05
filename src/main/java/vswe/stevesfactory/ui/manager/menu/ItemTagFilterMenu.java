package vswe.stevesfactory.ui.manager.menu;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;
import vswe.stevesfactory.api.logic.IClientDataStorage;
import vswe.stevesfactory.api.logic.IProcedure;
import vswe.stevesfactory.library.gui.TextureWrapper;
import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.screen.WidgetScreen;
import vswe.stevesfactory.library.gui.widget.*;
import vswe.stevesfactory.library.gui.widget.TextField.BackgroundStyle;
import vswe.stevesfactory.library.gui.widget.box.LinearList;
import vswe.stevesfactory.logic.FilterType;
import vswe.stevesfactory.logic.item.ItemTagFilter;
import vswe.stevesfactory.logic.procedure.IItemFilterTarget;
import vswe.stevesfactory.ui.manager.FactoryManagerGUI;
import vswe.stevesfactory.ui.manager.editor.FlowComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class ItemTagFilterMenu<P extends IProcedure & IClientDataStorage & IItemFilterTarget> extends MultiLayerMenu<P> {

    private final int id;
    private final String name;

    private final RadioButton whitelist, blacklist;
    private final LinearList<Entry> fields;
    private SettingsEditor settings;

    public ItemTagFilterMenu(int id) {
        this(id, I18n.format("menu.sfm.ItemFilter.Tag"));
    }

    public ItemTagFilterMenu(int id, String name) {
        this.id = id;
        this.name = name;

        RadioController filterTypeController = new RadioController();
        whitelist = new RadioButton(filterTypeController);
        blacklist = new RadioButton(filterTypeController);
        int y = HEADING_BOX.getPortionHeight() + 4;
        whitelist.setLocation(4, y);
        whitelist.setLabel(I18n.format("gui.sfm.whitelist"));
        blacklist.setLocation(getWidth() / 2, y);
        blacklist.setLabel(I18n.format("gui.sfm.blacklist"));

        int contentY = whitelist.getYBottom() + 4;

        OpenSettingsButton openSettings = new OpenSettingsButton(getWidth() - 2 - 12, getHeight() + getContentHeight() - 2 - 12);
        AbstractIconButton addEntryButton = new AbstractIconButton(getWidth() - 4 - 8, contentY, 8, 8) {
            @Override
            public TextureWrapper getTextureNormal() {
                return FactoryManagerGUI.ADD_ENTRY_ICON;
            }

            @Override
            public TextureWrapper getTextureHovered() {
                return FactoryManagerGUI.ADD_ENTRY_HOVERED_ICON;
            }

            @Override
            public void render(int mouseX, int mouseY, float particleTicks) {
                super.render(mouseX, mouseY, particleTicks);
                if (isHovered()) {
                    WidgetScreen.getCurrent().setHoveringText(I18n.format("menu.sfm.ItemFilter.Tags.AddEntry"), mouseX, mouseY);
                }
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                fields.addChildren(new Entry());
                fields.reflow();
                return true;
            }
        };

        fields = new LinearList<>(addEntryButton.getX() - 4 * 2, getContentHeight() - whitelist.getHeight() - 4 * 2);
        fields.setLocation(4, contentY);

        addChildren(whitelist);
        addChildren(blacklist);
        addChildren(fields);
        addChildren(addEntryButton);
        addChildren(openSettings);
    }

    @Override
    public SettingsEditor getEditor() {
        return settings;
    }

    @Override
    public void onLinkFlowComponent(FlowComponent<P> flowComponent) {
        super.onLinkFlowComponent(flowComponent);
        ItemTagFilter filter = getLinkedFilter();
        Set<Tag<Item>> tags = filter.getTags();
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
                whitelist.check(true);
                break;
            case BLACKLIST:
                blacklist.check(true);
                break;
        }
        whitelist.onChecked = () -> filter.type = FilterType.WHITELIST;
        blacklist.onChecked = () -> filter.type = FilterType.BLACKLIST;

        settings = new SettingsEditor(this);
        NumberField<Integer> stackLimitInput = settings.addIntegerInput(1, 0, Integer.MAX_VALUE);
        stackLimitInput.setValue(filter.stackLimit);
        stackLimitInput.setBackgroundStyle(BackgroundStyle.RED_OUTLINE);
        stackLimitInput.setLabel(I18n.format("menu.sfm.ItemFilter.Traits.Amount"));
        stackLimitInput.onValueUpdated = i -> filter.stackLimit = i;
        Checkbox checkbox = settings.addOption(filter.isMatchingAmount(), "menu.sfm.ItemFilter.Traits.MatchAmount");
        checkbox.onStateChange = b -> {
            filter.setMatchingAmount(b);
            stackLimitInput.setEnabled(b);
        };
    }

    @Override
    protected void saveData() {
        super.saveData();
        ItemTagFilter filter = getLinkedFilter();
        filter.getTags().clear();
        for (Entry entry : fields.getChildren()) {
            Tag<Item> tag = entry.createTag();
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

            tag = new TextField(0, 0, 75, getHeight());
            tag.setBackgroundStyle(BackgroundStyle.RED_OUTLINE);
            tag.setFontHeight(7);
            int buttonSize = 9;
            AbstractIconButton removeEntry = new AbstractIconButton(
                    tag.getXRight() + 4, getHeight() / 2 - buttonSize / 2 - 1 /* Exclusive position, just to make it look nice */,
                    buttonSize, buttonSize) {
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
                    Entry entry = Entry.this;
                    LinearList<Entry> list = entry.getParentWidget();
                    list.getChildren().remove(entry);
                    list.reflow();
                    return true;
                }
            };

            children = ImmutableList.of(tag, removeEntry);
        }

        @Override
        public void render(int mouseX, int mouseY, float particleTicks) {
            RenderEventDispatcher.onPreRender(this, mouseX, mouseY);
            super.render(mouseX, mouseY, particleTicks);
            RenderEventDispatcher.onPostRender(this, mouseX, mouseY);
        }

        @Override
        public Collection<IWidget> getChildren() {
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
        public LinearList<Entry> getParentWidget() {
            return (LinearList<Entry>) Objects.requireNonNull(super.getParentWidget());
        }
    }
}
