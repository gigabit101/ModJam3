package vswe.stevesfactory.components;


import gigabit101.AdvancedSystemManager2.Localization;
import gigabit101.AdvancedSystemManager2.blocks.ConnectionBlockType;
import gigabit101.AdvancedSystemManager2.components.ComponentMenuContainer;

import java.util.List;

public class ComponentMenuInventory extends ComponentMenuContainer {
    public ComponentMenuInventory(gigabit101.AdvancedSystemManager2.components.FlowComponent parent) {
        super(parent, ConnectionBlockType.INVENTORY);
    }

    @Override
    public String getName() {
        return Localization.INVENTORY_MENU.toString();
    }

    @Override
    public void addErrors(List<String> errors) {
        if (selectedInventories.isEmpty()) {
            errors.add(Localization.NO_INVENTORY_ERROR.toString());
        }
    }
}
