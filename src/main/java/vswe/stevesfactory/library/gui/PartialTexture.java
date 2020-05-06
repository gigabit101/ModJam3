package vswe.stevesfactory.library.gui;

import com.google.common.base.MoreObjects;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;

import java.util.Objects;

import static vswe.stevesfactory.library.gui.Render2D.*;

class PartialTexture implements Texture {

    private ResourceLocation texture;
    private int texWidth;
    private int texHeight;
    private int portionX;
    private int portionY;
    private int portionWidth;
    private int portionHeight;

    public PartialTexture(ResourceLocation texture, int texWidth, int texHeight, int portionX, int portionY, int portionWidth, int portionHeight) {
        this.texture = texture;
        this.texWidth = texWidth;
        this.texHeight = texHeight;
        this.portionX = portionX;
        this.portionY = portionY;
        this.portionWidth = portionWidth;
        this.portionHeight = portionHeight;
    }

    @Override
    public ResourceLocation getResourceLocation() {
        return texture;
    }

    @Override
    public int getTextureWidth() {
        return texWidth;
    }

    @Override
    public int getTextureHeight() {
        return texHeight;
    }

    @Override
    public int getPortionX() {
        return portionX;
    }

    @Override
    public int getPortionY() {
        return portionY;
    }

    @Override
    public int getPortionWidth() {
        return portionWidth;
    }

    @Override
    public int getPortionHeight() {
        return portionHeight;
    }

    @Override
    public void render(int x1, int y1, int x2, int y2, float z) {
        bindTexture(texture);
        beginTexturedQuad();
        vertices(x1, y1, x2, y2, z);
        draw();
    }

    @Override
    public void vertices(int x1, int y1, int x2, int y2, float z) {
        float uFactor = 1F / texWidth;
        float vFactor = 1F / texHeight;
        int px2 = portionX + portionWidth;
        int py2 = portionY + portionHeight;
        float u1 = portionX * uFactor;
        float v1 = portionY * vFactor;
        float u2 = px2 * uFactor;
        float v2 = py2 * vFactor;
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.pos(x1, y1, z).tex(u1, v1).endVertex();
        buffer.pos(x1, y2, z).tex(u1, v2).endVertex();
        buffer.pos(x2, y2, z).tex(u2, v2).endVertex();
        buffer.pos(x2, y1, z).tex(u2, v1).endVertex();
    }

    @Override
    public boolean isComplete() {
        return false;
    }

    @Override
    public PartialTexture offset(int x, int y) {
        return new PartialTexture(texture, texWidth, texHeight, portionX + x, portionY + y, portionWidth, portionHeight);
    }

    @Override
    public PartialTexture down(int y) {
        return offset(0, y);
    }

    @Override
    public PartialTexture up(int y) {
        return offset(0, -y);
    }

    @Override
    public PartialTexture right(int x) {
        return offset(x, 0);
    }

    @Override
    public PartialTexture left(int x) {
        return offset(-x, 0);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("texture", texture)
                .add("texWidth", texWidth)
                .add("texHeight", texHeight)
                .add("portionX", portionX)
                .add("portionY", portionY)
                .add("portionWidth", portionWidth)
                .add("portionHeight", portionHeight)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PartialTexture that = (PartialTexture) o;
        return texWidth == that.texWidth &&
                texHeight == that.texHeight &&
                portionX == that.portionX &&
                portionY == that.portionY &&
                portionWidth == that.portionWidth &&
                portionHeight == that.portionHeight &&
                texture.equals(that.texture);
    }

    @Override
    public int hashCode() {
        return Objects.hash(texture, texWidth, texHeight, portionX, portionY, portionWidth, portionHeight);
    }
}
