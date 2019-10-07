package vswe.stevesfactory.logic.procedure;

import com.google.common.base.Preconditions;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import vswe.stevesfactory.api.logic.IExecutionContext;
import vswe.stevesfactory.logic.AbstractProcedure;
import vswe.stevesfactory.logic.Procedures;
import vswe.stevesfactory.api.item.CraftingBufferElement;
import vswe.stevesfactory.ui.manager.editor.FlowComponent;
import vswe.stevesfactory.utils.MyCraftingInventory;

import java.util.Optional;

public class CraftingProcedure extends AbstractProcedure {

    private ICraftingRecipe recipe;
    private CraftingInventory inv = new MyCraftingInventory();

    public CraftingProcedure() {
        super(Procedures.CRAFTING.getFactory());
    }

    @Override
    public void execute(IExecutionContext context) {
        pushFrame(context, 0);
        updateRecipe(context);
        if (hasError()) {
            return;
        }

        CraftingBufferElement buffer = new CraftingBufferElement(context);
        buffer.setRecipe(recipe);
    }

    public boolean hasError() {
        return recipe == null;
    }

    private void updateRecipe(IExecutionContext context) {
        if (recipe == null) {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            // In all cases we will not get null from the above invocation if we are on a server thread
            Preconditions.checkState(server != null, "Illegal to execute procedure on client side");
            Optional<ICraftingRecipe> recipe = server.getRecipeManager().getRecipe(IRecipeType.CRAFTING, inv, context.getControllerWorld());
            this.recipe = recipe.orElse(null);
        }
    }

    public void setIngredient(int index, ItemStack ingredient) {
        inv.setInventorySlotContents(index, ingredient);
        // Invalidate recipe to force rematch recipe on the next execution
        recipe = null;
    }

    @Override
    public FlowComponent<CraftingProcedure> createFlowComponent() {
        FlowComponent<CraftingProcedure> f = FlowComponent.of(this);
        return f;
    }
}
