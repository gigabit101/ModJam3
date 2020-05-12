package vswe.stevesfactory.library.gui.window;

import lombok.Getter;
import lombok.val;
import vswe.stevesfactory.library.gui.Render2D;
import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.widget.IWidget;
import vswe.stevesfactory.library.gui.widget.button.SimpleIconButton;
import vswe.stevesfactory.library.gui.widget.panel.SettingsEditor;

import java.util.ArrayList;
import java.util.List;

public class SettingsEditorWindow extends AbstractPopupWindow {

    @Getter
    private final SettingsEditor editor;
    private final List<IWidget> children = new ArrayList<>();

    public SettingsEditorWindow(int width) {
        this.editor = new SettingsEditor();
        this.editor.setBorders(2);
        this.editor.setBorderTop(9 + 2);
        this.editor.setWidth(width);
        this.editor.attachWindow(this);
        children.add(editor);

        val close = new SimpleIconButton(Render2D.CLOSE_ICON, Render2D.CLOSE_ICON_HOVERED);
        close.attachWindow(this);
        close.alignBottom(editor.getInnerY() - 1);
        close.alignRight(editor.getXRight() - 1);
        close.setClickAction(b -> discard());
        children.add(close);
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
        renderVanillaStyleBackground();
        super.renderChildren(mouseX, mouseY, partialTicks);
        RenderEventDispatcher.onPostRender(this, mouseX, mouseY);
    }
}
