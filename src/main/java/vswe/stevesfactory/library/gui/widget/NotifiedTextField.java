package vswe.stevesfactory.library.gui.widget;

import lombok.val;

import java.util.function.Consumer;

public class NotifiedTextField extends TextField {

    public Consumer<String> onValueUpdated = __ -> {};

    public NotifiedTextField(int width, int height) {
        super(width, height);
    }

    public NotifiedTextField() {
    }

    @Override
    protected boolean updateText(String text) {
        val result = super.updateText(text);
        onValueUpdated.accept(text);
        return result;
    }
}
