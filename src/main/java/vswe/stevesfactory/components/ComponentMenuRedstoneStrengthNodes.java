package vswe.stevesfactory.components;


import gigabit101.AdvancedSystemManager2.Localization;
import gigabit101.AdvancedSystemManager2.components.ComponentMenuRedstoneStrength;

public class ComponentMenuRedstoneStrengthNodes extends ComponentMenuRedstoneStrength {
    public ComponentMenuRedstoneStrengthNodes(gigabit101.AdvancedSystemManager2.components.FlowComponent parent) {
        super(parent);
    }

    @Override
    public String getName() {
        return Localization.REDSTONE_STRENGTH_MENU_CONDITION.toString();
    }

    @Override
    public boolean isVisible() {
        return true;
    }
}
