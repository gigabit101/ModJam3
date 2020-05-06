package vswe.stevesfactory.ui.manager.menu;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import vswe.stevesfactory.api.logic.ITrigger;
import vswe.stevesfactory.library.gui.ScissorTest;
import vswe.stevesfactory.library.gui.contextmenu.CallbackEntry;
import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.widget.AbstractWidget;
import vswe.stevesfactory.library.gui.widget.mixin.LeafWidgetMixin;
import vswe.stevesfactory.library.gui.widget.panel.VerticalList;
import vswe.stevesfactory.logic.procedure.FunctionInvokeProcedure;
import vswe.stevesfactory.logic.procedure.IFunctionHat;
import vswe.stevesfactory.ui.manager.FactoryManagerGUI;
import vswe.stevesfactory.ui.manager.editor.EditorPanel;
import vswe.stevesfactory.ui.manager.editor.FlowComponent;
import vswe.stevesfactory.ui.manager.editor.Menu;

import java.util.List;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static vswe.stevesfactory.library.gui.Render2D.*;

public class InvocationTargetMenu extends Menu<FunctionInvokeProcedure> {

    public static final int HORIZONTAL_MARGIN = 4;
    public static final int VERTICAL_MARGIN = 2;

    private VerticalList<ListEntry> options;

    private int selected = -1;

    public InvocationTargetMenu() {
        options = new VerticalList<>();
        options.setLocation(HORIZONTAL_MARGIN, HEADING_BOX.getPortionHeight() + VERTICAL_MARGIN);
        options.setDimensions(getWidth() - HORIZONTAL_MARGIN * 2, getContentHeight() - VERTICAL_MARGIN * 2);

        addChildren(options);

        injectAction(() -> new CallbackEntry(null, "menu.sfm.InvocationTarget.Rescan", b -> scanTargets()));
    }

    @Override
    public void onLinkFlowComponent(FlowComponent<FunctionInvokeProcedure> flowComponent) {
        super.onLinkFlowComponent(flowComponent);
        scanTargets();
    }

    private void scanTargets() {
        EditorPanel editor = FactoryManagerGUI.get().getTopLevel().editorPanel;
        FunctionInvokeProcedure p = getLinkedProcedure();
        int i = 0;
        for (FlowComponent<?> component : editor.getFlowComponents()) {
            if (component.getProcedure() instanceof ITrigger) {
                ITrigger trigger = (ITrigger) component.getProcedure();
                ListEntry entry = new ListEntry(component, i);
                entry.setWidth(options.getWidth() - options.getBarWidth());
                options.addChildren(entry);
                if (trigger == p.getTarget()) {
                    selected = i;
                }
                i++;
            }
        }
        options.reflow();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (!isInside(mouseX, mouseY)) {
            return false;
        }
        return false;
    }

    @Override
    public String getHeadingText() {
        return I18n.format("menu.sfm.InvocationTarget");
    }

    @Override
    public List<String> populateErrors(List<String> errors) {
        if (selected == -1) {
            errors.add(I18n.format("error.sfm.InvocationTarget.Unspecified"));
        }
        return errors;
    }

    private class ListEntry extends AbstractWidget implements LeafWidgetMixin {

        private final FlowComponent<?> target;
        private final int index;

        public ListEntry(FlowComponent<?> target, int index) {
            this.setHeight(14);
            this.target = target;
            this.index = index;
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == GLFW_MOUSE_BUTTON_LEFT) {
                InvocationTargetMenu.this.selected = index;
                InvocationTargetMenu.this.getLinkedProcedure().setTarget(target.getProcedure());
            }
            return true;
        }

        @Override
        public void render(int mouseX, int mouseY, float partialTicks) {
            RenderEventDispatcher.onPreRender(this, mouseX, mouseY);

            usePlainColorGLStates();
            Tessellator.getInstance().getBuffer().begin(GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
            int x1 = getAbsoluteX();
            int y1 = getAbsoluteY();
            int x2 = getAbsoluteXRight();
            int y2 = getAbsoluteYBottom();
            coloredRect(x1, y1, x2, y2, InvocationTargetMenu.this.selected == this.index ? 0xffffff66 : 0xff8c8c8c);
            coloredRect(x1 + 1, y1 + 1, x2 - 1, y2 - 1, isInside(mouseX, mouseY) ? 0xff737373 : 0xffc9c9c9);
            Tessellator.getInstance().draw();

            ScissorTest test = ScissorTest.scaled(x1, y1, x2, y2);
            String name = target.getName();
            if (target.getProcedure() instanceof IFunctionHat) {
                name += " (" + ((IFunctionHat) target.getProcedure()).getFunctionName() + ")";
            }
            renderVerticallyCenteredText(name, x1 + 2, y1, y2, getZLevel(), 0xff000000);
            test.destroy();

            if (isInside(mouseX, mouseY)) {
                FactoryManagerGUI.get().scheduleTooltip(name, mouseX, mouseY);
            }

            RenderEventDispatcher.onPostRender(this, mouseX, mouseY);
        }
    }
}
