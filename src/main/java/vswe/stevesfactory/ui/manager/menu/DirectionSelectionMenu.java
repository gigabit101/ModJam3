package vswe.stevesfactory.ui.manager.menu;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.Direction;
import vswe.stevesfactory.api.logic.IClientDataStorage;
import vswe.stevesfactory.api.logic.IProcedure;
import vswe.stevesfactory.library.gui.Render2D;
import vswe.stevesfactory.logic.procedure.IDirectionTarget;
import vswe.stevesfactory.ui.manager.editor.FlowComponent;
import vswe.stevesfactory.ui.manager.editor.Menu;

import java.util.List;

public class DirectionSelectionMenu<P extends IDirectionTarget & IProcedure & IClientDataStorage> extends Menu<P> {

    private final int id;
    private final String name;
    private final String errorMessage;

    // TODO refactor with EnumMap
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
        down.onStateChanged = b -> getLinkedProcedure().setEnabled(id, Direction.DOWN, b);
        up = new DirectionButton(Direction.UP);
        up.onStateChanged = b -> getLinkedProcedure().setEnabled(id, Direction.UP, b);
        north = new DirectionButton(Direction.NORTH);
        north.onStateChanged = b -> getLinkedProcedure().setEnabled(id, Direction.NORTH, b);
        south = new DirectionButton(Direction.SOUTH);
        south.onStateChanged = b -> getLinkedProcedure().setEnabled(id, Direction.SOUTH, b);
        east = new DirectionButton(Direction.EAST);
        east.onStateChanged = b -> getLinkedProcedure().setEnabled(id, Direction.EAST, b);
        west = new DirectionButton(Direction.WEST);
        west.onStateChanged = b -> getLinkedProcedure().setEnabled(id, Direction.WEST, b);
        activationButton = new ActivationButton(down);

        final int y = HEADING_BOX.getPortionHeight() + 4;

        down.setLocation(2, y);
        north.setLocation(down.getX(), down.getY() + down.getHeight() + 6);
        west.setLocation(down.getX(), north.getY() + north.getHeight() + 6);

        up.setLocation(getWidth() - 2 - up.getWidth(), y);
        south.setLocation(up.getX(), up.getY() + up.getHeight() + 6);
        east.setLocation(up.getX(), south.getY() + south.getHeight() + 6);

        int leftMid = down.getX() + down.getWidth();
        int rightMid = up.getX();
        int x = Render2D.computeCenterX(leftMid, rightMid, activationButton.getFullWidth());
        activationButton.setLocation(x, y);
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
        for (Direction direction : getLinkedProcedure().getDirections(id)) {
            switch (direction) {
                case DOWN:
                    down.setSelected(true);
                    break;
                case UP:
                    up.setSelected(true);
                    break;
                case NORTH:
                    north.setSelected(true);
                    break;
                case SOUTH:
                    south.setSelected(true);
                    break;
                case WEST:
                    west.setSelected(true);
                    break;
                case EAST:
                    east.setSelected(true);
                    break;
            }
        }
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
