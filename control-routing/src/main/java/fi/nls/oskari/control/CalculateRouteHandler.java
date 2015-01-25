package fi.nls.oskari.control;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.util.ResponseHelper;
import org.postgis.PGgeometry;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

@OskariActionRoute("CalculateRoute")
public class CalculateRouteHandler extends ActionHandler {
    @Override
    public void handleAction(ActionParameters params) throws ActionException {
        try {
            Class.forName("org.postgresql.Driver");
            String url = "jdbc:postgresql://localhost:5432/oskaridb";
            java.sql.Connection conn = DriverManager.getConnection(url, "oskari", "W3jept2MqqZRX4J");

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
            while (r.next()) {
      /*
      * Retrieve the geometry as an object then cast it to the geometry type.
      * Print things out.
      */
                PGgeometry geom = (PGgeometry) r.getObject(1);
                System.out.println(geom.toString());
            }
            s.close();
            conn.close();
            ResponseHelper.writeResponse(params, "Hello " + params.getUser().getFirstname());
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
}
