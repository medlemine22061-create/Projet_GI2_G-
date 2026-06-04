package model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Abstract parent class for medical sites displayed on the map.
 * A medical site can be a hospital or a collection center
 */
public abstract class MedicalSite implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String id;
    private String name;
    private Position position;

    /**
     * Creates a medical site.
     *
     * @param id unique identifier of the medical site
     * @param name name of the medical site
     * @param position position of the medical site on the map
     */
    protected MedicalSite(String id, String name, Position position) {
        this.id = requireText(id, "id");
        this.name = requireText(name, "name");
        this.position = Objects.requireNonNull(position, "position cannot be null");
    }

    /**
     * Updates the position of the medical site.
     *
     * @param position new position
     */
    public void updatePosition(Position position) {
        this.position = Objects.requireNonNull(position, "position cannot be null");
    }

    /**
     * Checks that a text value is not null or blank.
     *
     * @param value value to check
     * @param fieldName field name used in the error message
     * @return valid text
     */
    public static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be null or blank");
        }
        return value;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Position getPosition() {
        return position;
    }

    public void setName(String name) {
        this.name = requireText(name, "name");
    }

    @Override
    public String toString() {
        return name + " [" + id + "] at " + position;
    }
}