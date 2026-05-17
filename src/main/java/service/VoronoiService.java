package service;

import model.DroneBase;
import model.MedicalRequest;
import model.VoronoiCell;

import java.util.ArrayList;
import java.util.List;

public class VoronoiService {

    private DistanceService distanceService = new DistanceService();

    public List<VoronoiCell> computeVoronoiCells(
            List<DroneBase> bases,
            List<MedicalRequest> requests
    ) {

        List<VoronoiCell> cells = new ArrayList<>();

        if (bases == null || bases.isEmpty()) {
            return cells;
        }

        // Création d'une cellule par base
        for (DroneBase base : bases) {

            VoronoiCell cell = new VoronoiCell(base);

            cells.add(cell);
        }

        // Répartition des requêtes dans la cellule la plus proche
        for (MedicalRequest request : requests) {

            VoronoiCell nearestCell = null;

            double minDistance = Double.MAX_VALUE;

            for (VoronoiCell cell : cells) {

                double distance = distanceService.calculateDistance(
                        request.getPosition(),
                        cell.getSite().getPosition()
                );

                if (distance < minDistance) {

                    minDistance = distance;

                    nearestCell = cell;
                }
            }

            if (nearestCell != null) {

                nearestCell.addRequest(request);
            }
        }

        return cells;
    }
}
