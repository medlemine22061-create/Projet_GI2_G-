package model;

import model.enums.DroneStatus;
import java.io.Serializable;
import java.util.Objects;

public class Drone implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String id;
    private double autonomy;
    private double batteryLevel;
    private double maxPayload;
    private DroneStatus status;
    private Position position;
    private double speed;
    /**
     * Creates a drone.
     *
     * @param id unique identifier
     * @param position current position
     * @param autonomy maximum distance the drone can fly when fully charged
     * @param maxPayload maximum payload
     */
    public Drone(String id, double autonomy, double batteryLevel, double maxPayload, double speed, Position position) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.autonomy = autonomy;
        this.batteryLevel = batteryLevel;
        this.maxPayload = maxPayload;
        this.speed = speed;
        this.position = Objects.requireNonNull(position, "position cannot be null");
        this.status = DroneStatus.AVAILABLE;
    }

    /**
     * Returns true if this drone is available for a mission.
     *
     * @return true if status is AVAILABLE
     */
    public boolean isAvailable() {
        return status == DroneStatus.AVAILABLE;
    }

    /**
     * Checks whether this drone has enough autonomy and battery
     * to complete the given route.
     *
     * @param route the planned route
     * @return true if the drone can complete the route
     */
    public boolean canDoMission(Route route) {
        if (route == null) {
            return false;
        }

        double possibleDistance = autonomy * (batteryLevel / 100.0);
        return route.computeDistance() <= possibleDistance;
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
     * Updates the battery level.
     *
     * @param batteryLevel new battery level (0-100)
     * @throws IllegalArgumentException if value is out of range
     */
    public void updateBatteryLevel(double batteryLevel) {
        if (batteryLevel < 0 || batteryLevel > 100) {
            throw new IllegalArgumentException("batteryLevel must be between 0 and 100");
        }

        this.batteryLevel = batteryLevel;
    }

    /**
     * Returns the drone speed in km/h.
     *
     * @return speed
     */
    public double getSpeed() {
        return speed;
    }

    public String getId() {
        return id;
    }

    /**
     * Returns the maximum range of the drone in km.
     *
     * @return autonomy in km
     */
    public double getAutonomy() {
        return autonomy;
    }

    /**
     * Returns the current battery level (0-100).
     *
     * @return battery level as percentage
     */
    public double getBatteryLevel() {
        return batteryLevel;
    }

    /**
     * Returns the maximum payload the drone can carry in kg.
     *
     * @return max payload in kg
     */
    public double getMaxPayload() {
        return maxPayload;
    }

    /**
     * Returns the current operational status.
     *
     * @return drone status
     */
    public DroneStatus getStatus() {
        return status;
    }

    public Position getPosition() {
        return position;
    }

    /**
     * Sets the operational status of the drone.
     *
     * @param status new status
     */
    public void setStatus(DroneStatus status) {
        this.status = Objects.requireNonNull(status, "status cannot be null");
    }
    public void setSpeed(double speed) {
        if (speed <= 0) {
            throw new IllegalArgumentException("speed must be positive");
        }

        this.speed = speed;
    }
}