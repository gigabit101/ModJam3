package vswe.stevesfactory.library.gui.layout.properties;

import com.mojang.datafixers.util.Either;

public enum HorizontalAlignment {
    LEFT,
    CENTER,
    RIGHT;

    private final Either<HorizontalAlignment, VerticalAlignment> union = Either.left(this);

    public Either<HorizontalAlignment, VerticalAlignment> asUnion() {
        return union;
    }
}
