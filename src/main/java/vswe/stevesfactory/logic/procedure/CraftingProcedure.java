package vswe.stevesfactory.logic.procedure;

import com.google.common.base.Preconditions;
import lombok.val;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import vswe.stevesfactory.api.logic.IExecutionContext;
import vswe.stevesfactory.logic.AbstractProcedure;
import vswe.stevesfactory.logic.item.CraftingBufferElement;
import vswe.stevesfactory.logic.item.RecipeInfo;
import vswe.stevesfactory.setup.ModProcedures;
import vswe.stevesfactory.ui.manager.editor.FlowComponent;
import vswe.stevesfactory.ui.manager.menu.CraftingRecipeMenu;
import vswe.stevesfactory.utils.IOHelper;

import javax.annotation.Nonnull;

public class CraftingProcedure extends AbstractProcedure implements ICraftingGrid {

    private CraftingInventory inventory = createCraftingInventory();
    private transient RecipeInfo info;

    public CraftingProcedure() {
        super(ModProcedures.crafting);
    }

    @Override
    public void execute(IExecutionContext context) {
        pushFrame(context, 0);
        updateRecipe(context);
        if (hasError()) {
            return;
        }

        val buffer = new CraftingBufferElement(context);
        buffer.setRecipe(info);
        context.getItemBuffers(CraftingBufferElement.class)
                .put(buffer.getStack().getItem(), buffer);
    }

    private void updateRecipe(IExecutionContext context) {
        if (info == null) {
            val server = ServerLifecycleHooks.getCurrentServer();
            // In all cases we will not get null from the above invocation if we are on a server thread
            Preconditions.checkState(server != null, "Illegal to execute procedure on client side");
            val lookup = server.getRecipeManager().getRecipe(IRecipeType.CRAFTING, inventory, context.getControllerWorld());
            info = lookup.map(RecipeInfo::new).orElse(null);
        }
    }

    public boolean hasError() {
        // Error for execution (server side)
        return info == null || info.getRecipe().isDynamic();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public FlowComponent<CraftingProcedure> createFlowComponent() {
        val f = new FlowComponent<>(this);
        f.addMenu(new CraftingRecipeMenu<>());
        return f;
    }

    @Override
    public CraftingInventory getInventory() {
        return inventory;
    }

    @Override
    public ItemStack getIngredient(int slot) {
        return inventory.getStackInSlot(slot);
    }

    @Override
    public void setIngredient(int slot, ItemStack ingredient) {
        inventory.setInventorySlotContents(slot, ingredient);
        info = null;
    }

    @Override
    public CompoundNBT serialize() {
        val tag = super.serialize();
        tag.put("RecipeInv", IOHelper.writeInventory(inventory));
        return tag;
    }

    @Override
    public void deserialize(CompoundNBT tag) {
        super.deserialize(tag);
        IOHelper.readInventory(tag.getList("RecipeInv", Constants.NBT.TAG_COMPOUND), inventory);
    }

    private static CraftingInventory createCraftingInventory() {
        return new CraftingInventory(new Container(null, -1) {
            @Override
            public boolean canInteractWith(@Nonnull PlayerEntity player) {
                return false;
            }
        }, 3, 3);
    }
}
