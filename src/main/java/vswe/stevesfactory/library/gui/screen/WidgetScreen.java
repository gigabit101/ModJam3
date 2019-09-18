package vswe.stevesfactory.library.gui.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.client.config.GuiUtils;
import org.lwjgl.glfw.GLFW;
import vswe.stevesfactory.StevesFactoryManager;
import vswe.stevesfactory.library.collections.CompositeUnmodifiableList;
import vswe.stevesfactory.library.gui.window.IWindow;
import vswe.stevesfactory.library.gui.TextureWrapper;
import vswe.stevesfactory.library.gui.debug.Inspections;
import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.window.IPopupWindow;

import java.util.*;
import java.util.function.Consumer;

public abstract class WidgetScreen extends Screen implements IGuiEventListener {

    public static final TextureWrapper ITEM_SLOT = TextureWrapper.ofFlowComponent(0, 106, 18, 18);

    public static WidgetScreen getCurrentScreen() {
        return (WidgetScreen) Minecraft.getInstance().currentScreen;
    }

    public static int scaledWidth() {
        return Minecraft.getInstance().mainWindow.getScaledWidth();
    }

    public static int scaledHeight() {
        return Minecraft.getInstance().mainWindow.getScaledHeight();
    }

    private IWindow primaryWindow;
    private List<IWindow> regularWindows = new ArrayList<>();
    private List<IPopupWindow> popupWindows = new ArrayList<>();
    private Collection<IWindow> windows;

    private final Queue<Consumer<WidgetScreen>> tasks = new ArrayDeque<>();

    private final WidgetTreeInspections inspectionHandler = new WidgetTreeInspections();

    private final ArrayList<String> cachedHoveringTextList = new ArrayList<>();
    private List<String> hoveringText;
    private int hoveringTextX, hoveringTextY;

    protected WidgetScreen(ITextComponent title) {
        super(title);
        // Safe downwards erasure cast
        @SuppressWarnings("unchecked") List<IWindow> popupWindows = (List<IWindow>) (List<? extends IWindow>) this.popupWindows;
        windows = CompositeUnmodifiableList.of(regularWindows, popupWindows);
    }

    @Override
    protected void init() {
        StevesFactoryManager.logger.trace("(Re)initialized widget-based GUI {}", this);
        primaryWindow = null;
        regularWindows.clear();
        popupWindows.clear();
        RenderEventDispatcher.listeners.put(Inspections.class, inspectionHandler);
    }

    @Override
    public void tick() {
        while (!tasks.isEmpty()) {
            tasks.remove().accept(this);
        }

        popupWindows.removeIf(popup -> {
            if (popup.shouldDiscard()) {
                popup.onRemoved();
                return true;
            }
            return false;
        });

        float particleTicks = Minecraft.getInstance().getRenderPartialTicks();
        for (IWindow window : windows) {
            window.update(particleTicks);
        }
        primaryWindow.update(particleTicks);
    }

    protected void initializePrimaryWindow(IWindow primaryWindow) {
        if (this.primaryWindow == null) {
            this.primaryWindow = primaryWindow;
        } else {
            throw new IllegalStateException("Already initialized the primary window " + this.primaryWindow);
        }
    }

    public IWindow getPrimaryWindow() {
        return primaryWindow;
    }

    @Override
    public void render(int mouseX, int mouseY, float particleTicks) {
        // Dark transparent overlay
        renderBackground();

        inspectionHandler.startCycle();
        primaryWindow.render(mouseX, mouseY, particleTicks);
        for (IWindow window : windows) {
            window.render(mouseX, mouseY, particleTicks);
        }
        inspectionHandler.endCycle();

        // This should do nothing because we are not adding vanilla buttons
        super.render(mouseX, mouseY, particleTicks);

        if (hoveringText != null) {
            GuiUtils.drawHoveringText(hoveringText, hoveringTextX, hoveringTextY, scaledWidth(), scaledHeight(), Integer.MAX_VALUE, font);
            hoveringText = null;
        }
    }

    public void addWindow(IWindow window) {
        regularWindows.add(window);
    }

    public void clearWindows() {
        regularWindows.forEach(IWindow::onRemoved);
        regularWindows.clear();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (windows.stream().anyMatch(window -> window.mouseClicked(mouseX, mouseY, button))) {
            return true;
        } else {
            return primaryWindow.mouseClicked(mouseX, mouseY, button);
        }
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (windows.stream().anyMatch(window -> window.mouseReleased(mouseX, mouseY, button))) {
            return true;
        } else {
            return primaryWindow.mouseReleased(mouseX, mouseY, button);
        }
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragAmountX, double dragAmountY) {
        if (windows.stream().anyMatch(window -> window.mouseDragged(mouseX, mouseY, button, dragAmountX, dragAmountY))) {
            return true;
        } else {
            return primaryWindow.mouseDragged(mouseX, mouseY, button, dragAmountX, dragAmountY);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amountScrolled) {
        if (windows.stream().anyMatch(window -> window.mouseScrolled(mouseX, mouseY, amountScrolled))) {
            return true;
        } else {
            return primaryWindow.mouseScrolled(mouseX, mouseY, amountScrolled);
        }
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        for (IWindow window : windows) {
            window.mouseMoved(mouseX, mouseY);
        }
        primaryWindow.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (windows.stream().anyMatch(window -> window.keyPressed(keyCode, scanCode, modifiers))) {
            return true;
        } else if (primaryWindow.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }

        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_E) {
            this.onClose();
            Minecraft.getInstance().player.closeScreen();
            return true;
        }
        return false;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (windows.stream().anyMatch(window -> window.keyReleased(keyCode, scanCode, modifiers))) {
            return true;
        } else {
            return primaryWindow.keyReleased(keyCode, scanCode, modifiers);
        }
    }

    @Override
    public boolean charTyped(char charTyped, int keyCode) {
        if (windows.stream().anyMatch(window -> window.charTyped(charTyped, keyCode))) {
            return true;
        } else {
            return primaryWindow.charTyped(charTyped, keyCode);
        }
    }

    @Override
    public void onClose() {
        super.onClose();
        for (IWindow window : windows) {
            window.onRemoved();
        }
        primaryWindow.onRemoved();
    }

    public void scheduleTask(Consumer<WidgetScreen> task) {
        tasks.add(task);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public void setHoveringText(List<String> hoveringText, int x, int y) {
        this.hoveringText = hoveringText;
        this.hoveringTextX = x;
        this.hoveringTextY = y;
    }

    public void setHoveringText(ItemStack stack, int x, int y) {
        setHoveringText(getTooltipFromItem(stack), x, y);
    }

    public void setHoveringText(String hoveringText, int x, int y) {
        cachedHoveringTextList.clear();
        cachedHoveringTextList.add(hoveringText);
        setHoveringText(cachedHoveringTextList, x, y);
    }

    public void addPopupWindow(IPopupWindow popup) {
        popupWindows.add(popup);
    }

    public void removePopupWindow(IPopupWindow popup) {
        popupWindows.remove(popup);
        popup.onRemoved();
    }
}
