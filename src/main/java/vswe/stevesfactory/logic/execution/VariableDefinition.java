package vswe.stevesfactory.logic.execution;

import com.google.common.base.Preconditions;
import net.minecraft.nbt.CompoundNBT;

import java.util.Objects;

public final class VariableDefinition<T> {

    // Common data
    public final int id;
    public final Class<T> type;

    // Client data
    public String description;

    public VariableDefinition(int id, Class<T> type) {
        this.id = id;
        this.type = type;
    }

    public Variable<T> instantiate() {
        return new Variable<>();
    }

    // Limit so that there can be only one of each ID
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VariableDefinition<?> that = (VariableDefinition<?>) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public CompoundNBT write() {
        CompoundNBT tag = new CompoundNBT();
        tag.putInt("ID", id);
        tag.putString("TypeName", type.getName());
        tag.putString("Description", description);
        return tag;
    }

    public static <T> VariableDefinition<T> read(CompoundNBT tag, Class<T> typeMask) {
        int id = tag.getInt("ID");
        String typeName = tag.getString("TypeName");
        Preconditions.checkState(typeMask.getName().equals(typeName));

        VariableDefinition<T> var = new VariableDefinition<>(id, typeMask);
        readExtraInfo(tag, var);
        return var;
    }

    public static VariableDefinition<?> read(CompoundNBT tag) {
        int id = tag.getInt("ID");
        String typeName = tag.getString("TypeName");
        Class<?> type;
        try {
            type = Class.forName(typeName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Unable to find class " + typeName + " while reading VariableDefinition from CompountNBT.", e);
        }

        VariableDefinition<?> var = new VariableDefinition<>(id, type);
        readExtraInfo(tag, var);
        return var;
    }

    private static void readExtraInfo(CompoundNBT tag, VariableDefinition<?> var) {
        var.description = tag.getString("Description");
    }
}
