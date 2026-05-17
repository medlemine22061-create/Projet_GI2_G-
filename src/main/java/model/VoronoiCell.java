package model;

import java.util.ArrayList;
import java.util.List;

public class VoronoiCell {

    private DroneBase site;

    private List<Point2D> vertices;

    private List<MedicalRequest> requests;

    private ZoneStatistics statistics;

    public VoronoiCell(DroneBase site) {

        this.site = site;

        this.vertices = new ArrayList<>();

        this.requests = new ArrayList<>();

        this.statistics = new ZoneStatistics();
    }

    public DroneBase getSite() {
        return site;
    }

    public List<Point2D> getVertices() {
        return vertices;
    }

    public List<MedicalRequest> getRequests() {
        return requests;
    }

    public ZoneStatistics getStatistics() {
        return statistics;
    }

    public void setStatistics(ZoneStatistics statistics) {
        this.statistics = statistics;
    }

    public void addVertex(Point2D point) {
        vertices.add(point);
    }

    public void addRequest(MedicalRequest request) {
        requests.add(request);
    }

    public int getRequestCount() {
        return requests.size();
    }

    @Override
    public String toString() {

        return "VoronoiCell{" +
                "site=" + site.getName() +
                ", vertices=" + vertices.size() +
                ", requests=" + requests.size() +
                '}';
    }
}
