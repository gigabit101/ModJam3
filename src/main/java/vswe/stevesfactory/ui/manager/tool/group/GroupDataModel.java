package vswe.stevesfactory.ui.manager.tool.group;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import vswe.stevesfactory.ui.manager.FactoryManagerGUI;

import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class GroupDataModel {

    public static final String DEFAULT_GROUP = "";

    private Int2ObjectMap<Consumer<String>> addListeners = new Int2ObjectOpenHashMap<>();
    private int addListenersId = 0;
    private Int2ObjectMap<Consumer<String>> removeListeners = new Int2ObjectOpenHashMap<>();
    private int removeListenersId = 0;
    private Int2ObjectMap<BiConsumer<String, String>> updateListeners = new Int2ObjectOpenHashMap<>();
    private int updateListenersId = 0;
    private Int2ObjectMap<Consumer<String>> selectListeners = new Int2ObjectOpenHashMap<>();
    private int selectListenersId = 0;
    private String currentGroup = DEFAULT_GROUP;

    public int addListenerAdd(Consumer<String> listener) {
        int id = addListenersId++;
        addListeners.put(id, listener);
        return id;
    }

    public void removeListenerAdd(int id) {
        addListeners.remove(id);
    }

    public int addListenerRemove(Consumer<String> listener) {
        int id = removeListenersId++;
        removeListeners.put(id, listener);
        return id;
    }

    public void removeListenerRemove(int id) {
        removeListeners.remove(id);
    }

    public int addListenerUpdate(BiConsumer<String, String> listener) {
        int id = updateListenersId++;
        updateListeners.put(id, listener);
        return id;
    }

    public void removeListenerUpdate(int id) {
        updateListeners.remove(id);
    }

    /**
     * Add a listener for when the user re-selects current group. Note that this will <b>not</b> be fired for the
     * default selection {@link GroupDataModel#DEFAULT_GROUP}.
     */
    public int addListenerSelect(Consumer<String> listener) {
        int id = selectListenersId++;
        selectListeners.put(id, listener);
        return id;
    }

    public void removeListenerSelect(int id) {
        selectListeners.remove(id);
    }

    public Collection<String> getGroups() {
        return FactoryManagerGUI.get().getController().getGroups();
    }

    public boolean addGroup(String group) {
        if (FactoryManagerGUI.get().getController().getGroups().add(group)) {
            for (Consumer<String> listener : addListeners.values()) {
                listener.accept(group);
            }
            return true;
        }
        return false;
    }

    public boolean removeGroup(String group) {
        if (FactoryManagerGUI.get().getController().getGroups().remove(group)) {
            for (Consumer<String> listener : removeListeners.values()) {
                listener.accept(group);
            }
            return true;
        }
        return false;
    }

    public boolean updateGroup(String from, String to) {
        FactoryManagerGUI gui = FactoryManagerGUI.get();
        if (gui.getController().getGroups().remove(from)) {
            gui.getController().getGroups().add(to);
            for (BiConsumer<String, String> listener : updateListeners.values()) {
                listener.accept(from, to);
            }
            return true;
        }
        return false;
    }

    public String getCurrentGroup() {
        return currentGroup;
    }

    public boolean setCurrentGroup(String group) {
        if (!this.currentGroup.equals(group)) {
            this.currentGroup = group;
            for (Consumer<String> listener : selectListeners.values()) {
                listener.accept(group);
            }
            return true;
        }
        return false;
    }
}
