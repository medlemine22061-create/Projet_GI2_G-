package service;

import model.DelaunayTriangle;
import model.DroneBase;

import java.util.ArrayList;
import java.util.List;

public class DelaunayService {

    public List<DelaunayTriangle> computeDelaunayTriangles(
            List<DroneBase> bases
    ) {

        List<DelaunayTriangle> triangles = new ArrayList<>();

        if (bases == null || bases.size() < 3) {
            return triangles;
        }

        // Version simplifiée :
        // création de triangles successifs

        for (int i = 0; i < bases.size() - 2; i++) {

            DroneBase a = bases.get(i);

            DroneBase b = bases.get(i + 1);

            DroneBase c = bases.get(i + 2);

            DelaunayTriangle triangle =
                    new DelaunayTriangle(a, b, c);

            triangles.add(triangle);
        }

        return triangles;
    }
}
