package service;

import model.MapData;

public class MapUpdateService {

    private AssignmentService assignmentService;
    private VoronoiService voronoiService;
    private DelaunayService delaunayService;
    private StatisticsService statisticsService;

    public MapUpdateService() {
        this.assignmentService = new AssignmentService();
        this.voronoiService = new VoronoiService();
        this.delaunayService = new DelaunayService();
        this.statisticsService = new StatisticsService();
    }

    public void updateMap(MapData mapData) {

        if (mapData == null) {
            return;
        }

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
