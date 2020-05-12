package vswe.stevesfactory.ui.manager.editor;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import lombok.val;
import vswe.stevesfactory.api.logic.Connection;
import vswe.stevesfactory.api.logic.IProcedure;
import vswe.stevesfactory.library.gui.widget.AbstractContainer;
import vswe.stevesfactory.library.gui.widget.IWidget;
import vswe.stevesfactory.ui.manager.FactoryManagerGUI;

import java.awt.*;
import java.util.Map;
import java.util.function.Function;

public abstract class ConnectionNodes<N extends INode> extends AbstractContainer<N> implements IWidget {

    public static ConnectionNodes<EndNode> inputNodes(int amount) {
        return new ConnectionNodes<EndNode>(amount, EndNode::new) {
            @Override
            public void readConnections(Map<IProcedure, FlowComponent<?>> m, IProcedure procedure) {
                // Only connect on output nodes
            }
        };
    }

    public static ConnectionNodes<StartNode> outputNodes(int amount) {
        return new ConnectionNodes<StartNode>(amount, StartNode::new) {
            @Override
            public void readConnections(Map<IProcedure, FlowComponent<?>> m, IProcedure procedure) {
                ImmutableList<StartNode> nodes = this.getChildren();
                Connection[] successors = procedure.successors();
                Preconditions.checkState(successors.length == nodes.size());

                for (int i = 0; i < successors.length; i++) {
                    Connection connection = successors[i];
                    if (connection == null) {
                        continue;
                    }
                    IProcedure successor = connection.getDestination();
                    FlowComponent<?> other = m.get(successor);
                    StartNode start = nodes.get(i);
                    EndNode end = other.getInputNodes().nodes.get(connection.getDestinationInputIndex());
                    String group = other.getGroup();

                    INode[] allNodes = new INode[1 + connection.getPolylineNodes().size() + 1];
                    allNodes[0] = start;
                    allNodes[allNodes.length - 1] = end;
                    FactoryManagerGUI.get().getPrimaryWindow().connectionsPanel.addChildren(group, start.shadow);
                    FactoryManagerGUI.get().getPrimaryWindow().connectionsPanel.addChildren(group, end.shadow);
                    // Generate the intermediate nodes
                    int j = 1;
                    for (Point pos : connection.getPolylineNodes()) {
                        allNodes[j] = new IntermediateNode();
                        allNodes[j].setLocation(pos);
                        // Other's group is the same as our group
                        FactoryManagerGUI.get().getPrimaryWindow().connectionsPanel.addChildren(group, allNodes[j]);
                        j++;
                    }
                    // Connect the intermediate nodes
                    for (int k = 0; k < allNodes.length - 1; k++) {
                        INode from = allNodes[k];
                        INode to = allNodes[k + 1];
                        ConnectionsPanel.connect(from, to);
                    }
                }
            }
        };
    }

    private final ImmutableList<N> nodes;

    public ConnectionNodes(int amountNodes, Function<Integer, N> factory) {
        this.setHeight(ConnectionsPanel.REGULAR_HEIGHT);
        ImmutableList.Builder<N> nodes = ImmutableList.builder();
        for (int i = 0; i < amountNodes; i++) {
            val node = factory.apply(i);
            nodes.add(node);
        }
        this.nodes = nodes.build();
    }

    @Override
    public void onInitialAttach() {
        super.onInitialAttach();

        for (val node : nodes) {
            node.attach(this);
        }
    }

    private static void removeNode(INode node) {
        ConnectionsPanel panel = FactoryManagerGUI.get().getPrimaryWindow().connectionsPanel;
        panel.removeChildren(node);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.renderChildren(mouseX, mouseY, partialTicks);
    }

    @Override
    public void reflow() {
        int segments = nodes.size() + 1;
        int emptyWidth = getWidth() - nodes.size() * ConnectionsPanel.REGULAR_WIDTH;
        int segmentsWidth = emptyWidth / segments;

        int x = segmentsWidth;
        for (N node : nodes) {
            node.setX(x);
            x += node.getWidth() + segmentsWidth;
        }
    }

    @Override
    public ImmutableList<N> getChildren() {
        return nodes;
    }

    public abstract void readConnections(Map<IProcedure, FlowComponent<?>> m, IProcedure procedure);

    public void removeAllConnections() {
        for (N node : nodes) {
            // TODO no hard coding
            if (node instanceof StartNode) {
                ConnectionsPanel.removeConnection((StartNode) node);
            } else {
                ConnectionsPanel.removeConnection((EndNode) node);
            }
        }
    }
}
