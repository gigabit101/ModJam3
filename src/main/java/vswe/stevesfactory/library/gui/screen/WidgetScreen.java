package vswe.stevesfactory.library.gui.screen;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.client.gui.GuiUtils;
import org.apache.commons.lang3.tuple.Triple;
import vswe.stevesfactory.StevesFactoryManager;
import vswe.stevesfactory.library.collections.CompositeCollection;
import vswe.stevesfactory.library.gui.debug.Inspections;
import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.window.IPopupWindow;
import vswe.stevesfactory.library.gui.window.IWindow;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_E;
import static vswe.stevesfactory.library.gui.Render2D.*;

public abstract class WidgetScreen<C extends WidgetContainer> extends ContainerScreen<C> implements IGuiEventListener {

    /**
     * @throws ClassCastException If the current open screen is not a WidgetScreen
     */
    public static WidgetScreen assertActive() {
        return (WidgetScreen) Minecraft.getInstance().currentScreen;
    }

    @Nullable
    public static WidgetScreen activeNullable() {
        Screen screen = Minecraft.getInstance().currentScreen;
        if (screen instanceof WidgetScreen) {
            return (WidgetScreen) screen;
        }
        return null;
    }

    @Nonnull
    public static Optional<WidgetScreen> active() {
        Screen screen = Minecraft.getInstance().currentScreen;
        return Optional.ofNullable(screen instanceof WidgetScreen ? (WidgetScreen) screen : null);
    }

    public static int screenWidth() {
        Screen screen = Minecraft.getInstance().currentScreen;
        if (screen != null) {
            return screen.width;
        }
        return 0;
    }

    public static int screenHeight() {
        Screen screen = Minecraft.getInstance().currentScreen;
        if (screen != null) {
            return screen.height;
        }
        return 0;
    }

    private IWindow primaryWindow;
    private List<IWindow> regularWindows = new ArrayList<>();
    private TreeSet<IPopupWindow> popupWindows = new TreeSet<>();
    private Collection<IWindow> windows;

    private final WidgetTreeInspections inspectionHandler = new WidgetTreeInspections();
    private final Queue<Triple<List<String>, Integer, Integer>> tooltipRenderQueue = new ArrayDeque<>();
    private final Queue<Runnable> taskQueue = new ArrayDeque<>();

    protected WidgetScreen(C container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        // Safe downwards erasure cast
        @SuppressWarnings("unchecked") Collection<IWindow> popupWindowsView = (Collection<IWindow>) (Collection<? extends IWindow>) new DescendingTreeSetBackedUnmodifiableCollection<>(popupWindows);
        windows = new CompositeCollection<>(regularWindows, popupWindowsView);
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
        while (!taskQueue.isEmpty()) {
            taskQueue.remove().run();
        }

        popupWindows.removeIf(popup -> {
            if (popup.shouldDiscard()) {
                popup.onRemoved();
                return true;
            }
            return false;
        });

        float partialTicks = Minecraft.getInstance().getRenderPartialTicks();
        for (IWindow window : windows) {
            window.update(partialTicks);
        }
        primaryWindow.update(partialTicks);
    }

    protected final void setPrimaryWindow(IWindow primaryWindow) {
        Preconditions.checkState(this.primaryWindow == null, "Already initialized the primary window " + this.primaryWindow);
        this.primaryWindow = primaryWindow;
    }

    public IWindow getPrimaryWindow() {
        return primaryWindow;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        // Dark transparent overlay
        renderBackground();

        inspectionHandler.startCycle();
        RenderSystem.enableDepthTest();
        RenderSystem.enableAlphaTest();
        primaryWindow.render(mouseX, mouseY, partialTicks);
        for (IWindow window : regularWindows) {
            window.render(mouseX, mouseY, partialTicks);
        }
        // We want to render things away from the screen first (painter's algorithm)
        RenderSystem.pushMatrix();
        float zOff = CONTEXT_MENU_Z - POPUP_WINDOW_Z;
        for (IPopupWindow window : popupWindows) {
            window.render(mouseX, mouseY, partialTicks);
            RenderSystem.translatef(0F, 0F, zOff);
        }
        RenderSystem.popMatrix();
        RenderSystem.disableDepthTest();
        inspectionHandler.endCycle();

        // This should do nothing because we are not adding vanilla buttons
        super.render(mouseX, mouseY, partialTicks);

        while (!tooltipRenderQueue.isEmpty()) {
            Triple<List<String>, Integer, Integer> entry = tooltipRenderQueue.remove();
            GuiUtils.drawHoveringText(entry.getLeft(), entry.getMiddle(), entry.getRight(), windowWidth(), windowHeight(), Integer.MAX_VALUE, fontRenderer());
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
        boolean captured = false;
        IPopupWindow capturedWindow = null;
        passEvents:
        {
            for (IWindow window : regularWindows) {
                if (window.mouseClicked(mouseX, mouseY, button)) {
                    captured = true;
                    break passEvents;
                }
            }
            for (IPopupWindow window : popupWindows) {
                if (window.mouseClicked(mouseX, mouseY, button)) {
                    capturedWindow = window;
                    captured = true;
                    break passEvents;
                }
            }
        }

        if (capturedWindow != null) {
            raiseWindowToTop(capturedWindow);
        }
        if (captured) {
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
        if (keyCode == GLFW_KEY_E) {
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
    public void removed() {
        for (IWindow window : windows) {
            window.onRemoved();
        }
        primaryWindow.onRemoved();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public void addPopupWindow(IPopupWindow popup) {
        popup.setOrder(nextOrderIndex());
        popupWindows.add(popup);
        popup.onAdded(this);
    }

    public void removePopupWindow(IPopupWindow popup) {
        popupWindows.remove(popup);
        popup.onRemoved();
    }

    public void defer(Runnable task) {
        taskQueue.add(task);
    }

    @SuppressWarnings("SuspiciousNameCombination") // Tuple3 is acting weird
    public void scheduleTooltip(List<String> lines, int x, int y) {
        tooltipRenderQueue.add(Triple.of(lines, x, y));
    }

    public void scheduleTooltip(String line, int x, int y) {
        scheduleTooltip(ImmutableList.of(line), x, y);
    }

    public void scheduleTooltip(ItemStack stack, int x, int y) {
        scheduleTooltip(getTooltipFromItem(stack), x, y);
    }

    private int nextOrderIndex = 0;

    public void raiseWindowToTop(IPopupWindow window) {
        popupWindows.remove(window);
        window.setOrder(nextOrderIndex());
        popupWindows.add(window);
    }

    public int nextOrderIndex() {
        return nextOrderIndex++;
    }
}
