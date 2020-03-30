package vswe.stevesfactory.ui.manager.tool.group;

import vswe.stevesfactory.ui.manager.FactoryManagerGUI;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class GroupDataModel {

    public static final String DEFAULT_GROUP = "";

    private FactoryManagerGUI gui;
    private List<Consumer<String>> addListeners = new ArrayList<>();
    private List<Consumer<String>> removeListeners = new ArrayList<>();
    private List<BiConsumer<String, String>> updateListeners = new ArrayList<>();
    private List<Consumer<String>> selectListeners = new ArrayList<>();
    private String currentGroup = DEFAULT_GROUP;

    public GroupDataModel(FactoryManagerGUI gui) {
        this.gui = gui;
    }

    public int addListenerAdd(Consumer<String> listener) {
        addListeners.add(listener);
        return addListeners.size() - 1;
    }

    public void removeListenerAdd(int id) {
        addListeners.remove(id);
    }

    public int addListenerRemove(Consumer<String> listener) {
        removeListeners.add(listener);
        return removeListeners.size() - 1;
    }

    public void removeListenerRemove(int id) {
        removeListeners.remove(id);
    }

    public int addListenerUpdate(BiConsumer<String, String> listener) {
        updateListeners.add(listener);
        return updateListeners.size() - 1;
    }

    public void removeListenerUpdate(int id) {
        updateListeners.remove(id);
    }

    /**
     * Add a listener for when the user re-selects current group. Note that this will <b>not</b> be fired for the
     * default selection {@link GroupDataModel#DEFAULT_GROUP}.
     */
    public int addListenerSelect(Consumer<String> listener) {
        selectListeners.add(listener);
        return selectListeners.size() - 1;
    }

    public void removeListenerSelect(int id) {
        selectListeners.remove(id);
    }

    public Collection<String> getGroups() {
        return gui.getController().getGroups();
    }

    public boolean addGroup(String group) {
        if (gui.getController().getGroups().add(group)) {
            for (Consumer<String> listener : addListeners) {
                listener.accept(group);
            }
            return true;
        }
        return false;
    }

    public boolean removeGroup(String group) {
        if (gui.getController().getGroups().remove(group)) {
            for (Consumer<String> listener : removeListeners) {
                listener.accept(group);
            }
            return true;
        }
        return false;
    }

    public boolean updateGroup(String from, String to) {
        if (gui.getController().getGroups().remove(from)) {
            gui.getController().getGroups().add(to);
            for (BiConsumer<String, String> listener : updateListeners) {
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
            for (Consumer<String> listener : selectListeners) {
                listener.accept(group);
            }
            return true;
        }
        return false;
    }
}
