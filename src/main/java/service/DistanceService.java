package service;

import model.Point2D;

public class DistanceService {

    public double calculateDistance(Point2D a, Point2D b) {

        double dx = b.getX() - a.getX();

        double dy = b.getY() - a.getY();

        return Math.sqrt(dx * dx + dy * dy);
    }

    public double calculateTravelTime(
            double distance,
            double droneSpeed
    ) {

        if (droneSpeed <= 0) {
            return 0;
        }

        return distance / droneSpeed;
    }
}
