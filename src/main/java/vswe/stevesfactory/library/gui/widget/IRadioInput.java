package vswe.stevesfactory.library.gui.widget;

public interface IRadioInput extends IWidget {

    boolean isChecked();

    void setChecked(boolean checked);

    void check(boolean checked);

    int getIndex();

    RadioController getRadioController();
}
