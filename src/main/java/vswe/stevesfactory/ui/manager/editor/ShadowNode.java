package vswe.stevesfactory.ui.manager.editor;

import vswe.stevesfactory.library.gui.widget.AbstractWidget;
import vswe.stevesfactory.library.gui.widget.mixin.LeafWidgetMixin;

import javax.annotation.Nullable;

/**
 * Shadow of a node that lives somewhere else than the connections panel. All events will be redirected to the linked
 * node, but no rendering will be done by default.
 * <p>
 * Rendering redirection may be done by inherit from the class and redirect {@link #render(int, int, float)} to the
 * linked node manually.
 */
public class ShadowNode extends AbstractWidget implements INode, LeafWidgetMixin {

    private final INode handle;

    public ShadowNode(INode handle) {
        this.setLocation(handle.getX(), handle.getY());
        this.setDimensions(handle.getWidth(), handle.getHeight());
        this.handle = handle;
    }

    @Nullable
    @Override
    public INode getPrevious() {
        return handle.getPrevious();
    }

    @Nullable
    @Override
    public INode getNext() {
        return handle.getNext();
    }

    @Override
    public void connectTo(INode next) {
        handle.connectTo(next);
    }

    @Override
    public void connectFrom(INode previous) {
        handle.connectFrom(previous);
    }

    @Override
    public void disconnectNext() {
        handle.disconnectNext();
    }

    @Override
    public void disconnectPrevious() {
        handle.disconnectPrevious();
    }

    @Override
    public Type getType() {
        return handle.getType();
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
    }
}
