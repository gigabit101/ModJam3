package vswe.stevesfactory.logic.execution;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.MutableClassToInstanceMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.world.World;
import vswe.stevesfactory.Config;
import vswe.stevesfactory.api.logic.IExecutionContext;
import vswe.stevesfactory.api.logic.IProcedure;
import vswe.stevesfactory.api.logic.fluid.IFluidBuffer;
import vswe.stevesfactory.api.logic.item.IItemBuffer;
import vswe.stevesfactory.api.network.INetworkController;
import vswe.stevesfactory.logic.item.CraftingBufferElement;
import vswe.stevesfactory.logic.item.DirectBufferElement;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;

/**
 * A common execution context implementation for procedures.
 * <p>
 * This should only exist on server side. Usages on client usually indicates a design problem.
 */
public class ProcedureExecutor implements IExecutionContext {

    private static final Object2IntMap<Class<? extends IItemBuffer>> itemOrderAssociation = new Object2IntOpenHashMap<>();
    private static int nextItemOrder = 0;
    private static final Object2IntMap<Class<? extends IFluidBuffer>> fluidOrderAssociation = new Object2IntOpenHashMap<>();
    private static int nextFluidOrder = 0;

    public static void registerItemBufferType(Class<? extends IItemBuffer> type) {
        itemOrderAssociation.put(type, nextItemOrder++);
    }

    public static void registerFluidBufferType(Class<? extends IFluidBuffer> type) {
        fluidOrderAssociation.put(type, nextFluidOrder++);
    }

    static {
        registerItemBufferType(CraftingBufferElement.class);
        registerItemBufferType(DirectBufferElement.class);
    }

    private final INetworkController controller;
    private final World world;

    private Deque<IProcedure> executionStack = new ArrayDeque<>();
    private Object2IntOpenHashMap<IProcedure> executionFrequency = new Object2IntOpenHashMap<>();

    @SuppressWarnings("unchecked")
    private Map<Item, IItemBuffer>[] itemBufferElements = new IdentityHashMap[itemOrderAssociation.size()];
    @SuppressWarnings("unchecked")
    private Map<Fluid, IFluidBuffer>[] fluidBufferElements = new IdentityHashMap[fluidOrderAssociation.size()];
    private List<Variable<?>> variables = new ArrayList<>();
    private ClassToInstanceMap<Object> customData = MutableClassToInstanceMap.create();

    public ProcedureExecutor(INetworkController controller, World world) {
        this.controller = controller;
        this.world = world;
        this.populateVariables();
    }

    private void populateVariables() {
        for (VariableDefinition<?> def : controller.getVariableDefinitions()) {
            variables.add(def.instantiate());
        }
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
            int freq = executionFrequency.addTo(frame, 1);
            if (freq < Config.COMMON.repeatThreshold.get()) {
                executionStack.push(frame);
            }
        }
    }

    public void start(IProcedure hat) {
        executionStack.push(hat);
        while (!executionStack.isEmpty()) {
            executionStack.poll().execute(this);
        }
        cleanup();
    }

    private Map<Item, IItemBuffer> getOrCreateItemBuffers(int index) {
        if (itemBufferElements[index] != null) {
            return itemBufferElements[index];
        }
        Map<Item, IItemBuffer> map = new IdentityHashMap<>();
        itemBufferElements[index] = map;
        return map;
    }

    @Override
    public <T extends IItemBuffer> Map<Item, T> getItemBuffers(Class<T> type) {
        int index = itemOrderAssociation.getInt(type);
        // Returns checked type -> entry put is checked too
        @SuppressWarnings("unchecked") Map<Item, T> map = (Map<Item, T>) getOrCreateItemBuffers(index);
        return map;
    }

    private Map<Fluid, IFluidBuffer> getOrCreateFluidBuffers(int index) {
        if (fluidBufferElements[index] != null) {
            return fluidBufferElements[index];
        }
        Map<Fluid, IFluidBuffer> map = new IdentityHashMap<>();
        fluidBufferElements[index] = map;
        return map;
    }

    @Override
    public <T extends IFluidBuffer> Map<Fluid, T> getFluidBuffers(Class<T> type) {
        int index = itemOrderAssociation.getInt(type);
        // Returns checked type -> entry put is checked too
        @SuppressWarnings("unchecked") Map<Fluid, T> map = (Map<Fluid, T>) getOrCreateFluidBuffers(index);
        return map;
    }

    @Override
    public List<Variable<?>> getVariables() {
        return variables;
    }

    @Override
    public ClassToInstanceMap<Object> getCustomData() {
        return customData;
    }

    @Override
    public void forEachItemBuffer(BiConsumer<Item, IItemBuffer> lambda) {
        for (Map<Item, IItemBuffer> buffers : itemBufferElements) {
            if (buffers == null) {
                continue;
            }
            buffers.forEach(lambda);
        }
    }

    @Override
    public void forEachFluidBuffer(BiConsumer<Fluid, IFluidBuffer> lambda) {
        for (Map<Fluid, IFluidBuffer> buffers : fluidBufferElements) {
            if (buffers == null) {
                continue;
            }
            buffers.forEach(lambda);
        }
    }

    private void cleanup() {
        for (Map<Item, IItemBuffer> buffers : itemBufferElements) {
            if (buffers == null) {
                continue;
            }
            for (Map.Entry<Item, IItemBuffer> entry : buffers.entrySet()) {
                IItemBuffer element = entry.getValue();
                element.cleanup();
            }
        }
        for (Map<Fluid, IFluidBuffer> buffers : fluidBufferElements) {
            if (buffers == null) {
                continue;
            }
            for (Map.Entry<Fluid, IFluidBuffer> entry : buffers.entrySet()) {
                IFluidBuffer element = entry.getValue();
                element.cleanup();
            }
        }
    }
}
