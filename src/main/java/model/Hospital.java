package model;

import java.util.Objects;

/**
 * Represents a hospital receiving an organ delivery.
 */
public class Hospital extends MedicalSite {

    private static final long serialVersionUID = 1L;

    private final boolean hasTransplantUnit;

    /**
     * Creates a hospital.
     *
     * @param id unique identifier
     * @param name hospital name
     * @param position hospital position
     * @param hasTransplantUnit true if the hospital has a transplant unit
     */
    public Hospital(String id, String name, Position position, boolean hasTransplantUnit) {
        super(id, name, position);
        this.hasTransplantUnit = hasTransplantUnit;
    }

    /**
     * Records that the hospital received a mission.
     *
     * @param mission received mission
     */
    public void receiveMission(Mission mission) {
        Objects.requireNonNull(mission, "mission cannot be null");
        mission.addHistoryEvent("Mission received by hospital " + getName());
    }

    public boolean hasTransplantUnit() {
        return hasTransplantUnit;
    }
}