package model;

import java.util.ArrayList;
import java.util.List;

public class MapData {

    private List<DroneBase> droneBases = new ArrayList<>();
    private List<Hospital> hospitals = new ArrayList<>();

    public List<DroneBase> getDroneBases() {
        return droneBases;
    }

    public List<Hospital> getHospitals() {
        return hospitals;
    }
}