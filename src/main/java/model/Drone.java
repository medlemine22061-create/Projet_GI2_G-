package model;

import model.enums.DroneStatus;
import java.io.Serializable;
import java.util.Objects;

public class Drone implements Serializable {

    private static final long serialVersionUID = 1L;

    private double speed;
    private final String id;
    private double autonomy;
    private double batteryLevel;
    private double maxPayload;
    private DroneStatus status;
    private Position position;

    /**
     * Creates a drone.
     *
     * @param id unique identifier
     * @param position current position
     * @param autonomy maximum distance the drone can fly when fully charged
     * @param maxPayload maximum payload
     * @param speed the speed of the Drone
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

    public boolean isAvailable() {
        return status == DroneStatus.AVAILABLE;
    }

    public boolean canDoMission(Route route) {
        if (route == null) {
            return false;
        }

        double remainingAutonomy = autonomy * (batteryLevel / 100.0);
        return route.computeDistance() <= remainingAutonomy;
    }

    public void updatePosition(Position position) {
        this.position = Objects.requireNonNull(position, "position cannot be null");
    }

    public void updateBatteryLevel(double batteryLevel) {
        if (batteryLevel < 0 || batteryLevel > 100) {
            throw new IllegalArgumentException("batteryLevel must be between 0 and 100");
        }

        this.batteryLevel = batteryLevel;
    }

    public String getId() {
        return id;
    }

    public double getAutonomy() {
        return autonomy;
    }

    public double getBatteryLevel() {
        return batteryLevel;
    }

    public double getMaxPayload() {
        return maxPayload;
    }

    public DroneStatus getStatus() {
        return status;
    }

    public Position getPosition() {
        return position;
    }

    public void setStatus(DroneStatus status) {
        this.status = Objects.requireNonNull(status, "status cannot be null");
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        if (speed <= 0) {
            throw new IllegalArgumentException("speed must be positive");
        }

        this.speed = speed;
    }
}
