package model;

public class MedicalRequest extends MapElement {

    private OrganType organType;
    private PriorityLevel priority;

    private Hospital receiverHospital;
    private Hospital donorCenter;

    private DroneBase assignedDroneBase;

    private double estimatedDistance;
    private double estimatedTime;

    public MedicalRequest(
            int id,
            String name,
            OrganType organType,
            PriorityLevel priority,
            Hospital receiverHospital
    ) {
        super(id, name, receiverHospital.getPosition());

        this.organType = organType;
        this.priority = priority;
        this.receiverHospital = receiverHospital;
        this.donorCenter = null;
        this.assignedDroneBase = null;
        this.estimatedDistance = 0;
        this.estimatedTime = 0;
    }

    public OrganType getOrganType() {
        return organType;
    }

    public PriorityLevel getPriority() {
        return priority;
    }

    public Hospital getReceiverHospital() {
        return receiverHospital;
    }

    public Hospital getDonorCenter() {
        return donorCenter;
    }

    public void setDonorCenter(Hospital donorCenter) {
        this.donorCenter = donorCenter;
    }

    public DroneBase getAssignedDroneBase() {
        return assignedDroneBase;
    }

    public void setAssignedDroneBase(DroneBase assignedDroneBase) {
        this.assignedDroneBase = assignedDroneBase;
    }

    public double getEstimatedDistance() {
        return estimatedDistance;
    }

    public void setEstimatedDistance(double estimatedDistance) {
        this.estimatedDistance = estimatedDistance;
    }

    public double getEstimatedTime() {
        return estimatedTime;
    }

    public void setEstimatedTime(double estimatedTime) {
        this.estimatedTime = estimatedTime;
    }

    @Override
    public String toString() {
        return "MedicalRequest{" +
                "name='" + getName() + '\'' +
                ", organType=" + organType +
                ", receiverHospital=" + receiverHospital.getName() +
                ", donorCenter=" + (donorCenter != null ? donorCenter.getName() : "none") +
                ", assignedDroneBase=" + (assignedDroneBase != null ? assignedDroneBase.getName() : "none") +
                ", distance=" + estimatedDistance +
                '}';
    }
}