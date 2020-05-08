package vswe.stevesfactory.library.gui.window;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import vswe.stevesfactory.Config;
import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.layout.FlowLayout;
import vswe.stevesfactory.library.gui.screen.BackgroundRenderers;
import vswe.stevesfactory.library.gui.screen.WidgetScreen;
import vswe.stevesfactory.library.gui.widget.AbstractWidget;
import vswe.stevesfactory.library.gui.widget.IWidget;
import vswe.stevesfactory.library.gui.widget.Paragraph;
import vswe.stevesfactory.library.gui.widget.TextField;
import vswe.stevesfactory.library.gui.widget.button.ColoredTextButton;
import vswe.stevesfactory.library.gui.widget.button.IButton;
import vswe.stevesfactory.library.gui.widget.panel.Panel;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.Supplier;

public class Dialog extends AbstractPopupWindow {

    public static Dialog createPrompt(
            String message,
            Supplier<? extends TextField> fieldProvider,
            String confirm, String cancel,
            BiConsumer<Integer, String> onConfirm,
            BiConsumer<Integer, String> onCancel
    ) {
        Dialog dialog = dialog(message);

        TextField inputBox = fieldProvider.get();
        inputBox.setBorderBottom(4);
        dialog.insertBeforeButtons(inputBox);
        dialog.onPostReflow = inputBox::expandHorizontally;

        dialog.buttons.addChildren(ColoredTextButton.of(I18n.format(confirm), b1 -> onConfirm.accept(b1, inputBox.getText())));
        dialog.bindRemoveSelf2LastButton();
        dialog.buttons.addChildren(ColoredTextButton.of(I18n.format(cancel), b -> onCancel.accept(b, inputBox.getText())));
        dialog.bindRemoveSelf2LastButton();

        dialog.reflow();
        dialog.centralize();
        dialog.setFocusedWidget(inputBox);
        return dialog;
    }

    public static Dialog createBiSelectionDialog(String message, IntConsumer onConfirm) {
        return createBiSelectionDialog(message, onConfirm, IButton.DUMMY);
    }

    public static Dialog createBiSelectionDialog(String message, IntConsumer onConfirm, IntConsumer onCancel) {
        return createBiSelectionDialog(message, I18n.format("gui.sfm.ok"), I18n.format("gui.sfm.cancel"), onConfirm, onCancel);
    }

    public static Dialog createBiSelectionDialog(String message, String confirm, String cancel, IntConsumer onConfirm, IntConsumer onCancel) {
        Dialog dialog = dialog(message);

        dialog.buttons.addChildren(ColoredTextButton.of(confirm, onConfirm));
        dialog.bindRemoveSelf2LastButton();
        dialog.buttons.addChildren(ColoredTextButton.of(cancel, onCancel));
        dialog.bindRemoveSelf2LastButton();

        dialog.reflow();
        dialog.centralize();
        return dialog;
    }

    public static Dialog createDialog(String message) {
        return createDialog(message, IButton.DUMMY);
    }

    public static Dialog createDialog(String message, IntConsumer onConfirm) {
        return createDialog(message, I18n.format("gui.sfm.ok"), onConfirm);
    }

    public static Dialog createDialog(String message, String ok, IntConsumer onConfirm) {
        Dialog dialog = dialog(message);

        dialog.buttons.addChildren(ColoredTextButton.of(ok, onConfirm));
        dialog.bindRemoveSelf2LastButton();

        dialog.reflow();
        dialog.centralize();
        return dialog;
    }

    private static Dialog dialog(String message) {
        Dialog dialog = new Dialog();
        dialog.messageBox.setBorderTop(5);
        dialog.messageBox.addLineSplit(Config.CLIENT.dialogMessageMaxWidth.get(), message);
        return dialog;
    }

    public static final Consumer<Dialog> VANILLA_STYLE_RENDERER = d -> {
        RenderSystem.enableAlphaTest();
        BackgroundRenderers.drawVanillaStyle4x4(d.getX(), d.getY(), d.getWidth(), d.getHeight(), d.getZLevel());
    };
    public static final int VANILLA_STYLE_BORDER_SIZE = 4;

    public static final Consumer<Dialog> FLAT_STYLE_RENDERER = d -> {
        RenderSystem.disableAlphaTest();
        BackgroundRenderers.drawFlatStyle(d.getX(), d.getY(), d.getWidth(), d.getHeight(), d.getZLevel());
        RenderSystem.enableAlphaTest();
    };
    public static final int FLAT_STYLE_BORDER_SIZE = 2 + 1;

    private Consumer<Dialog> backgroundRenderer;
    private int borderSize;

    private final Paragraph messageBox;
    private final Panel<IButton> buttons;
    private final List<AbstractWidget> children;

    public Runnable onPreReflow = () -> {};
    public Runnable onPostReflow = () -> {};

    public Dialog() {
        this.messageBox = new Paragraph(10, 10, new ArrayList<>());
        this.messageBox.setBorders(0);
        this.messageBox.setFitContents(true);
        this.buttons = new Panel<>();
        this.buttons.setDimensions(10, 10);
        this.children = new ArrayList<>();
        children.add(messageBox);
        children.add(buttons);
        for (AbstractWidget child : children) {
            child.attachWindow(this);
        }

        this.useVanillaBorders();
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        RenderEventDispatcher.onPreRender(this, mouseX, mouseY);
        backgroundRenderer.accept(this);
        renderChildren(mouseX, mouseY, partialTicks);
        RenderEventDispatcher.onPostRender(this, mouseX, mouseY);
    }

    public void reflow() {
        onPreReflow.run();

        buttons.adjustMinHeight();

        int widths = buttons.getChildren().stream()
                .mapToInt(IWidget::getFullWidth)
                .sum();
        int margins = (buttons.getChildren().size() - 1) * 2;
        buttons.setWidth(widths + margins);
        FlowLayout.vertical(children, 0, 0, 0);
        updateDimensions();
        updatePosition();

        buttons.expandHorizontally();
        FlowLayout.reverseHorizontal(buttons.getChildren(), buttons.getWidth(), 0, 2);

        onPostReflow.run();
    }

    private void updateDimensions() {
        int rightmost = 0;
        int bottommost = 0;
        for (IWidget child : children) {
            int right = child.getX() + child.getFullHeight();
            int bottom = child.getY() + child.getFullWidth();
            if (right > rightmost) {
                rightmost = right;
            }
            if (bottom > bottommost) {
                bottommost = bottom;
            }
        }
        setContents(rightmost, bottommost);
    }

    public Paragraph getMessageBox() {
        return messageBox;
    }

    public Panel<IButton> getButtons() {
        return buttons;
    }

    public void insertBeforeMessage(AbstractWidget widget) {
        widget.attachWindow(this);
        children.add(0, widget);
    }

    public void insertBeforeButtons(AbstractWidget widget) {
        widget.attachWindow(this);
        children.add(children.size() - 1, widget);
    }

    public void appendChild(AbstractWidget widget) {
        widget.attachWindow(this);
        children.add(widget);
    }

    @Override
    public int getBorderSize() {
        return borderSize;
    }

    @Override
    public List<? extends IWidget> getChildren() {
        return children;
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
        IButton button = buttons.getChildren().get(buttonID);
        if (button.hasClickAction()) {
            IntConsumer oldAction = button.getClickAction();
            button.setClickAction(b -> {
                discard();
                oldAction.accept(b);
            });
        } else {
            button.setClickAction(b -> discard());
        }
    }

    public void bindRemoveSelf2LastButton() {
        bindRemoveSelf(buttons.getChildren().size() - 1);
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean tryAddSelfToActiveGUI() {
        if (Minecraft.getInstance().currentScreen instanceof WidgetScreen<?>) {
            addSelfTo(WidgetScreen.assertActive());
            return true;
        }
        return false;
    }

    public void addSelfTo(WidgetScreen<?> gui) {
        gui.defer(() -> gui.addPopupWindow(this));
    }
}
