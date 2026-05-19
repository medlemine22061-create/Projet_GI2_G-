package service;

import model.DroneBase;
import model.MedicalRequest;

import java.util.List;

public class AssignmentService {

    private DistanceService distanceService = new DistanceService();

    public DroneBase findNearestDroneBase(
            MedicalRequest request,
            List<DroneBase> bases) {

        DroneBase nearest = null;
        double minDistance = Double.MAX_VALUE;

        for (DroneBase base : bases) {

            double distance =
                    distanceService.calculateDistance(
                            base.getPosition(),
                            request.getReceiverHospital().getPosition()
                    );

            if (distance < minDistance) {
                minDistance = distance;
                nearest = base;
            }
        }

        return nearest;
    }
}