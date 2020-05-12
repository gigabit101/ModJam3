package vswe.stevesfactory.ui.manager.menu;

import lombok.val;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipeType;
import vswe.stevesfactory.api.logic.IClientDataStorage;
import vswe.stevesfactory.api.logic.IProcedure;
import vswe.stevesfactory.library.gui.layout.properties.Side;
import vswe.stevesfactory.library.gui.layout.properties.VerticalAlignment;
import vswe.stevesfactory.library.gui.widget.slot.ItemSlot;
import vswe.stevesfactory.logic.procedure.ICraftingGrid;
import vswe.stevesfactory.ui.manager.editor.FlowComponent;
import vswe.stevesfactory.ui.manager.editor.Menu;

import java.util.List;

public class CraftingRecipeMenu<P extends IProcedure & IClientDataStorage & ICraftingGrid> extends Menu<P> {

    private final ItemSlot[] ingredients = new ItemSlot[9];
    private final ItemSlot product;
    private ICraftingRecipe recipe;

    public CraftingRecipeMenu() {
        for (int i = 0; i < ingredients.length; i++) {
            val slot = ingredients[i] = new ItemSlot(ItemStack.EMPTY);
            slot.setInventorySelectAction();
            slot.setCtxMenuClear();
        }
        product = new ItemSlot(ItemStack.EMPTY);
        product.setCtxMenuClear();
    }

    @Override
    public void onInitialAttach() {
        super.onInitialAttach();

        for (val ingredient : ingredients) {
            addChildren(ingredient);
        }
        addChildren(product);
        reflow();
    }

    @Override
    public void onLinkFlowComponent(FlowComponent<P> flowComponent) {
        super.onLinkFlowComponent(flowComponent);
        val procedure = getLinkedProcedure();
        for (int i = 0; i < ingredients.length; i++) {
            ingredients[i].setRenderedStack(procedure.getIngredient(i));
        }
        updateRecipeProduct(); // Sets `product` displaying item stack
    }

    @Override
    public void reflow() {
        int x = 0;
        int y = 0;
        int i = 1;
        for (val slot : ingredients) {
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
        val world = Minecraft.getInstance().world;
        assert world != null;
        val inventory = getLinkedProcedure().getInventory();
        val lookup = world.getRecipeManager().getRecipe(IRecipeType.CRAFTING, inventory, world);
        this.recipe = lookup.orElse(null);
        val stack = lookup
                .map(r -> r.getCraftingResult(inventory))
                .orElse(ItemStack.EMPTY);
        this.product.setRenderedStack(stack);
    }

    private void onSetIngredient(int slot, ItemStack ingredient) {
        val procedure = getLinkedProcedure();
        procedure.setIngredient(slot, ingredient);
        updateRecipeProduct();
    }

    private void onClearIngredients() {
        for (val slot : ingredients) {
            slot.clearRenderedStack();
        }
        product.clearRenderedStack();
    }
}
