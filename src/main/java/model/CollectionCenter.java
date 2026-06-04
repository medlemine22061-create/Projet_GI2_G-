package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a medical collection center where organs are prepared.
 */
public class CollectionCenter extends MedicalSite {

    private static final long serialVersionUID = 1L;

    private final List<String> availableOrganTypes;

    /**
     * Creates a collection center.
     *
     * @param id unique identifier
     * @param name center name
     * @param position center position
     * @param availableOrganTypes organ types available in the center
     */
    public CollectionCenter(String id, String name, Position position, List<String> availableOrganTypes) {
        super(id, name, position);
        this.availableOrganTypes = availableOrganTypes == null
                ? new ArrayList<>()
                : new ArrayList<>(availableOrganTypes);
    }

    /**
     * Adds an organ type to the center.
     *
     * @param organType organ type to add
     */
    public void addOrganType(String organType) {
        if (organType != null && !organType.isBlank() && !availableOrganTypes.contains(organType)) {
            availableOrganTypes.add(organType);
        }
    }

    /**
     * Removes an organ type from the center.
     *
     * @param organType organ type to remove
     */
    public void removeOrganType(String organType) {
        availableOrganTypes.remove(organType);
    }

    /**
     * Checks if the center has a specific organ type.
     *
     * @param organType organ type
     * @return true if the organ type is available
     */
    public boolean hasOrganType(String organType) {
        return availableOrganTypes.contains(organType);
    }

    /**
     * Prepares an organ for delivery.
     * Since the MedicalContainer class was removed, this method only validates availability.
     *
     * @param organType organ type to prepare
     */
    public void prepareOrgan(String organType) {
        if (!hasOrganType(organType)) {
            throw new IllegalArgumentException("Organ type not available: " + organType);
        }
    }

    public List<String> getAvailableOrganTypes() {
        return new ArrayList<>(availableOrganTypes);
    }
}