package vswe.stevesfactory.ui.manager.editor;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.CompoundNBT;
import vswe.stevesfactory.api.logic.Connection;
import vswe.stevesfactory.api.logic.IClientDataStorage;
import vswe.stevesfactory.api.logic.IProcedure;
import vswe.stevesfactory.library.gui.Render2D;
import vswe.stevesfactory.library.gui.contextmenu.CallbackEntry;
import vswe.stevesfactory.library.gui.contextmenu.ContextMenuBuilder;
import vswe.stevesfactory.library.gui.contextmenu.Section;
import vswe.stevesfactory.library.gui.debug.ITextReceiver;
import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.screen.WidgetScreen;
import vswe.stevesfactory.library.gui.widget.AbstractContainer;
import vswe.stevesfactory.library.gui.widget.IWidget;
import vswe.stevesfactory.library.gui.widget.MinimumVerticalList;
import vswe.stevesfactory.library.gui.widget.TextField;
import vswe.stevesfactory.library.gui.widget.panel.VerticalList;
import vswe.stevesfactory.library.gui.window.Dialog;
import vswe.stevesfactory.ui.manager.tool.group.Grouplist;
import vswe.stevesfactory.ui.manager.tool.inspector.Inspector;
import vswe.stevesfactory.utils.NetworkHelper;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.List;
import java.util.Queue;
import java.util.*;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;

public class FlowComponent<P extends IProcedure & IClientDataStorage> extends AbstractContainer<IWidget> implements Comparable<FlowComponent<?>> {

    private P procedure;

    private final TextField nameBox;
    private final ConnectionNodes<EndNode> inputNodes;
    private final ConnectionNodes<StartNode> outputNodes;
    private final ErrorIndicator errorIndicator;
    private final MinimumVerticalList<Menu<P>> menus;
    // A list that refers to all the widgets above
    private final List<IWidget> children;

    private int zIndex;

    // Temporary data
    private int initialDragLocalX;
    private int initialDragLocalY;

    public FlowComponent(P procedure) {
        this.setDimensions(64, 20);
//        // Record reference only, initialization is done in #onInitialAttach()
//        this.procedure = procedure;
        // The cursor looks a bit to short (and cute) with these numbers, might want change them?
        this.nameBox = new TextField();
        this.nameBox.setLocation(6, 8);
        this.nameBox.setWidth(this.getWidth() - nameBox.getX() - 2);
        this.nameBox.setBackgroundStyle(TextField.BackgroundStyle.NONE);
        this.nameBox.setText(procedure.getName());
        this.nameBox.setTextColor(0xff303030, 0xff303030);
        this.nameBox.setEditable(false);
        this.nameBox.getTextRenderer().setFontHeight(6);
        this.inputNodes = ConnectionNodes.inputNodes(procedure.predecessors().length);
        this.outputNodes = ConnectionNodes.outputNodes(procedure.successors().length);
        this.errorIndicator = ErrorIndicator.error();
        this.menus = new MinimumVerticalList<>();
        this.children = ImmutableList.of(nameBox, inputNodes, outputNodes, errorIndicator);
        // Initialize data related to the procedure object
        this.setLinkedProcedure(procedure);

        errorIndicator.setLocation(2, 8);
    }

    @Override
    public void onInitialAttach() {
        for (IWidget child : children) {
            child.attach(this);
        }
    }

    public String getName() {
        return nameBox.getText();
    }

    public void setName(String name) {
        this.nameBox.setText(name);
        this.procedure.setName(name);
    }

    @Override
    public List<IWidget> getChildren() {
        return children;
    }

    public VerticalList<Menu<P>> getMenusBox() {
        return menus;
    }

    public void collapseAllMenus() {
        for (Menu<P> menu : menus.getChildren()) {
            menu.collapse();
        }
        menus.setScrollDistance(0F);
    }

    public void expandAllMenus() {
        for (Menu<P> menu : menus.getChildren()) {
            menu.expand();
        }
    }

    @Override
    public void reflow() {
        nameBox.scrollToFront();
        inputNodes.setWidth(getWidth());
        inputNodes.setY(-ConnectionsPanel.REGULAR_HEIGHT);
        inputNodes.reflow();
        outputNodes.setWidth(getWidth());
        outputNodes.setY(getHeight());
        outputNodes.reflow();
        menus.reflow();
    }

    public FlowComponent<P> addMenu(Menu<P> menu) {
        menus.addChildren(menu);
        menu.onLinkFlowComponent(this);
        return this;
    }

    @Override
    public FlowComponent<P> addChildren(IWidget widget) {
        if (widget instanceof Menu) {
            @SuppressWarnings("unchecked") Menu<P> menu = (Menu<P>) widget;
            return addMenu(menu);
        } else {
            throw new IllegalArgumentException("Flow components do not accept new child widgets with type other than Menu");
        }
    }

    @Override
    public FlowComponent<P> addChildren(Collection<IWidget> widgets) {
        for (IWidget widget : widgets) {
            addChildren(widget);
        }
        return this;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        RenderEventDispatcher.onPreRender(this, mouseX, mouseY);

        RenderSystem.disableTexture();
        RenderSystem.color3f(1F, 1F, 1F);
        int x = getAbsoluteX();
        int y = getAbsoluteY();
        Render2D.beginColoredQuad();
        Render2D.borderedRect(x, y, x + getWidth(), y + getHeight());
        Render2D.draw();
        RenderSystem.enableTexture();

        nameBox.render(mouseX, mouseY, partialTicks);
        inputNodes.render(mouseX, mouseY, partialTicks);
        outputNodes.render(mouseX, mouseY, partialTicks);
        errorIndicator.render(mouseX, mouseY, partialTicks);

        if (nameBox.isInside(mouseX, mouseY)) {
            WidgetScreen.assertActive().scheduleTooltip(getName(), mouseX, mouseY);
        }

        RenderEventDispatcher.onPostRender(this, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            clearDrag();
            return true;
        }
        if (!isInside(mouseX, mouseY)) {
            clearDrag();
            return false;
        }

        getWindow().setFocusedWidget(this);
        Inspector inspector = Inspector.getActiveInspector();
        if (inspector != null) {
            inspector.openFlowComponent(this);
        }
        if (button == GLFW_MOUSE_BUTTON_LEFT) {
            initialDragLocalX = (int) mouseX - getAbsoluteX();
            initialDragLocalY = (int) mouseY - getAbsoluteY();
        } else {
            clearDrag();
        }
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (!isEnabled()) {
            return false;
        }
        if (super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
            return true;
        }
        if (isDragging()) {
            EditorPanel parent = getParent();
            int x = (int) mouseX - parent.getAbsoluteX() - initialDragLocalX;
            int y = (int) mouseY - parent.getAbsoluteY() - initialDragLocalY;
            setLocation(x, y);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (!isEnabled()) {
            return false;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scroll) {
        if (!isEnabled()) {
            return false;
        }
        return super.mouseScrolled(mouseX, mouseY, scroll);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!isEnabled()) {
            return false;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (!isEnabled()) {
            return false;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char charTyped, int keyCode) {
        if (!isEnabled()) {
            return false;
        }
        return super.charTyped(charTyped, keyCode);
    }

    @Override
    public void update(float partialTicks) {
        super.update(partialTicks);
        // TODO don't do it the brutal force way: use data modification events
        if (Minecraft.getInstance().world.getGameTime() % 10 == 0) {
            repopulateErrors();
        }
    }

    private void clearDrag() {
        initialDragLocalX = -1;
        initialDragLocalY = -1;
    }

    private boolean isDragging() {
        return initialDragLocalX != -1 && initialDragLocalY != -1;
    }

    public void attach(EditorPanel parent) {
        this.attach((IWidget) parent);
        reflow();
    }

    @Override
    protected void buildContextMenu(ContextMenuBuilder builder) {
        Section section = builder.obtainSection("FlowComponent");
        section.addChildren(new CallbackEntry(Render2D.DELETE, "gui.sfm.FactoryManager.Editor.Delete", b -> actionDelete()));
        section.addChildren(new CallbackEntry(Render2D.CUT, "gui.sfm.FactoryManager.Editor.Cut", b -> actionCut()));
        section.addChildren(new CallbackEntry(Render2D.COPY, "gui.sfm.FactoryManager.Editor.Copy", b -> actionCopy()));
        section.addChildren(new CallbackEntry(null, "gui.sfm.FactoryManager.Editor.ChangeGroup", b -> actionChangeGroup()));
        super.buildContextMenu(builder);
    }

    private void actionDelete() {
        if (Screen.hasShiftDown()) {
            Dialog.createBiSelectionDialog(
                    I18n.format("gui.sfm.FactoryManager.Editor.DeleteAll.ConfirmMsg"),
                    I18n.format("gui.sfm.yes"),
                    I18n.format("gui.sfm.no"),
                    b -> removeGraph(this), b -> {}
            ).tryAddSelfToActiveGUI();
        } else {
            remove();
        }
    }

    private void actionCopy() {
        save();
        CompoundNBT tag = procedure.serialize();
        Minecraft.getInstance().keyboardListener.setClipboardString(tag.toString());
    }

    private void actionCut() {
        actionCopy();
        remove();
    }

    private void actionChangeGroup() {
        Grouplist.createSelectGroupDialog(
                newGroup -> {
                    disconnect();
                    setGroup(newGroup);
                },
                () -> {
                }).tryAddSelfToActiveGUI();
    }

    public void save() {
        for (Menu<?> menu : menus.getChildren()) {
            menu.saveData();
        }
        P proc = getProcedure();
        for (int i = 0; i < proc.successors().length; i++) {
            Connection conn = proc.successors()[i];
            if (conn == null) {
                continue;
            }
            StartNode start = outputNodes.getChildren().get(i);

            conn.getPolylineNodes().clear();
            INode next = start.getNext();
            while (next != null && !next.getType().isTerminal()) {
                INode current = next;
                conn.getPolylineNodes().add(new Point(current.getPosition()));
                next = current.getNext();
            }
        }
    }

    public static void removeGraph(FlowComponent<?> start) {
        Set<FlowComponent<?>> visited = new HashSet<>();
        Queue<FlowComponent<?>> nexts = new ArrayDeque<>();
        nexts.add(start);
        while (!nexts.isEmpty()) {
            FlowComponent<?> node = nexts.remove();
            for (EndNode conn : node.inputNodes.getChildren()) {
                StartNode pair = conn.getStart();
                if (pair == null) {
                    continue;
                }
                FlowComponent<?> prev = pair.getFlowComponent();
                if (visited.contains(prev)) {
                    continue;
                }
                visited.add(prev);
                nexts.add(prev);
            }
            for (StartNode conn : node.outputNodes.getChildren()) {
                EndNode pair = conn.getEnd();
                if (pair == null) {
                    continue;
                }
                FlowComponent<?> next = pair.getFlowComponent();
                if (visited.contains(next)) {
                    continue;
                }
                visited.add(next);
                nexts.add(next);
            }
        }
        for (FlowComponent<?> node : visited) {
            node.remove();
        }
    }

    public void disconnect() {
        // Update GUI connections
        inputNodes.removeAllConnections();
        outputNodes.removeAllConnections();
        // Update procedure graph connections
        NetworkHelper.removeAllConnectionsFor(procedure);
    }

    public void remove() {
        disconnect();
        procedure.invalidate();
        getParent().removeFlowComponent(this);
    }

    public ConnectionNodes<EndNode> getInputNodes() {
        return inputNodes;
    }

    public ConnectionNodes<StartNode> getOutputNodes() {
        return outputNodes;
    }

    @Nonnull
    @Override
    public EditorPanel getParent() {
        return Objects.requireNonNull((EditorPanel) super.getParent());
    }

    public int getZIndex() {
        return zIndex;
    }

    public void setZIndex(int z) {
        this.zIndex = z;
    }

    @Override
    public int compareTo(FlowComponent<?> that) {
        return this.getZIndex() - that.getZIndex();
    }

    public P getProcedure() {
        return procedure;
    }

    public String getGroup() {
        return procedure.getGroup();
    }

    public void setGroup(String group) {
        procedure.setGroup(group);
    }

    public IClientDataStorage getDataHandler() {
        return procedure;
    }

    public void setLinkedProcedure(P procedure) {
        this.procedure = procedure;
        setName(procedure.getName());
        setLocation(procedure.getComponentX(), procedure.getComponentY());
        repopulateErrors();
    }

    private void repopulateErrors() {
        errorIndicator.clearErrors();
        for (Menu<P> menu : menus.getChildren()) {
            errorIndicator.populateErrors(menu);
        }
    }

    public void readConnections(Map<IProcedure, FlowComponent<?>> m) {
        this.inputNodes.readConnections(m, procedure);
        this.outputNodes.readConnections(m, procedure);
    }

    @Override
    public void setLocation(int x, int y) {
        super.setLocation(x, y);
        procedure.setComponentX(x);
        procedure.setComponentY(y);
    }

    @Override
    public void provideInformation(ITextReceiver receiver) {
        super.provideInformation(receiver);
        receiver.line("Z=" + this.getZIndex());
    }
}
