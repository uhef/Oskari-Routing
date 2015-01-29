package fi.nls.oskari.control;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.routing.RouteEndPoints;
import fi.nls.oskari.routing.RoutingService;
import fi.nls.oskari.util.ResponseHelper;
import org.codehaus.jackson.map.ObjectMapper;
import org.postgis.Geometry;
import org.postgis.LineString;
import org.postgis.MultiLineString;
import org.postgis.Point;

import java.util.*;

@OskariActionRoute("CalculateRoute")
public class CalculateRouteHandler extends ActionHandler {
    private RoutingService routingService = new RoutingService();

    private Map<String, Object> toGeoJSON(MultiLineString multiLineString) {
        Map<String, Object> ret = new HashMap<String, Object>();
        List<List<List<Double>>> coordinates = new ArrayList<List<List<Double>>>();
        for(int i = 0; i < multiLineString.getLines().length; ++i) {
            LineString lineString = multiLineString.getLine(i);
            List<List<Double>> lineStringCoordinates = new ArrayList<List<Double>>();
            for(int j = 0; j < lineString.numPoints(); ++j) {
                Point point = lineString.getPoint(j);
                List<Double> pointCoordinates = new ArrayList<Double>();
                pointCoordinates.add(point.x);
                pointCoordinates.add(point.y);
                lineStringCoordinates.add(pointCoordinates);
            }
            coordinates.add(lineStringCoordinates);
        }
        ret.put("type", "MultiLineString");
        ret.put("coordinates", coordinates);
        return ret;
    }

    @Override
    public void handleAction(ActionParameters params) throws ActionException {
        try {
            System.out.println("Start lon: " + params.getRequiredParam("startLon"));
            System.out.println("Start lat: " + params.getRequiredParam("startLat"));
            System.out.println("End lon: " + params.getRequiredParam("endLon"));
            System.out.println("End lat: " + params.getRequiredParam("endLat"));

            Long foo = routingService.hevonen();
            System.out.println("Routing service says: " + foo);

            List<LineString> routeLines = new ArrayList<LineString>();
            List<Geometry> geometries = routingService.calculateRoute(
                    new RouteEndPoints(
                            Double.parseDouble(params.getRequiredParam("startLon")),
                            Double.parseDouble(params.getRequiredParam("startLat")),
                            Double.parseDouble(params.getRequiredParam("endLon")),
                            Double.parseDouble(params.getRequiredParam("endLat"))));
            Iterator<Geometry> resultIterator = geometries.iterator();
            while (resultIterator.hasNext()) {
                routeLines.add((LineString)resultIterator.next());
            }

            ObjectMapper mapper = new ObjectMapper();
            LineString[] routeLineArray = new LineString[routeLines.size()];
            routeLines.toArray(routeLineArray);
            MultiLineString routeMultiLineString = new MultiLineString(routeLineArray);
            ResponseHelper.writeResponse(params, mapper.writeValueAsString(toGeoJSON(routeMultiLineString)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
