package vswe.stevesfactory.blocks;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.*;
import net.minecraftforge.items.*;
import vswe.stevesfactory.setup.ModBlocks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

// TODO drop stored item when broken
public class BlockInteractorTileEntity extends TileEntity implements IItemHandler {

    public static final int UNREADY = 0;
    public static final int READY_SIMULATE = 1;
    public static final int READY_PERFORMED = 2;

    private LazyOptional<IItemHandler> itemHandlerCap = LazyOptional.of(() -> this);

    private int state = UNREADY;
    private long lastUpdateGT = -1L;

    private List<ItemStack> droppedItems = null;
    private int readerIndex = -1;

    public BlockInteractorTileEntity() {
        super(ModBlocks.blockInteractorTileEntity.get());
    }

    private boolean isUnready() {
        return state == UNREADY;
    }

    private void breakFrontBlock(boolean remove) {
        if (world == null) {
            droppedItems = ImmutableList.of();
            readerIndex = -1;
            return;
        }

        if (state != READY_PERFORMED && world.getGameTime() != lastUpdateGT) {
            lastUpdateGT = world.getGameTime();
            state = UNREADY;
        }
        switch (state) {
            case UNREADY: {
                Direction facing = this.getBlockState().get(BlockStateProperties.FACING);
                BlockPos neighborPos = pos.offset(facing);
                BlockState neighbor = world.getBlockState(neighborPos);
                if (neighbor.isAir(world, neighborPos)) {
                    return;
                }
                droppedItems = Block.getDrops(neighbor, (ServerWorld) world, neighborPos, world.getTileEntity(neighborPos));
                readerIndex = droppedItems.size() - 1;
                if (remove) {
                    world.removeBlock(neighborPos, false);
                    state = READY_PERFORMED;
                } else {
                    state = READY_SIMULATE;
                }
                break;
            }
            case READY_SIMULATE: {
                if (remove) {
                    Direction facing = this.getBlockState().get(BlockStateProperties.FACING);
                    BlockPos neighborPos = pos.offset(facing);
                    world.removeBlock(neighborPos, false);
                    state = READY_PERFORMED;
                }
                break;
            }
            case READY_PERFORMED: {
                break;
            }
        }
    }

    private ItemStack getNextItem(boolean remove) {
        breakFrontBlock(remove);
        // Tried to break block in front, but nothing happened -> unable to do anything
        if (isUnready()) {
            return ItemStack.EMPTY;
        }
        return droppedItems.get(readerIndex);
    }

    private void advance() {
        readerIndex--;
        if (readerIndex < 0) {
            state = UNREADY;
        }
    }

    @Override
    public int getSlots() {
        return 1;
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {
        if (slot != 0) {
            return ItemStack.EMPTY;
        }
        return getNextItem(false);
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (slot != 0) {
            throw new IndexOutOfBoundsException("Slot index ${slot} is not in range of [0,0]!");
        }
        if (world == null || !(world instanceof ServerWorld)) {
            return stack;
        }

        Item item = stack.getItem();
        Direction facing = this.getBlockState().get(BlockStateProperties.FACING);
        BlockPos neighborPos = pos.offset(facing);
        BlockState neighbor = world.getBlockState(neighborPos);

        FakePlayer fake = FakePlayerFactory.getMinecraft((ServerWorld) world);
        BlockRayTraceResult rayTraceResult = world.rayTraceBlocks(new RayTraceContext(
                new Vec3d(pos), new Vec3d(neighborPos),
                RayTraceContext.BlockMode.OUTLINE,
                RayTraceContext.FluidMode.NONE,
                fake));
        BlockItemUseContext ctx = new BlockItemUseContext(new ItemUseContext(fake, Hand.MAIN_HAND, rayTraceResult));

        boolean replaceable = neighbor.isReplaceable(ctx);
        boolean placeable = item instanceof BlockItem;
        if (replaceable && placeable) {
            BlockState state = ((BlockItem) item).getBlock().getStateForPlacement(ctx);
            if (state != null && state.isValidPosition(world, neighborPos)) {
                if (!simulate) {
                    world.setBlockState(neighborPos, state);
                }
                return ItemHandlerHelper.copyStackWithSize(stack, stack.getCount() - 1);
            }
        }
        return stack;
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (slot != 0) {
            throw new IndexOutOfBoundsException("Slot index ${slot} is not in range of [0,0]!");
        }
        if (world == null || !(world instanceof ServerWorld)) {
            return ItemStack.EMPTY;
        }

        if (simulate) {
            ItemStack next = getNextItem(false);
            if (next.isEmpty()) {
                return next;
            }

            ItemStack result = next.copy();
            result.setCount(Math.min(result.getCount(), amount));
            return result;
        } else {
            ItemStack next = getNextItem(true);
            if (next.isEmpty()) {
                return next;
            }

            int difference = next.getCount() - amount;
            if (difference > 0) {
                // Stores more than wanted
                next.shrink(amount);
                return ItemHandlerHelper.copyStackWithSize(next, amount);
            } else {
                // Stores fewer than or exactly as wanted
                advance();
                return next;
            }
        }
    }

    @Override
    public int getSlotLimit(int slot) {
        return 64;
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return slot == 0;
    }

    public void dropItems() {
        if (state == READY_PERFORMED) {
            for (ItemStack item : droppedItems) {
                InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), item);
            }
        }
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return itemHandlerCap.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void read(CompoundNBT compound) {
        super.read(compound);
        state = compound.getInt("State");
        readerIndex = compound.getInt("ReaderIdx");
        droppedItems.clear();
        ListNBT serializedItems = compound.getList("DroppedItems", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < serializedItems.size(); i++) {
            droppedItems.add(ItemStack.read(compound));
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        compound.putInt("State", state);
        compound.putInt("ReaderIdx", readerIndex);
        ListNBT serializedItems = new ListNBT();
        if (droppedItems != null) {
            for (ItemStack item : droppedItems) {
                serializedItems.add(item.write(new CompoundNBT()));
            }
        }
        compound.put("DroppedItems", serializedItems);
        return super.write(compound);
    }
}
