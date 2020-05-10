package vswe.stevesfactory.ui.manager.menu;

import com.mojang.blaze3d.systems.RenderSystem;
import lombok.val;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.Direction;
import vswe.stevesfactory.api.logic.IClientDataStorage;
import vswe.stevesfactory.api.logic.IProcedure;
import vswe.stevesfactory.logic.procedure.IDirectionTarget;
import vswe.stevesfactory.ui.manager.editor.FlowComponent;
import vswe.stevesfactory.ui.manager.editor.Menu;

import java.util.List;

public class DirectionSelectionMenu<P extends IDirectionTarget & IProcedure & IClientDataStorage> extends Menu<P> {

    private final int id;
    private final String name;
    private final String errorMessage;

    private final DirectionButton down, up, north, south, east, west;
    private final ActivationButton activationButton;

    public DirectionSelectionMenu(int id) {
        this(id, I18n.format("menu.sfm.TargetSides"), I18n.format("error.sfm.ItemIO.NoTarget"));
    }

    public DirectionSelectionMenu(int id, String name, String errorMessage) {
        this.id = id;
        this.name = name;
        this.errorMessage = errorMessage;

        down = new DirectionButton(Direction.DOWN);
        up = new DirectionButton(Direction.UP);
        north = new DirectionButton(Direction.NORTH);
        south = new DirectionButton(Direction.SOUTH);
        east = new DirectionButton(Direction.EAST);
        west = new DirectionButton(Direction.WEST);
        activationButton = new ActivationButton(down);

        down.setLocation(0, 0);
        up.setLocation(getWidth() - 2 - up.getWidth(), 0);
        north.setLocation(down.getX(), down.getY() + down.getHeight() + 6);
        south.setLocation(up.getX(), up.getY() + up.getHeight() + 6);
        west.setLocation(down.getX(), north.getY() + north.getHeight() + 6);
        east.setLocation(up.getX(), south.getY() + south.getHeight() + 6);

        int leftMid = down.getXRight();
        int rightMid = up.getX();
        activationButton.alignCenterX(leftMid, rightMid);
        activationButton.setY(0);
        activationButton.setEditingState(false);
    }

    @Override
    public void onInitialAttach() {
        super.onInitialAttach();

        addChildren(down);
        addChildren(up);
        addChildren(north);
        addChildren(south);
        addChildren(east);
        addChildren(west);
        addChildren(activationButton);
    }

    @Override
    public void onLinkFlowComponent(FlowComponent<P> flowComponent) {
        super.onLinkFlowComponent(flowComponent);
        val proc = flowComponent.getProcedure();

        down.onStateChanged = b -> proc.setEnabled(id, Direction.DOWN, b);
        up.onStateChanged = b -> proc.setEnabled(id, Direction.UP, b);
        north.onStateChanged = b -> proc.setEnabled(id, Direction.NORTH, b);
        south.onStateChanged = b -> proc.setEnabled(id, Direction.SOUTH, b);
        east.onStateChanged = b -> proc.setEnabled(id, Direction.EAST, b);
        west.onStateChanged = b -> proc.setEnabled(id, Direction.WEST, b);

        val directions = proc.getDirections(id);
        down.setSelected(directions.contains(Direction.DOWN));
        up.setSelected(directions.contains(Direction.UP));
        north.setSelected(directions.contains(Direction.NORTH));
        south.setSelected(directions.contains(Direction.SOUTH));
        east.setSelected(directions.contains(Direction.EAST));
        west.setSelected(directions.contains(Direction.WEST));
    }

    void clearEditing() {
        activationButton.setEditingState(false);
    }

    void editDirection(DirectionButton button) {
        activationButton.setEditingState(true);
        activationButton.setTarget(button);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        RenderSystem.color3f(1F, 1F, 1F);
        RenderSystem.enableTexture();
        super.render(mouseX, mouseY, partialTicks);
    }

    @Override
    public String getHeadingText() {
        return name;
    }

    @Override
    public List<String> populateErrors(List<String> errors) {
        if (!hasAnythingSelected()) {
            errors.add(errorMessage);
        }
        return errors;
    }

    private boolean hasAnythingSelected() {
        return down.isSelected()
                || up.isSelected()
                || north.isSelected()
                || south.isSelected()
                || east.isSelected()
                || west.isSelected();
    }
}
