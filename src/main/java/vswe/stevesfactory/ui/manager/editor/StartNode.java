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

public final class StartNode extends AbstractIconButton implements INode {

    public static final TextureWrapper OUTPUT_NORMAL = TextureWrapper.ofFlowComponent(18, 45, REGULAR_WIDTH, REGULAR_HEIGHT);
    public static final TextureWrapper OUTPUT_HOVERED = OUTPUT_NORMAL.toRight(1);

    private INode next;
    private EndNode end;

    private final int index;
    private final ShadowNode shadow;

    public StartNode(int index) {
        super(0, 0, REGULAR_WIDTH, REGULAR_HEIGHT);
        this.index = index;
        this.shadow = new ShadowNode(this);
        FactoryManagerGUI.getActiveGUI().getTopLevel().connectionsPanel.addChildren(shadow);
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
        EditorPanel editor = FactoryManagerGUI.getActiveGUI().getTopLevel().editorPanel;
        int x = this.getAbsoluteX() - editor.getAbsoluteX();
        int y = this.getAbsoluteY() - editor.getAbsoluteY();
        shadow.setLocation(x, y);
    }

    @Override
    public void onRemoved() {
        FactoryManagerGUI.getActiveGUI().getTopLevel().connectionsPanel.removeChildren(shadow);
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
        if (next != null) {
            IntermediateNode.dragOutIntermediateNode(this, next, (int) mouseX, (int) mouseY);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (isFocused()) {
            FactoryManagerGUI.getActiveGUI().getTopLevel().connectionsPanel.onTerminalNodeClick(Either.left(this), button);
            return true;
        }
        return false;
    }

    @Override
    public void connectTo(INode next) {
        this.next = next;
        if (next instanceof EndNode) {
            this.end = (EndNode) next;
        }
    }

    @Override
    public void connectFrom(INode previous) {
    }

    @Override
    public void disconnectNext() {
        if (next == end) {
            this.end = null;
        }
        this.next = null;
    }

    @Override
    public void disconnectPrevious() {
    }

    @Nullable
    @Override
    public INode getPrevious() {
        return null;
    }

    @Nullable
    @Override
    public INode getNext() {
        return next;
    }

    @Override
    public TextureWrapper getTextureNormal() {
        return OUTPUT_NORMAL;
    }

    @Override
    public TextureWrapper getTextureHovered() {
        return OUTPUT_HOVERED;
    }

    public boolean isConnected() {
        return end != null;
    }

    @Override
    public Type getType() {
        return Type.START;
    }

    public int getIndex() {
        return index;
    }

    public EndNode getEnd() {
        return end;
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
