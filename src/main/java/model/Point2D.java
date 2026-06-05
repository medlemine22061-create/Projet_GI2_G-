package model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a two-dimensional position on the map.
 */
public class Point2D implements Serializable {

    private static final long serialVersionUID = 1L;

    private double x;
    private double y;

    /**
     * Creates a position.
     *
     * @param x horizontal coordinate
     * @param y vertical coordinate
     */
    public Point2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Computes the Euclidean distance to another position.
     *
     * @param position target position
     * @return distance between the two positions
     */
    public double distanceTo(Point2D position) {
        Objects.requireNonNull(position, "position cannot be null");

        double dx = x - position.x;
        double dy = y - position.y;

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