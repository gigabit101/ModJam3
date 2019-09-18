package vswe.stevesfactory.utils;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tags.*;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;
import java.util.function.IntFunction;

public final class IOHelper {

    private IOHelper() {
    }

    public static ListNBT writeBlockPoses(Collection<BlockPos> poses) {
        return writeBlockPoses(poses, new ListNBT());
    }

    public static ListNBT writeBlockPoses(Collection<BlockPos> poses, ListNBT target) {
        for (BlockPos pos : poses) {
            target.add(NBTUtil.writeBlockPos(pos));
        }
        return target;
    }

    public static <T extends Collection<BlockPos>> T readBlockPoses(ListNBT serializedPoses, T target) {
        for (int i = 0; i < serializedPoses.size(); i++) {
            target.add(NBTUtil.readBlockPos(serializedPoses.getCompound(i)));
        }
        return target;
    }

    public static void writeBlockPoses(Collection<BlockPos> poses, PacketBuffer buf) {
        buf.writeInt(poses.size());
        for (BlockPos linkable : poses) {
            buf.writeBlockPos(linkable);
        }
    }

    public static <T extends Collection<ItemStack>> T readItemStacks(ListNBT serializedFilters, T target) {
        for (int i = 0; i < serializedFilters.size(); i++) {
            target.add(ItemStack.read(serializedFilters.getCompound(i)));
        }
        return target;
    }

    public static ListNBT writeItemStacks(Collection<ItemStack> stacks) {
        return writeItemStacks(stacks, new ListNBT());
    }

    public static ListNBT writeItemStacks(Collection<ItemStack> filters, ListNBT target) {
        for (ItemStack stack : filters) {
            target.add(stack.write(new CompoundNBT()));
        }
        return target;
    }

    public static <T extends Collection<Tag<Item>>> T readItemTags(ListNBT serializedFilters, T target) {
        for (int i = 0; i < serializedFilters.size(); i++) {
            ResourceLocation id = new ResourceLocation(serializedFilters.getString(i));
            Tag<Item> tag = new ItemTags.Wrapper(id);
            target.add(tag);
        }
        return target;
    }

    public static <T> ListNBT writeTags(Collection<Tag<T>> tags, ListNBT target) {
        for (Tag<?> tag : tags) {
            target.add(new StringNBT(tag.getId().toString()));
        }
        return target;
    }

    public static <T> ListNBT writeTags(Collection<Tag<T>> tags) {
        return writeTags(tags, new ListNBT());
    }

    public static <T extends Collection<BlockPos>> T readBlockPoses(PacketBuffer buf, T target) {
        int size = buf.readInt();
        for (int i = 0; i < size; i++) {
            target.add(buf.readBlockPos());
        }
        return target;
    }

    public static <T extends Collection<BlockPos>> T readBlockPosesSized(PacketBuffer buf, IntFunction<T> targetFactory) {
        int size = buf.readInt();
        T target = targetFactory.apply(size);
        for (int i = 0; i < size; i++) {
            target.add(buf.readBlockPos());
        }
        return target;
    }

    public static int[] direction2Index(Collection<Direction> directions) {
        int[] res = new int[directions.size()];
        int i = 0;
        for (Direction direction : directions) {
            res[i] = direction.getIndex();
            i++;
        }
        return res;
    }

    public static List<Direction> index2Direction(int[] indices) {
        List<Direction> res = new ArrayList<>(indices.length);
        for (int index : indices) {
            res.add(Direction.byIndex(index));
        }
        return res;
    }
}
