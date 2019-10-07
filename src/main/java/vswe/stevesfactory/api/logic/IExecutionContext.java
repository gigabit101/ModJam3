package vswe.stevesfactory.api.logic;

import net.minecraft.item.Item;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.MutablePair;
import vswe.stevesfactory.api.item.CraftingBufferElement;
import vswe.stevesfactory.api.item.DirectBufferElement;
import vswe.stevesfactory.api.network.INetworkController;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * A one-use only context object for program execution data storage.
 */
public interface IExecutionContext {

    INetworkController getController();

    World getControllerWorld();

    void push(@Nullable IProcedure frame);

    // TODO distinguish between direct buffers and crafting buffers
    Map<Item, MutablePair<DirectBufferElement, CraftingBufferElement>> getItemBufferElements();
}
