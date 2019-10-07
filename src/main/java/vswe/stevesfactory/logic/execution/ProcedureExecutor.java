package vswe.stevesfactory.logic.execution;

import net.minecraft.item.Item;
import net.minecraft.world.World;
import vswe.stevesfactory.api.logic.IExecutionContext;
import vswe.stevesfactory.api.logic.IProcedure;
import vswe.stevesfactory.api.network.INetworkController;
import vswe.stevesfactory.api.item.IItemBufferElement;

import javax.annotation.Nullable;
import java.util.*;

/**
 * A common execution context implementation for procedures.
 * <p>
 * This should only exist on server side. Usages on client probably indicates a design problem.
 */
public class ProcedureExecutor implements IExecutionContext {

    private final INetworkController controller;
    private final World world;

    private Deque<IProcedure> executionStack = new ArrayDeque<>();
    private Map<Item, IItemBufferElement> itemBufferElements = new IdentityHashMap<>();

    public ProcedureExecutor(INetworkController controller, World world) {
        this.controller = controller;
        this.world = world;
    }

    @Override
    public INetworkController getController() {
        return controller;
    }

    @Override
    public World getControllerWorld() {
        return world;
    }

    @Override
    public void push(@Nullable IProcedure frame) {
        if (frame != null) {
            executionStack.push(frame);
        }
    }

    public void start(IProcedure hat) {
        executionStack.push(hat);
        while (!executionStack.isEmpty()) {
            executionStack.pop().execute(this);
        }
        cleanup();
    }

    @Override
    public Map<Item, IItemBufferElement> getItemBufferElements() {
        return itemBufferElements;
    }

    private void cleanup() {
        for (Map.Entry<Item, IItemBufferElement> entry : itemBufferElements.entrySet()) {
            entry.getValue().cleanup();
        }
    }
}
