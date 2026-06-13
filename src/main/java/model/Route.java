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

    /**
     * Adds an intermediate waypoint to the route.
     *
     * @param position waypoint position
     */
    public void addWaypoint(Position position) {
        if (position != null) {
            waypoints.add(position);
        }
    }

    /**
     * Removes a waypoint from the route.
     *
     * @param position waypoint to remove
     */
    public void removeWaypoint(Position position) {
        waypoints.remove(position);
    }

    /**
     * Computes the total route distance in canvas units.
     * Sums distances: origin -&gt; waypoints -&gt; destination.
     *
     * @return total distance
     */
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

    /**
     * Estimates the flight time based on drone speed.
     *
     * @param drone the drone that will fly this route
     * @return estimated time in hours
     */
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