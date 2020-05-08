package vswe.stevesfactory.library.gui.layout.properties;

import com.mojang.datafixers.util.Either;

public enum VerticalAlignment {
    TOP,
    CENTER,
    BOTTOM;

    private final Either<HorizontalAlignment, VerticalAlignment> union = Either.right(this);

    public Either<HorizontalAlignment, VerticalAlignment> asUnion() {
        return union;
    }
}
