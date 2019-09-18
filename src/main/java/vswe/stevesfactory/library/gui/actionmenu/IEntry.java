package vswe.stevesfactory.library.gui.actionmenu;

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

    void attach(ActionMenu actionMenu);
}
