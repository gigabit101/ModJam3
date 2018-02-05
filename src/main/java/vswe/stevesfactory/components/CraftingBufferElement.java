package vswe.stevesfactory.components;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.items.IItemHandler;
import vswe.stevesfactory.blocks.ConnectionBlockType;
import vswe.stevesfactory.tiles.TileEntityManager;

import javax.annotation.Nonnull;
import java.util.*;

public class CraftingBufferElement implements IItemBufferElement, IItemBufferSubElement
{
    private static final ItemStack DUMMY_ITEM = ItemStack.EMPTY;

    private CommandExecutor executor;
    private ComponentMenuCrafting craftingMenu;
    private ComponentMenuContainerScrap scrapMenu;
    private IRecipe recipe;
    private ItemStack result;
    private boolean isCrafting;
    private boolean justRemoved;
    private int overflowBuffer;
    private List<ItemStack> containerItems;

    public CraftingBufferElement(CommandExecutor executor, ComponentMenuCrafting craftingMenu, ComponentMenuContainerScrap scrapMenu)
    {
        this.executor = executor;
        this.craftingMenu = craftingMenu;
        this.scrapMenu = scrapMenu;
        recipe = craftingMenu.getDummy().getRecipe();
        if(recipe != null)
        {
            result = recipe.getCraftingResult(craftingMenu.getDummy());
        }
        else
            {
                result = ItemStack.EMPTY;
            }
        containerItems = new ArrayList<ItemStack>();
    }


    @Override
    public void prepareSubElements()
    {
        isCrafting = true;
        justRemoved = false;
    }

    @Override
    public IItemBufferSubElement getSubElement()
    {
        if (isCrafting && !result.isEmpty())
        {
            isCrafting = false;
            return this;
        } else
        {
            return null;
        }
    }

    @Override
    public void removeSubElement()
    {
        //nothing to do
    }

    @Override
    public void releaseSubElements()
    {
        if (!result.isEmpty())
        {
            if (overflowBuffer > 0)
            {
                ItemStack overflow = result.copy();
                overflow.setCount(overflowBuffer);
                disposeOfExtraItem(overflow);
                overflowBuffer = 0;
            }
            for (ItemStack containerItem : containerItems)
            {
                disposeOfExtraItem(containerItem);
            }
            containerItems.clear();
        }
    }

    private static final double SPEED_MULTIPLIER = 0.05F;
    private static final Random rand = new Random();

    private void disposeOfExtraItem(ItemStack itemStack)
    {
        TileEntityManager manager = craftingMenu.getParent().getManager();
        List<SlotInventoryHolder> inventories = CommandExecutor.getContainers(manager, scrapMenu, ConnectionBlockType.INVENTORY);

        for (SlotInventoryHolder inventoryHolder : inventories)
        {
            IItemHandler inventory = inventoryHolder.getInventory();

            for (int i = 0; i < inventory.getSlots(); i++)
            {
                if (inventory.insertItem(i, itemStack, true) != itemStack)
                {
                    ItemStack itemInSlot = inventory.getStackInSlot(i);
                    if (itemInSlot.isEmpty() || (itemInSlot.isItemEqual(itemStack) && ItemStack.areItemStackTagsEqual(itemStack, itemInSlot) && itemStack.isStackable()))
                    {
                        int itemCountInSlot = itemInSlot.isEmpty() ? 0 : itemInSlot.getCount();

                        int moveCount = Math.min(itemStack.getCount(), Math.min(inventory.getSlotLimit(i), itemStack.getMaxStackSize()) - itemCountInSlot);

                        if (moveCount > 0)
                        {
                            if (itemInSlot.isEmpty())
                            {
                                itemInSlot = itemStack.copy();
                                itemInSlot.setCount(0);
                                inventory.insertItem(i, itemInSlot, false);
                            }

                            itemInSlot.grow(moveCount);
                            itemStack.shrink(moveCount);
                            if (itemStack.getCount() == 0)
                            {
                                return;
                            }
                        }
                    }
                }
            }
        }


        double spawnX = manager.getPos().getX() + rand.nextDouble() * 0.8 + 0.1;
        double spawnY = manager.getPos().getY() + rand.nextDouble() * 0.3 + 1.1;
        double spawnZ = manager.getPos().getZ() + rand.nextDouble() * 0.8 + 0.1;

        EntityItem entityitem = new EntityItem(manager.getWorld(), spawnX, spawnY, spawnZ, itemStack);

        entityitem.motionX = rand.nextGaussian() * SPEED_MULTIPLIER;
        entityitem.motionY = rand.nextGaussian() * SPEED_MULTIPLIER + 0.2F;
        entityitem.motionZ = rand.nextGaussian() * SPEED_MULTIPLIER;

        manager.getWorld().spawnEntity(entityitem);
    }

    @Override
    public int retrieveItemCount(int moveCount)
    {
        return moveCount; //no limit
    }

    @Override
    public void decreaseStackSize(int moveCount)
    {
        //no limit
    }

    @Override
    public void remove()
    {
        //nothing to do
    }

    @Override
    public void onUpdate()
    {
        for (IItemHandler inventory : inventories)
        {
//            inventory.markDirty();
        }
        inventories.clear();
    }


    @Override
    public int getSizeLeft()
    {
        if (!justRemoved)
        {
            return overflowBuffer > 0 ? overflowBuffer : findItems(false) ? result.getCount() : 0;
        } else
        {
            justRemoved = false;
            return 0;
        }
    }

    @Override
    public void reduceAmount(int amount)
    {
        justRemoved = true;
        if (overflowBuffer > 0)
        {
            overflowBuffer = overflowBuffer - amount;
        } else
        {
            findItems(true);
            overflowBuffer = result.getCount() - amount;
        }
        isCrafting = true;
    }

    @Override
    public ItemStack getItemStack()
    {
        if (useAdvancedDetection())
        {
            findItems(false);
        }

        return result;
    }

    private List<IItemHandler> inventories = new ArrayList<IItemHandler>();

    private boolean useAdvancedDetection()
    {
        return craftingMenu.getResultItem().getFuzzyMode() != FuzzyMode.PRECISE;
    }

    private boolean findItems(boolean remove)
    {
        Map<Integer, ItemStack> foundItems = new HashMap<Integer, ItemStack>();
        for (ItemBufferElement itemBufferElement : executor.itemBuffer)
        {
            int count = itemBufferElement.retrieveItemCount(9);
            for (Iterator<SlotStackInventoryHolder> iterator = itemBufferElement.getSubElements().iterator(); iterator.hasNext(); )
            {
                IItemBufferSubElement itemBufferSubElement = iterator.next();
                ItemStack itemstack = itemBufferSubElement.getItemStack();
                int subCount = Math.min(count, itemBufferSubElement.getSizeLeft());
                for (int i = 0; i < 9; i++)
                {
                    CraftingSetting setting = (CraftingSetting) craftingMenu.getSettings().get(i);
                    if (foundItems.get(i) == null)
                    {
                        if (!setting.isValid())
                        {
                            foundItems.put(i, DUMMY_ITEM);
                        }
                        else if (subCount > 0 && setting.isEqualForCommandExecutor(itemstack))
                        {
                            foundItems.put(i, itemstack.copy());

                            if (craftingMenu.getDummy().isItemValidForRecipe(recipe, craftingMenu.getResultItem(), foundItems, useAdvancedDetection()))
                            {
                                subCount--;
                                count--;
                                if (remove)
                                {
                                    if (itemstack.getItem().hasContainerItem(itemstack))
                                    {
                                        containerItems.add(itemstack.getItem().getContainerItem(itemstack));
                                    }
                                    itemBufferElement.decreaseStackSize(1);
                                    itemBufferSubElement.reduceAmount(1);
                                    if (itemBufferSubElement.getSizeLeft() == 0)
                                    {
                                        itemBufferSubElement.remove();
                                        iterator.remove();
                                    }
                                    inventories.add(((SlotStackInventoryHolder) itemBufferSubElement).getInventory());
                                }
                            } else
                            {
                                foundItems.remove(i);
                            }
                        }
                    }
                }
            }
        }

        if (foundItems.size() == 9)
        {
            result = craftingMenu.getDummy().getResult(foundItems);
            result = result != null ? result.copy() : null;
            return true;
        } else
        {
            return false;
        }
    }
}
