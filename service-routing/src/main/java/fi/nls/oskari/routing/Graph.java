package fi.nls.oskari.routing;

import java.util.List;

public interface Graph {
    Boolean hasNeighbors(Integer node);

    List<DistanceNode> getNeighbors(Integer node);
}
