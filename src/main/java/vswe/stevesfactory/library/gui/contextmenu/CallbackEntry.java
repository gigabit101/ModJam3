package vswe.stevesfactory.library.gui.contextmenu;

import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.function.IntConsumer;

public class CallbackEntry extends DefaultEntry {

    private final IntConsumer callback;

    public CallbackEntry(@Nullable ResourceLocation icon, String translationKey, IntConsumer callback) {
        super(icon, translationKey);
        this.callback = callback;
    }

    @Override
    public boolean onMouseClicked(double mouseX, double mouseY, int button) {
        super.onMouseClicked(mouseX, mouseY, button);
        callback.accept(button);
        return true;
    }
}
