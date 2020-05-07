package vswe.stevesfactory.ui.manager.selection;

import net.minecraft.util.ResourceLocation;
import vswe.stevesfactory.api.logic.IProcedureType;
import vswe.stevesfactory.library.gui.Render2D;
import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.widget.AbstractWidget;
import vswe.stevesfactory.library.gui.widget.mixin.LeafWidgetMixin;
import vswe.stevesfactory.ui.manager.FactoryManagerGUI;

public class SingularComponentChoice extends AbstractWidget implements IComponentChoice, LeafWidgetMixin {

    private final IProcedureType<?> type;

    public SingularComponentChoice(IProcedureType<?> type) {
        this.setDimensions(16, 16);
        this.type = type;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        RenderEventDispatcher.onPreRender(this, mouseX, mouseY);
        renderBackground(mouseX, mouseY);
        Render2D.bindTexture(getIcon());
        Render2D.beginTexturedQuad();
        Render2D.textureVertices(getAbsoluteX(), getAbsoluteY(), getAbsoluteXRight(), getAbsoluteYBottom(), getZLevel(), 0F, 0F, 1F, 1F);
        Render2D.draw();
        if (isInside(mouseX, mouseY)) {
            FactoryManagerGUI.get().scheduleTooltip(type.getLocalizedName(), mouseX, mouseY);
        }
        RenderEventDispatcher.onPostRender(this, mouseX, mouseY);
    }

    @Override
    public boolean onMouseClicked(double mouseX, double mouseY, int button) {
        createFlowComponent(type);
        getWindow().setFocusedWidget(this);
        return true;
    }

    public ResourceLocation getIcon() {
        return type.getIcon();
    }
}
