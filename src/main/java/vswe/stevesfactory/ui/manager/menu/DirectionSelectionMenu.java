package vswe.stevesfactory.ui.manager.menu;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.Direction;
import vswe.stevesfactory.api.logic.IProcedure;
import vswe.stevesfactory.api.logic.IProcedureClientData;
import vswe.stevesfactory.logic.procedure.IDirectionTarget;
import vswe.stevesfactory.ui.manager.editor.FlowComponent;
import vswe.stevesfactory.ui.manager.editor.Menu;
import vswe.stevesfactory.utils.RenderingHelper;

import java.util.List;

public class DirectionSelectionMenu<P extends IDirectionTarget & IProcedure & IProcedureClientData> extends Menu<P> {

    private final int id;
    private final String name;

    private DirectionButton down, up, north, south, east, west;
    private ActivationButton activationButton;

    public DirectionSelectionMenu(int id) {
        this(id, I18n.format("gui.sfm.Menu.TargetSides"));
    }

    public DirectionSelectionMenu(int id, String name) {
        this.id = id;
        this.name = name;

        down = new DirectionButton(Direction.DOWN);
        up = new DirectionButton(Direction.UP);
        north = new DirectionButton(Direction.NORTH);
        south = new DirectionButton(Direction.SOUTH);
        east = new DirectionButton(Direction.EAST);
        west = new DirectionButton(Direction.WEST);
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
        activationButton.setLocation(RenderingHelper.getXForAlignedCenter(leftMid, rightMid, activationButton.getWidth()), y);
        activationButton.setEditingState(false);

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
        for (Direction direction : flowComponent.getLinkedProcedure().getDirections(id)) {
            switch (direction) {
                case DOWN:
                    down.selected = true;
                    break;
                case UP:
                    up.selected = true;
                    break;
                case NORTH:
                    north.selected = true;
                    break;
                case SOUTH:
                    south.selected = true;
                    break;
                case WEST:
                    west.selected = true;
                    break;
                case EAST:
                    east.selected = true;
                    break;
            }
        }
    }

    @Override
    protected void updateData() {
        List<Direction> directions = getLinkedProcedure().getDirections(id);
        directions.clear();
        if (down.selected) {
            directions.add(Direction.DOWN);
        }
        if (up.selected) {
            directions.add(Direction.UP);
        }
        if (north.selected) {
            directions.add(Direction.NORTH);
        }
        if (south.selected) {
            directions.add(Direction.SOUTH);
        }
        if (east.selected) {
            directions.add(Direction.EAST);
        }
        if (west.selected) {
            directions.add(Direction.WEST);
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
    public void render(int mouseX, int mouseY, float particleTicks) {
        GlStateManager.color3f(1F, 1F, 1F);
        GlStateManager.enableTexture();
        super.render(mouseX, mouseY, particleTicks);
    }

    @Override
    public String getHeadingText() {
        return name;
    }
}
