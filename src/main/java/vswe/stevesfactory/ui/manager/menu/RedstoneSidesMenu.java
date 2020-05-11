package vswe.stevesfactory.ui.manager.menu;

import lombok.val;
import net.minecraft.util.Direction;
import vswe.stevesfactory.api.logic.IClientDataStorage;
import vswe.stevesfactory.api.logic.IProcedure;
import vswe.stevesfactory.library.gui.layout.FlowLayout;
import vswe.stevesfactory.library.gui.widget.Checkbox;
import vswe.stevesfactory.logic.procedure.IDirectionTarget;
import vswe.stevesfactory.ui.manager.editor.FlowComponent;
import vswe.stevesfactory.ui.manager.editor.Menu;
import vswe.stevesfactory.utils.Utils;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class RedstoneSidesMenu<P extends IProcedure & IClientDataStorage & IDirectionTarget> extends Menu<P> {

    private final Map<Direction, Checkbox> sides;

    private final String menuName;
    private final int id;

    public RedstoneSidesMenu(int id, String menuName) {
        this.id = id;
        this.menuName = menuName;

        this.sides = new EnumMap<>(Direction.class);
    }

    @Override
    public void onInitialAttach() {
        super.onInitialAttach();

        for (Direction direction : Utils.DIRECTIONS) {
            val box = new Checkbox();
            addChildren(box);
            sides.put(direction, box);
        }
        FlowLayout.table(0, 0, this.getWidth(), sides.values());
        for (val entry : sides.entrySet()) {
            val direction = entry.getKey();
            val box = entry.getValue();
            addChildren(box.makeLabel().translate("gui.sfm." + direction.getName()));
        }
    }

    @Override
    public void onLinkFlowComponent(FlowComponent<P> flowComponent) {
        super.onLinkFlowComponent(flowComponent);
        val procedure = getLinkedProcedure();
        for (val entry : sides.entrySet()) {
            val box = entry.getValue();
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
