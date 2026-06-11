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

    public UserPoint(String id, Position position) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.position = Objects.requireNonNull(position, "position cannot be null");
    }

    public void updatePosition(Position position) {
        this.position = Objects.requireNonNull(position, "position cannot be null");
    }

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