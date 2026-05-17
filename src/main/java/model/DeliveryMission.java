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

    public DeliveryMission(
            String id,
            DroneBase droneBase,
            Hospital originHospital,
            Hospital destinationHospital,
            MedicalRequest request,
            double distance,
            double estimatedTime
    ) {

        this.id = id;
        this.droneBase = droneBase;
        this.originHospital = originHospital;
        this.destinationHospital = destinationHospital;
        this.request = request;
        this.distance = distance;
        this.estimatedTime = estimatedTime;

        this.status = "PENDING";
    }

    public String getId() {
        return id;
    }

    public DroneBase getDroneBase() {
        return droneBase;
    }

    public Hospital getOriginHospital() {
        return originHospital;
    }

    public Hospital getDestinationHospital() {
        return destinationHospital;
    }

    public MedicalRequest getRequest() {
        return request;
    }

    public double getDistance() {
        return distance;
    }

    public double getEstimatedTime() {
        return estimatedTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isCompleted() {
        return status.equals("COMPLETED");
    }

    public boolean isInProgress() {
        return status.equals("IN_PROGRESS");
    }

    public boolean isPending() {
        return status.equals("PENDING");
    }

    @Override
    public String toString() {

        return "DeliveryMission{" +
                "id='" + id + '\'' +
                ", droneBase=" + droneBase.getName() +
                ", originHospital=" + originHospital.getName() +
                ", destinationHospital=" + destinationHospital.getName() +
                ", distance=" + distance +
                ", estimatedTime=" + estimatedTime +
                ", status='" + status + '\'' +
                '}';
    }
}