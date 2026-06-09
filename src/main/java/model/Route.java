package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents the route followed by a drone.
 */
public class Route implements Serializable {
    private static final long serialVersionUID = 1L;

    private final CollectionCenter origin;
    private final Hospital destination;
    private final List<Position> waypoints;

    public Route(CollectionCenter origin, Hospital destination) {
        this.origin = Objects.requireNonNull(origin, "origin cannot be null");
        this.destination = Objects.requireNonNull(destination, "destination cannot be null");
        this.waypoints = new ArrayList<>();
    }

    public void addWaypoint(Position position) {
        if (position != null) {
            waypoints.add(position);
        }
    }

    public void removeWaypoint(Position position) {
        waypoints.remove(position);
    }

    public double computeDistance() {
        double distance = 0.0;

        Position current = origin.getPosition();

        for (Position waypoint : waypoints) {
            distance += current.distanceTo(waypoint);
            current = waypoint;
        }

        distance += current.distanceTo(destination.getPosition());

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

    @Override
    public String toString() {
        return "Route{origin="
                + origin.getName()
                + ", destination="
                + destination.getName()
                + ", distance="
                + computeDistance()
                + "}";
    }
}