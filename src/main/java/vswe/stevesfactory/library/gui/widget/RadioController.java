package vswe.stevesfactory.library.gui.widget;

import com.google.common.base.Preconditions;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class RadioController {

    private final List<RadioButton> radioButtons = new ArrayList<>();
    private int checkedIndex = -1;

    public List<RadioButton> getRadioButtons() {
        return radioButtons;
    }

    public int add(RadioButton button) {
        radioButtons.add(button);
        return radioButtons.size() - 1;
    }

    public void checkRadioButton(int index) {
        Preconditions.checkArgument(index < radioButtons.size());
        RadioButton checkedButton = getCurrentCheckedButton();
        if (checkedButton != null) {
            checkedButton.setChecked(false);
        }
        checkedIndex = index;
    }

    @Nullable
    public RadioButton getCurrentCheckedButton() {
        if (checkedIndex == -1) {
            return null;
        }
        return radioButtons.get(checkedIndex);
    }
}
