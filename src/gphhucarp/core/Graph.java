package gphhucarp.core;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.random.RandomDataGenerator;

import java.util.*;

/**
 * A graph contains a number of nodes and arcs.
 * Each node has an integer id.
 * Each arc is an Arc class object.
 *
 * Created by gphhucarp on 14/06/17.
 */
public class Graph {
    private List<Integer> nodes; // the node ids are ascendingly ordered
    private Map<Pair<Integer, Integer>, Arc> arcMap;
    private double[][] estCostMatrix; // the estimated cost of edges
    private double[][] estDistMatrix; // the estimated distance between nodes
    private int[][] pathFrom; // the precedent node of j along the shortest path from i to j
    private int[][] pathTo; // the successive node of i along the shortest path from i to j
    private Map<Integer, List<Arc>> outNeighbourMap; // the outgoing neighbours of each node.
    private Map<Integer, List<Arc>> inNeighbourMap; // the incoming neighbours of each node.

    public Graph(List<Integer> nodes, Map<Pair<Integer, Integer>, Arc> arcMap) {
        this.nodes = nodes;
        this.arcMap = arcMap;
        calcNeighbours();
        calcEstDistMatrix();
    }

    public List<Integer> getNodes() {
        return nodes;
    }

    public Map<Pair<Integer, Integer>, Arc> getArcMap() {
        return arcMap;
    }

    public Arc getArc(int fromNode, int toNode) {
        return arcMap.get(Pair.of(fromNode, toNode));
    }

    public List<Arc> getOutNeighbour(int node) {
        return outNeighbourMap.get(node);
    }

    public List<Arc> getInNeighbour(int node) {
        return inNeighbourMap.get(node);
    }

    /**
     * Calculate the outgoing and incoming neighbours of each node of the graph.
     */
    public void calcNeighbours() {
        outNeighbourMap = new HashMap<>();
        inNeighbourMap = new HashMap<>();

        for (int i = 0; i < nodes.size(); i++) {
            outNeighbourMap.put(nodes.get(i), new ArrayList<>());
            inNeighbourMap.put(nodes.get(i), new ArrayList<>());
        }

        for (Arc arc : arcMap.values()) {
            outNeighbourMap.get(arc.getFrom()).add(arc);
            inNeighbourMap.get(arc.getTo()).add(arc);
        }
    }

    /**
     * Calculate the estimated distance matrix by running Dijkstra's algorithm
     * on the estimated cost matrix, starting from each node.
     * This is efficient for sparse graphs.
     */
    private void calcEstDistMatrix() {
        int maxNodeId = nodes.get(nodes.size()-1); // get the boundaries of the matrices
        estCostMatrix = new double[maxNodeId+1][maxNodeId+1];
        estDistMatrix = new double[maxNodeId+1][maxNodeId+1];
        pathFrom = new int[maxNodeId+1][maxNodeId+1]; // -1 means no precedent node
        pathTo = new int[maxNodeId+1][maxNodeId+1];

        for (int i = 0; i < nodes.size(); i++) {
            for (int j = 0; j < nodes.size(); j++) {
                estCostMatrix[nodes.get(i)][nodes.get(j)] = Double.POSITIVE_INFINITY;
                estDistMatrix[nodes.get(i)][nodes.get(j)] = Double.POSITIVE_INFINITY;
            }
            estCostMatrix[nodes.get(i)][nodes.get(i)] = 0;
            estDistMatrix[nodes.get(i)][nodes.get(i)] = 0;
        }

        // initialise estCostMatrix with the expected costs, i.e. the mean of the random distributions
        for (Arc arc : arcMap.values())
            estCostMatrix[arc.getFrom()][arc.getTo()] = arc.getExpectedDeadheadingCost();

        for (int i = 0; i < nodes.size(); i++) {
            calcEstDistancesFrom(nodes.get(i));
        }

        // get the successive nodes from the predecent nodes in the shortest paths
        for (int i = 0; i < nodes.size(); i++) {
            for (int j = 0; j < nodes.size(); j++) {
                if (j == i)
                    continue;

                int currNode = nodes.get(j);
                int predNode = pathFrom[nodes.get(i)][currNode];

                while (predNode != nodes.get(i)) {
                    currNode = predNode;
                    predNode = pathFrom[nodes.get(i)][currNode];
                }

                pathTo[nodes.get(i)][nodes.get(j)] = currNode;
            }
        }
    }

    /**
     * Calculate the estimated distances from a node to all other nodes.
     * This is done by Dijkstra's algorithm.
     * @param node the starting node.
     */
    private void calcEstDistancesFrom(int node) {
        // local class for search nodes in the priority queue
        class SearchNode {
            private int node;
            private double pathLength;
            private int pathFrom;

            public SearchNode(int node, double pathLength, int pathFrom) {
                this.node = node;
                this.pathLength = pathLength;
                this.pathFrom = pathFrom;
            }
        }

        // the priority queue uses path length as priority, the smaller the better
        // the tie breaker is the node id.
        PriorityQueue<SearchNode> pq =
                new PriorityQueue<>((o1, o2) -> {
                    double lengthDiff = o1.pathLength - o2.pathLength;

                    if (lengthDiff < 0)
                        return -1;
                    if (lengthDiff > 0)
                        return 1;
                    if (o1.node < o2.node)
                        return -1;
                    if (o1.node > o2.node)
                        return 1;
                    return 0;
                });
        pq.add(new SearchNode(node, 0, -1));

        // whether each node is visited or not, initially all false
        boolean[] visited = new boolean[nodes.get(nodes.size()-1)+1];

        while (!pq.isEmpty()) {
            SearchNode next = pq.poll();

            if (visited[next.node])
                continue;

            visited[next.node] = true;
            estDistMatrix[node][next.node] = next.pathLength;
            pathFrom[node][next.node] = next.pathFrom;

            for (Arc arc : outNeighbourMap.get(next.node)) {
                int neigh = arc.getTo();
                if (visited[neigh])
                    continue;

                double lengthToNeigh = next.pathLength + estCostMatrix[arc.getFrom()][arc.getTo()];
                pq.add(new SearchNode(neigh, lengthToNeigh, next.node));
            }
        }
    }

    /**
     * Recalculate the estimated distance from one node to another node.
     * This is normally done after an edge failure is detected
     * in the uncertain CARP.
     * It is a Dijkstra's algorithm with early stop.
     * @param fromNode the node to start from.
     * @param toNode the node to end with.
     */
    public void recalcEstDistanceBetween(int fromNode, int toNode) {
        // local class for search nodes in the priority queue
        class SearchNode {
            private int node;
            private double pathLength;
            private int pathFrom;

            private SearchNode(int node, double pathLength, int pathFrom) {
                this.node = node;
                this.pathLength = pathLength;
                this.pathFrom = pathFrom;
            }
        }

        // the priority queue uses path length as priority, the smaller the better
        PriorityQueue<SearchNode> pq =
                new PriorityQueue<>((o1, o2) -> {
                    double lengthDiff = o1.pathLength - o2.pathLength;

                    if (lengthDiff < 0)
                        return -1;
                    if (lengthDiff > 0)
                        return 1;
                    return 0;
                });
        pq.add(new SearchNode(fromNode, 0, -1));

        // whether each node is visited or not, initially all false
        boolean[] visited = new boolean[nodes.get(nodes.size()-1)+1];

        while (!pq.isEmpty()) {
            SearchNode next = pq.poll();

            if (visited[next.node])
                continue;

            visited[next.node] = true;
            estDistMatrix[fromNode][next.node] = next.pathLength;
            pathFrom[fromNode][next.node] = next.pathFrom;

            if (next.node == toNode)
                return;

            for (Arc arc : outNeighbourMap.get(next.node)) {
                int neigh = arc.getTo();
                if (visited[neigh])
                    continue;

                double lengthToNeigh = next.pathLength + estCostMatrix[arc.getFrom()][arc.getTo()];
                pq.add(new SearchNode(neigh, lengthToNeigh, next.node));
            }
        }
    }



    /**
     * Get the estimated cost from one node to another.
     * @param fromNode the former node.
     * @param toNode the latter node.
     * @return the estimated cost.
     */
    public double getEstCost(int fromNode, int toNode) {
        return estCostMatrix[fromNode][toNode];
    }

    /**
     * Get the estimated distance from one node to another.
     * @param fromNode the former node.
     * @param toNode the latter node.
     * @return the estimated distance.
     */
    public double getEstDistance(int fromNode, int toNode) {
        return estDistMatrix[fromNode][toNode];
    }

    /**
     * Get the estimated distance from one arc to another.
     * @param fromArc the former arc.
     * @param toArc the latter arc.
     * @return the estimated distance.
     */
    public double getEstDistance(Arc fromArc, Arc toArc) {
        return estDistMatrix[fromArc.getTo()][toArc.getFrom()];
    }

    /**
     * The predecent node of toNode in the shortest path from fromNode to toNode
     * @param fromNode the from node.
     * @param toNode the to node.
     * @return the predecent node of toNode.
     */
    public int getPathFrom(int fromNode, int toNode) {
        return pathFrom[fromNode][toNode];
    }

    /**
     * The successive node of fromNode in the shortest path from fromNode to toNode
     * @param fromNode the from node.
     * @param toNode the to node.
     * @return the successive node of fromNode.
     */
    public int getPathTo(int fromNode, int toNode) {
        return pathTo[fromNode][toNode];
    }

    @Override
    public String toString() {
        String str = "Graph: \n";
        for (Arc arc : arcMap.values()) {
            str = str + arc.toString();
        }

        return str;
    }

    /**
     * Update the estimated cost matrix by setting [from][to] to newCost
     * @param from the from node.
     * @param to the to node.
     * @param newCost the new cost.
     */
    public void updateEstCostMatrix(int from, int to, double newCost) {
        estCostMatrix[from][to] = newCost;
    }
}
