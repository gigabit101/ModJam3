package vswe.stevesfactory.ui.manager.selection;

import net.minecraft.util.ResourceLocation;
import vswe.stevesfactory.api.logic.IProcedureType;
import vswe.stevesfactory.library.gui.actionmenu.ActionMenu;
import vswe.stevesfactory.library.gui.actionmenu.CallbackEntry;
import vswe.stevesfactory.library.gui.actionmenu.IEntry;
import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.screen.WidgetScreen;
import vswe.stevesfactory.library.gui.widget.AbstractWidget;
import vswe.stevesfactory.library.gui.widget.mixin.LeafWidgetMixin;
import vswe.stevesfactory.ui.manager.editor.EditorPanel;
import vswe.stevesfactory.utils.RenderingHelper;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GroupComponentChoice extends AbstractWidget implements IComponentChoice, LeafWidgetMixin {

    private ComponentGroup group;

    public GroupComponentChoice(ComponentGroup group) {
        super(0, 0, 16, 16);
        this.group = group;
    }

    @Override
    public void render(int mouseX, int mouseY, float particleTicks) {
        RenderEventDispatcher.onPreRender(this, mouseX, mouseY);
        renderBackground(mouseX, mouseY);
        RenderingHelper.drawCompleteTexture(getAbsoluteX(), getAbsoluteY(), getAbsoluteXRight(), getAbsoluteYBottom(), getIcon());
        RenderEventDispatcher.onPostRender(this, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        List<IEntry> entries = new ArrayList<>();
        for (IProcedureType<?> type : group.getMembers()) {
            entries.add(new CallbackEntry(type.getIcon(), type.getLocalizedName(), b -> createFlowComponent(type)));
        }
        ActionMenu actionMenu = ActionMenu.atCursor(getAbsoluteXRight() + 2, getAbsoluteY(), entries);
        WidgetScreen.getCurrentScreen().addPopupWindow(actionMenu);
        return true;
    }

    public ResourceLocation getIcon() {
        return group.getIcon();
    }

    @Nonnull
    @Override
    public SelectionPanel getParentWidget() {
        return Objects.requireNonNull((SelectionPanel) super.getParentWidget());
    }

    @Override
    public EditorPanel getEditorPanel() {
        return getParentWidget().getParentWidget().editorPanel;
    }
}
