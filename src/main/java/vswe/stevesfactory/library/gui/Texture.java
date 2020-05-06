package vswe.stevesfactory.library.gui;

import net.minecraft.util.ResourceLocation;

public interface Texture {

    public static final Texture NONE = new Texture() {
        @Override
        public ResourceLocation getResourceLocation() {
            return Render2D.INVALID_TEXTURE;
        }

        @Override
        public int getTextureWidth() {
            return 0;
        }

        @Override
        public int getTextureHeight() {
            return 0;
        }

        @Override
        public int getPortionX() {
            return 0;
        }

        @Override
        public int getPortionY() {
            return 0;
        }

        @Override
        public int getPortionWidth() {
            return 0;
        }

        @Override
        public int getPortionHeight() {
            return 0;
        }

        @Override
        public void render(int x1, int y1, int x2, int y2, float z) {
        }

        @Override
        public void vertices(int x1, int y1, int x2, int y2, float z) {
        }

        @Override
        public boolean isComplete() {
            return true;
        }

        @Override
        public Texture offset(int x, int y) {
            return this;
        }

        @Override
        public Texture up(int y) {
            return this;
        }

        @Override
        public Texture down(int y) {
            return this;
        }

        @Override
        public Texture left(int x) {
            return this;
        }

        @Override
        public Texture right(int x) {
            return this;
        }
    };

    public static Texture portion(ResourceLocation texture, int texWidth, int texHeight, int portionX, int portionY, int portionWidth, int portionHeight) {
        if (texWidth == portionWidth && texHeight == portionHeight && portionX == 0 && portionY == 0) {
            return complete(texture, texWidth, texHeight);
        }
        return new PartialTexture(texture, texWidth, texHeight, portionX, portionY, portionWidth, portionHeight);
    }

    public static Texture portion256x256(ResourceLocation texture, int portionX, int portionY, int portionWidth, int portionHeight) {
        return new PartialTexture(texture, 256, 256, portionX, portionY, portionWidth, portionHeight);
    }

    public static Texture complete(ResourceLocation texture, int width, int height) {
        return new CompleteTexture(texture, width, height);
    }

    public static Texture complete256x256(ResourceLocation texture) {
        return new CompleteTexture(texture, 256, 256);
    }

    ResourceLocation getResourceLocation();

    int getTextureWidth();

    int getTextureHeight();

    int getPortionX();

    int getPortionY();

    int getPortionWidth();

    int getPortionHeight();

    void render(int x1, int y1, int x2, int y2, float z);

    default void render(int x1, int y1, int x2, int y2) {
        render(x1, y1, x2, y2, 0F);
    }

    default void render(int x, int y, float z) {
        render(x, y, x + getPortionWidth(), y + getPortionHeight(), z);
    }

    default void render(int x, int y) {
        render(x, y, 0F);
    }

    void vertices(int x1, int y1, int x2, int y2, float z);

    default void vertices(int x1, int y1, int x2, int y2) {
        vertices(x1, y1, x2, y2, 0F);
    }

    default void vertices(int x, int y, float z) {
        vertices(x, y, x + getPortionWidth(), y + getPortionHeight(), z);
    }

    default void vertices(int x, int y) {
        vertices(x, y, 0F);
    }

    boolean isComplete();

    Texture offset(int x, int y);

    Texture up(int y);

    Texture down(int y);

    Texture left(int x);

    Texture right(int x);

    default Texture moveUp(int times) {
        return up(getPortionHeight() * times);
    }

    default Texture moveDown(int times) {
        return down(getPortionHeight() * times);
    }

    default Texture moveLeft(int times) {
        return left(getPortionWidth() * times);
    }

    default Texture moveRight(int times) {
        return right(getPortionWidth() * times);
    }
}
