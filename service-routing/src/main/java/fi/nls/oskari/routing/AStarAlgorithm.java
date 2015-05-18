package fi.nls.oskari.routing;

import org.postgis.Geometry;

import java.util.*;

public class AStarAlgorithm {
    private static class CameFrom {
        public Integer node;
        public Geometry geometry;

        public CameFrom(Integer node, Geometry geometry) {
            this.node = node;
            this.geometry = geometry;
        }
    }

    private static class PriorityNode implements Comparable<PriorityNode> {
        public Integer node;
        public Double priority;

        public PriorityNode(Integer node, Double priority) {
            this.node = node;
            this.priority = priority;
        }

        @Override
        public int compareTo(PriorityNode other) {
            if (priority < other.priority) {
                return -1;
            }
            if (priority > other.priority) {
                return 1;
            }
            return 0;
        }
    }

    public static Stack<Geometry> calculateRoute(RouteEndPoints endPoints, Integer startNode, Integer endNode, Graph graph) {
        Map<Integer, CameFrom> cameFrom = new HashMap<Integer, CameFrom>();
        Map<Integer, Double> gValue = new HashMap<Integer, Double>();
        PriorityQueue<PriorityNode> fringe = new PriorityQueue<PriorityNode>();

        fringe.add(new PriorityNode(startNode, 0.0));
        cameFrom.put(startNode, new CameFrom(startNode, null));
        gValue.put(startNode, 0.0);

        while(fringe.size() > 0) {
            Integer currentNode = fringe.poll().node;
            if (currentNode.equals(endNode)) {
                return reconstructPath(cameFrom, startNode, endNode);
            }

            if(graph.hasNeighbors(currentNode)) {
                List<DistanceNode> neighbors = graph.getNeighbors(currentNode);
                for(DistanceNode neighbor : neighbors) {
                    Double tentativeG = gValue.get(currentNode) + neighbor.distance;
                    if(!gValue.containsKey(neighbor.node) || tentativeG < gValue.get(neighbor.node)) {
                        gValue.put(neighbor.node, tentativeG);
                        Double priority = calculatePriority(tentativeG, neighbor.lon, neighbor.lat, endPoints.getEndLon(), endPoints.getEndLat());
                        fringe.add(new PriorityNode(neighbor.node, priority));
                        cameFrom.put(neighbor.node, new CameFrom(currentNode, neighbor.getGeometry()));
                    }
                }
            }
        }

        return null;
    }

    private static Stack<Geometry> reconstructPath(Map<Integer, CameFrom> cameFrom, Integer startNode, Integer endNode) {
        Stack<Geometry> route = new Stack<Geometry>();
        Integer current = endNode;
        while(!current.equals(startNode)) {
            CameFrom from = cameFrom.get(current);
            route.push(from.geometry);
            current = from.node;
        }
        return route;
    }

    private static Double calculatePriority(Double tentativeG, Double lon, Double lat, Double endLon, Double endLat) {
        Double lonDiff = lon - endLon;
        Double latDiff = lat - endLat;
        Double distanceBetweenNodes = Math.sqrt((lonDiff * lonDiff) + (latDiff * latDiff));
        return tentativeG + distanceBetweenNodes;
    }
}

