package vswe.stevesfactory.ui.manager.editor;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.GlStateManager;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import vswe.stevesfactory.api.logic.Connection;
import vswe.stevesfactory.api.logic.IProcedure;
import vswe.stevesfactory.library.gui.TextureWrapper;
import vswe.stevesfactory.library.gui.widget.AbstractContainer;
import vswe.stevesfactory.library.gui.widget.AbstractIconButton;
import vswe.stevesfactory.library.gui.widget.mixin.LeafWidgetMixin;
import vswe.stevesfactory.library.gui.widget.mixin.ResizableWidgetMixin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

import static org.lwjgl.opengl.GL11.*;
import static vswe.stevesfactory.ui.manager.editor.ControlFlow.Node;

public abstract class ControlFlow extends AbstractContainer<Node> implements ResizableWidgetMixin {

    public static abstract class Node extends AbstractIconButton implements LeafWidgetMixin {

        public static void drawConnectionLine(Node first, Node second) {
            drawConnectionLine(first.getCenterX(), first.getCenterY(), second.getCenterX(), second.getCenterY());
        }

        public static void drawConnectionLine(Node source, int mx, int my) {
            drawConnectionLine(source.getCenterX(), source.getCenterY(), mx, my);
        }

        public static void drawConnectionLine(int x1, int y1, int x2, int y2) {
            GlStateManager.disableTexture();
            glEnable(GL11.GL_LINE_SMOOTH);
            glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
            glLineWidth(4);
            glColor3f(94F / 255F, 94F / 255F, 94F / 255F);
            glBegin(GL11.GL_LINES);
            {
                glVertex3f(x1, y1, 0F);
                glVertex3f(x2, y2, 0F);
            }
            glEnd();
            glColor3f(1F, 1F, 1F);
            GlStateManager.enableTexture();
        }

        public static final int WIDTH = 7;
        public static final int HEIGHT = 6;

        private Node pairedNode;
        private int index;

        public Node(ControlFlow parent, int index) {
            super(0, 0, WIDTH, HEIGHT);
            setParentWidget(parent);
            this.index = index;
        }

        /**
         * Connect to the parameter node.
         *
         * @implSpec Implementations should invoke the parameter node's {@link #onConnect(Node)} method with itself as the parameter.
         */
        public void connect(Node other) {
            // Ensure one node only ever connects to at most one other node
            // If we don't disconnect, the old connected node and the new node will both link to this node
            if (pairedNode != null) {
                pairedNode.disconnect();
            }
            pairedNode = other;
            other.onConnect(this);
        }

        /**
         * Called when {@link #connect(Node)} gets invoked on the paired node.
         */
        protected void onConnect(Node source) {
            if (pairedNode != null) {
                pairedNode.disconnect();
            }
            pairedNode = source;
        }

        public boolean shouldConnect(Node other) {
            return other.getFlowComponent() != this.getFlowComponent();
        }

        /**
         * Disconnect this node and the paired node.
         *
         * @implSpec Implementations should invoke the paired node's {@link #onDisconnect()} method.
         */
        public void disconnect() {
            if (pairedNode == null) {
                return;
            }
            pairedNode.onDisconnect();
            pairedNode = null;
        }

        /**
         * Called when {@link #disconnect()} gets invoked on the paired node.
         */
        protected void onDisconnect() {
            pairedNode = null;
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            switch (button) {
                case GLFW.GLFW_MOUSE_BUTTON_LEFT:
                    EditorPanel editor = getFlowComponent().getParentWidget();
                    // If failed to finish connection (a connection has not been started yet)
                    if (!editor.tryFinishConnection(this)) {
                        editor.startConnection(this);
                    }
                    break;
                case GLFW.GLFW_MOUSE_BUTTON_RIGHT:
                    disconnect();
                    break;
            }
            return true;
        }

        @Nullable
        public Node getPairedNode() {
            return pairedNode;
        }

        public int getCenterX() {
            return getAbsoluteX() + getTextureNormal().getPortionWidth() / 2;
        }

        public int getCenterY() {
            return getAbsoluteY() + getTextureNormal().getPortionHeight() / 2;
        }

        public int getIndex() {
            return index;
        }

        @Nonnull
        @Override
        public ControlFlow getParentWidget() {
            return (ControlFlow) Objects.requireNonNull(super.getParentWidget());
        }

        public FlowComponent<?> getFlowComponent() {
            return (FlowComponent<?>) getParentWidget().getParentWidget();
        }

        public IProcedure getLinkedProcedure() {
            return getFlowComponent().getLinkedProcedure();
        }

        @Override
        public void render(int mouseX, int mouseY, float particleTicks) {
            GlStateManager.color3f(1F, 1F, 1F);
            super.render(mouseX, mouseY, particleTicks);
        }
    }

    static final class InputNode extends Node {

        private static final TextureWrapper INPUT_NORMAL = TextureWrapper.ofFlowComponent(18, 51, WIDTH, HEIGHT);
        private static final TextureWrapper INPUT_HOVERED = INPUT_NORMAL.toRight(1);

        public InputNode(ControlFlow parent, int index) {
            super(parent, index);
        }

        @Override
        public void connect(Node other) {
            if (other instanceof InputNode) {
                throw new IllegalArgumentException();
            }
            super.connect(other);
        }

        @Override
        protected void onConnect(Node source) {
            if (source instanceof InputNode) {
                throw new IllegalArgumentException();
            }
            super.onConnect(source);
        }

        @Override
        public boolean shouldConnect(Node other) {
            return other instanceof OutputNode && super.shouldConnect(other);
        }

        @Override
        public TextureWrapper getTextureNormal() {
            return INPUT_NORMAL;
        }

        @Override
        public TextureWrapper getTextureHovered() {
            return INPUT_HOVERED;
        }

        @Nullable
        @Override
        public OutputNode getPairedNode() {
            return (OutputNode) super.getPairedNode();
        }
    }

    static final class OutputNode extends Node {

        private static final TextureWrapper OUTPUT_NORMAL = TextureWrapper.ofFlowComponent(18, 45, WIDTH, HEIGHT);
        private static final TextureWrapper OUTPUT_HOVERED = OUTPUT_NORMAL.toRight(1);

        public OutputNode(ControlFlow parent, int index) {
            super(parent, index);
        }

        @Override
        public void connect(Node other) {
            if (other instanceof OutputNode) {
                throw new IllegalArgumentException();
            }
            super.connect(other);
            linkToOther(other);
        }

        @Override
        protected void onConnect(Node source) {
            if (source instanceof OutputNode) {
                throw new IllegalArgumentException();
            }
            super.onConnect(source);
            linkToOther(source);
        }

        private void linkToOther(Node other) {
            if (!getParentWidget().bootstrapping) {
                IProcedure target = other.getLinkedProcedure();
                Connection.createAndOverride(getLinkedProcedure(), getIndex(), target, other.getIndex());
            }
        }

        @Override
        protected void onDisconnect() {
            super.onDisconnect();
        }

        @Override
        public boolean shouldConnect(Node other) {
            return other instanceof InputNode && super.shouldConnect(other);
        }

        @Override
        public TextureWrapper getTextureNormal() {
            return OUTPUT_NORMAL;
        }

        @Override
        public TextureWrapper getTextureHovered() {
            return OUTPUT_HOVERED;
        }

        @Override
        public void render(int mouseX, int mouseY, float particleTicks) {
            // Connection render handled in EditorPanel
            super.render(mouseX, mouseY, particleTicks);
        }

        public void renderConnectionLine() {
            InputNode other = getPairedNode();
            if (other != null) {
                drawConnectionLine(this, other);
            }
        }

        @Nullable
        @Override
        public InputNode getPairedNode() {
            return (InputNode) super.getPairedNode();
        }
    }

    public static ControlFlow inputNodes(int amount) {
        return new ControlFlow(amount, InputNode::new) {
            @Override
            void readConnections(Map<IProcedure, FlowComponent<?>> m, IProcedure procedure) {
                // Only connect on output nodes
            }
        };
    }

    public static ControlFlow outputNodes(int amount) {
        return new ControlFlow(amount, OutputNode::new) {
            @Override
            void readConnections(Map<IProcedure, FlowComponent<?>> m, IProcedure procedure) {
                bootstrapping = true;
                ImmutableList<Node> nodes = getChildren();
                Connection[] successors = procedure.successors();
                Preconditions.checkState(successors.length == nodes.size());

                for (int i = 0; i < successors.length; i++) {
                    Connection connection = successors[i];
                    if (connection == null) {
                        continue;
                    }
                    IProcedure successor = connection.getDestination();
                    FlowComponent<?> other = m.get(successor);
                    Node from = nodes.get(i);
                    Node to = other.getInputNodes().nodes.get(connection.getDestinationInputIndex());

                    from.connect(to);
                }
                bootstrapping = false;
            }
        };
    }

    private final ImmutableList<Node> nodes;
    boolean bootstrapping = false;

    public ControlFlow(int amountNodes, BiFunction<ControlFlow, Integer, ? extends Node> factory) {
        super(0, 0, 0, Node.HEIGHT);
        ImmutableList.Builder<Node> builder = ImmutableList.builder();
        for (int i = 0; i < amountNodes; i++) {
            builder.add(factory.apply(this, i));
        }
        this.nodes = builder.build();
    }

    @Override
    public void reflow() {
        int sections = nodes.size() + 1;
        int emptyWidth = getWidth() - nodes.size() * Node.WIDTH;
        int sectionWidth = emptyWidth / sections;

        int x = sectionWidth;
        for (Node node : nodes) {
            node.setX(x);
            x += Node.WIDTH + sectionWidth;
        }
    }

    @Override
    public ImmutableList<Node> getChildren() {
        return nodes;
    }

    abstract void readConnections(Map<IProcedure, FlowComponent<?>> m, IProcedure procedure);

    public void removeConnection(int i) {
        nodes.get(i).disconnect();
    }

    public boolean removeConnection(Node other) {
        for (Node node : nodes) {
            if (node.getPairedNode() == other) {
                node.disconnect();
                return true;
            }
        }
        return false;
    }

    public boolean removeConnection(FlowComponent component) {
        for (Node node : nodes) {
            if (node.getParentWidget().getParentWidget() == component) {
                node.disconnect();
                return true;
            }
        }
        return false;
    }

    public void removeAllConnections() {
        for (Node node : nodes) {
            node.disconnect();
        }
    }

    @Override
    public void onParentPositionChanged() {
        super.onParentPositionChanged();
        // AbstractWidget overrides the method, therefore we need to trigger this manually here9
        notifyChildrenForPositionChange();
    }
}
