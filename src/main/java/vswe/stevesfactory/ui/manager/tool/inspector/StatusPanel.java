package vswe.stevesfactory.ui.manager.tool.inspector;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.resources.I18n;
import vswe.stevesfactory.library.gui.Render2D;
import vswe.stevesfactory.library.gui.TextRenderer;
import vswe.stevesfactory.library.gui.Texture;
import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.widget.AbstractContainer;
import vswe.stevesfactory.library.gui.widget.IWidget;
import vswe.stevesfactory.library.gui.widget.TextField;
import vswe.stevesfactory.library.gui.widget.button.SimpleIconButton;
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
    private final List<IWidget> children = new ArrayList<>();
    private final TextField nameBox;
    private final SimpleIconButton renameButton;
    private final SimpleIconButton submitButton;
    private final SimpleIconButton cancelButton;

    private String previousName;

    public StatusPanel() {
        this.setDimensions(120, 64);

        nameBox = new TextField(100, 12);
        nameBox.setLocation(2, 6);
        nameBox.setBackgroundStyle(TextField.BackgroundStyle.NONE);
        nameBox.setText(I18n.format("gui.sfm.FactoryManager.Tool.Inspector.NoTarget"));
        nameBox.setTextColor(0xff303030, 0xff303030);
        nameBox.setEditable(false);
        children.add(nameBox);

        int baseX = getXRight() - 10;
        int baseY = 2;
        renameButton = new SimpleIconButton(RENAME_NORMAL, RENAME_HOVERING);
        renameButton.setLocation(baseX, baseY + 6);
        renameButton.setEnabled(false);
        renameButton.setClickAction(__ -> {
            if (renameButton.isEnabled() && opened != null) {
                setRenamingState();
            }
        });
        children.add(renameButton);
        submitButton = new SimpleIconButton(SUBMIT_NORMAL, SUBMIT_HOVERING);
        submitButton.setLocation(baseX + 2, baseY + 3);
        submitButton.setEnabled(false);
        submitButton.setClickAction(__ -> {
            if (submitButton.isEnabled() && opened != null) {
                // Keep input name
                opened.setName(nameBox.getText());
                setWaitingState();
            }
        });
        children.add(submitButton);
        cancelButton = new SimpleIconButton(CANCEL_NORMAL, CANCEL_HOVERING);
        cancelButton.setLocation(baseX + 2, baseY + 11);
        cancelButton.setEnabled(false);
        cancelButton.setClickAction(__ -> {
            if (cancelButton.isEnabled() && opened != null) {
                // Discard input name
                nameBox.setText(previousName);
                opened.setName(previousName);
                setWaitingState();
            }
        });
        children.add(cancelButton);
    }

    @Override
    public void onInitialAttach() {
        super.onInitialAttach();

        for (IWidget child : children) {
            child.attach(this);
        }
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
            RenderSystem.enableAlphaTest();
            ToolboxPanel.GROUP_LIST_ICON.render(x + 2, y2, x + 2 + 8, y2 + 8);
            TextRenderer tr = TextRenderer.vanilla();
            tr.setFontHeight(7);
            tr.setTextColor(0xff404040);
            tr.renderText(GroupButton.formatGroupName(opened.getGroup()), x + 2 + 8 + 2, y2, getZLevel());
        } else {
//            TextRenderer tr = TextRenderer.vanilla();
//            tr.setFontHeight(9);
//            tr.setTextColor(0xff404040);
//            tr.renderText(I18n.format("gui.sfm.FactoryManager.Tool.Inspector.NoTarget"), x + 2, y + 2, getZLevel());
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
