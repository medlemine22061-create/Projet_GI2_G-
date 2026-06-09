package model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a two-dimensional position on the map.
 */
public class Position implements Serializable {

    private static final long serialVersionUID = 1L;

    private double x;
    private double y;

    /**
     * Creates a position.
     *
     * @param x horizontal coordinate
     * @param y vertical coordinate
     */
    public Position(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Computes the Euclidean distance to another position.
     *
     * @param other target position
     * @return distance between the two positions
     */
    public double distanceTo(Position other) {
        if (other == null) {
            throw new IllegalArgumentException("other cannot be null");
        }

        double dx = this.x - other.x;
        double dy = this.y - other.y;

        return Math.sqrt(dx * dx + dy * dy);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }

}