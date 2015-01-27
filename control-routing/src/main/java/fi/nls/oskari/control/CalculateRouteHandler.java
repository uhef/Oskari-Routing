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
    private Map<String, Object> toGeoJSON(PGgeometry geometry) {
        Map<String, Object> ret = new HashMap<String, Object>();
        MultiLineString multiLineString = (MultiLineString)geometry.getGeometry();
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

    /*
    * Add the geometry types to the connection. Note that you
    * must cast the connection to the pgsql-specific connection
    * implementation before calling the addDataType() method.
    */
            ((org.postgresql.PGConnection) conn).addDataType("geometry", Class.forName("org.postgis.PGgeometry"));
            ((org.postgresql.PGConnection) conn).addDataType("box3d", Class.forName("org.postgis.PGbox3d"));
    /*
    * Create a statement and execute a select query.
    */
            Statement s = conn.createStatement();
            ResultSet r = s.executeQuery("select geom from tieviiva where gid = 10");
            PGgeometry geometry = null;
            while (r.next()) {
      /*
      * Retrieve the geometry as an object then cast it to the geometry type.
      * Print things out.
      */
                geometry = (PGgeometry) r.getObject(1);
                System.out.println(geometry.toString());
            }
            s.close();
            conn.close();
            ResponseHelper.writeResponse(params, mapper.writeValueAsString(toGeoJSON(geometry)));
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
}
