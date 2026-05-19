package model;

public class MedicalRequest {

    private String name;
    private OrganType organType;
    private Hospital receiverHospital;
    private DroneBase assignedDroneBase;

    public MedicalRequest(String name,
                          OrganType organType,
                          Hospital receiverHospital) {

        this.name = name;
        this.organType = organType;
        this.receiverHospital = receiverHospital;
    }

    public String getName() {
        return name;
    }

    public OrganType getOrganType() {
        return organType;
    }

    public Hospital getReceiverHospital() {
        return receiverHospital;
    }

    public DroneBase getAssignedDroneBase() {
        return assignedDroneBase;
    }

    public void setAssignedDroneBase(DroneBase assignedDroneBase) {
        this.assignedDroneBase = assignedDroneBase;
    }
}