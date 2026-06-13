package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents one simplified Voronoi cell.
 */
public class VoronoiCell implements Serializable {
    private static final long serialVersionUID = 1L;

    private final MedicalSite owner;
    private final List<Position> points;
    private final List<UserPoint> userPoints;

    private double surface;
    private double density;

    public VoronoiCell(MedicalSite owner) {
        this.owner = Objects.requireNonNull(owner, "owner cannot be null");
        this.points = new ArrayList<>();
        this.userPoints = new ArrayList<>();
        this.surface = 0.0;
        this.density = 0.0;
    }

    /**
     * Adds a grid point to this Voronoi cell.
     *
     * @param position grid point belonging to this cell
     */
    public void addPoint(Position position) {
        if (position != null) {
            points.add(position);
        }
    }

    public void addVertex(Position position) {
        addPoint(position);
    }

    /**
     * Assigns a user point (doctor) to this Voronoi cell.
     *
     * @param userPoint user point to add
     */
    public void addUserPoint(UserPoint userPoint) {
        if (userPoint != null) {
            userPoints.add(userPoint);
        }
    }

    /**
     * Clears all user points from this cell before recomputation.
     */
    public void resetUserPoints() {
        userPoints.clear();
    }

    /**
     * Checks whether a position belongs to this Voronoi cell.
     *
     * @param position position to test
     * @return true if the position is in this cell
     */
    public boolean contains(Position position) {
        if (position == null) {
            return false;
        }

        for (Position point : points) {
            if (point.distanceTo(position) < 1.0) {
                return true;
            }
        }

        return false;
    }

    public double computeSurface(double step) {
        this.surface = points.size() * step * step;
        return surface;
    }

    public double computeSurface() {
        return surface;
    }

    /**
     * Computes the density of this cell as user points per surface unit.
     *
     * @return density value
     */
    public double computeDensity() {
        if (surface <= 0) {
            density = 0.0;
        } else {
            density = userPoints.size() / surface;
        }

        return density;
    }

    public double getMinDistanceToUserPoints() {
        if (userPoints.isEmpty()) {
            return 0.0;
        }

        double min = Double.MAX_VALUE;

        for (UserPoint point : userPoints) {
            double distance = owner.getPosition().distanceTo(point.getPosition());
            min = Math.min(min, distance);
        }

        return min;
    }

    public double getMaxDistanceToUserPoints() {
        if (userPoints.isEmpty()) {
            return 0.0;
        }

        double max = 0.0;

        for (UserPoint point : userPoints) {
            double distance = owner.getPosition().distanceTo(point.getPosition());
            max = Math.max(max, distance);
        }

        return max;
    }

    public double getAverageDistanceToUserPoints() {
        if (userPoints.isEmpty()) {
            return 0.0;
        }

        double sum = 0.0;

        for (UserPoint point : userPoints) {
            sum += owner.getPosition().distanceTo(point.getPosition());
        }

        return sum / userPoints.size();
    }

    public MedicalSite getOwner() {
        return owner;
    }

    public List<Position> getPoints() {
        return new ArrayList<>(points);
    }

    public List<Position> getVertices() {
        return getPoints();
    }

    public List<UserPoint> getUserPoints() {
        return new ArrayList<>(userPoints);
    }

    public double getSurface() {
        return surface;
    }

    public double getDensity() {
        return density;
    }

    public int getNumberOfUserPoints() {
        return userPoints.size();
    }
}