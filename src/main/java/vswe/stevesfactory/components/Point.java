package vswe.stevesfactory.components;


public class Point {
    private int x, y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Point() {
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public gigabit101.AdvancedSystemManager2.components.Point copy() {
        return new gigabit101.AdvancedSystemManager2.components.Point(x, y);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        gigabit101.AdvancedSystemManager2.components.Point point = (gigabit101.AdvancedSystemManager2.components.Point) o;

        if (x != point.x) return false;
        if (y != point.y) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        return result;
    }

    public void adjustToGrid() {
        x = ((x - 2) / 10) * 10 + 2;
        y = ((y - 4) / 10) * 10 + 4;
    }
}
