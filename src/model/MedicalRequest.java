package model;

public class MedicalRequest extends MapElement {
    private OrganType organType;
    private PriorityLevel priority;
    private Hospital receiverHospital;
    private DroneBase assignedDroneBase;
    private double estimatedDistance;
    private double estimatedTime;
}