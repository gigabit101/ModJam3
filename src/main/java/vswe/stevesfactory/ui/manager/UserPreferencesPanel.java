package vswe.stevesfactory.ui.manager;

import com.google.common.collect.ImmutableList;
import net.minecraft.util.ResourceLocation;
import vswe.stevesfactory.library.gui.widget.IWidget;
import vswe.stevesfactory.library.gui.actionmenu.AbstractEntry;
import vswe.stevesfactory.library.gui.widget.AbstractContainer;
import vswe.stevesfactory.utils.RenderingHelper;

import java.util.Collection;

public class UserPreferencesPanel extends AbstractContainer<IWidget> {

    @Override
    public Collection<IWidget> getChildren() {
        return ImmutableList.of();
    }

    @Override
    public void reflow() {
    }

    // "AM" stands for Action Menu
    public static class OpenerEntry extends AbstractEntry {

        public static final ResourceLocation ICON = RenderingHelper.linkTexture("gui/actions/preferences.png");

        public OpenerEntry() {
            super(ICON, "gui.sfm.ActionMenu.UserPreferences");
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            // TODO open user preferences panel
            return super.mouseClicked(mouseX, mouseY, button);
        }
    }
}
