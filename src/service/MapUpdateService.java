package service;

import model.MapData;

public class MapUpdateService {
    private AssignmentService assignmentService;
    private VoronoiService voronoiService;
    private DelaunayService delaunayService;
    private StatisticsService statisticsService;

    public void updateMap(MapData mapData) {
        assignmentService.assignRequestsToNearestBases(mapData);
        mapData.setVoronoiCells(voronoiService.computeVoronoiCells(
                mapData.getDroneBases(),
                mapData.getMedicalRequests()
        ));
        mapData.setDelaunayTriangles(delaunayService.computeDelaunayTriangles(
                mapData.getDroneBases()
        ));
        statisticsService.updateAllStatistics(mapData);
    }
}
