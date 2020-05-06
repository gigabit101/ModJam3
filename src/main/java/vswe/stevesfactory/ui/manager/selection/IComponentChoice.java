package vswe.stevesfactory.ui.manager.selection;

import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import vswe.stevesfactory.api.logic.IProcedureType;
import vswe.stevesfactory.api.network.INetworkController;
import vswe.stevesfactory.library.gui.Render2D;
import vswe.stevesfactory.library.gui.screen.WidgetScreen;
import vswe.stevesfactory.library.gui.widget.IWidget;
import vswe.stevesfactory.ui.manager.FactoryManagerGUI;
import vswe.stevesfactory.ui.manager.editor.EditorPanel;
import vswe.stevesfactory.ui.manager.editor.FlowComponent;
import vswe.stevesfactory.utils.NetworkHelper;

import java.util.Objects;

public interface IComponentChoice extends IWidget {

    default void renderBackground(double mouseX, double mouseY) {
        int x1 = getAbsoluteX();
        int y1 = getAbsoluteY();
        int x2 = getAbsoluteX() + getWidth();
        int y2 = getAbsoluteY() + getHeight();
        Render2D.bindTexture(isInside(mouseX, mouseY) ? SelectionPanel.BACKGROUND_HOVERED : SelectionPanel.BACKGROUND_NORMAL);
        Render2D.beginTexturedQuad();
        Render2D.textureVertices(
                x1, y1, x2, y2, getZLevel(),
                0F, 0F, 1F, 1F
        );
        Render2D.draw();
    }

    default void createFlowComponent(IProcedureType<?> type) {
        BlockPos controllerPos = ((FactoryManagerGUI) WidgetScreen.assertActive()).getController().getPosition();
        INetworkController controller = (INetworkController) Objects.requireNonNull(Minecraft.getInstance().world.getTileEntity(controllerPos));
        EditorPanel editor = getEditorPanel();

        FlowComponent<?> comp = NetworkHelper.fabricateInstance(controller, type).createFlowComponent();
        // Magic number so that the flow component don't overlap with the selection panel
        comp.setLocation(10, 20);
        comp.setGroup(FactoryManagerGUI.get().groupModel.getCurrentGroup());
        editor.addChildren(comp);
    }

    EditorPanel getEditorPanel();
}
