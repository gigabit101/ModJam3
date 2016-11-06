package vswe.stevesfactory.components;

import gigabit101.AdvancedSystemManager2.Localization;
import gigabit101.AdvancedSystemManager2.blocks.ConnectionBlockType;
import gigabit101.AdvancedSystemManager2.components.ComponentMenuContainer;

import java.util.List;


public class ComponentMenuCamouflage extends ComponentMenuContainer {
    public ComponentMenuCamouflage(gigabit101.AdvancedSystemManager2.components.FlowComponent parent) {
        super(parent, ConnectionBlockType.CAMOUFLAGE);
    }

    @Override
    public String getName() {
        return Localization.CAMOUFLAGE_BLOCK_MENU.toString();
    }

    @Override
    protected void initRadioButtons() {
        //nothing here
    }

    @Override
    public void addErrors(List<String> errors) {
        if (selectedInventories.isEmpty()) {
            errors.add(Localization.NO_CAMOUFLAGE_BLOCKS_ERROR.toString());
        }
    }
}
