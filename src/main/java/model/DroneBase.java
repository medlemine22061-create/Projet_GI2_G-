package model;

public class DroneBase extends MapElement {

    private int availableDrones;
    private double batteryLevel;
    private double maxRange;
    private boolean active;

    public DroneBase(
            int id,
            String name,
            Point2D position,
            int availableDrones,
            double batteryLevel,
            double maxRange
    ) {

        super(id, name, position);

        this.availableDrones = availableDrones;
        this.batteryLevel = batteryLevel;
        this.maxRange = maxRange;
        this.active = true;
    }

    public int getAvailableDrones() {
        return availableDrones;
    }

    public void setAvailableDrones(int availableDrones) {
        this.availableDrones = availableDrones;
    }

    public double getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(double batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    public double getMaxRange() {
        return maxRange;
    }

    public void setMaxRange(double maxRange) {
        this.maxRange = maxRange;
    }

    public boolean isActive() {
        return active;
    }

    public void activate() {
        active = true;
    }

    public void deactivate() {
        active = false;
    }

    public boolean hasAvailableDrone() {
        return availableDrones > 0;
    }

    public void dispatchDrone() {

        if (availableDrones > 0) {
            availableDrones--;
        }
    }

    public void returnDrone() {
        availableDrones++;
    }

    @Override
    public String toString() {

        return "DroneBase{" +
                "name='" + getName() + '\'' +
                ", drones=" + availableDrones +
                ", battery=" + batteryLevel +
                ", range=" + maxRange +
                ", active=" + active +
                '}';
    }
}
