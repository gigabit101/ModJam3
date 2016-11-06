package vswe.stevesfactory.components;


import gigabit101.AdvancedSystemManager2.Localization;
import gigabit101.AdvancedSystemManager2.components.ComponentMenuInventory;

public class ComponentMenuInventoryCondition extends ComponentMenuInventory {
    public ComponentMenuInventoryCondition(gigabit101.AdvancedSystemManager2.components.FlowComponent parent) {
        super(parent);
    }

    @Override
    protected void initRadioButtons() {
        radioButtonsMulti.add(new RadioButtonInventory(0, Localization.RUN_SHARED_ONCE));
        radioButtonsMulti.add(new RadioButtonInventory(1, Localization.REQUIRE_ALL_TARGETS));
        radioButtonsMulti.add(new RadioButtonInventory(2, Localization.REQUIRE_ONE_TARGET));
    }
}
