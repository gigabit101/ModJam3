package vswe.stevesfactory.library.gui;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;
import vswe.stevesfactory.StevesFactoryManager;

import java.awt.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@OnlyIn(Dist.CLIENT)
public final class DisplayListCaches {

    private DisplayListCaches() {
    }

    private static final Cache<Rectangle, Integer> VANILLA_BACKGROUND_CACHE = CacheBuilder.newBuilder()
            .expireAfterAccess(60, TimeUnit.SECONDS)
            .removalListener(removal -> {
                StevesFactoryManager.logger.info("Removed background display list with size {}", removal.getKey());
                GLAllocation.deleteDisplayLists((Integer) removal.getValue());
            })
            .build();

    public static int createVanillaStyleBackground(Rectangle rectangle) {
        return createVanillaStyleBackground(rectangle, 0F);
    }

    public static int createVanillaStyleBackground(Rectangle rectangle, float z) {
        try {
            return VANILLA_BACKGROUND_CACHE.get(rectangle, () -> {
                StevesFactoryManager.logger.info("Created background display list with size {}", rectangle);

                int id = GLAllocation.generateDisplayLists(1);
                GlStateManager.newList(id, GL11.GL_COMPILE);
                {
                    BackgroundRenderers.drawVanillaStyle(rectangle.x, rectangle.y, rectangle.width, rectangle.height, z);
                }
                GlStateManager.endList();
                return id;
            });
        } catch (ExecutionException e) {
            StevesFactoryManager.logger.error("Exception when creating OpenGL display list for {} for vanilla-style background", rectangle, e);
            return -1;
        }
    }

    public static int createVanillaStyleBackground(int x, int y, int width, int height) {
        return createVanillaStyleBackground(new Rectangle(x, y, width, height));
    }
}
