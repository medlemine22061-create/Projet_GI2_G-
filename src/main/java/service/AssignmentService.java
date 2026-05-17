package service;

import model.*;

import java.util.List;

public class AssignmentService {

    private DistanceService distanceService = new DistanceService();

    public Hospital findNearestDonorCenter(MedicalRequest request, List<Hospital> hospitals) {

        Hospital bestCenter = null;
        double minDistance = Double.MAX_VALUE;

        for (Hospital hospital : hospitals) {

            if (hospital.canProvideOrgan(request.getOrganType())) {

                double distance = distanceService.calculateDistance(
                        hospital.getPosition(),
                        request.getReceiverHospital().getPosition()
                );

                if (distance < minDistance) {
                    minDistance = distance;
                    bestCenter = hospital;
                }
            }
        }

        return bestCenter;
    }

    public DroneBase findBestDroneBase(MedicalRequest request, List<DroneBase> bases) {

        if (request.getDonorCenter() == null) {
            return null;
        }

        DroneBase bestBase = null;
        double minTotalDistance = Double.MAX_VALUE;

        for (DroneBase base : bases) {

            if (!base.isActive() || !base.hasAvailableDrone()) {
                continue;
            }

            double distanceBaseToDonor = distanceService.calculateDistance(
                    base.getPosition(),
                    request.getDonorCenter().getPosition()
            );

            double distanceDonorToReceiver = distanceService.calculateDistance(
                    request.getDonorCenter().getPosition(),
                    request.getReceiverHospital().getPosition()
            );

            double totalDistance = distanceBaseToDonor + distanceDonorToReceiver;

            if (totalDistance < minTotalDistance) {
                minTotalDistance = totalDistance;
                bestBase = base;
            }
        }

        return bestBase;
    }

    public void assignRequestsToNearestBases(MapData mapData) {

        for (MedicalRequest request : mapData.getMedicalRequests()) {

            Hospital donorCenter = findNearestDonorCenter(
                    request,
                    mapData.getHospitals()
            );

            request.setDonorCenter(donorCenter);

            DroneBase bestBase = findBestDroneBase(
                    request,
                    mapData.getDroneBases()
            );

            request.setAssignedDroneBase(bestBase);

            if (donorCenter != null && bestBase != null) {

                double distance1 = distanceService.calculateDistance(
                        bestBase.getPosition(),
                        donorCenter.getPosition()
                );

                double distance2 = distanceService.calculateDistance(
                        donorCenter.getPosition(),
                        request.getReceiverHospital().getPosition()
                );

                double totalDistance = distance1 + distance2;

                request.setEstimatedDistance(totalDistance);
                request.setEstimatedTime(distanceService.calculateTravelTime(totalDistance, 60.0));
            }
        }
    }
}