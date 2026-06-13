package model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a user point on the map.
 * In our project, a user point can represent a delivery request location,
 * a medical demand point, or a point to assign to the nearest medical site.
 */
public class UserPoint implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String id;
    private Position position;
    private MedicalSite nearestSite;

    /**
     * Creates a user point (doctor) on the map.
     * In this project, each user point represents a medical doctor
     * who can issue delivery requests from their hospital.
     *
     * @param id       unique identifier (e.g. "DOC-M1")
     * @param position position on the map (same as the doctor's hospital)
     */
    public UserPoint(String id, Position position) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.position = Objects.requireNonNull(position, "position cannot be null");
    }

    /**
     * Updates the current position of the drone.
     *
     * @param position new position
     */
    public void updatePosition(Position position) {
        this.position = Objects.requireNonNull(position, "position cannot be null");
    }

    /**
     * Assigns the nearest medical site to this user point (computed by Voronoi).
     *
     * @param nearestSite nearest site
     */
    public void assignNearestSite(MedicalSite nearestSite) {
        this.nearestSite = nearestSite;
    }

    public String getId() {
        return id;
    }

    public Position getPosition() {
        return position;
    }

    public MedicalSite getNearestSite() {
        return nearestSite;
    }

    @Override
    public String toString() {
        String nearestName = nearestSite == null ? "none" : nearestSite.getName();

        return "UserPoint{"
                + "id='" + id + '\''
                + ", position=(" + position.getX() + ", " + position.getY() + ")"
                + ", nearestSite=" + nearestName
                + '}';
    }
}