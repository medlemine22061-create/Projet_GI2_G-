package model;

import model.enums.PriorityLevel;
import model.enums.RequestStatus;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a medical request for organ delivery.
 */
public class DeliveryRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String id;
    private final String organType;
    private final PriorityLevel priorityLevel;
    private final LocalDateTime requestDate;
    private RequestStatus status;
    private final CollectionCenter origin;
    private final Hospital destination;
    private final MedicalStaff createdBy;

    /**
     * Creates a delivery request.
     *
     * @param id unique identifier
     * @param organType requested organ type
     * @param priorityLevel medical priority level
     * @param origin origin collection center
     * @param destination destination hospital
     * @param createdBy medical staff member who created the request
     */
    public DeliveryRequest(String id, String organType, PriorityLevel priorityLevel,
                           CollectionCenter origin, Hospital destination, MedicalStaff createdBy) {
        this.id = MedicalSite.requireText(id, "id");
        this.organType = MedicalSite.requireText(organType, "organType");
        this.priorityLevel = Objects.requireNonNull(priorityLevel, "priorityLevel cannot be null");
        this.origin = Objects.requireNonNull(origin, "origin cannot be null");
        this.destination = Objects.requireNonNull(destination, "destination cannot be null");
        this.createdBy = Objects.requireNonNull(createdBy, "createdBy cannot be null");
        this.requestDate = LocalDateTime.now();
        this.status = RequestStatus.PENDING;
    }

    /**
     * Validates the request if the center has the organ and the hospital can receive it.
     *
     * @return true if the request is valid
     */
    public boolean validate() {
        if (origin.hasOrganType(organType) && destination.hasTransplantUnit()) {
            status = RequestStatus.VALIDATED;
            return true;
        }
        return false;
    }

    /**
     * Cancels the request.
     */
    public void cancel() {
        status = RequestStatus.CANCELLED;
    }

    public void updateStatus(RequestStatus status) {
        this.status = Objects.requireNonNull(status, "status cannot be null");
    }

    public String getId() {
        return id;
    }

    public String getOrganType() {
        return organType;
    }

    public PriorityLevel getPriorityLevel() {
        return priorityLevel;
    }

    public LocalDateTime getRequestDate() {
        return requestDate;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public CollectionCenter getOrigin() {
        return origin;
    }

    public Hospital getDestination() {
        return destination;
    }

    public MedicalStaff getCreatedBy() {
        return createdBy;
    }
}