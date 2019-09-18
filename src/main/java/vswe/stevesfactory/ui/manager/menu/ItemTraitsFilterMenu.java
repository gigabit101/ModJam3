package vswe.stevesfactory.ui.manager.menu;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import vswe.stevesfactory.api.logic.IProcedure;
import vswe.stevesfactory.api.logic.IProcedureClientData;
import vswe.stevesfactory.library.gui.TextureWrapper;
import vswe.stevesfactory.library.gui.layout.properties.HorizontalAlignment;
import vswe.stevesfactory.library.gui.layout.properties.Side;
import vswe.stevesfactory.library.gui.screen.WidgetScreen;
import vswe.stevesfactory.library.gui.widget.*;
import vswe.stevesfactory.library.gui.widget.box.ScrollArrow;
import vswe.stevesfactory.library.gui.widget.box.WrappingList;
import vswe.stevesfactory.logic.FilterType;
import vswe.stevesfactory.logic.item.IItemFilter;
import vswe.stevesfactory.logic.item.ItemTraitsFilter;
import vswe.stevesfactory.logic.procedure.IItemFilterTarget;
import vswe.stevesfactory.ui.manager.FactoryManagerGUI;
import vswe.stevesfactory.ui.manager.editor.FlowComponent;
import vswe.stevesfactory.ui.manager.editor.Menu;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class ItemTraitsFilterMenu<P extends IProcedure & IProcedureClientData & IItemFilterTarget> extends Menu<P> {

    private static final Supplier<Integer> FILTER_SLOTS = () -> 20;

    private final int id;

    private final RadioButton whitelist, blacklist;
    private final WrappingList<FilterSlot> slots;
    private FilterSettings settings;
    private IWidget openEditor;

    public ItemTraitsFilterMenu(int id) {
        this.id = id;

        RadioController filterTypeController = new RadioController();
        whitelist = new RadioButton(filterTypeController);
        blacklist = new RadioButton(filterTypeController);
        int y = HEADING_BOX.getPortionHeight() + 4;
        whitelist.setLocation(4, y);
        whitelist.translateLabel("gui.sfm.whitelist");
        blacklist.setLocation(getWidth() / 2, y);
        blacklist.translateLabel("gui.sfm.blacklist");

        slots = new WrappingList<>(false);
        slots.setLocation(4, whitelist.getYBottom() + 4);
        slots.setItemsPerRow(5);
        slots.setVisibleRows(2);
        slots.setDimensions(slots.getContentArea().width, getContentHeight() - whitelist.getHeight() - 4 * 2);
        slots.getScrollUpArrow().setLocation(100, 0);
        slots.alignArrows();

        AbstractIconButton openSettings = new AbstractIconButton(0, 0, 12, 12) {
            @Override
            public TextureWrapper getTextureNormal() {
                return FactoryManagerGUI.SETTINGS_ICON;
            }

            @Override
            public TextureWrapper getTextureHovered() {
                return FactoryManagerGUI.SETTINGS_ICON_HOVERED;
            }

            @Override
            public void render(int mouseX, int mouseY, float particleTicks) {
                super.render(mouseX, mouseY, particleTicks);
                if (isHovered()) {
                    WidgetScreen.getCurrentScreen().setHoveringText(I18n.format("gui.sfm.Menu.ItemFilter.Traits.Settings"), mouseX, mouseY);
                }
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                openEditor(getSettings());
                return true;
            }
        };
        ScrollArrow arrow = slots.getScrollDownArrow();
        int ax = slots.getX() + arrow.getX();
        int ay = slots.getY() + arrow.getY();
        openSettings.alignTo(ax, ay, ax + arrow.getWidth(), ay + arrow.getHeight(), Side.BOTTOM, HorizontalAlignment.CENTER);
        openSettings.moveY(8);

        addChildren(whitelist);
        addChildren(blacklist);
        addChildren(slots);
        addChildren(openSettings);
    }

    @Override
    public void onLinkFlowComponent(FlowComponent<P> flowComponent) {
        super.onLinkFlowComponent(flowComponent);
        ItemTraitsFilter filter = getLinkedFilter();
        for (Integer i = 0; i < FILTER_SLOTS.get(); i++) {
            ItemStack stack;
            if (i < filter.getItems().size()) {
                stack = filter.getItems().get(i);
            } else {
                stack = ItemStack.EMPTY;
                filter.getItems().add(ItemStack.EMPTY);
            }
            FilterSlot slot = new FilterSlot(stack);
            slots.addElement(slot);
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
        ItemTraitsFilter filter = getLinkedFilter();
        int i = 0;
        for (FilterSlot slot : slots.getContents()) {
            filter.getItems().set(i, slot.stack);
            i++;
        }

        if (whitelist.isChecked()) {
            filter.type = FilterType.WHITELIST;
        } else {
            filter.type = FilterType.BLACKLIST;
        }
    }

    public FilterSettings getSettings() {
        if (settings == null) {
            settings = new FilterSettings(this);
            settings.setParentWidget(this);
        }
        return settings;
    }

    public ItemTraitsFilter getLinkedFilter() {
        return (ItemTraitsFilter) getLinkedProcedure().getFilter(id);
    }

    @Override
    public String getHeadingText() {
        return I18n.format("gui.sfm.Menu.ItemFilter.Traits");
    }

    @Override
    public void renderContents(int mouseX, int mouseY, float particleTicks) {
        GlStateManager.color3f(1F, 1F, 1F);
        if (openEditor != null) {
            getToggleStateButton().render(mouseX, mouseY, particleTicks);
            openEditor.render(mouseX, mouseY, particleTicks);
        } else {
            super.renderContents(mouseX, mouseY, particleTicks);
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
        }
        super.mouseMoved(mouseX, mouseY);
    }

    @Override
    public void update(float particleTicks) {
        if (openEditor != null) {
            openEditor.update(particleTicks);
        }
        super.update(particleTicks);
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
        if (openEditor != null) {
            openEditor.onRemoved();
        }
        this.openEditor = editor;
        if (editor != null) {
            editor.setParentWidget(this);
            editor.setLocation(0, HEADING_BOX.getPortionHeight());
        }
    }
}
