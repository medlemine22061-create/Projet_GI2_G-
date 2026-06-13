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

    /**
     * Starts the mission: sets status to IN_PROGRESS and records start time.
     */
    public void start() {
        status = MissionStatus.IN_PROGRESS;
        startTime = LocalDateTime.now();
        drone.setStatus(DroneStatus.IN_MISSION);
        addHistoryEvent("Mission started");
    }

    /**
     * Completes the mission: sets status to DELIVERED.
     */
    public void complete() {
        status = MissionStatus.DELIVERED;
        arrivalTime = LocalDateTime.now();
        drone.setStatus(DroneStatus.AVAILABLE);
        addHistoryEvent("Mission completed");
    }

    /**
     * Cancels the mission and makes the drone available again.
     */
    public void cancel() {
        status = MissionStatus.CANCELLED;
        drone.setStatus(DroneStatus.AVAILABLE);
        addHistoryEvent("Mission cancelled");
    }

    /**
     * Confirms that the organ has been received by a medical staff member.
     *
     * @param staff the staff member who received the delivery
     */
    public void confirmReception(MedicalStaff staff) {
        this.receivedBy = Objects.requireNonNull(staff, "staff cannot be null");
        this.receptionConfirmed = true;
        this.status = MissionStatus.DELIVERED;
        this.arrivalTime = LocalDateTime.now();
        drone.setStatus(DroneStatus.AVAILABLE);
        addHistoryEvent("Reception confirmed by " + staff.getFullName());
    }

    /**
     * Updates live tracking data during the mission.
     *
     * @param position       current drone position
     * @param batteryLevel   current battery level (0-100)
     * @param organTemperature temperature of the organ container
     * @param shockLevel       vibration/shock level recorded
     */
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

    /**
     * Appends an event to the mission history log.
     *
     * @param event description of the event
     */
    public void addHistoryEvent(String event) {
        if (event != null && !event.isBlank()) {
            history.add(LocalDateTime.now() + " - " + event);
        }
    }

    /**
     * Returns the unique mission identifier.
     *
     * @return mission ID
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the delivery request linked to this mission.
     *
     * @return delivery request
     */
    public DeliveryRequest getRequest() {
        return request;
    }

    /**
     * Returns the drone assigned to this mission.
     *
     * @return assigned drone
     */
    public Drone getDrone() {
        return drone;
    }

    /**
     * Returns the planned route for this mission.
     *
     * @return mission route
     */
    public Route getRoute() {
        return route;
    }

    /**
     * Returns the current mission status.
     *
     * @return mission status
     */
    public MissionStatus getStatus() {
        return status;
    }

    /**
     * Returns the priority level of the associated request.
     *
     * @return priority level
     */
    public PriorityLevel getPriorityLevel() {
        return request.getPriorityLevel();
    }

    /**
     * Returns true if the hospital has confirmed reception of the organ.
     *
     * @return true if reception confirmed
     */
    public boolean isReceptionConfirmed() {
        return receptionConfirmed;
    }

    /**
     * Returns the full event history of the mission.
     *
     * @return list of history events
     */
    public List<String> getHistory() {
        return new ArrayList<>(history);
    }

    /**
     * Returns the current position of the drone during the mission.
     *
     * @return current drone position
     */
    public Position getCurrentPosition() {
        return currentPosition;
    }

    /**
     * Returns the current battery level (0-100).
     *
     * @return battery level as percentage
     */
    public double getBatteryLevel() {
        return batteryLevel;
    }
}