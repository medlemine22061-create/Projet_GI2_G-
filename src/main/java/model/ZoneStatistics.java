package model;

public class ZoneStatistics {

    private int requestCount;

    private double area;

    private double density;

    private double minDistance;

    private double maxDistance;

    private double averageDistance;

    private double averageDeliveryTime;

    public ZoneStatistics() {

        this.requestCount = 0;

        this.area = 0;

        this.density = 0;

        this.minDistance = 0;

        this.maxDistance = 0;

        this.averageDistance = 0;

        this.averageDeliveryTime = 0;
    }

    public int getRequestCount() {
        return requestCount;
    }

    public void setRequestCount(int requestCount) {
        this.requestCount = requestCount;
    }

    public double getArea() {
        return area;
    }

    public void setArea(double area) {
        this.area = area;
    }

    public double getDensity() {
        return density;
    }

    public void setDensity(double density) {
        this.density = density;
    }

    public double getMinDistance() {
        return minDistance;
    }

    public void setMinDistance(double minDistance) {
        this.minDistance = minDistance;
    }

    public double getMaxDistance() {
        return maxDistance;
    }

    public void setMaxDistance(double maxDistance) {
        this.maxDistance = maxDistance;
    }

    public double getAverageDistance() {
        return averageDistance;
    }

    public void setAverageDistance(double averageDistance) {
        this.averageDistance = averageDistance;
    }

    public double getAverageDeliveryTime() {
        return averageDeliveryTime;
    }

    public void setAverageDeliveryTime(double averageDeliveryTime) {
        this.averageDeliveryTime = averageDeliveryTime;
    }

    @Override
    public String toString() {

        return "ZoneStatistics{" +
                "requestCount=" + requestCount +
                ", area=" + area +
                ", density=" + density +
                ", averageDistance=" + averageDistance +
                ", averageDeliveryTime=" + averageDeliveryTime +
                '}';
    }
}
