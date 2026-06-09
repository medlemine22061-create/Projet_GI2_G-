package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents one simplified Voronoi cell.
 *
 * A cell belongs to one medical site and contains the grid points that are
 * closer to this site than to the other sites.
 */
public class VoronoiCell implements Serializable {
    private static final long serialVersionUID = 1L;

    private final MedicalSite owner;
    private final List<Position> points;
    private double surface;
    private double density;
    private int numberOfMissions;

    public VoronoiCell(MedicalSite owner) {
        this.owner = Objects.requireNonNull(owner, "owner cannot be null");
        this.points = new ArrayList<>();
        this.surface = 0.0;
        this.density = 0.0;
        this.numberOfMissions = 0;
    }

    public void addPoint(Position position) {
        if (position != null) {
            points.add(position);
        }
    }

    /**
     * Alias kept if your old code uses addVertex().
     */
    public void addVertex(Position position) {
        addPoint(position);
    }

    /**
     * Simplified contains method.
     *
     * Since the cell is approximated by grid points, this method checks if the
     * tested position is close to one of the points of the cell.
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

    /**
     * Computes an approximated surface.
     *
     * Each grid point represents approximately a square of size step x step.
     */
    public double computeSurface(double step) {
        this.surface = points.size() * step * step;
        return surface;
    }

    /**
     * Default version if the step is not provided.
     */
    public double computeSurface() {
        return surface;
    }

    public double computeDensity() {
        if (surface <= 0) {
            density = 0.0;
        } else {
            density = numberOfMissions / surface;
        }

        return density;
    }

    public void incrementMissionCount() {
        numberOfMissions++;
        computeDensity();
    }

    public MedicalSite getOwner() {
        return owner;
    }

    public List<Position> getPoints() {
        return new ArrayList<>(points);
    }

    /**
     * Alias kept if your old code uses getVertices().
     */
    public List<Position> getVertices() {
        return getPoints();
    }

    public double getSurface() {
        return surface;
    }

    public double getDensity() {
        return density;
    }

    public int getNumberOfMissions() {
        return numberOfMissions;
    }
}