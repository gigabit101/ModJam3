package vswe.stevesfactory.library.gui.window;

import lombok.Getter;
import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.widget.IWidget;
import vswe.stevesfactory.library.gui.widget.panel.SettingsEditor;

import java.util.ArrayList;
import java.util.List;

public class SettingsEditorWindow extends AbstractPopupWindow {

    @Getter
    private final SettingsEditor editor;
    private final List<IWidget> children = new ArrayList<>();

    public SettingsEditorWindow(int width) {
        this.editor = new SettingsEditor();
        this.editor.setWidth(width);
    }

    @Override
    public int getBorderSize() {
        return 4;
    }

    @Override
    public List<? extends IWidget> getChildren() {
        return children;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        RenderEventDispatcher.onPreRender(this, mouseX, mouseY);
        super.renderChildren(mouseX, mouseY, partialTicks);
        RenderEventDispatcher.onPostRender(this, mouseX, mouseY);
    }
}
