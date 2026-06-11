package model;

import model.enums.PriorityLevel;

import model.enums.DroneStatus;
import model.enums.MissionStatus;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents an actual drone delivery mission.
 */
public class Mission implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String id;
    private final DeliveryRequest request;
    private final Drone drone;
    private final Route route;
    private LocalDateTime startTime;
    private LocalDateTime arrivalTime;
    private MissionStatus status;
    private Position currentPosition;
    private double batteryLevel;
    private double organTemperature;
    private double shockLevel;
    private boolean receptionConfirmed;
    private MedicalStaff receivedBy;
    private final List<String> history;

    public Mission(DeliveryRequest request, Drone drone, Route route) {
        this.id = "M-" + System.currentTimeMillis();
        this.request = Objects.requireNonNull(request, "request cannot be null");
        this.drone = Objects.requireNonNull(drone, "drone cannot be null");
        this.route = Objects.requireNonNull(route, "route cannot be null");
        this.status = MissionStatus.CREATED;
        this.currentPosition = drone.getPosition();
        this.batteryLevel = drone.getBatteryLevel();
        this.receptionConfirmed = false;
        this.history = new ArrayList<>();
        addHistoryEvent("Mission created");
    }

    public void start() {
        status = MissionStatus.IN_PROGRESS;
        startTime = LocalDateTime.now();
        drone.setStatus(DroneStatus.IN_MISSION);
        addHistoryEvent("Mission started");
    }

    public void complete() {
        status = MissionStatus.DELIVERED;
        arrivalTime = LocalDateTime.now();
        drone.setStatus(DroneStatus.AVAILABLE);
        addHistoryEvent("Mission completed");
    }

    public void cancel() {
        status = MissionStatus.CANCELLED;
        drone.setStatus(DroneStatus.AVAILABLE);
        addHistoryEvent("Mission cancelled");
    }

    public void confirmReception(MedicalStaff staff) {
        this.receivedBy = Objects.requireNonNull(staff, "staff cannot be null");
        this.receptionConfirmed = true;
        this.status = MissionStatus.DELIVERED;
        this.arrivalTime = LocalDateTime.now();
        drone.setStatus(DroneStatus.AVAILABLE);
        addHistoryEvent("Reception confirmed by " + staff.getFullName());
    }

    public void updateTracking(Position position, double batteryLevel,
                               double organTemperature, double shockLevel) {
        this.currentPosition = Objects.requireNonNull(position, "position cannot be null");
        this.batteryLevel = batteryLevel;
        this.organTemperature = organTemperature;
        this.shockLevel = shockLevel;

        drone.updatePosition(position);

        if (batteryLevel >= 0 && batteryLevel <= 100) {
            drone.updateBatteryLevel(batteryLevel);
        }

        addHistoryEvent("Tracking updated at " + position);
    }

    public void addHistoryEvent(String event) {
        if (event != null && !event.isBlank()) {
            history.add(LocalDateTime.now() + " - " + event);
        }
    }

    public String getId() {
        return id;
    }

    public DeliveryRequest getRequest() {
        return request;
    }

    public Drone getDrone() {
        return drone;
    }

    public Route getRoute() {
        return route;
    }

    public MissionStatus getStatus() {
        return status;
    }

    public PriorityLevel getPriorityLevel() {
        return request.getPriorityLevel();
    }

    public boolean isReceptionConfirmed() {
        return receptionConfirmed;
    }

    public List<String> getHistory() {
        return new ArrayList<>(history);
    }

    public Position getCurrentPosition() {
        return currentPosition;
    }

    public double getBatteryLevel() {
        return batteryLevel;
    }
}
