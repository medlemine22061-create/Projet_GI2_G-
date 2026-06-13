package model;

import model.enums.PriorityLevel;
import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a doctor or medical staff member using the platform.
 */
public class MedicalStaff implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String id;
    private String firstName;
    private String lastName;
    private String role;
    private String email;
    private String phoneNumber;
    private Hospital hospital;

    /**
     * Creates a medical staff member.
     *
     * @param id unique identifier
     * @param firstName first name
     * @param lastName last name
     * @param role role in the hospital
     * @param email email address
     * @param phoneNumber phone number
     * @param hospital hospital where the staff member works
     */
    public MedicalStaff(String id, String firstName, String lastName, String role,
                        String email, String phoneNumber, Hospital hospital) {
        this.id = MedicalSite.requireText(id, "id");
        this.firstName = MedicalSite.requireText(firstName, "firstName");
        this.lastName = MedicalSite.requireText(lastName, "lastName");
        this.role = MedicalSite.requireText(role, "role");
        this.email = MedicalSite.requireText(email, "email");
        this.phoneNumber = phoneNumber == null ? "" : phoneNumber;
        this.hospital = Objects.requireNonNull(hospital, "hospital cannot be null");
    }

    /**
     * Creates a delivery request.
     *
     * @param origin collection center
     * @param destination destination hospital
     * @param organType organ type
     * @param priorityLevel medical priority level
     * @return created delivery request
     */
    public DeliveryRequest createDeliveryRequest(CollectionCenter origin,
                                                 Hospital destination,
                                                 String organType,
                                                 PriorityLevel priorityLevel) {
        return new DeliveryRequest(
                "REQ-" + System.currentTimeMillis(),
                organType,
                priorityLevel,
                origin,
                destination,
                this
        );
    }

    /**
     * Validates the reception of a mission.
     *
     * @param mission mission to validate
     */
    public void validateReception(Mission mission) {
        Objects.requireNonNull(mission, "mission cannot be null");
        mission.confirmReception(this);
    }

    /**
     * Returns the unique identifier of this staff member.
     *
     * @return staff ID
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the first name.
     *
     * @return first name
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Returns the last name.
     *
     * @return last name
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Returns the phone number.
     *
     * @return phone number
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getRole() {
        return role;
    }

    public String getEmail() {
        return email;
    }

    public Hospital getHospital() {
        return hospital;
    }
}