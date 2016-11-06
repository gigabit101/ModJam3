package vswe.stevesfactory.components;


import gigabit101.AdvancedSystemManager2.Localization;
import gigabit101.AdvancedSystemManager2.components.ComponentMenuListOrder;
import gigabit101.AdvancedSystemManager2.components.FlowComponent;

public class ComponentMenuListOrderVariable extends ComponentMenuListOrder {
    public ComponentMenuListOrderVariable(FlowComponent parent) {
        super(parent);
    }

    @Override
    public boolean isVisible() {
        return getParent().getConnectionSet() == gigabit101.AdvancedSystemManager2.components.ConnectionSet.STANDARD;
    }

    @Override
    public String getName() {
        return Localization.VALUE_ORDER_MENU.toString();
    }
}
