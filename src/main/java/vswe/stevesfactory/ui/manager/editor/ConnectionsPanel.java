package vswe.stevesfactory.ui.manager.editor;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Either;
import vswe.stevesfactory.api.logic.Connection;
import vswe.stevesfactory.library.gui.Render2D;
import vswe.stevesfactory.library.gui.ScissorTest;
import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.layout.properties.BoxSizing;
import vswe.stevesfactory.ui.manager.DynamicWidthWidget;
import vswe.stevesfactory.ui.manager.FactoryManagerGUI;

import java.util.*;
import java.util.function.Function;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT;
import static org.lwjgl.opengl.GL11.*;

public final class ConnectionsPanel extends DynamicWidthWidget<INode> {

    public static final int REGULAR_WIDTH = 7;
    public static final int REGULAR_HEIGHT = 6;

    // These methods break graph validness! Use with care

    public static void connect(INode from, INode to) {
        from.connectTo(to);
        to.connectFrom(from);
    }

    public static void connectAndOverride(StartNode start, EndNode end) {
        if (start.isConnected()) {
            removeConnection(start);
        }
        if (end.isConnected()) {
            removeConnection(end);
        }
        ConnectionsPanel.connect(start, end);
    }

    public static void disconnect(INode from, INode to) {
        from.disconnectNext();
        to.disconnectPrevious();
    }

    // Safe connection handling methods

    public static void mergeConnection(INode from, INode to, INode middle) {
        ConnectionsPanel.disconnect(from, middle);
        ConnectionsPanel.disconnect(middle, to);
        ConnectionsPanel.connect(from, to);
    }

    public static IntermediateNode subdivideConnection(INode from, INode to) {
        ConnectionsPanel.disconnect(from, to);
        IntermediateNode middle = new IntermediateNode();
        ConnectionsPanel.connect(from, middle);
        ConnectionsPanel.connect(middle, to);
        return middle;
    }

    public static void removeConnection(StartNode start) {
        start.onEdgeRemoval();
        INode nextTarget = start;
        while (true) {
            INode current = nextTarget;
            INode next = current.getNext();
            if (next == null) {
                break;
            }
            ConnectionsPanel.disconnect(current, next);
            nextTarget = next;
            next.onEdgeRemoval();
        }
    }

    public static void removeConnection(EndNode end) {
        end.onEdgeRemoval();
        INode nextTarget = end;
        while (true) {
            INode current = nextTarget;
            INode previous = current.getPrevious();
            if (previous == null) {
                break;
            }
            ConnectionsPanel.disconnect(previous, current);
            nextTarget = previous;
            previous.onEdgeRemoval();
        }
    }

    public static void drawConnectionLine(INode first, INode second) {
        drawConnectionLine(
                Render2D.computeCenterX(first), Render2D.computeCenterY(first),
                Render2D.computeCenterX(second), Render2D.computeCenterY(second)
        );
    }

    public static void drawConnectionLine(int x1, int y1, int x2, int y2) {
        RenderSystem.disableTexture();
        glEnable(GL_LINE_SMOOTH);
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
        glLineWidth(4);
        glColor3f(94F / 255F, 94F / 255F, 94F / 255F);
        glBegin(GL_LINES);
        {
            glVertex3f(x1, y1, 0F);
            glVertex3f(x2, y2, 0F);
        }
        glEnd();
        glColor3f(1F, 1F, 1F);
        RenderSystem.enableTexture();
    }

    private Map<String, Set<INode>> groupMappedChildren = new HashMap<>();
    private Set<INode> children;
    public boolean disabledModification = false;

    private Either<StartNode, EndNode> selectedNode = null;

    public ConnectionsPanel() {
        super(WidthOccupierType.MAX_WIDTH);
        // Default group child widget collection
        children = new HashSet<>();
        groupMappedChildren.put("", children);
        // No need to remove because this panel has the same lifetime as the GUI
        FactoryManagerGUI.get().groupModel.addListenerSelect(current -> children = groupMappedChildren.computeIfAbsent(current, __ -> new HashSet<>()));
    }

    @Override
    public Set<INode> getChildren() {
        return children;
    }

    // This is meant to be directly in back of the EditorPanel
    @Override
    public BoxSizing getBoxSizing() {
        return BoxSizing.PHANTOM;
    }

    @Override
    public void reflow() {
    }

    @Override
    protected void reflowAfter() {
        FactoryManagerGUI.get().defer(() -> {
            EditorPanel editor = FactoryManagerGUI.get().getPrimaryWindow().editorPanel;
            setLocation(editor.getX(), editor.getY());
            setDimensions(editor.getWidth(), editor.getHeight());
        });
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        RenderEventDispatcher.onPreRender(this, mouseX, mouseY);
        EditorPanel editor = FactoryManagerGUI.get().getPrimaryWindow().editorPanel;

        ScissorTest test = ScissorTest.scaled(getAbsoluteX(), getAbsoluteY(), getWidth(), getHeight());
        RenderSystem.pushMatrix();
        RenderSystem.translatef(editor.getXOffset(), editor.getYOffset(), 0F);
        {
            // Separate widget rendering and connection rendering to put the connections behind the widgets
            for (INode child : children) {
                // Start of a chain
                if (child.getPrevious() == null) {
                    INode current = child;
                    INode next = child.getNext();
                    while (next != null) {
                        ConnectionsPanel.drawConnectionLine(current, next);
                        current = next;
                        next = next.getNext();
                    }
                }
            }
            for (INode child : children) {
                child.render(mouseX - editor.getXOffset(), mouseY - editor.getYOffset(), partialTicks);
            }
        }
        RenderSystem.popMatrix();
        test.destroy();

        if (selectedNode != null) {
            INode node = selectedNode.map(Function.identity(), Function.identity());
            // Manual translation due to the other end point needs to be at the mouse position
            ConnectionsPanel.drawConnectionLine(
                    Render2D.computeCenterX(node) + editor.getXOffset(), Render2D.computeCenterY(node) + editor.getYOffset(),
                    mouseX, mouseY);
        }

        RenderEventDispatcher.onPostRender(this, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        EditorPanel editor = FactoryManagerGUI.get().getPrimaryWindow().editorPanel;
        if (super.mouseClicked(mouseX - editor.getXOffset(), mouseY - editor.getYOffset(), button)) {
            return true;
        }

        if (button == GLFW_MOUSE_BUTTON_RIGHT && selectedNode != null) {
            clearSelection();
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        EditorPanel editor = FactoryManagerGUI.get().getPrimaryWindow().editorPanel;
        return super.mouseReleased(mouseX - editor.getXOffset(), mouseY - editor.getYOffset(), button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        EditorPanel editor = FactoryManagerGUI.get().getPrimaryWindow().editorPanel;
        return super.mouseDragged(mouseX - editor.getXOffset(), mouseY - editor.getYOffset(), button, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scroll) {
        EditorPanel editor = FactoryManagerGUI.get().getPrimaryWindow().editorPanel;
        return super.mouseScrolled(mouseX - editor.getXOffset(), mouseY - editor.getYOffset(), scroll);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        EditorPanel editor = FactoryManagerGUI.get().getPrimaryWindow().editorPanel;
        super.mouseMoved(mouseX - editor.getXOffset(), mouseY - editor.getYOffset());
    }

    // Centralized logic for terminal (start and end) nodes.
    // Intermediate node logic handled within the class itself

    public void onTerminalNodeClick(Either<StartNode, EndNode> node, int button) {
        switch (button) {
            case GLFW_MOUSE_BUTTON_LEFT: {
                handleTerminalNodeConnecting(node);
                break;
            }
            case GLFW_MOUSE_BUTTON_RIGHT: {
                handleTerminalNodeDisconnecting(node);
                break;
            }
        }
    }

    private void handleTerminalNodeConnecting(Either<StartNode, EndNode> node) {
        // Select the node if nothing is selected
        if (selectedNode == null) {
            this.selectedNode = node;
            return;
        }

        // Other wise try to connect both nodes
        StartNode start = node.map(Function.identity(), __ -> selectedNode.left().orElse(null));
        EndNode end = node.map(__ -> selectedNode.right().orElse(null), Function.identity());
        if (start == null || end == null) {
            // Invalid node pair, do nothing
            return;
        }

        // Create connection in GUI
        ConnectionsPanel.connectAndOverride(start, end);
        // Create connection in actual flowchart
        Connection.createAndOverride(start.getProcedure(), start.index, end.getProcedure(), end.index);

        clearSelection();
    }

    private void handleTerminalNodeDisconnecting(Either<StartNode, EndNode> node) {
        StartNode start = node.map(Function.identity(), EndNode::getStart);
        EndNode end = node.map(StartNode::getEnd, Function.identity());
        if (start == null || end == null) {
            // Incomplete connection, do nothing
            return;
        }
        ConnectionsPanel.removeConnection(start);
    }

    public void clearSelection() {
        selectedNode = null;
    }

    @Override
    public ConnectionsPanel addChildren(INode node) {
        if (!disabledModification) {
            children.add(node);
            node.attach(this);
        }
        return this;
    }

    @Override
    public ConnectionsPanel addChildren(Collection<INode> nodes) {
        if (!disabledModification) {
            for (INode node : nodes) {
                addChildren(node);
            }
        }
        return this;
    }

    /**
     * Special add child widget function for retrieving from serialized data. Avoid using during GUI lifetime.
     */
    public void addChildren(String group, INode node) {
        if (!disabledModification) {
            groupMappedChildren.computeIfAbsent(group, __ -> new HashSet<>()).add(node);
            node.attach(this);
        }
    }

    public void removeChildren(INode node) {
        if (!disabledModification) {
            for (Set<INode> children : groupMappedChildren.values()) {
                if (children.remove(node)) {
                    node.onRemoved();
                }
            }
        }
    }

    public void moveGroup(String from, String to) {
        Set<INode> original = groupMappedChildren.computeIfAbsent(from, __ -> new HashSet<>());
        groupMappedChildren.computeIfAbsent(to, __ -> new HashSet<>()).addAll(original);
        original.clear();
    }

    @Override
    public void notifyChildrenForPositionChange() {
        for (Set<INode> children : groupMappedChildren.values()) {
            for (INode child : children) {
                child.onParentPositionChanged();
            }
        }
    }
}
