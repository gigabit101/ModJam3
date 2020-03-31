package vswe.stevesfactory.ui.manager.editor;

import com.mojang.datafixers.util.Either;
import vswe.stevesfactory.api.logic.IProcedure;
import vswe.stevesfactory.library.gui.TextureWrapper;
import vswe.stevesfactory.library.gui.widget.AbstractIconButton;
import vswe.stevesfactory.ui.manager.FactoryManagerGUI;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT;
import static vswe.stevesfactory.ui.manager.editor.ConnectionsPanel.REGULAR_HEIGHT;
import static vswe.stevesfactory.ui.manager.editor.ConnectionsPanel.REGULAR_WIDTH;

public final class EndNode extends AbstractIconButton implements INode {

    public static final TextureWrapper INPUT_NORMAL = TextureWrapper.ofFlowComponent(18, 51, REGULAR_WIDTH, REGULAR_HEIGHT);
    public static final TextureWrapper INPUT_HOVERED = INPUT_NORMAL.toRight(1);

    private INode previous;
    private StartNode start;

    public final int index;
    public final ShadowNode shadow;

    public EndNode(int index) {
        super(0, 0, REGULAR_WIDTH, REGULAR_HEIGHT);
        this.index = index;
        this.shadow = new ShadowNode(this);
        // This action will do nothing if we are reading from network data
        FactoryManagerGUI.get().getTopLevel().connectionsPanel.addChildren(shadow);
    }

    @Override
    public void onParentPositionChanged() {
        super.onParentPositionChanged();
        updateShadowPosition();
    }

    @Override
    public void onRelativePositionChanged() {
        super.onRelativePositionChanged();
        updateShadowPosition();
    }

    private void updateShadowPosition() {
        EditorPanel editor = FactoryManagerGUI.get().getTopLevel().editorPanel;
        int x = this.getAbsoluteX() - editor.getAbsoluteX();
        int y = this.getAbsoluteY() - editor.getAbsoluteY();
        shadow.setLocation(x, y);
    }

    @Override
    public void onRemoved() {
        FactoryManagerGUI.get().getTopLevel().connectionsPanel.removeChildren(shadow);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        switch (button) {
            case GLFW_MOUSE_BUTTON_LEFT:
                getWindow().setFocusedWidget(this);
                return true;
            case GLFW_MOUSE_BUTTON_RIGHT:
                ConnectionsPanel.removeConnection(this);
                return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (previous != null) {
            IntermediateNode.dragOutIntermediateNode(previous, this, (int) mouseX, (int) mouseY);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (isFocused()) {
            FactoryManagerGUI.get().getTopLevel().connectionsPanel.onTerminalNodeClick(Either.right(this), button);
            return true;
        }
        return false;
    }

    @Override
    public void connectTo(INode next) {
    }

    @Override
    public void connectFrom(INode previous) {
        if (previous instanceof StartNode) {
            this.start = (StartNode) previous;
        }
        this.previous = previous;
    }

    @Override
    public void disconnectNext() {
    }

    @Override
    public void disconnectPrevious() {
        if (previous == start) {
            this.start = null;
        }
        this.previous = null;
    }

    @Nullable
    @Override
    public INode getPrevious() {
        return previous;
    }

    @Nullable
    @Override
    public INode getNext() {
        return null;
    }

    @Override
    public TextureWrapper getTextureNormal() {
        return INPUT_NORMAL;
    }

    @Override
    public TextureWrapper getTextureHovered() {
        return INPUT_HOVERED;
    }

    public boolean isConnected() {
        return start != null;
    }

    @Override
    public Type getType() {
        return Type.END;
    }

    public StartNode getStart() {
        return start;
    }

    @Nonnull
    @Override
    public ConnectionNodes<?> getParentWidget() {
        return (ConnectionNodes<?>) Objects.requireNonNull(super.getParentWidget());
    }

    public FlowComponent<?> getFlowComponent() {
        return (FlowComponent<?>) getParentWidget().getParentWidget();
    }

    public IProcedure getProcedure() {
        return getFlowComponent().getProcedure();
    }
}
