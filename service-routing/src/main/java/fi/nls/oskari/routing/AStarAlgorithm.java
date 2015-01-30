package fi.nls.oskari.routing;

import org.postgis.Geometry;

import java.util.*;

public class AStarAlgorithm {
    private static class CameFrom {
        public Integer node;
        public Integer edge;

        public CameFrom(Integer node, Integer edge) {
            this.node = node;
            this.edge = edge;
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

    public static List<Geometry> calculateRoute(RouteEndPoints endPoints, Integer startNode, Integer endNode, Graph graph) {
        Map<Integer, CameFrom> cameFrom = new HashMap<Integer, CameFrom>();
        Map<Integer, Double> gValue = new HashMap<Integer, Double>();
        PriorityQueue<PriorityNode> fringe = new PriorityQueue<PriorityNode>();

        fringe.add(new PriorityNode(startNode, 0.0));
        cameFrom.put(startNode, new CameFrom(startNode, null));
        gValue.put(startNode, 0.0);

        while(fringe.size() > 0) {
            Integer currentNode = fringe.poll().node;
            if(currentNode == endNode) {
                System.out.println("I would reconstruct the path now...");
                return new ArrayList<Geometry>();
            }

            if(graph.hasNeighbors(currentNode)) {
                List<DistanceNode> neighbors = graph.getNeighbors(currentNode);
                for(DistanceNode neighbor : neighbors) {
                    Double tentativeG = gValue.get(currentNode) + neighbor.distance;
                    if(!gValue.containsKey(neighbor.node) || tentativeG < gValue.get(neighbor.node)) {
                        gValue.put(neighbor.node, tentativeG);
                        Double priority = calculatePriority();
                        fringe.add(new PriorityNode(neighbor.node, priority));
                        cameFrom.put(neighbor.node, new CameFrom(currentNode, neighbor.edge));
                    }
                }
            }
        }

        System.out.println("I didn't find a solution :(...");
        return null;
    }

    private static Double calculatePriority() {
        return 0.0;
    }
}
