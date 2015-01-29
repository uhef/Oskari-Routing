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

    public Long hevonen() {
        try {
            SqlMapClient client = getSqlMapClient();
            Long results = (Long)client.queryForObject("Routing.foo");
            return results;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new RuntimeException("Failed to query", e);
        }
    }

    public List<Geometry> calculateRoute() {
        try {
            SqlMapClient client = getSqlMapClient();
            List results = client.queryForList("Routing.calculateRoute");
            List<Geometry> output = new ArrayList<Geometry>();
            for (Object result : results) {
                PGgeometry lol = (PGgeometry) result;
                output.add(lol.getGeometry());
            }
            return output;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new RuntimeException("Failed to query", e);
        }
    }
}
