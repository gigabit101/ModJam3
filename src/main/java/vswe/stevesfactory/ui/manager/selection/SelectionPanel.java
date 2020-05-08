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
import vswe.stevesfactory.library.gui.widget.mixin.FullHeightMajorMixin;
import vswe.stevesfactory.ui.manager.DynamicWidthWidget;
import vswe.stevesfactory.ui.manager.FactoryManagerGUI;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static vswe.stevesfactory.library.gui.Render2D.DOWN_RIGHT_4_STRICT_TABLE;

public final class SelectionPanel extends DynamicWidthWidget<IComponentChoice> implements FullHeightMajorMixin {

    public static final ResourceLocation BACKGROUND_NORMAL = new ResourceLocation(StevesFactoryManagerAPI.MODID, "textures/gui/component_background/background_normal.png");
    public static final ResourceLocation BACKGROUND_HOVERED = new ResourceLocation(StevesFactoryManagerAPI.MODID, "textures/gui/component_background/background_hovered.png");

    private ImmutableList<IComponentChoice> staticIcons;
    private List<IComponentChoice> addendumIcons;
    private List<IComponentChoice> icons;

    public SelectionPanel() {
        super(WidthOccupierType.MIN_WIDTH);
        this.setBorders(4);
    }

    @Override
    public void onInitialAttach() {
        super.onInitialAttach();

        this.staticIcons = createStaticIcons();
        this.addendumIcons = new ArrayList<>();
        this.icons = CompositeUnmodifiableList.of(staticIcons, addendumIcons);
    }

    private ImmutableList<IComponentChoice> createStaticIcons() {
        ImmutableList.Builder<IComponentChoice> icons = ImmutableList.builder();
        for (ComponentGroup group : ComponentGroup.groups) {
            GroupComponentChoice element = new GroupComponentChoice(group);
            element.attach(this);
            icons.add(element);
        }
        for (IProcedureType<?> type : ComponentGroup.ungroupedTypes) {
            SingularComponentChoice element = new SingularComponentChoice(type);
            element.attach(this);
            icons.add(element);
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
                .map(furthest -> furthest.getX() + furthest.getFullWidth())
                .orElse(0);
        setWidth(w);
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
            }
        }
        return false;
    }

    @Override
    protected void buildContextMenu(ContextMenuBuilder builder) {
        Section section = builder.obtainSection("Window");
        section.addChildren(new CallbackEntry(null, "gui.sfm.FactoryManager.Generic.ToggleFullscreen", b -> FactoryManagerGUI.get().getPrimaryWindow().toggleFullscreen()));
        super.buildContextMenu(builder);
    }
}
