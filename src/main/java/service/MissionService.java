package service;

import model.*;

public class MissionService {

    private DistanceService distanceService = new DistanceService();

    public DeliveryMission createMission(MedicalRequest request) {

        if (request == null ||
                request.getAssignedDroneBase() == null ||
                request.getDonorCenter() == null ||
                request.getReceiverHospital() == null) {
            return null;
        }

        double totalDistance = distanceService.calculateDistance(
                request.getAssignedDroneBase().getPosition(),
                request.getDonorCenter().getPosition()
        ) + distanceService.calculateDistance(
                request.getDonorCenter().getPosition(),
                request.getReceiverHospital().getPosition()
        );

        double estimatedTime = distanceService.calculateTravelTime(totalDistance, 60.0);

        DeliveryMission mission = new DeliveryMission(
                "MISSION-" + System.currentTimeMillis(),
                request.getAssignedDroneBase(),
                request.getDonorCenter(),
                request.getReceiverHospital(),
                request,
                totalDistance,
                estimatedTime
        );

        mission.setStatus("IN_PROGRESS");
        request.getAssignedDroneBase().dispatchDrone();

        return mission;
    }
}
