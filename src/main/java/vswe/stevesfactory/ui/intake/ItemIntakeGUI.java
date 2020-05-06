package vswe.stevesfactory.ui.intake;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;
import vswe.stevesfactory.library.gui.Render2D;
import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.layout.FlowLayout;
import vswe.stevesfactory.library.gui.screen.DisplayListCaches;
import vswe.stevesfactory.library.gui.screen.WidgetScreen;
import vswe.stevesfactory.library.gui.widget.*;
import vswe.stevesfactory.library.gui.widget.button.ColoredTextButton;
import vswe.stevesfactory.library.gui.window.AbstractWindow;
import vswe.stevesfactory.network.NetworkHandler;
import vswe.stevesfactory.network.PacketSyncIntakeData;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.lwjgl.opengl.GL11.glCallList;

public class ItemIntakeGUI extends WidgetScreen<ItemIntakeContainer> {

    public ItemIntakeGUI(ItemIntakeContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    protected void init() {
        super.init();
        setPrimaryWindow(new PrimaryWindow());
    }

    @Override
    public void removed() {
        // Server data
        NetworkHandler.sendToServer(new PacketSyncIntakeData(
                Objects.requireNonNull(container.intake.getWorld()).getDimension().getType(),
                container.intake.getPos(),
                container.intake.getRadius(), container.intake.isRendering(), container.intake.getMode()));

        super.removed();
    }

    public class PrimaryWindow extends AbstractWindow {

        public static final int WIDTH = 180;
        public static final int HEIGHT = 120;

        private int backgroundDL;

        private NumberField<Integer> radius;
        private ColoredTextButton mode;
        private Checkbox rendering;
        private List<IWidget> children = new ArrayList<>();

        public PrimaryWindow() {
            setContents(WIDTH, HEIGHT);
            updatePosAndDL();

            radius = NumberField.integerFieldRanged(33, 12, 1, 0, container.intake.getMaximumRadius());
            radius.attachWindow(this);
            radius.setValue(container.intake.getRadius());
            radius.setBackgroundStyle(TextField.BackgroundStyle.RED_OUTLINE);
            radius.onValueUpdated = container.intake::setRadius;
            mode = ColoredTextButton.of(container.intake.getMode().statusTranslationKey);
            mode.attachWindow(this);
            mode.setClickAction(b -> {
                container.intake.cycleMode();
                mode.setText(I18n.format(container.intake.getMode().statusTranslationKey));
            });
            rendering = new Checkbox();
            rendering.setDimensions(8, 8);
            rendering.attachWindow(this);
            rendering.setChecked(container.intake.isRendering());
            rendering.onStateChange = container.intake::setRendering;
            Label renderingLabel = new Label(rendering).translate("gui.sfm.ItemIntake.RenderWorkingArea");

            ColoredTextButton btnSaveData = ColoredTextButton.of("gui.sfm.ItemIntake.SaveData");
            btnSaveData.attachWindow(this);
            btnSaveData.setClickAction(b -> onClose());
            btnSaveData.setWidth(getContentWidth());

            children.add(radius);
            children.add(mode);
            children.add(rendering);
            children.add(btnSaveData);
            children.add(renderingLabel);
            FlowLayout.vertical(children, 0, 0, 2);

            btnSaveData.alignBottom(getContentHeight());
        }

        private void updatePosAndDL() {
            centralize();
            backgroundDL = DisplayListCaches.createVanillaStyleBackground(getX(), getY(), getWidth(), getHeight());
        }

        @Override
        public int getBorderSize() {
            return 4;
        }

        @Override
        public List<? extends IWidget> getChildren() {
            return children;
        }

        @Override
        public void render(int mouseX, int mouseY, float partialTicks) {
            RenderEventDispatcher.onPreRender(this, mouseX, mouseY);
            glCallList(backgroundDL);
            renderChildren(mouseX, mouseY, partialTicks);
            Render2D.renderVerticallyCenteredText(
                    I18n.format("gui.sfm.ItemIntake.Radius"),
                    radius.getAbsoluteXRight() + 2,
                    radius.getAbsoluteY(), radius.getAbsoluteYBottom(),
                    0F,
                    0xff404040);
            RenderEventDispatcher.onPostRender(this, mouseX, mouseY);
        }
    }
}
