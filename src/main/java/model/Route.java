package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a route between a collection center and a hospital.
 */
public class Route implements Serializable {

    private static final long serialVersionUID = 1L;

    private final CollectionCenter origin;
    private final Hospital destination;
    private final List<Position> waypoints;

    /**
     * Creates a route.
     *
     * @param origin origin collection center
     * @param destination destination hospital
     */
    public Route(CollectionCenter origin, Hospital destination) {
        this.origin = Objects.requireNonNull(origin, "origin cannot be null");
        this.destination = Objects.requireNonNull(destination, "destination cannot be null");
        this.waypoints = new ArrayList<>();
    }

    public void addWaypoint(Position position) {
        waypoints.add(Objects.requireNonNull(position, "position cannot be null"));
    }

    public void removeWaypoint(Position position) {
        waypoints.remove(position);
    }

    public double computeDistance() {
        double distance = 0.0;
        Position previous = origin.getPosition();

        for (Position waypoint : waypoints) {
            distance += previous.distanceTo(waypoint);
            previous = waypoint;
        }

        distance += previous.distanceTo(destination.getPosition());
        return distance;
    }

    public double estimateTime(Drone drone) {
        if (drone == null || drone.getSpeed() <= 0) {
            return 0.0;
        }

        return computeDistance() / drone.getSpeed();
    }

    public CollectionCenter getOrigin() {
        return origin;
    }

    public Hospital getDestination() {
        return destination;
    }

    public List<Position> getWaypoints() {
        return new ArrayList<>(waypoints);
    }
}
