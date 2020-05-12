package vswe.stevesfactory.library.gui.window.mixin;

import vswe.stevesfactory.library.gui.debug.ITextReceiver;
import vswe.stevesfactory.library.gui.debug.Inspections;
import vswe.stevesfactory.library.gui.window.IWindow;

public interface WindowOverlayInfoMixin extends IWindow, Inspections.IInfoProvider, Inspections.IHighlightRenderer {

    @Override
    default void provideInformation(ITextReceiver receiver) {
        receiver.line(this.toString());
        receiver.line("Position=(${getPosition().x}, ${getPosition().y})");
        receiver.line("Dimensions=(${getBorder().width}, ${getBorder().width})");
        receiver.line("ContentPosition=(${getContentX()}, ${getContentY()})");
        receiver.line("ContentDimensions=(${getContentWidth()}, ${getContentHeight()})");
        receiver.line("BorderSize=${getBorderSize()}");
        receiver.line("Z=${getZLevel()}");
    }

    @Override
    default void renderHighlight() {
        Inspections.renderBorderedHighlight(
                getX(), getY(),
                getContentX(), getContentY(),
                getContentWidth(), getContentHeight(),
                getWidth(), getHeight());
    }
}
