package vswe.stevesfactory.ui.manager.menu;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;
import vswe.stevesfactory.api.logic.IProcedure;
import vswe.stevesfactory.api.logic.IProcedureClientData;
import vswe.stevesfactory.library.gui.TextureWrapper;
import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.screen.WidgetScreen;
import vswe.stevesfactory.library.gui.widget.*;
import vswe.stevesfactory.library.gui.widget.box.LinearList;
import vswe.stevesfactory.logic.FilterType;
import vswe.stevesfactory.logic.item.ItemTagFilter;
import vswe.stevesfactory.logic.procedure.IItemFilterTarget;
import vswe.stevesfactory.ui.manager.FactoryManagerGUI;
import vswe.stevesfactory.ui.manager.editor.FlowComponent;
import vswe.stevesfactory.ui.manager.editor.Menu;

import javax.annotation.Nonnull;
import java.util.*;

public class ItemTagFilterMenu<P extends IProcedure & IProcedureClientData & IItemFilterTarget> extends Menu<P> {

    private final int id;

    private final RadioButton whitelist, blacklist;
    private final LinearList<Entry> fields;

    public ItemTagFilterMenu(int id) {
        this.id = id;

        RadioController filterTypeController = new RadioController();
        whitelist = new RadioButton(filterTypeController);
        blacklist = new RadioButton(filterTypeController);
        int y = HEADING_BOX.getPortionHeight() + 4;
        whitelist.setLocation(4, y);
        whitelist.translateLabel("gui.sfm.whitelist");
        blacklist.setLocation(getWidth() / 2, y);
        blacklist.translateLabel("gui.sfm.blacklist");

        int contentY = whitelist.getYBottom() + 4;

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
                    WidgetScreen.getCurrentScreen().setHoveringText(I18n.format("gui.sfm.Menu.AddEntry"), mouseX, mouseY);
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
        for (int i = 0; i < 2; i++) {
            fields.addChildren(new Entry());
        }
        fields.reflow();

        addChildren(whitelist);
        addChildren(blacklist);
        addChildren(fields);
        addChildren(addEntryButton);
    }

    @Override
    public void onLinkFlowComponent(FlowComponent<P> flowComponent) {
        super.onLinkFlowComponent(flowComponent);
        ItemTagFilter filter = getLinkedFilter();
        for (Tag<Item> tag : filter.getTags()) {
            Entry entry = new Entry();
            entry.readTag(tag);
            fields.addChildren(entry);
        }

        switch (filter.type) {
            case WHITELIST:
                whitelist.check(true);
                break;
            case BLACKLIST:
                blacklist.check(true);
                break;
        }
    }

    @Override
    protected void updateData() {
        super.updateData();
        ItemTagFilter filter = getLinkedFilter();
        for (Entry entry : fields.getChildren()) {
            filter.getTags().add(entry.createTag());
        }

        if (whitelist.isChecked()) {
            filter.type = FilterType.WHITELIST;
        } else {
            filter.type = FilterType.BLACKLIST;
        }
    }

    public ItemTagFilter getLinkedFilter() {
        return (ItemTagFilter) getLinkedProcedure().getFilter(id);
    }

    @Override
    public String getHeadingText() {
        return I18n.format("gui.sfm.Menu.ItemFilter.Tag");
    }

    private static class Entry extends AbstractContainer<IWidget> {

        private final TextField tag;
        private final List<IWidget> children;

        public Entry() {
            setDimensions(90, 14);

            tag = new TextField(0, 0, 75, getHeight());
            AbstractIconButton removeEntry = new AbstractIconButton(
                    tag.getXRight() + 4, getHeight() / 2 - 9 / 2 - 1 /* Exclusive position, just to make it look nice */,
                    9, 9) {
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

        public Tag<Item> createTag() {
            ResourceLocation id = new ResourceLocation(tag.getText());
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
