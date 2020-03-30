package vswe.stevesfactory.ui.manager.tool.inspector;

import net.minecraft.client.resources.I18n;
import vswe.stevesfactory.library.gui.RenderingHelper;
import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.widget.AbstractContainer;
import vswe.stevesfactory.library.gui.widget.IWidget;
import vswe.stevesfactory.ui.manager.editor.FlowComponent;
import vswe.stevesfactory.ui.manager.tool.group.GroupButton;
import vswe.stevesfactory.ui.manager.tool.group.GroupDataModel;
import vswe.stevesfactory.ui.manager.tool.group.Grouplist;
import vswe.stevesfactory.ui.manager.toolbox.ToolboxPanel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class StatusPanel extends AbstractContainer<IWidget> {

    private FlowComponent<?> opened = null;
    private List<IWidget> children = new ArrayList<>();

    public StatusPanel() {
        super(0, 0, 120, 64);
    }

    @Override
    public void render(int mouseX, int mouseY, float particleTicks) {
        RenderEventDispatcher.onPreRender(this, mouseX, mouseY);

        int x = getAbsoluteX();
        int y = getAbsoluteY();
        if (opened != null) {
            int y1 = y + 2;
            RenderingHelper.drawText(opened.getName(), x + 2, y1, 9, 0xff404040);
            int y2 = y1 + 9 /* font height of name */ + 2 /* margin */;
            ToolboxPanel.GROUP_LIST_ICON.draw(x + 2, y2, 8, 8);
            RenderingHelper.drawText(GroupButton.formatGroupName(opened.getGroup()), x + 2 + 8 + 2, y2, 7, 0xff404040);
        } else {
            RenderingHelper.drawText(I18n.format("gui.sfm.FactoryManager.Tool.Inspector.NoTarget"), x + 2, y + 2, 9, 0xff404040);
        }

        super.render(mouseX, mouseY, particleTicks);
        RenderEventDispatcher.onPostRender(this, mouseX, mouseY);
    }

    @Override
    public Collection<IWidget> getChildren() {
        return children;
    }

    @Override
    public void reflow() {
    }

    public void openFlowComponent(FlowComponent<?> target) {
        this.opened = target;
    }
}
