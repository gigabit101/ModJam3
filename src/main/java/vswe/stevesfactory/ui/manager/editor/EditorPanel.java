package vswe.stevesfactory.ui.manager.editor;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.glfw.GLFW;
import vswe.stevesfactory.api.logic.CommandGraph;
import vswe.stevesfactory.api.logic.IProcedure;
import vswe.stevesfactory.api.network.INetworkController;
import vswe.stevesfactory.library.gui.actionmenu.ActionMenu;
import vswe.stevesfactory.library.gui.actionmenu.CallbackEntry;
import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.screen.WidgetScreen;
import vswe.stevesfactory.library.gui.widget.mixin.RelocatableContainerMixin;
import vswe.stevesfactory.library.gui.window.Dialog;
import vswe.stevesfactory.ui.manager.FactoryManagerGUI;
import vswe.stevesfactory.ui.manager.UserPreferencesPanel;
import vswe.stevesfactory.ui.manager.editor.ControlFlow.Node;
import vswe.stevesfactory.ui.manager.editor.ControlFlow.OutputNode;
import vswe.stevesfactory.utils.NetworkHelper;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public final class EditorPanel extends DynamicWidthWidget<FlowComponent<?>> implements RelocatableContainerMixin<FlowComponent<?>> {

    /**
     * This is a tree set (ordered set) because handling z-index of the flow components need things to be sorted.
     */
    private TreeSet<FlowComponent<?>> children = new TreeSet<>();
    private Collection<FlowComponent<?>> childrenView = new DescendingTreeSetBackedUnmodifiableCollection<>(children);
    private int nextZIndex = 0;
    private int nextID = 0;

    // Node connection state
    private Node selectedNode;

    public EditorPanel() {
        super(WidthOccupierType.MAX_WIDTH);
        readProcedures();
    }

    public void readProcedures() {
        BlockPos controllerPos = ((FactoryManagerGUI) WidgetScreen.getCurrentScreen()).controllerPos;
        INetworkController controller = Objects.requireNonNull((INetworkController) Minecraft.getInstance().world.getTileEntity(controllerPos));

        Map<IProcedure, FlowComponent<?>> m = new HashMap<>();
        for (CommandGraph graph : controller.getCommandGraphs()) {
            for (IProcedure procedure : graph.collect()) {
                FlowComponent<?> f = procedure.createFlowComponent();
                m.put(procedure, f);
                addChildren(f);
            }
        }

        for (FlowComponent<?> child : children) {
            child.readConnections(m);
        }
    }

    public TreeSet<FlowComponent<?>> getFlowComponents() {
        return children;
    }

    @Override
    public Collection<FlowComponent<?>> getChildren() {
        return childrenView;
    }

    @Override
    public EditorPanel addChildren(FlowComponent<?> widget) {
        widget.setParentWidget(this);
        widget.setZIndex(nextZIndex());
        children.add(widget);
        return this;
    }

    @Override
    public EditorPanel addChildren(Collection<FlowComponent<?>> widgets) {
        for (FlowComponent<?> widget : widgets) {
            widget.setParentWidget(this);
            widget.setZIndex(nextZIndex());
        }
        children.addAll(widgets);
        return this;
    }

    @Override
    public void render(int mouseX, int mouseY, float particleTicks) {
        RenderEventDispatcher.onPreRender(this, mouseX, mouseY);
        if (selectedNode != null) {
            Node.drawConnectionLine(selectedNode, mouseX, mouseY);
        }

        // If this is put into the rendering logic of the nodes, the connection line will be above of of the flow components if
        // they are in a certain order.
        // The ideal way to solve this is to use depth testing, however it is such a huge amount of work to change all the GUI code written,
        // TODO use depth test instead of painter's algorithm
        for (FlowComponent<?> child : children) {
            for (Node node : child.getOutputNodes().getChildren()) {
                ((OutputNode) node).renderConnectionLine();
            }
        }

        // Iterate in ascending order for rendering as a special case
        for (FlowComponent<?> child : children) {
            child.render(mouseX, mouseY, particleTicks);
        }
        RenderEventDispatcher.onPostRender(this, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Cancel node selection
        if (selectedNode != null && button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            selectedNode = null;
            return true;
        }

        // All other events will be iterated in descending order
        for (FlowComponent<?> child : getChildren()) {
            // We know all child widgets are FlowComponent<?>'s, which are containers, therefore we can safely ignore whether the mouse is in box or not
            if (child.mouseClicked(mouseX, mouseY, button)) {
                raiseComponentToTop(child);
                return true;
            }
        }
        if (isInside(mouseX, mouseY)) {
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                getWindow().setFocusedWidget(this);
            }
            if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                openActionMenu(mouseX, mouseY);
            }
            return true;
        }
        return false;
    }

    private void openActionMenu(double mouseX, double mouseY) {
        ActionMenu actionMenu = ActionMenu.atCursor(mouseX, mouseY, ImmutableList.of(
                new CallbackEntry(FactoryManagerGUI.PASTE_ICON, "gui.sfm.ActionMenu.Paste", b -> actionPaste()),
                // TODO implement
                new CallbackEntry(null, "gui.sfm.ActionMenu.CleanupProcedures", b -> {}),
                new CallbackEntry(null, "gui.sfm.ActionMenu.ToggleFullscreen", b -> actionToggleFullscreen()),
                new UserPreferencesPanel.OpenerEntry()
        ));
        WidgetScreen.getCurrentScreen().addPopupWindow(actionMenu);
    }

    private void actionPaste() {
        String json = minecraft().keyboardListener.getClipboardString();
        CompoundNBT tag;
        try {
            tag = JsonToNBT.getTagFromJson(json);
        } catch (CommandSyntaxException e) {
            Dialog.createDialog("gui.sfm.ActionMenu.Paste.Procedure.Fail").tryAddSelfToActiveGUI();
            return;
        }

        INetworkController controller = FactoryManagerGUI.getActiveGUI().getController();
        IProcedure procedure = NetworkHelper.recreateProcedureAndAdd(controller, tag);

        addChildren(procedure.createFlowComponent());
    }

    private void actionToggleFullscreen() {
        FactoryManagerGUI.getActiveGUI().getPrimaryWindow().toggleFullscreen();
    }

    @Override
    public void reflow() {
    }

    public void removeFlowComponent(FlowComponent<?> flowComponent) {
        children.remove(flowComponent);
    }

    private void raiseComponentToTop(FlowComponent<?> target) {
        // Move the flow component to top by setting its z-index to the maximum z-index ever given out
        target.setZIndex(nextZIndex());

        updateChild(target);
    }

    private int nextZIndex() {
        return nextZIndex++;
    }

    private int getLastDistributedZIndex() {
        return nextZIndex - 1;
    }

    private void updateChild(FlowComponent<?> child) {
        children.remove(child);
        children.add(child);
    }

    int nextID() {
        return nextID++;
    }

    public void startConnection(Node source) {
        selectedNode = source;
    }

    public boolean tryFinishConnection(Node target) {
        if (selectedNode != null && selectedNode.shouldConnect(target) && target.shouldConnect(selectedNode)) {
            target.connect(selectedNode);
            selectedNode = null;
            return true;
        }
        return false;
    }

    public void saveAll() {
        for (FlowComponent<?> flowComponent : getChildren()) {
            flowComponent.save();
        }
    }

    @MethodsReturnNonnullByDefault
    @ParametersAreNonnullByDefault
    private static class DescendingTreeSetBackedUnmodifiableCollection<E> extends AbstractCollection<E> {

        private final TreeSet<E> s;

        public DescendingTreeSetBackedUnmodifiableCollection(TreeSet<E> s) {
            this.s = s;
        }

        public int size() {
            return this.s.size();
        }

        public boolean isEmpty() {
            return this.s.isEmpty();
        }

        public boolean contains(Object var1) {
            return this.s.contains(var1);
        }

        public Object[] toArray() {
            return this.s.toArray();
        }

        public <T> T[] toArray(T[] a) {
            return this.s.toArray(a);
        }

        public String toString() {
            return this.s.toString();
        }

        @Override
        public Iterator<E> iterator() {
            return s.descendingIterator();
        }

        public boolean add(E e) {
            throw new UnsupportedOperationException();
        }

        public boolean remove(Object o) {
            throw new UnsupportedOperationException();
        }

        public boolean containsAll(Collection<?> c) {
            return this.s.containsAll(c);
        }

        public boolean addAll(Collection<? extends E> c) {
            throw new UnsupportedOperationException();
        }

        public boolean removeAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        public boolean retainAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        public void clear() {
            throw new UnsupportedOperationException();
        }

        public void forEach(Consumer<? super E> c) {
            this.s.forEach(c);
        }

        public boolean removeIf(Predicate<? super E> p) {
            throw new UnsupportedOperationException();
        }

        public Spliterator<E> spliterator() {
            return this.s.spliterator();
        }

        public Stream<E> stream() {
            return this.s.stream();
        }

        public Stream<E> parallelStream() {
            return this.s.parallelStream();
        }
    }
}
