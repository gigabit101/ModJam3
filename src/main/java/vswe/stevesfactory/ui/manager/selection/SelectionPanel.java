package vswe.stevesfactory.ui.manager.selection;

import com.google.common.collect.ImmutableList;
import net.minecraft.util.ResourceLocation;
import vswe.stevesfactory.StevesFactoryManager;
import vswe.stevesfactory.api.logic.IProcedureType;
import vswe.stevesfactory.library.collections.CompositeUnmodifiableList;
import vswe.stevesfactory.library.gui.contextmenu.CallbackEntry;
import vswe.stevesfactory.library.gui.contextmenu.ContextMenu;
import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.screen.WidgetScreen;
import vswe.stevesfactory.library.gui.widget.IWidget;
import vswe.stevesfactory.ui.manager.DynamicWidthWidget;
import vswe.stevesfactory.ui.manager.FactoryManagerGUI;
import vswe.stevesfactory.ui.manager.FactoryManagerGUI.TopLevelWidget;

import javax.annotation.Nonnull;
import java.util.*;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT;
import static vswe.stevesfactory.ui.manager.FactoryManagerGUI.DOWN_RIGHT_4_STRICT_TABLE;

public final class SelectionPanel extends DynamicWidthWidget<IComponentChoice> {

    public static final ResourceLocation BACKGROUND_NORMAL = new ResourceLocation(StevesFactoryManager.MODID, "textures/gui/component_background/background_normal.png");
    public static final ResourceLocation BACKGROUND_HOVERED = new ResourceLocation(StevesFactoryManager.MODID, "textures/gui/component_background/background_hovered.png");

    private final ImmutableList<IComponentChoice> staticIcons;
    private final List<IComponentChoice> addendumIcons;
    private final List<IComponentChoice> icons;

    public SelectionPanel() {
        super(WidthOccupierType.MIN_WIDTH);

        this.staticIcons = createStaticIcons();
        this.addendumIcons = new ArrayList<>();
        this.icons = CompositeUnmodifiableList.of(staticIcons, addendumIcons);
    }

    private ImmutableList<IComponentChoice> createStaticIcons() {
        ImmutableList.Builder<IComponentChoice> icons = ImmutableList.builder();
        for (ComponentGroup group : ComponentGroup.groups) {
            icons.add(new GroupComponentChoice(group));
        }
        for (IProcedureType<?> type : ComponentGroup.ungroupedTypes) {
            icons.add(new SingularComponentChoice(type));
        }
        return icons.build();
    }

    @Override
    public void render(int mouseX, int mouseY, float particleTicks) {
        RenderEventDispatcher.onPreRender(this, mouseX, mouseY);
        for (IComponentChoice icon : staticIcons) {
            icon.render(mouseX, mouseY, particleTicks);
        }
        RenderEventDispatcher.onPostRender(this, mouseX, mouseY);
    }

    @Override
    public List<IComponentChoice> getChildren() {
        return icons;
    }

    @Override
    public void reflow() {
        setWidth(Integer.MAX_VALUE);
        DOWN_RIGHT_4_STRICT_TABLE.reflow(getDimensions(), getChildren());
        int w = getChildren().stream()
                .max(Comparator.comparingInt(IWidget::getX))
                .map(furthest -> furthest.getX() + furthest.getWidth())
                .orElse(0) + DOWN_RIGHT_4_STRICT_TABLE.componentMargin;
        setWidth(w);
    }

    @Nonnull
    @Override
    public TopLevelWidget getParentWidget() {
        return Objects.requireNonNull((TopLevelWidget) super.getParentWidget());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (isInside(mouseX, mouseY)) {
            if (button == GLFW_MOUSE_BUTTON_LEFT) {
                getWindow().setFocusedWidget(this);
            } else if (button == GLFW_MOUSE_BUTTON_RIGHT) {
                openActionMenu();
            }
            return true;
        }
        return false;
    }

    private void openActionMenu() {
        ContextMenu contextMenu = ContextMenu.atCursor(ImmutableList.of(
                new CallbackEntry(null, "gui.sfm.FactoryManager.Generic.ToggleFullscreen", b -> FactoryManagerGUI.get().getPrimaryWindow().toggleFullscreen())
        ));
        WidgetScreen.getCurrent().addPopupWindow(contextMenu);
    }
}
