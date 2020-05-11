package vswe.stevesfactory.library.gui.widget;

public interface IRadioInput extends IWidget {

    boolean isChecked();

    /**
     * Set the internal state of this input only, do not update other linked inputs.
     */
    void setChecked(boolean checked);

    void setCheckedAndUpdate(boolean checked);

    int getIndex();

    RadioController getRadioController();
}
