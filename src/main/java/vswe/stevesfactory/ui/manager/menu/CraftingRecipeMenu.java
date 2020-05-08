package vswe.stevesfactory.ui.manager.menu;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipeType;
import vswe.stevesfactory.api.logic.IClientDataStorage;
import vswe.stevesfactory.api.logic.IProcedure;
import vswe.stevesfactory.library.gui.layout.properties.Side;
import vswe.stevesfactory.library.gui.layout.properties.VerticalAlignment;
import vswe.stevesfactory.library.gui.widget.IWidget;
import vswe.stevesfactory.logic.procedure.ICraftingGrid;
import vswe.stevesfactory.ui.manager.editor.FlowComponent;
import vswe.stevesfactory.ui.manager.editor.Menu;

import java.util.List;
import java.util.Optional;

public class CraftingRecipeMenu<P extends IProcedure & IClientDataStorage & ICraftingGrid> extends Menu<P> {

    @SuppressWarnings("unchecked") // We don't really care the concrete generic parameter
    private IngredientSlot[] ingredients = new CraftingRecipeMenu.IngredientSlot[9];
    private ProductSlot product;
    private ICraftingRecipe recipe;

    public CraftingRecipeMenu() {
        for (int i = 0; i < ingredients.length; i++) {
            ingredients[i] = new IngredientSlot(ItemStack.EMPTY, i);
        }
        product = new ProductSlot(ItemStack.EMPTY);
    }

    @Override
    public void onInitialAttach() {
        super.onInitialAttach();

        for (IngredientSlot ingredient : ingredients) {
            addChildren(ingredient);
        }
        addChildren(product);
        reflow();
    }

    @Override
    public void onLinkFlowComponent(FlowComponent<P> flowComponent) {
        super.onLinkFlowComponent(flowComponent);
        P procedure = getLinkedProcedure();
        for (IngredientSlot slot : ingredients) {
            slot.stack = procedure.getIngredient(slot.slot);
        }
        updateRecipeProduct(); // Sets `product` displaying item stack
    }

    @Override
    public void reflow() {
        int x = 4;
        int y = HEADING_BOX.getPortionHeight() + 4;
        int i = 1;
        for (IngredientSlot slot : ingredients) {
            slot.setLocation(x, y);
            if (i % 3 == 0) {
                x = 4;
                y += slot.getWidth() + 4;
            } else {
                x += slot.getHeight() + 4;
            }
            i++;
        }

        // 0 1 2
        // 3 4 5 P
        // 6 7 8
        product.alignTo(ingredients[5], Side.RIGHT, VerticalAlignment.CENTER.asUnion());
        product.moveX(4);
    }

    @Override
    public String getHeadingText() {
        return I18n.format("menu.sfm.RecipeConfiguration");
    }

    @Override
    public List<String> populateErrors(List<String> errors) {
        if (recipe == null) {
            errors.add(I18n.format("error.sfm.CraftingProcedure.NoRecipe"));
        } else if (recipe.isDynamic()) {
            errors.add(I18n.format("error.sfm.CraftingProcedure.Dynamic"));
        }
        return errors;
    }

    private void updateRecipeProduct() {
        ClientWorld world = Minecraft.getInstance().world;
        CraftingInventory inventory = getLinkedProcedure().getInventory();
        Optional<ICraftingRecipe> lookup = world.getRecipeManager().getRecipe(IRecipeType.CRAFTING, inventory, world);
        this.recipe = lookup.orElse(null);
        ItemStack stack = lookup
                .map(r -> r.getCraftingResult(inventory))
                .orElse(ItemStack.EMPTY);
        this.product.setStack(stack);
    }

    private void onSetIngredient(int slot, ItemStack ingredient) {
        P procedure = getLinkedProcedure();
        procedure.setIngredient(slot, ingredient);
        updateRecipeProduct();
    }

    private void onClearIngredients() {
        for (IngredientSlot slot : ingredients) {
            slot.setStack(ItemStack.EMPTY);
        }
        product.setStack(ItemStack.EMPTY);
    }

    public class IngredientSlot extends ConfigurableSlot<IWidget> {

        public final int slot;

        public IngredientSlot(ItemStack stack, int slot) {
            super(stack);
            this.slot = slot;
        }

        @Override
        protected boolean hasEditor() {
            return false;
        }

        @Override
        protected IWidget createEditor() {
            return null;
        }

        @Override
        protected void onRightClick() {
            setStack(ItemStack.EMPTY);
        }

        @Override
        protected void onSetStack() {
            CraftingRecipeMenu.this.onSetIngredient(slot, stack);
        }
    }

    public class ProductSlot extends ConfigurableSlot<IWidget> {

        public ProductSlot(ItemStack stack) {
            super(stack);
        }

        @Override
        protected boolean hasEditor() {
            return false;
        }

        @Override
        protected IWidget createEditor() {
            return null;
        }

        @Override
        protected void onLeftClick() {
            // No inventory selection dialog for the product slot
        }

        @Override
        protected void onRightClick() {
            CraftingRecipeMenu.this.onClearIngredients();
        }
    }
}
