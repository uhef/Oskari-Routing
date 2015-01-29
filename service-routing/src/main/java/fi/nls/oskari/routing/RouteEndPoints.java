package fi.nls.oskari.routing;

public class RouteEndPoints {
    private Double startLon;
    private Double startLat;
    private Double endLon;
    private Double endLat;

    public RouteEndPoints(Double startLon, Double startLat, Double endLon, Double endLat) {
        this.startLon = startLon;
        this.startLat = startLat;
        this.endLon = endLon;
        this.endLat = endLat;
    }

    public Double getStartLon() {
        return startLon;
    }

    public Double getStartLat() {
        return startLat;
    }

    public Double getEndLon() {
        return endLon;
    }

    public Double getEndLat() {
        return endLat;
    }
}
