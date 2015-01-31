package fi.nls.oskari.routing;

import com.ibatis.common.resources.Resources;
import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapClientBuilder;
import com.ibatis.sqlmap.client.SqlMapSession;
import org.postgis.Geometry;
import org.postgis.PGgeometry;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class RoutingService {
    private SqlMapClient client = null;
    private static String SQL_MAP_LOCATION = "META-INF/SqlMapConfig.xml";

    private SqlMapClient getSqlMapClient() {
        if (client != null) { return client; }

        Reader reader = null;
        try {
            reader = Resources.getResourceAsReader(SQL_MAP_LOCATION);
            client = SqlMapClientBuilder.buildSqlMapClient(reader);
            return client;
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve SQL client", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public List<Geometry> calculateRoute(RouteEndPoints endPoints, String algorithm) {
        if (algorithm.equals("astar")) {
            SqlMapClient client = getSqlMapClient();
            SqlMapSession session = client.openSession();
            try {
                PgRoutingTableGraph graph = new PgRoutingTableGraph(session);
                Integer startNode = (Integer)session.queryForObject("Routing.closestNode", new Coordinates(endPoints.getStartLon(), endPoints.getStartLat()));
                Integer endNode = (Integer)session.queryForObject("Routing.closestNode", new Coordinates(endPoints.getEndLon(), endPoints.getEndLat()));
                return AStarAlgorithm.calculateRoute(endPoints, startNode, endNode, graph);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                throw new RuntimeException("A Star algorithm or graph enquery failed: ", e);
            } finally {
                session.close();
            }
        } else {
            try {
                SqlMapClient client = getSqlMapClient();
                List<Geometry> results = new ArrayList<Geometry>();
                for (Object row : client.queryForList("Routing.calculateRoute", endPoints)) {
                    PGgeometry geometry = (PGgeometry) row;
                    results.add(geometry.getGeometry());
                }
                return results;
            } catch (Exception e) {
                System.out.println(e.getMessage());
                throw new RuntimeException("Failed to query", e);
            }
        }
    }
}
