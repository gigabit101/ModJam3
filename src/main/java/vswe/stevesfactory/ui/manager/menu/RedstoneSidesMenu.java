package vswe.stevesfactory.ui.manager.menu;

import net.minecraft.util.Direction;
import vswe.stevesfactory.api.logic.IClientDataStorage;
import vswe.stevesfactory.api.logic.IProcedure;
import vswe.stevesfactory.library.gui.layout.FlowLayout;
import vswe.stevesfactory.library.gui.widget.*;
import vswe.stevesfactory.logic.procedure.IDirectionTarget;
import vswe.stevesfactory.ui.manager.editor.FlowComponent;
import vswe.stevesfactory.ui.manager.editor.Menu;
import vswe.stevesfactory.utils.Utils;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;

public class RedstoneSidesMenu<P extends IProcedure & IClientDataStorage & IDirectionTarget> extends Menu<P> {

    private final RadioInput firstOption, secondOption;
    private final Label firstOptionLabel, secondOptionLabel;
    private final Paragraph info;
    private final Map<Direction, Checkbox> sides;

    private final String menuName;
    private final int id;

    // TODO split into two menus
    public RedstoneSidesMenu(int id, BooleanSupplier firstOptionGetter, Runnable firstOptionSetter, String firstOptionName, BooleanSupplier secondOptionGetter, Runnable secondOptionSetter, String secondOptionName, String menuName, String infoText) {
        this.id = id;
        this.menuName = menuName;

        RadioController filterTypeController = new RadioController();
        firstOption = new RadioInput(filterTypeController);
        secondOption = new RadioInput(filterTypeController);
        int y = HEADING_BOX.getPortionHeight() + 4;
        firstOption.setLocation(4, y);
        firstOption.check(firstOptionGetter.getAsBoolean());
        firstOption.setCheckAction(firstOptionSetter);
        secondOption.setLocation(getWidth() / 2, y);
        secondOption.check(secondOptionGetter.getAsBoolean());
        secondOption.setCheckAction(secondOptionSetter);

        // TODO parent
        firstOptionLabel = firstOption.makeLabel().text(firstOptionName);
        secondOptionLabel = secondOption.makeLabel().text(secondOptionName);

        sides = new EnumMap<>(Direction.class);
        for (Direction direction : Utils.DIRECTIONS) {
            Checkbox box = new Checkbox();
            addChildren(box);
            // TODO label pos
            addChildren(box.makeLabel().translate("gui.sfm." + direction.getName()));
            sides.put(direction, box);
        }
        FlowLayout.table(4, HEADING_BOX.getPortionHeight() + 30, getWidth(), sides);

        info = new Paragraph(getWidth() - 4 * 2, 16, new ArrayList<>());
        info.getTextRenderer().setFontHeight(6);
        info.addLineSplit(infoText);
        info.setLocation(4, firstOption.getYBottom() + 2);
    }

    @Override
    public void onInitialAttach() {
        super.onInitialAttach();

        addChildren(firstOption);
        addChildren(secondOption);
        // TODO label pos
        addChildren(firstOptionLabel);
        addChildren(secondOptionLabel);
        addChildren(info);
    }

    @Override
    public void onLinkFlowComponent(FlowComponent<P> flowComponent) {
        super.onLinkFlowComponent(flowComponent);
        P procedure = getLinkedProcedure();
        for (Map.Entry<Direction, Checkbox> entry : sides.entrySet()) {
            Checkbox box = entry.getValue();
            box.setChecked(procedure.isEnabled(id, entry.getKey()));
            box.onStateChange = b -> procedure.setEnabled(id, entry.getKey(), b);
        }
    }

    @Override
    public String getHeadingText() {
        return menuName;
    }

    @Override
    public List<String> populateErrors(List<String> errors) {
        return errors;
    }

}
