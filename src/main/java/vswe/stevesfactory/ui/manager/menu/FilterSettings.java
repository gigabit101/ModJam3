package vswe.stevesfactory.ui.manager.menu;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.resources.I18n;
import vswe.stevesfactory.library.gui.TextureWrapper;
import vswe.stevesfactory.library.gui.screen.WidgetScreen;
import vswe.stevesfactory.library.gui.widget.*;
import vswe.stevesfactory.logic.item.ItemTraitsFilter;
import vswe.stevesfactory.ui.manager.FactoryManagerGUI;
import vswe.stevesfactory.ui.manager.editor.Menu;

import javax.annotation.Nonnull;
import java.util.*;

public class FilterSettings extends AbstractContainer<IWidget> {

    private final List<IWidget> children;

    public FilterSettings(ItemTraitsFilterMenu<?> menu) {
        setDimensions(menu.getWidth(), menu.getHeight() - Menu.HEADING_BOX.getPortionHeight());

        AbstractIconButton close = new AbstractIconButton(getWidth() - 8 - 1, getHeight() - 8 - 1, 8, 8) {
            @Override
            public void render(int mouseX, int mouseY, float particleTicks) {
                super.render(mouseX, mouseY, particleTicks);
                if (isHovered()) {
                    WidgetScreen.getCurrentScreen().setHoveringText(I18n.format("gui.sfm.Menu.CloseEditor.Info"), mouseX, mouseY);
                }
            }

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
                menu.openEditor(null);
                return super.mouseClicked(mouseX, mouseY, button);
            }
        };

        ItemTraitsFilter filter = menu.getLinkedFilter();
        int x = 4;
        Checkbox matchAmount = new Checkbox(x, 4, 8, 8);
        matchAmount.setChecked(filter.isMatchingAmount());
        matchAmount.translateLabel("gui.sfm.Menu.MatchAmount");
        matchAmount.onStateChange = filter::setMatchingAmount;
        Checkbox matchDamage = new Checkbox(x, matchAmount.getY() + matchAmount.getHeight() + 4, 8, 8);
        matchDamage.setChecked(filter.isMatchingDamage());
        matchDamage.translateLabel("gui.sfm.Menu.MatchDamage");
        matchDamage.onStateChange = filter::setMatchingDamage;
        Checkbox matchTag = new Checkbox(x, matchDamage.getY() + matchDamage.getHeight() + 4, 8, 8);
        matchTag.setChecked(filter.isMatchingTag());
        matchTag.translateLabel("gui.sfm.Menu.MatchTag");
        matchTag.onStateChange = filter::setMatchingTag;

        children = ImmutableList.of(close, matchAmount, matchDamage, matchTag);
    }

    @Override
    public Collection<IWidget> getChildren() {
        return children;
    }

    @Override
    public void reflow() {
    }

    @Nonnull
    @Override
    public ItemTraitsFilterMenu<?> getParentWidget() {
        return (ItemTraitsFilterMenu<?>) Objects.requireNonNull(super.getParentWidget());
    }
}
