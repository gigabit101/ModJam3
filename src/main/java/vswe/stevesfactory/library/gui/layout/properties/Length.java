package vswe.stevesfactory.library.gui.layout.properties;

import com.google.common.base.Preconditions;
import vswe.stevesfactory.library.gui.widget.IWidget;

public abstract class Length<T> {

    public static <T> Length<T> px(int pixels) {
        return new Length<T>() {
            {
                resolved = true;
            }

            @Override
            public void resolve(T target) {
            }

            @Override
            public float get() {
                return pixels;
            }
        };
    }

    public static <T extends IFractionalLengthHandler> Length<T> fr(int numerator) {
        return new Fr<>(numerator);
    }

    public static final class Fr<T extends IFractionalLengthHandler> extends Length<T> {

        private final int numerator;
        private float multiplier;
        private float length;

        private Fr(int numerator) {
            this.numerator = numerator;
        }

        @Override
        public void resolve(T target) {
            multiplier = (float) numerator / target.getDenominator();
            length = target.getTotalLength() * multiplier;
            this.resolved = true;
        }

        @Override
        public float get() {
            return length;
        }

        public float getMultiplier() {
            return multiplier;
        }

        public int getNumerator() {
            return numerator;
        }
    }

    public static <T extends IFractionalLengthHandler> Length<T> auto() {
        return fr(1);
    }

    public static Length<IWidget> ww(int n) {
        Preconditions.checkArgument(n <= 100);
        return new Length<IWidget>() {
            private float actualLength;

            @Override
            public void resolve(IWidget target) {
                actualLength = (float) target.getWindow().getWidth() / 100 * n;
                this.resolved = true;
            }

            @Override
            public float get() {
                return actualLength;
            }
        };
    }

    public static Length<IWidget> wh(int n) {
        Preconditions.checkArgument(n <= 100);
        return new Length<IWidget>() {
            private float actualLength;

            @Override
            public void resolve(IWidget target) {
                actualLength = (float) target.getWindow().getHeight() / 100 * n;
                this.resolved = true;
            }

            @Override
            public float get() {
                return actualLength;
            }
        };
    }

    protected boolean resolved = false;

    public abstract void resolve(T target);

    public abstract float get();

    public int getInt() {
        return (int) get();
    }
}
