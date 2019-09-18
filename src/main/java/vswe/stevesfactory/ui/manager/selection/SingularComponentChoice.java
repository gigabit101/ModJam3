package vswe.stevesfactory.ui.manager.selection;

import net.minecraft.util.ResourceLocation;
import vswe.stevesfactory.api.logic.IProcedureType;
import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.widget.AbstractWidget;
import vswe.stevesfactory.library.gui.widget.mixin.LeafWidgetMixin;
import vswe.stevesfactory.ui.manager.editor.EditorPanel;
import vswe.stevesfactory.utils.RenderingHelper;

import javax.annotation.Nonnull;
import java.util.Objects;

public class SingularComponentChoice extends AbstractWidget implements IComponentChoice, LeafWidgetMixin {

    private final IProcedureType<?> type;

    public SingularComponentChoice(IProcedureType<?> type) {
        super(0, 0, 16, 16);
        this.type = type;
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
        createFlowComponent(type);
        return true;
    }

    public ResourceLocation getIcon() {
        return type.getIcon();
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
