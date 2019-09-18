package vswe.stevesfactory.ui.manager.selection;

import com.google.common.collect.ImmutableList;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.glfw.GLFW;
import vswe.stevesfactory.StevesFactoryManager;
import vswe.stevesfactory.api.logic.IProcedure;
import vswe.stevesfactory.api.logic.IProcedureType;
import vswe.stevesfactory.library.collections.CompositeUnmodifiableList;
import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.ui.manager.FactoryManagerGUI.TopLevelWidget;
import vswe.stevesfactory.ui.manager.editor.DynamicWidthWidget;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
        int w = getWidth();
        setWidth(Integer.MAX_VALUE);
        DOWN_RIGHT_4_STRICT_TABLE.reflow(getDimensions(), getChildren());
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
        if (isInside(mouseX, mouseY) && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            getWindow().setFocusedWidget(this);
            return true;
        }
        return false;
    }
}
