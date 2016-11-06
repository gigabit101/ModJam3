package vswe.stevesfactory.components;


import gigabit101.AdvancedSystemManager2.Localization;
import gigabit101.AdvancedSystemManager2.components.ComponentMenuRedstoneSidesTrigger;

public class ComponentMenuRedstoneSidesNodes extends ComponentMenuRedstoneSidesTrigger {

    public ComponentMenuRedstoneSidesNodes(gigabit101.AdvancedSystemManager2.components.FlowComponent parent) {
        super(parent);
    }

    @Override
    public String getName() {
        return Localization.REDSTONE_SIDES_MENU.toString();
    }

    @Override
    public boolean isVisible() {
        return true;
    }
}
