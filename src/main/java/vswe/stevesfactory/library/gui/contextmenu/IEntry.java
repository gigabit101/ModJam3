package vswe.stevesfactory.library.gui.contextmenu;

import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import vswe.stevesfactory.library.gui.widget.IWidget;
import vswe.stevesfactory.library.gui.widget.mixin.ResizableWidgetMixin;

import javax.annotation.Nullable;
import java.awt.*;

public interface IEntry extends IWidget, ResizableWidgetMixin {

    /**
     * This icon must have a size of 16*16, and action menus will assume so to function. Failure to do so might create undefined behaviors.
     */
    @Nullable
    ResourceLocation getIcon();

    default String getText() {
        return I18n.format(getTranslationKey());
    }

    String getTranslationKey();

    @Override
    Dimension getDimensions();

    void attach(ContextMenu contextMenu);

    /**
     * If returns {@code true}, the context menu this entry belongs to will be forced to keep alive regardless of it's defined discard
     * condition. This is used for keep nested sub-menus alive when they are not hovered.
     * <p>
     * This should not effect invokation of {@link ContextMenu#discard()}, only other direct ways of marking a context menu dead. An example
     * can be seen in {@link SubContextMenu#kill()}
     */
    boolean forceAlive();
}
