package fi.nls.oskari.routing;

import org.postgis.Geometry;
import org.postgis.PGgeometry;

public class DistanceNode {
    public Integer node;
    public Integer edge;
    public Double lon;
    public Double lat;
    public Double distance;
    private Geometry geometry;

    public void setGeom(Object input) {
        PGgeometry geom = (PGgeometry) input;
        this.geometry = geom.getGeometry();
    }

    public Geometry getGeometry() {
        return geometry;
    }
}
