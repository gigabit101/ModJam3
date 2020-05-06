package vswe.stevesfactory.ui.manager.selection;

import com.google.common.collect.ImmutableList;
import net.minecraft.util.ResourceLocation;
import vswe.stevesfactory.api.StevesFactoryManagerAPI;
import vswe.stevesfactory.api.logic.IProcedureType;
import vswe.stevesfactory.library.collections.CompositeUnmodifiableList;
import vswe.stevesfactory.library.gui.contextmenu.CallbackEntry;
import vswe.stevesfactory.library.gui.contextmenu.ContextMenuBuilder;
import vswe.stevesfactory.library.gui.contextmenu.Section;
import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.widget.IWidget;
import vswe.stevesfactory.ui.manager.DynamicWidthWidget;
import vswe.stevesfactory.ui.manager.FactoryManagerGUI;
import vswe.stevesfactory.ui.manager.FactoryManagerGUI.TopLevelWidget;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT;
import static vswe.stevesfactory.library.gui.Render2D.DOWN_RIGHT_4_STRICT_TABLE;

public final class SelectionPanel extends DynamicWidthWidget<IComponentChoice> {

    public static final ResourceLocation BACKGROUND_NORMAL = new ResourceLocation(StevesFactoryManagerAPI.MODID, "textures/gui/component_background/background_normal.png");
    public static final ResourceLocation BACKGROUND_HOVERED = new ResourceLocation(StevesFactoryManagerAPI.MODID, "textures/gui/component_background/background_hovered.png");

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
    public void render(int mouseX, int mouseY, float partialTicks) {
        RenderEventDispatcher.onPreRender(this, mouseX, mouseY);
        for (IComponentChoice icon : staticIcons) {
            icon.render(mouseX, mouseY, partialTicks);
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
                .orElse(0) + DOWN_RIGHT_4_STRICT_TABLE.tableGap;
        setWidth(w);
    }

    @Nonnull
    @Override
    public TopLevelWidget getParent() {
        return Objects.requireNonNull((TopLevelWidget) super.getParent());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (isInside(mouseX, mouseY)) {
            if (button == GLFW_MOUSE_BUTTON_LEFT) {
                getWindow().setFocusedWidget(this);
                return true;
            } else if (button == GLFW_MOUSE_BUTTON_RIGHT) {
                return false;
            }
        }
        return false;
    }

    @Override
    protected void buildContextMenu(ContextMenuBuilder builder) {
        Section section = builder.obtainSection("");
        section.addChildren(new CallbackEntry(null, "gui.sfm.FactoryManager.Generic.ToggleFullscreen", b -> FactoryManagerGUI.get().getPrimaryWindow().toggleFullscreen()));
        super.buildContextMenu(builder);
    }
}
