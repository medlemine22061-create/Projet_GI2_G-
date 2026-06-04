package model;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a Voronoi cell owned by a medical site.
 */
public class VoronoiCell implements Serializable {

    private static final long serialVersionUID = 1L;

    private final MedicalSite owner;
    private final List<Point2D> vertices;
    private int numberOfMissions;

    /**
     * Creates a Voronoi cell.
     *
     * @param owner medical site that owns the cell
     */
    public VoronoiCell(MedicalSite owner) {
        this.owner = Objects.requireNonNull(owner, "owner cannot be null");
        this.vertices = new ArrayList<>();
        this.numberOfMissions = 0;
    }

    /**
     * Adds a vertex to the cell.
     *
     * @param position vertex position
     */
    public void addVertex(Point2D position) {
        vertices.add(Objects.requireNonNull(position, "position cannot be null"));
    }

    /**
     * Checks if a position belongs to the cell.
     * This simplified version only checks that the position is not null and that the cell has vertices.
     * A real geometric test can be implemented later.
     *
     * @param position position to test
     * @return true if the position is considered inside the cell
     */
    public boolean contains(Point2D position) {
        return position != null && !vertices.isEmpty();
    }

    /**
     * Computes the surface of the cell using the polygon shoelace formula.
     *
     * @return surface of the cell
     */
    public double computeSurface() {
        if (vertices.size() < 3) {
            return 0.0;
        }

        double sum = 0.0;

        for (int i = 0; i < vertices.size(); i++) {
            Point2D current = vertices.get(i);
            Point2D next = vertices.get((i + 1) % vertices.size());

            sum += current.getX() * next.getY();
            sum -= next.getX() * current.getY();
        }

        return Math.abs(sum) / 2.0;
    }

    /**
     * Computes the mission density in this cell.
     *
     * @return number of missions divided by the cell surface
     */
    public double computeDensity() {
        double surface = computeSurface();

        if (surface == 0.0) {
            return 0.0;
        }

        return numberOfMissions / surface;
    }

    /**
     * Increments the number of missions associated with this cell.
     */
    public void incrementMissionCount() {
        numberOfMissions++;
    }

    /**
     * Returns the medical site that owns this cell.
     *
     * @return owner medical site
     */
    public MedicalSite getOwner() {
        return owner;
    }

    /**
     * Returns the vertices of the cell.
     *
     * @return copy of the vertices list
     */
    public List<Point2D> getVertices() {
        return new ArrayList<>(vertices);
    }

    /**
     * Returns the number of missions associated with this cell.
     *
     * @return number of missions
     */
    public int getNumberOfMissions() {
        return numberOfMissions;
    }
}
