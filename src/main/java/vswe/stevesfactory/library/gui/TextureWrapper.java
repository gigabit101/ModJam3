package vswe.stevesfactory.library.gui;

import com.google.common.base.MoreObjects;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import vswe.stevesfactory.StevesFactoryManager;

import java.awt.*;

import static org.lwjgl.opengl.GL11.GL_QUADS;

/**
 * Wraps around a ResourceLocation, used to simplify drawing 2D textures.
 */
public class TextureWrapper {

    public static final ResourceLocation INVALID_TEXTURE = RenderingHelper.linkTexture("textures/invalid");
    public static final TextureWrapper NONE = new TextureWrapper(INVALID_TEXTURE, 0, 0, 0, 0, 0, 0) {
        @Override
        public void draw(int x, int y) {
        }

        @Override
        public void draw(int x, int y, int width, int height) {
        }
    };

    public static final ResourceLocation FLOW_COMPONENTS = RenderingHelper.linkTexture("gui/flow_components.png");

    public static TextureWrapper ofFlowComponent(int tx, int ty, int portionWidth, int portionHeight) {
        return new TextureWrapper(FLOW_COMPONENTS, 256, 256, tx, ty, portionWidth, portionHeight);
    }

    public static TextureWrapper ofGUITexture(String path, int textureWidth, int textureHeight, int tx, int ty, int portionWidth, int portionHeight) {
        return new TextureWrapper("textures/gui/" + path, textureWidth, textureHeight, tx, ty, portionWidth, portionHeight);
    }

    private final ResourceLocation texture;
    private final int textureWidth, textureHeight, tx, ty, portionWidth, portionHeight;

    public TextureWrapper(String path, int textureWidth, int textureHeight, int tx, int ty, int portionWidth, int portionHeight) {
        this(new ResourceLocation(StevesFactoryManager.MODID, path), textureWidth, textureHeight, tx, ty, portionWidth, portionHeight);
    }

    public TextureWrapper(ResourceLocation texture, int textureWidth, int textureHeight, int tx, int ty, int portionWidth, int portionHeight) {
        this.texture = texture;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        this.tx = tx;
        this.ty = ty;
        this.portionWidth = portionWidth;
        this.portionHeight = portionHeight;
    }

    public void draw(int x, int y) {
        draw(x, y, portionWidth, portionHeight);
    }

    public void draw(int x, int y, int width, int height) {
        RenderSystem.enableAlphaTest();
        RenderSystem.color3f(1F, 1F, 1F);
        RenderingHelper.bindTexture(texture);
        RenderingHelper.getRenderer().begin(GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        vertices(x, y, width, height);
        Tessellator.getInstance().draw();
    }

    public void vertices(int x, int y) {
        vertices(x, y, portionWidth, portionHeight);
    }

    public void vertices(int x, int y, int width, int height) {
        int x2 = x + width;
        int y2 = y + height;
        float uFactor = 1.0F / (float) textureWidth;
        float vFactor = 1.0F / (float) textureHeight;
        int tx2 = tx + portionWidth;
        int ty2 = ty + portionHeight;
        RenderingHelper.textureVertices(x, y, x2, y2, getZLevel(), tx * uFactor, ty * vFactor, tx2 * uFactor, ty2 * vFactor);
    }

    public Dimension getBounds() {
        return new Dimension(portionWidth, portionHeight);
    }

    public int getTextureWidth() {
        return textureWidth;
    }

    public int getTextureHeight() {
        return textureHeight;
    }

    public int getTx() {
        return tx;
    }

    public int getTy() {
        return ty;
    }

    public float getZLevel() {
        return 0F;
    }

    public int getPortionWidth() {
        return portionWidth;
    }

    public int getPortionHeight() {
        return portionHeight;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("texture", texture)
                .add("textureWidth", textureWidth)
                .add("textureHeight", textureHeight)
                .add("tx", tx)
                .add("ty", ty)
                .add("portionWidth", portionWidth)
                .add("portionHeight", portionHeight)
                .toString();
    }

    public String getName() {
        return texture.getPath();
    }

    public TextureWrapper offset(int x, int y) {
        return new TextureWrapper(texture, textureWidth, textureHeight, tx + x, ty + y, portionWidth, portionHeight);
    }

    public TextureWrapper toDown(int times) {
        return down(portionHeight * times);
    }

    public TextureWrapper down(int y) {
        return offset(0, y);
    }

    public TextureWrapper toUp(int times) {
        return up(portionHeight * times);
    }

    public TextureWrapper up(int y) {
        return offset(0, -y);
    }

    public TextureWrapper toRight(int times) {
        return right(portionWidth * times);
    }

    public TextureWrapper right(int x) {
        return offset(x, 0);
    }

    public TextureWrapper toLeft(int times) {
        return left(portionWidth * times);
    }

    public TextureWrapper left(int x) {
        return offset(-x, 0);
    }

    public TextureWrapper withZ(float z) {
        return new TextureWrapperWithZ(this, z);
    }

    private static class TextureWrapperWithZ extends TextureWrapper {

        private float zLevel;

        public TextureWrapperWithZ(TextureWrapper source, float zLevel) {
            super(source.texture, source.textureWidth, source.textureHeight, source.tx, source.ty, source.portionWidth, source.portionHeight);
            this.zLevel = zLevel;
        }

        @Override
        public float getZLevel() {
            return zLevel;
        }
    }

}
