package vswe.stevesfactory.ui.manager.selection;

import lombok.val;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import vswe.stevesfactory.api.logic.IProcedureType;
import vswe.stevesfactory.library.gui.Render2D;
import vswe.stevesfactory.library.gui.contextmenu.CallbackEntry;
import vswe.stevesfactory.library.gui.contextmenu.ContextMenu;
import vswe.stevesfactory.library.gui.contextmenu.DefaultEntry;
import vswe.stevesfactory.library.gui.contextmenu.Section;
import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.screen.WidgetScreen;
import vswe.stevesfactory.library.gui.widget.AbstractWidget;
import vswe.stevesfactory.library.gui.widget.mixin.LeafWidgetMixin;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;

public class GroupComponentChoice extends AbstractWidget implements IComponentChoice, LeafWidgetMixin {

    private ComponentGroup group;

    public GroupComponentChoice(ComponentGroup group) {
        this.setDimensions(16, 16);
        this.group = group;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        RenderEventDispatcher.onPreRender(this, mouseX, mouseY);

        renderBackground(mouseX, mouseY);

        Render2D.bindTexture(getIcon());
        Render2D.beginTexturedQuad();
        Render2D.textureVertices(
                getAbsoluteX(), getAbsoluteY(),
                getAbsoluteXRight(), getAbsoluteYBottom(),
                getZLevel(),
                0F, 0F,
                1F, 1F
        );
        Render2D.draw();

        if (isInside(mouseX, mouseY)) {
            WidgetScreen.assertActive().scheduleTooltip(I18n.format(group.getTranslationKey()), mouseX, mouseY);
        }

        RenderEventDispatcher.onPostRender(this, mouseX, mouseY);
    }

    @Override
    public boolean onMouseClicked(double mouseX, double mouseY, int button) {
        if (button != GLFW_MOUSE_BUTTON_LEFT) {
            return false;
        }

        val cm = ContextMenu.atCursor();
        cm.setPosition(getAbsoluteXRight() + 2, getAbsoluteY());
        val primary = new Section();
        cm.addSection(primary);

        if (group.getMembers().isEmpty()) {
            primary.addChildren(new DefaultEntry(null, "gui.sfm.FactoryManager.Selection.NoComponentGroupsPresent"));
        } else {
            for (val type : group.getMembers()) {
                primary.addChildren(new CallbackEntry(type.getIcon(), type.getLocalizedName(), b -> createFlowComponent(type)));
            }
        }

        cm.reflow();
        WidgetScreen.assertActive().addPopupWindow(cm);
        getWindow().setFocusedWidget(this);
        return true;
    }

    public ResourceLocation getIcon() {
        return group.getIcon();
    }
}
