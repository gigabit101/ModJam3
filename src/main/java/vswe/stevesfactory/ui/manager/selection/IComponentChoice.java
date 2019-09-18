package vswe.stevesfactory.ui.manager.selection;

import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import vswe.stevesfactory.api.logic.IProcedure;
import vswe.stevesfactory.api.logic.IProcedureType;
import vswe.stevesfactory.api.network.INetworkController;
import vswe.stevesfactory.library.gui.screen.WidgetScreen;
import vswe.stevesfactory.library.gui.widget.IWidget;
import vswe.stevesfactory.ui.manager.FactoryManagerGUI;
import vswe.stevesfactory.ui.manager.editor.EditorPanel;
import vswe.stevesfactory.ui.manager.editor.FlowComponent;
import vswe.stevesfactory.utils.RenderingHelper;

public interface IComponentChoice extends IWidget {

    default void renderBackground(double mouseX, double mouseY) {
        int x1 = getAbsoluteX();
        int y1 = getAbsoluteY();
        int x2 = getAbsoluteX() + getWidth();
        int y2 = getAbsoluteY() + getHeight();
        if (isInside(mouseX, mouseY)) {
            RenderingHelper.drawCompleteTexture(x1, y1, x2, y2, SelectionPanel.BACKGROUND_HOVERED);
        } else {
            RenderingHelper.drawCompleteTexture(x1, y1, x2, y2, SelectionPanel.BACKGROUND_NORMAL);
        }
    }

    default void createFlowComponent(IProcedureType<?> type) {
        EditorPanel editorPanel = getEditorPanel();
        BlockPos controllerPos = ((FactoryManagerGUI) WidgetScreen.getCurrentScreen()).controllerPos;
        INetworkController controller = (INetworkController) Minecraft.getInstance().world.getTileEntity(controllerPos);
        FlowComponent<?> flowComponent = type.createInstance(controller).createFlowComponent();
        // Magic number so that the flow component don't overlap with the selection panel
        flowComponent.setLocation(10, 20);
        editorPanel.addChildren(flowComponent);
    }

    EditorPanel getEditorPanel();
}
