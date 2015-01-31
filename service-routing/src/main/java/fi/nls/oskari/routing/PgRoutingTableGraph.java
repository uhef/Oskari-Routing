package fi.nls.oskari.routing;

import com.ibatis.sqlmap.client.SqlMapSession;

import java.sql.SQLException;
import java.util.List;

public class PgRoutingTableGraph implements Graph {
    private SqlMapSession session;

    public PgRoutingTableGraph(SqlMapSession session) {
        this.session = session;
    }

    @Override
    public Boolean hasNeighbors(Integer node) {
        try {
            return (Boolean) session.queryForObject("Routing.hasNeighbors", node);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException("Routing.hasNeighbors failed: ", e);
        }
    }

    @Override
    public List<DistanceNode> getNeighbors(Integer node) {
        try {
            return session.queryForList("Routing.neighbors", node);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException("Routing.neighbors failed: ", e);
        }
    }
}
