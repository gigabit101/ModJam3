package vswe.stevesfactory.ui.manager.tool.inspector;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Getter;
import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.layout.FlowLayout;
import vswe.stevesfactory.library.gui.widget.AbstractContainer;
import vswe.stevesfactory.library.gui.widget.IWidget;
import vswe.stevesfactory.ui.manager.FactoryManagerGUI;
import vswe.stevesfactory.ui.manager.editor.FlowComponent;

import javax.annotation.Nullable;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

public class Inspector extends AbstractContainer<IWidget> {

    @Nullable
    public static Inspector getActiveInspector() {
        IWidget tool = FactoryManagerGUI.get().getPrimaryWindow().toolHolderPanel.getContainedWidget();
        if (tool instanceof Inspector) {
            return (Inspector) tool;
        } else {
            return null;
        }
    }

    private final List<IWidget> children;
    private final StatusPanel status;
    private final PropertiesPanel props;

    public Inspector() {
        this.setWidth(120);
        status = new StatusPanel(); // with fixed size
        props = new PropertiesPanel();
        children = ImmutableList.of(status, props);
        reflow();
    }

    @Override
    public void onInitialAttach() {
        super.onInitialAttach();

        for (IWidget child : children) {
            child.attach(this);
        }
    }

    public void openFlowComponent(FlowComponent<?> target) {
        status.openFlowComponent(target);
        props.openFlowComponent(target);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        RenderEventDispatcher.onPreRender(this, mouseX, mouseY);

        RenderSystem.disableTexture();
        RenderSystem.lineWidth(1F);
        RenderSystem.color3f(64F / 255F, 64F / 255F, 64F / 255F);
        int y = status.getAbsoluteYBottom() + 1;
        glBegin(GL_LINES);
        glVertex3f(status.getAbsoluteX(), y, 0F);
        glVertex3f(status.getAbsoluteXRight(), y, 0F);
        glEnd();
        RenderSystem.enableTexture();

        super.renderChildren(mouseX, mouseY, partialTicks);
        RenderEventDispatcher.onPostRender(this, mouseX, mouseY);
    }

    @Override
    public List<IWidget> getChildren() {
        return children;
    }

    @Override
    public void reflow() {
        // 3px gap for the divider line (1px)
        FlowLayout.vertical(children, 0, 0, 3);
        props.setHeight(this.getHeight() - status.getHeight() - 3);
    }
}
