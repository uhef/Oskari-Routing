package fi.nls.oskari.routing;

import com.ibatis.common.resources.Resources;
import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapClientBuilder;
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
            return AStarAlgorithm.calculateRoute(endPoints, 0, 0, new Graph() {
                @Override
                public Boolean hasNeighbors(Integer node) {
                    return null;
                }

                @Override
                public List<DistanceNode> getNeighbors(Integer node) {
                    return null;
                }
            });
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
