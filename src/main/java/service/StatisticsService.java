package service;

import model.MapData;
import model.MedicalRequest;
import model.VoronoiCell;
import model.ZoneStatistics;

public class StatisticsService {

    public ZoneStatistics computeZoneStatistics(VoronoiCell cell) {

        ZoneStatistics statistics = new ZoneStatistics();

        if (cell == null) {
            return statistics;
        }

        int requestCount = cell.getRequests().size();
        statistics.setRequestCount(requestCount);

        // Version simple : aire fictive pour éviter une géométrie trop complexe au début
        double area = 100.0;
        statistics.setArea(area);

        if (area > 0) {
            statistics.setDensity(requestCount / area);
        }

        if (requestCount == 0) {
            cell.setStatistics(statistics);
            return statistics;
        }

        double minDistance = Double.MAX_VALUE;
        double maxDistance = 0.0;
        double totalDistance = 0.0;
        double totalTime = 0.0;

        for (MedicalRequest request : cell.getRequests()) {

            double distance = request.getEstimatedDistance();
            double time = request.getEstimatedTime();

            if (distance < minDistance) {
                minDistance = distance;
            }

            if (distance > maxDistance) {
                maxDistance = distance;
            }

            totalDistance += distance;
            totalTime += time;
        }

        statistics.setMinDistance(minDistance);
        statistics.setMaxDistance(maxDistance);
        statistics.setAverageDistance(totalDistance / requestCount);
        statistics.setAverageDeliveryTime(totalTime / requestCount);

        cell.setStatistics(statistics);

        return statistics;
    }

    public void updateAllStatistics(MapData mapData) {

        if (mapData == null) {
            return;
        }

        for (VoronoiCell cell : mapData.getVoronoiCells()) {
            computeZoneStatistics(cell);
        }
    }
}