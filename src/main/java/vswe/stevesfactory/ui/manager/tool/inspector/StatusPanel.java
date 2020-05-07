package vswe.stevesfactory.ui.manager.tool.inspector;

import net.minecraft.client.resources.I18n;
import vswe.stevesfactory.library.gui.Render2D;
import vswe.stevesfactory.library.gui.TextRenderer;
import vswe.stevesfactory.library.gui.Texture;
import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.widget.AbstractContainer;
import vswe.stevesfactory.library.gui.widget.CallbackIconButton;
import vswe.stevesfactory.library.gui.widget.IWidget;
import vswe.stevesfactory.library.gui.widget.TextField;
import vswe.stevesfactory.ui.manager.editor.FlowComponent;
import vswe.stevesfactory.ui.manager.tool.group.GroupButton;
import vswe.stevesfactory.ui.manager.toolbox.ToolboxPanel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class StatusPanel extends AbstractContainer<IWidget> {

    public static final Texture RENAME_NORMAL = Render2D.ofFlowComponent(0, 124, 9, 9);
    public static final Texture RENAME_HOVERING = RENAME_NORMAL.right(1);
    public static final Texture SUBMIT_NORMAL = Render2D.ofFlowComponent(0, 133, 7, 7);
    public static final Texture SUBMIT_HOVERING = SUBMIT_NORMAL.right(1);
    public static final Texture CANCEL_NORMAL = Render2D.ofFlowComponent(0, 140, 7, 7);
    public static final Texture CANCEL_HOVERING = CANCEL_NORMAL.right(1);

    private FlowComponent<?> opened = null;
    private List<IWidget> children = new ArrayList<>();
    private TextField nameBox;
    private CallbackIconButton renameButton;
    private CallbackIconButton submitButton;
    private CallbackIconButton cancelButton;

    private String previousName;

    public StatusPanel() {
        this.setDimensions(120, 64);
    }

    @Override
    public void onInitialAttach() {
        super.onInitialAttach();

        nameBox = new TextField(100, 16);
        nameBox.setLocation(2, 6);
        nameBox.attach(this);
        nameBox.setBackgroundStyle(TextField.BackgroundStyle.NONE);
        nameBox.setTextColor(0xff303030, 0xff303030);
        nameBox.setEditable(false);
        children.add(nameBox);

        int baseX = getXRight() - 10;
        int baseY = 2;
        renameButton = new CallbackIconButton(RENAME_NORMAL, RENAME_HOVERING);
        renameButton.attach(this);
        renameButton.setLocation(baseX, baseY + 6);
        renameButton.setEnabled(false);
        renameButton.onClick = b -> {
            if (renameButton.isEnabled() && opened != null) {
                setRenamingState();
                return true;
            }
            return false;
        };
        children.add(renameButton);
        submitButton = new CallbackIconButton(SUBMIT_NORMAL, SUBMIT_HOVERING);
        submitButton.attach(this);
        submitButton.setLocation(baseX + 2, baseY + 3);
        submitButton.setEnabled(false);
        submitButton.onClick = b -> {
            if (submitButton.isEnabled() && opened != null) {
                // Keep input name
                opened.setName(nameBox.getText());
                setWaitingState();
                return true;
            }
            return false;
        };
        children.add(submitButton);
        cancelButton = new CallbackIconButton(CANCEL_NORMAL, CANCEL_HOVERING);
        cancelButton.attach(this);
        cancelButton.setLocation(baseX + 2, baseY + 11);
        cancelButton.setEnabled(false);
        cancelButton.onClick = b -> {
            if (cancelButton.isEnabled() && opened != null) {
                // Discard input name
                nameBox.setText(previousName);
                opened.setName(previousName);
                setWaitingState();
                return true;
            }
            return false;
        };
        children.add(cancelButton);
    }

    private void setRenamingState() {
        renameButton.setEnabled(false);
        submitButton.setEnabled(true);
        cancelButton.setEnabled(true);
        nameBox.setEditable(true);
        nameBox.scrollToFront();
        previousName = nameBox.getText();
        getWindow().setFocusedWidget(nameBox);
    }

    private void setWaitingState() {
        renameButton.setEnabled(true);
        submitButton.setEnabled(false);
        cancelButton.setEnabled(false);
        nameBox.setEditable(false);
        nameBox.scrollToFront();
        previousName = "";
        getWindow().setFocusedWidget(null);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        RenderEventDispatcher.onPreRender(this, mouseX, mouseY);

        int x = getAbsoluteX();
        int y = getAbsoluteY();
        if (opened != null) {
            int y2 = y + 2 + 16 /* height of name */ + 2 /* margin */;
            ToolboxPanel.GROUP_LIST_ICON.render(x + 2, y2, 8, 8);
            TextRenderer tr = TextRenderer.vanilla();
            tr.setFontHeight(7);
            tr.setTextColor(0xff404040);
            tr.renderText(GroupButton.formatGroupName(opened.getGroup()), x + 2 + 8 + 2, y2, getZLevel());
        } else {
            TextRenderer tr = TextRenderer.vanilla();
            tr.setFontHeight(9);
            tr.setTextColor(0xff404040);
            tr.renderText(I18n.format("gui.sfm.FactoryManager.Tool.Inspector.NoTarget"), x + 2, y + 2, getZLevel());
        }

        super.renderChildren(mouseX, mouseY, partialTicks);
        RenderEventDispatcher.onPostRender(this, mouseX, mouseY);
    }

    @Override
    public Collection<IWidget> getChildren() {
        return children;
    }

    @Override
    public void reflow() {
    }

    public void openFlowComponent(FlowComponent<?> target) {
        this.opened = target;

        setWaitingState();
        nameBox.setText(target.getName());
        // For transition from <none selected> to something selected
        renameButton.setEnabled(true);
    }
}
