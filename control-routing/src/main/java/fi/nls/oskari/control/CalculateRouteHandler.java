package fi.nls.oskari.control;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.util.ResponseHelper;
import org.codehaus.jackson.map.ObjectMapper;
import org.postgis.LineString;
import org.postgis.MultiLineString;
import org.postgis.PGgeometry;
import org.postgis.Point;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@OskariActionRoute("CalculateRoute")
public class CalculateRouteHandler extends ActionHandler {
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
            Class.forName("org.postgresql.Driver");
            String url = "jdbc:postgresql://localhost:5432/oskaridb";
            java.sql.Connection conn = DriverManager.getConnection(url, "oskari", "W3jept2MqqZRX4J");
            ObjectMapper mapper = new ObjectMapper();

            ((org.postgresql.PGConnection) conn).addDataType("geometry", Class.forName("org.postgis.PGgeometry"));
            ((org.postgresql.PGConnection) conn).addDataType("box3d", Class.forName("org.postgis.PGbox3d"));

            Statement routeStatement = conn.createStatement();
            ResultSet routeResult = routeStatement.executeQuery("" +
                    "select geom2d from hkiroads where gid in " +
                    "(select id2 from pgr_astar('select gid as id, cast(source as int4), cast(target as int4), cost, x1, y1, x2, y2 from hkiroads', 265, 854, false, false))" +
                    "");
            List<LineString> routeLines = new ArrayList<LineString>();
            while (routeResult.next()) {
                PGgeometry linkGeometry = (PGgeometry) routeResult.getObject(1);
                routeLines.add((LineString) linkGeometry.getGeometry());
            }
            routeStatement.close();
            conn.close();

            LineString[] routeLineArray = new LineString[routeLines.size()];
            routeLines.toArray(routeLineArray);
            MultiLineString routeMultiLineString = new MultiLineString(routeLineArray);
            ResponseHelper.writeResponse(params, mapper.writeValueAsString(toGeoJSON(routeMultiLineString)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
