package vswe.stevesfactory.api.item;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.*;
import vswe.stevesfactory.api.logic.IExecutionContext;

import java.util.*;

public class CraftingBufferElement implements IItemBufferElement {

    private static final Map<IRecipe<?>, Map<Item, ItemStack>> cache = new HashMap<>();

    private static Map<Item, ItemStack> getIngredientStacks(IRecipe<?> recipe) {
        if (cache.containsKey(recipe)) {
            return cache.get(recipe);
        }

        Map<Item, ItemStack> ingredientStacks = new IdentityHashMap<>();
        for (Ingredient ingredient : recipe.getIngredients()) {
            for (ItemStack stack : ingredient.getMatchingStacks()) {
                Item item = stack.getItem();
                ItemStack ingredientStack = ingredientStacks.get(item);
                if (ingredientStack == null) {
                    ingredientStacks.put(item, stack.copy());
                } else {
                    ingredientStack.grow(stack.getCount());
                }
            }
        }
        cache.put(recipe, ingredientStacks);
        return ingredientStacks;
    }

    private final IExecutionContext context;

    private ICraftingRecipe recipe;
    private Map<Item, ItemStack> ingredientStacks = ImmutableMap.of();
    private ItemStack result;

    public CraftingBufferElement(IExecutionContext context) {
        this.context = context;
    }

    public IRecipe<?> getRecipe() {
        return recipe;
    }

    public void setRecipe(ICraftingRecipe recipe) {
        this.recipe = recipe;
        ingredientStacks = getIngredientStacks(recipe);
        // TODO inventory
        result = recipe.getCraftingResult(null);
        refresh();
    }

    @Override
    public ItemStack getStack() {
        return result;
    }

    @Override
    public void setStack(ItemStack stack) {
        result = stack;
    }

    @Override
    public int getUsed() {
        return 0;
    }

    @Override
    public void use(int amount) {
        Map<Item, IItemBufferElement> buffers = context.getItemBufferElements();
        for (ItemStack stack : ingredientStacks.values()) {
            IItemBufferElement bufferElement = buffers.get(stack.getItem());
            if (bufferElement != null) {
                int available = bufferElement.getStack().getCount();
                Preconditions.checkState(available >= amount);
                bufferElement.use(amount);
                break;
            }
        }
    }

    public void refresh() {
        Map<Item, IItemBufferElement> buffers = context.getItemBufferElements();
        int maxAvailable = 0;
        for (ItemStack matchable : ingredientStacks.values()) {
            // The total number of the ingredients needed in this recipe
            int needed = matchable.getCount();
            int found = 0;

            IItemBufferElement buffer = buffers.get(matchable.getItem());
            ItemStack source = buffer.getStack();
            // Number of available resource
            int available = source.getCount();
            // Number of crafting set performable, just looking at this ingredient
            int availableSets = available / needed;
            found += availableSets;

            maxAvailable = Math.max(maxAvailable, found);
        }
        int outputBase = recipe.getRecipeOutput().getCount();
        result.setCount(outputBase * maxAvailable);
    }

    @Override
    public void put(int amount) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void cleanup() {
    }
}
