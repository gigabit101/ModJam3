package vswe.stevesfactory.components;


import gigabit101.AdvancedSystemManager2.components.RadioButton;
import gigabit101.AdvancedSystemManager2.Localization;
import gigabit101.AdvancedSystemManager2.components.ComponentMenuItem;
import gigabit101.AdvancedSystemManager2.components.FlowComponent;
import gigabit101.AdvancedSystemManager2.components.IConditionStuffMenu;
import gigabit101.AdvancedSystemManager2.components.Setting;

import java.util.List;

public class ComponentMenuItemCondition extends ComponentMenuItem implements IConditionStuffMenu {
    public ComponentMenuItemCondition(FlowComponent parent) {
        super(parent);
    }


    @Override
    protected void initRadioButtons() {
        radioButtons.add(new gigabit101.AdvancedSystemManager2.components.RadioButton(RADIO_BUTTON_X_LEFT, RADIO_BUTTON_Y, Localization.REQUIRES_ALL));
        radioButtons.add(new RadioButton(RADIO_BUTTON_X_RIGHT, RADIO_BUTTON_Y, Localization.IF_ANY));
    }

    public boolean requiresAll() {
        return isFirstRadioButtonSelected();
    }

    @Override
    public void addErrors(List<String> errors) {
        for (Setting setting : getSettings()) {
            if (setting.isValid()) {
                return;
            }
        }

        errors.add(Localization.NO_CONDITION_ERROR.toString());
    }
}
