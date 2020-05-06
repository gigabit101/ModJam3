package vswe.stevesfactory.library.gui.widget.panel;

import net.minecraft.util.IStringSerializable;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import vswe.stevesfactory.library.gui.widget.*;
import vswe.stevesfactory.ui.manager.menu.BlockTarget;

import java.util.*;
import java.util.function.Consumer;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER;

public class FilteredList<T extends IWidget & IStringSerializable> extends AbstractList<T> {

    public static <T extends IWidget & IStringSerializable> Pair<WrappingList<T>, TextField> createSearchableList(List<T> list, String defaultText) {
        Pair<FilteredList<T>, TextField> pair = of(list, defaultText);
        FilteredList<T> filteredList = pair.getLeft();

        TextField textField = pair.getRight();
        WrappingList<T> wrappingList = new WrappingList<T>();
        filteredList.onUpdate = searchResult -> {
            wrappingList.setContentList(searchResult);
            wrappingList.reflow();
        };

        return Pair.of(wrappingList, textField);
    }

    public static <T extends IWidget & IStringSerializable> Pair<FilteredList<T>, TextField> of(List<T> list, String defaultText) {
        FilteredList<T> filteredList = new FilteredList<>(list);
        filteredList.updateSearch(defaultText);

        TextField textField = new TextField(64, 12) {
            @Override
            public boolean onKeyPressed(int keyCode, int scanCode, int modifiers) {
                if (keyCode == GLFW_KEY_ENTER) {
                    filteredList.updateSearch(getText());
                    return true;
                }
                return super.onKeyPressed(keyCode, scanCode, modifiers);
            }

            @Override
            public void onFocusChanged(boolean focus) {
                super.onFocusChanged(focus);
                if (!focus) {
                    filteredList.updateSearch(getText());
                }
            }
        }.setText(defaultText);

        return Pair.of(filteredList, textField);
    }

    private final List<T> backed;
    private List<T> searchResult;

    private Consumer<List<T>> onUpdate = l -> {
    };

    FilteredList(List<T> backed) {
        this.backed = backed;
        this.searchResult = backed;
    }

    public Consumer<List<T>> getOnUpdate() {
        return onUpdate;
    }

    public void setOnUpdate(Consumer<List<T>> onUpdate) {
        this.onUpdate = onUpdate;
    }

    public void updateSearch(String search) {
        if (search.isEmpty()) {
            searchResult = backed;
        } else {
            searchResult = new ArrayList<>();
            for (T child : backed) {
                if (StringUtils.containsIgnoreCase(child.getName(), search)) {
                    searchResult.add(child);
                }
            }
        }
        onUpdate.accept(searchResult);
    }

    @Override
    public T get(int i) {
        return searchResult.get(i);
    }

    @Override
    public int size() {
        return searchResult.size();
    }
}
