package vswe.stevesfactory.components;


import gigabit101.AdvancedSystemManager2.components.ConnectionSet;
import gigabit101.AdvancedSystemManager2.components.FlowComponent;

public class ComponentMenuContainerTypesVariable extends gigabit101.AdvancedSystemManager2.components.ComponentMenuContainerTypes {
    public ComponentMenuContainerTypesVariable(FlowComponent parent) {
        super(parent);
    }

    @Override
    public boolean isVisible() {
        return getParent().getConnectionSet() == ConnectionSet.EMPTY;
    }
}
