package vswe.stevesfactory.ui.manager.selection;

import lombok.val;
import vswe.stevesfactory.api.logic.IProcedureType;
import vswe.stevesfactory.library.gui.Render2D;
import vswe.stevesfactory.library.gui.widget.IWidget;
import vswe.stevesfactory.ui.manager.FactoryManagerGUI;
import vswe.stevesfactory.ui.manager.editor.FlowComponent;
import vswe.stevesfactory.utils.NetworkHelper;

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
        val gui = FactoryManagerGUI.get();
        val controller = gui.getController();
        val editor = gui.getPrimaryWindow().editorPanel;

        val comp = NetworkHelper.fabricateInstance(controller, type).createFlowComponent();
        // Magic number so that the flow component don't overlap with the selection panel
        comp.setLocation(10, 20);
        comp.setGroup(gui.groupModel.getCurrentGroup());
        editor.addChildren(comp);
    }
}
