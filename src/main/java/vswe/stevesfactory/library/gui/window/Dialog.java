package vswe.stevesfactory.library.gui.window;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import vswe.stevesfactory.library.gui.screen.BackgroundRenderers;
import vswe.stevesfactory.library.gui.widget.IWidget;
import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.layout.FlowLayout;
import vswe.stevesfactory.library.gui.screen.WidgetScreen;
import vswe.stevesfactory.library.gui.widget.TextField;
import vswe.stevesfactory.library.gui.widget.*;
import vswe.stevesfactory.library.gui.widget.box.Box;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.*;

public class Dialog extends AbstractPopupWindow {

    public static Dialog createPrompt(String message, BiConsumer<Integer, String> onConfirm) {
        return createPrompt(message, onConfirm, (b, t) -> {});
    }

    public static Dialog createPrompt(String message, BiConsumer<Integer, String> onConfirm, BiConsumer<Integer, String> onCancel) {
        return createPrompt(message, "", onConfirm, onCancel);
    }

    public static Dialog createPrompt(String message, String defaultText, BiConsumer<Integer, String> onConfirm) {
        return createPrompt(message, defaultText, onConfirm, (b, t) -> {});
    }

    public static Dialog createPrompt(String message, String defaultText, BiConsumer<Integer, String> onConfirm, BiConsumer<Integer, String> onCancel) {
        return createPrompt(message, defaultText, "gui.sfm.ok", "gui.sfm.cancel", onConfirm, onCancel);
    }

    public static Dialog createPrompt(String message, String defaultText, String confirm, String cancel, BiConsumer<Integer, String> onConfirm, BiConsumer<Integer, String> onCancel) {
        Dialog dialog = dialogue(message);

        TextField inputBox = new TextField(0, 0, 0, 16).setText(defaultText);
        dialog.insertBeforeButtons(inputBox);
        dialog.onPostReflow = inputBox::expandHorizontally;

        dialog.insertBeforeButtons(new Spacer(0, 4));

        dialog.buttons.addChildren(TextButton.of(confirm, b -> onConfirm.accept(b, inputBox.getText())));
        dialog.bindRemoveSelf2LastButton();
        dialog.buttons.addChildren(TextButton.of(cancel, b -> onCancel.accept(b, inputBox.getText())));
        dialog.bindRemoveSelf2LastButton();

        dialog.reflow();
        dialog.centralize();
        dialog.setFocusedWidget(inputBox);
        return dialog;
    }

    public static Dialog createBiSelectionDialog(String message, IntConsumer onConfirm) {
        return createBiSelectionDialog(message, onConfirm, TextButton.DUMMY);
    }

    public static Dialog createBiSelectionDialog(String message, IntConsumer onConfirm, IntConsumer onCancel) {
        return createBiSelectionDialog(message, "gui.sfm.ok", "gui.sfm.cancel", onConfirm, onCancel);
    }

    public static Dialog createBiSelectionDialog(String message, String confirm, String cancel, IntConsumer onConfirm, IntConsumer onCancel) {
        Dialog dialog = dialogue(message);

        dialog.buttons.addChildren(TextButton.of(confirm, onConfirm));
        dialog.bindRemoveSelf2LastButton();
        dialog.buttons.addChildren(TextButton.of(cancel, onCancel));
        dialog.bindRemoveSelf2LastButton();

        dialog.reflow();
        dialog.centralize();
        return dialog;
    }

    public static Dialog createDialog(String message) {
        return createDialog(message, TextButton.DUMMY);
    }

    public static Dialog createDialog(String message, IntConsumer onConfirm) {
        return createDialog(message, "gui.sfm.ok", onConfirm);
    }

    public static Dialog createDialog(String message, String ok, IntConsumer onConfirm) {
        Dialog dialog = dialogue(message);

        dialog.buttons.addChildren(TextButton.of(ok, onConfirm));
        dialog.bindRemoveSelf2LastButton();

        dialog.reflow();
        dialog.centralize();
        return dialog;
    }

    private static Dialog dialogue(String message) {
        Dialog dialog = new Dialog();
        dialog.insertBeforeMessage(new Spacer(0, 5));
        dialog.messageBox.addTranslatedLineSplit(160, message);
        return dialog;
    }

    public static final Consumer<Dialog> VANILLA_STYLE_RENDERER = d -> {
        GlStateManager.enableAlphaTest();
        BackgroundRenderers.drawVanillaStyle(d.position.x, d.position.y, d.border.width, d.border.height, 0F);
    };
    public static final int VANILLA_STYLE_BORDER_SIZE = 4;

    public static final Consumer<Dialog> FLAT_STYLE_RENDERER = d -> {
        GlStateManager.disableAlphaTest();
        BackgroundRenderers.drawFlatStyle(d.position.x, d.position.y, d.border.width, d.border.height, 0F);
        GlStateManager.enableAlphaTest();
    };
    public static final int FLAT_STYLE_BORDER_SIZE = 2 + 1;

    private Consumer<Dialog> backgroundRenderer;
    private int borderSize;

    private TextList messageBox;
    private Box<TextButton> buttons;
    private List<AbstractWidget> children;

    public Runnable onPreReflow = () -> {};
    public Runnable onPostReflow = () -> {};

    public Dialog() {
        this.messageBox = new TextList(10, 10, new ArrayList<>());
        this.messageBox.setFitContents(true);
        this.buttons = new Box<TextButton>(0, 0, 10, 10)
                .setLayout(b -> {
                    int x = 0;
                    for (TextButton button : b) {
                        button.setLocation(x, 0);
                        x += button.getWidth() + 2;
                    }
                });
        this.children = new ArrayList<>();
        {
            children.add(messageBox);
            children.add(buttons);
        }
        this.useVanillaBorders();

        for (AbstractWidget child : children) {
            child.setWindow(this);
        }
    }

    @Override
    public void render(int mouseX, int mouseY, float particleTicks) {
        RenderEventDispatcher.onPreRender(this, mouseX, mouseY);
        backgroundRenderer.accept(this);
        renderChildren(mouseX, mouseY, particleTicks);
        RenderEventDispatcher.onPostRender(this, mouseX, mouseY);
    }

    public void reflow() {
        onPreReflow.run();
        messageBox.expandHorizontally();
        buttons.reflow();
        buttons.adjustMinContent();

        FlowLayout.reflow(children);

        updateDimensions();
        updatePosition();
        onPostReflow.run();
    }

    private void updateDimensions() {
        int rightmost = 0;
        int bottommost = 0;
        for (IWidget child : children) {
            int right = child.getX() + child.getWidth();
            int bottom = child.getY() + child.getHeight();
            if (right > rightmost) {
                rightmost = right;
            }
            if (bottom > bottommost) {
                bottommost = bottom;
            }
        }
        setContents(rightmost, bottommost);
    }

    public TextList getMessageBox() {
        return messageBox;
    }

    public Box<TextButton> getButtons() {
        return buttons;
    }

    public void insertBeforeMessage(AbstractWidget widget) {
        widget.setWindow(this);
        children.add(0, widget);
    }

    public void insertBeforeButtons(AbstractWidget widget) {
        widget.setWindow(this);
        children.add(children.size() - 1, widget);
    }

    public void appendChild(AbstractWidget widget) {
        widget.setWindow(this);
        children.add(widget);
    }

    @Override
    public Dimension getBorder() {
        return border;
    }

    @Override
    public int getBorderSize() {
        return borderSize;
    }

    @Override
    public Dimension getContentDimensions() {
        return contents;
    }

    @Override
    public List<? extends IWidget> getChildren() {
        return children;
    }

    @Override
    public Point getPosition() {
        return position;
    }

    public void setStyle(Consumer<Dialog> renderer, int borderSize) {
        this.backgroundRenderer = renderer;
        this.borderSize = borderSize;
        reflow();
    }

    public void useFlatBorders() {
        setStyle(FLAT_STYLE_RENDERER, FLAT_STYLE_BORDER_SIZE);
    }

    public void useVanillaBorders() {
        setStyle(VANILLA_STYLE_RENDERER, VANILLA_STYLE_BORDER_SIZE);
    }

    public void bindRemoveSelf(int buttonID) {
        TextButton button = buttons.getChildren().get(buttonID);
        if (button.hasClickAction()) {
            IntConsumer oldAction = button.onClick;
            button.onClick = b -> {
                alive = false;
                oldAction.accept(b);
            };
        } else {
            button.onClick = b -> alive = false;
        }
    }

    public void bindRemoveSelf2LastButton() {
        bindRemoveSelf(buttons.getChildren().size() - 1);
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean tryAddSelfToActiveGUI() {
        if (Minecraft.getInstance().currentScreen instanceof WidgetScreen) {
            WidgetScreen.getCurrentScreen().addPopupWindow(this);
            return true;
        }
        return false;
    }
}
