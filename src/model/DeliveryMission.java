package model;

public class DeliveryMission {
    private String id;
    private DroneBase droneBase;
    private Hospital originHospital;
    private Hospital destinationHospital;
    private MedicalRequest request;
    private double distance;
    private double estimatedTime;
    private String status;
}
