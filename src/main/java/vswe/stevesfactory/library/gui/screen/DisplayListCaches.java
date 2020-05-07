package vswe.stevesfactory.library.gui.screen;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import vswe.stevesfactory.StevesFactoryManager;

import java.awt.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.lwjgl.opengl.GL11.*;

public final class DisplayListCaches {

    private DisplayListCaches() {
    }

    private static final Cache<Rectangle, Integer> VANILLA_BACKGROUND_CACHE = CacheBuilder.newBuilder()
            .expireAfterAccess(120, TimeUnit.SECONDS)
            .removalListener(removal -> {
                StevesFactoryManager.logger.info("Removed background display list with size {}", removal.getKey());
                glDeleteLists((Integer) removal.getValue(), 1);
            })
            .build();

    public static int createVanillaStyleBackground(Rectangle rectangle) {
        return createVanillaStyleBackground(rectangle, 0F);
    }

    public static int createVanillaStyleBackground(Rectangle rectangle, float z) {
        try {
            return VANILLA_BACKGROUND_CACHE.get(rectangle, () -> {
                StevesFactoryManager.logger.info("Created background display list with size {}", rectangle);

                int id = glGenLists(1);
                if (id == 0) {
                    throw new RuntimeException("Unable to allocate GL draw list!");
                }
                glNewList(id, GL_COMPILE);
                BackgroundRenderers.drawVanillaStyle4x4(rectangle.x, rectangle.y, rectangle.width, rectangle.height, z);
                glEndList();
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

    public static int createVanillaStyleBackground(int x, int y, int width, int height, float z) {
        return createVanillaStyleBackground(new Rectangle(x, y, width, height), z);
    }
}
