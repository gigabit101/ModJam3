package vswe.stevesfactory.ui.manager.menu;

import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import vswe.stevesfactory.api.logic.IClientDataStorage;
import vswe.stevesfactory.api.logic.IProcedure;
import vswe.stevesfactory.library.gui.layout.FlowLayout;
import vswe.stevesfactory.library.gui.widget.Paragraph;
import vswe.stevesfactory.library.gui.widget.RadioController;
import vswe.stevesfactory.library.gui.widget.RadioInput;
import vswe.stevesfactory.ui.manager.editor.Menu;

import java.util.ArrayList;
import java.util.List;

public class RadioOptionsMenu<P extends IProcedure & IClientDataStorage> extends Menu<P> {

    private final String heading;

    private final Paragraph info;
    private final List<RadioInput> options;

    // For keeping data from constructor to #onInitialAttach only, will dropped after attach!
    private String[] names;

    public RadioOptionsMenu(
            String heading,
            String info,
            List<Pair<Runnable, String>> optionSettings,
            int selectedOption
    ) {
        this.heading = heading;

        this.info = new Paragraph(this.getWidth(), 0, new ArrayList<>());
        this.info.setFitContents(true);
        this.info.getTextRenderer().setTextColor(0xff404040);
        this.info.getTextRenderer().setFontHeight(8);
        this.info.addLineSplit(info);

        this.options = new ArrayList<>();
        this.names = new String[optionSettings.size()];
        val controller = new RadioController();
        for (int i = 0; i < optionSettings.size(); i++) {
            val optionSetting = optionSettings.get(i);
            val action = optionSetting.getLeft();
            val name = optionSetting.getRight();

            val button = new RadioInput(controller);
            button.setCheckAction(action);
            button.setCheckedAndUpdate(i == selectedOption);
            this.options.add(button);

            this.names[i] = name;
        }
    }

    @Override
    public void onInitialAttach() {
        super.onInitialAttach();

        addChildren(info);
        for (val option : options) {
            addChildren(option);
        }

        FlowLayout.table(0, 0, this.getWidth(), options);

        for (int i = 0; i < options.size(); i++) {
            val option = options.get(i);
            val name = names[i];
            addChildren(option.makeLabel().text(name));
        }
        info.setY(options.get(options.size() - 1).getYBottom() + 4);

        this.names = null;
    }

    @Override
    public String getHeadingText() {
        return heading;
    }

    @Override
    public List<String> populateErrors(List<String> errors) {
        return errors;
    }
}
