package gphhucarp.core;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.random.RandomDataGenerator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

 /**
 * A sampled UCARP instance.
 * It includes the random demands and deadheading costs of each arc.
 * In addition, it samples actual demands and deadheading costs of the arcs
 * and store in the corresponding actualDemand and actualDeadheadingCost fields.
 *
 * Created by gphhucarp on 14/06/17.
 */
public class Instance {
    private String name = null; // the name of the instance

    private Graph graph;
    private List<Arc> tasks;
    private Map<Arc, Double> actDemandMap; // the actual (sampled) demand of tasks
    private Map<Arc, Double> actDeadheadingCostMap; // the actual (sampled) deadheading cost of arcs
    private int depot; // the depot id
    private Arc depotLoop; // the depot loop arc
    private double capacity;
    private int numVehicles;

    private double[][] actCostMatrix; // the actual cost of edges
    private double[][] actDistMatrix; // the actual distance between nodes

    // the uncertainty level is std/mean for random demands and deadheading costs
    private double demandUncertaintyLevel;
    private double costUncertaintyLevel;

    // the random seed and data generator for sampling.
    private long seed;
    private RandomDataGenerator rdg = new RandomDataGenerator();

    private Map<Arc, List<Arc>> taskToTaskMap; // the task-to-task map, used for generating features in the decision making process.

    public Instance(Graph graph, List<Arc> tasks, int depot, Arc depotLoop, double capacity, int numVehicles,
                    double demandUncertaintyLevel, double costUncertaintyLevel) {
        this.graph = graph;
        this.tasks = tasks;
        this.actDemandMap = new HashMap<>();
        this.actDeadheadingCostMap = new HashMap<>();
        this.depot = depot;
        this.depotLoop = depotLoop;
        this.capacity = capacity;
        this.numVehicles = numVehicles;
        this.demandUncertaintyLevel = demandUncertaintyLevel;
        this.costUncertaintyLevel = costUncertaintyLevel;

        this.actDemandMap.put(depotLoop, 0d);
        this.actDeadheadingCostMap.put(depotLoop, 0d);

        calcTaskToTaskMap();
    }

    public Instance(Graph graph, List<Arc> tasks, int depot, double capacity, int numVehicles,
                    double demandUncertaintyLevel, double costUncertaintyLevel) {
        this(graph, tasks, depot,
                new Arc(depot, depot, 0, 0, 0, null, 0, 0),
                capacity, numVehicles, demandUncertaintyLevel, costUncertaintyLevel);
    }

    /**
     * Default demandUncertaintyLevel = 0 and costUncertaintyLevel = 0.
     */
    public Instance(Graph graph, List<Arc> tasks, int depot, double capacity, int numVehicles) {
        this(graph, tasks, depot, capacity, numVehicles, 0d, 0d);
    }

    /**
     * Default numVehicles = 1, i.e. single vehicle.
     * Default demandUncertaintyLevel = 0 and costUncertaintyLevel = 0.
     */
    public Instance(Graph graph, List<Arc> tasks, int depot, double capacity) {
        this(graph, tasks, depot, capacity, 1);
    }

    /**
     * Default numVehicles = 1, i.e. single vehicle.
     */
    public Instance(Graph graph, List<Arc> tasks, int depot, double capacity,
                    double demandUncertaintyLevel, double costUncertaintyLevel) {
        this(graph, tasks, depot, capacity, 1, demandUncertaintyLevel, costUncertaintyLevel);
    }

    public Graph getGraph() {
        return graph;
    }

    public List<Arc> getTasks() {
        return tasks;
    }

    public Map<Arc, Double> getActDemandMap() {
        return actDemandMap;
    }

    public Map<Arc, Double> getActDeadheadingCostMap() {
        return actDeadheadingCostMap;
    }

    public double getActDemand(Arc task) {
        if (!actDemandMap.containsKey(task))
            return 0;

        return actDemandMap.get(task);
    }

    public double getActDeadheadingCost(Arc arc) {
        return actDeadheadingCostMap.get(arc);
    }

    /**
     * Get the actual cost from one node to another.
     * @param fromNode the former node.
     * @param toNode the latter node.
     * @return the actual cost.
     */
    public double getActCost(int fromNode, int toNode) {
        return actCostMatrix[fromNode][toNode];
    }

    /**
     * Get the actual distance from one node to another.
     * @param fromNode the former node.
     * @param toNode the latter node.
     * @return the actual distance.
     */
    public double getActDistance(int fromNode, int toNode) {
        return actDistMatrix[fromNode][toNode];
    }

    /**
     * Get the actual distance from one arc to another.
     * @param fromArc the former arc.
     * @param toArc the latter arc.
     * @return the actual distance.
     */
    public double getActDistance(Arc fromArc, Arc toArc) {
        return actDistMatrix[fromArc.getTo()][toArc.getFrom()];
    }

     public int getDepot() {
        return depot;
    }

    public Arc getDepotLoop() {
         return depotLoop;
     }

    public double getCapacity() {
        return capacity;
    }

    public int getNumVehicles() {
        return numVehicles;
    }

    public double getDemandUncertaintyLevel() {
        return demandUncertaintyLevel;
    }

    public double getCostUncertaintyLevel() {
        return costUncertaintyLevel;
    }

    public long getSeed() {
        return seed;
    }

    public Map<Arc, List<Arc>> getTaskToTaskMap() {
        return taskToTaskMap;
    }

    public void setDemandUncertaintyLevel(double demandUncertaintyLevel) {
        this.demandUncertaintyLevel = demandUncertaintyLevel;
    }

    public void setCostUncertaintyLevel(double costUncertaintyLevel) {
        this.costUncertaintyLevel = costUncertaintyLevel;
    }

    public void setSeed(long seed) {
        this.seed = seed;
        this.rdg.reSeed(seed);
        sample(rdg);
    }

    public String getName() {
         return name;
     }

    public void setName(String name) {
         this.name = name;
     }

    /**
     * Reset the random data generator, and re-sample the same instance.
     */
    public void reset() {
        this.rdg.reSeed(seed);
        sample(rdg);
    }

    public void calcTaskToTaskMap() {
        taskToTaskMap = new HashMap<>();
        // add the depot loop dummy task
        List<Arc> depotLoopAdjacencyList = new LinkedList<>(tasks);
        Collections.sort(depotLoopAdjacencyList,
                (o1, o2) -> Double.compare(graph.getEstDistance(depotLoop, o1), graph.getEstDistance(depotLoop, o2)));
        taskToTaskMap.put(depotLoop, depotLoopAdjacencyList);

        for (Arc task : tasks) {
            List<Arc> taskAdjacencyList = new LinkedList<>();

            for (Arc anotherTask: tasks) {
                if (anotherTask.equals(task) || anotherTask.equals(task.getInverse()))
                    continue;

                taskAdjacencyList.add(anotherTask);
            }

            Collections.sort(taskAdjacencyList,
                    (o1, o2) -> Double.compare(graph.getEstDistance(task, o1), graph.getEstDistance(task, o2)));
            taskToTaskMap.put(task, taskAdjacencyList);
        }
    }

    /**
     * Read a gdb/val/egl data file, which follow the same format.
     * The graph is undirected, so each edge corresponds to two arcs.
     * The nodes are indexed from 1 onward.
     * @param file the data file.
     * @param numVehicles the number of vehicles.
     * @param demandUncertaintyLevel the demand uncertainty level.
     * @param costUncertaintyLevel the deadheading cost uncertainty level.
     * @return the instance.
     */
    public static Instance readFromGVE(File file,
                                       int numVehicles,
                                       double demandUncertaintyLevel,
                                       double costUncertaintyLevel) {
        String line;
        String[] segments;

        List<Arc> tasks = new LinkedList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            reader.readLine(); // line 1: " NOMBRE : xxx"
            reader.readLine(); // line 2: " COMENTARIO : xxx (cota superior)"
            line = reader.readLine(); // line 3: " VERTICES : xxx"
            segments = line.split("\\s+");
            int numNodes = Integer.valueOf(segments[3]);
            line = reader.readLine(); // line 4: " ARISTAS_REQ : xxx"
            segments = line.split("\\s+");
            int numReq = Integer.valueOf(segments[3]);
            line = reader.readLine(); // line 5: " ARISTAS_NOREQ : xxx"
            segments = line.split("\\s+");
            int numNonReq = Integer.valueOf(segments[3]);
            reader.readLine(); // line 6: " VEHICULOS : xxx"
            line = reader.readLine(); // line 7: " CAPACIDAD : xxx"
            segments = line.split("\\s+");
            double capacity = Double.valueOf(segments[3]);
            reader.readLine(); // line 8: " TIPO_COSTES_ARISTAS : EXPLICITOS"
            reader.readLine(); // line 9: " COSTE_TOTAL_REQ : xxx"
            reader.readLine(); // line 10: " LISTA_ARISTAS_REQ :"

            List<Integer> nodes = new ArrayList<>();
            for (int i = 1; i < numNodes+1; i++)
                nodes.add(i);
            Map<Pair<Integer, Integer>, Arc> arcMap = new HashMap<>();
            for (int i = 0; i < numReq; i++) {
                line = reader.readLine();
                segments = line.split("[,()\\s]+");
                List<Double> numbers = new ArrayList<>();
                for (String seg : segments) {
                    if (NumberUtils.isNumber(seg))
                        numbers.add(Double.valueOf(seg));
                }

                double from = numbers.get(0);
                double to = numbers.get(1);
                double cost = numbers.get(2);
                double demand = numbers.get(3);
                int fromInt = (int)from;
                int toInt = (int)to;

                Arc arc1 = new Arc(fromInt, toInt, demand, cost, cost, null,
                        demandUncertaintyLevel, costUncertaintyLevel);
                Arc arc2 = new Arc(toInt, fromInt, demand, cost, cost, arc1,
                        demandUncertaintyLevel, costUncertaintyLevel);
                arc1.setInverse(arc2);

                arcMap.put(Pair.of(fromInt, toInt), arc1);
                arcMap.put(Pair.of(toInt, fromInt), arc2);
                tasks.add(arc1);
                tasks.add(arc2);
            }

            if (numNonReq > 0) {
                reader.readLine(); // read " LISTA_ARISTAS_NOREQ :"
                for (int i = 0; i < numNonReq; i++) {
                    line = reader.readLine();
                    segments = line.split("[,()\\s]+");
                    List<Double> numbers = new ArrayList<>();
                    for (String seg : segments) {
                        if (NumberUtils.isNumber(seg))
                            numbers.add(Double.valueOf(seg));
                    }

                    double from = numbers.get(0);
                    double to = numbers.get(1);
                    double cost = numbers.get(2);
                    int fromInt = (int)from;
                    int toInt = (int)to;

                    Arc arc1 = new Arc(fromInt, toInt, 0, cost, cost, null,
                            demandUncertaintyLevel, costUncertaintyLevel);
                    Arc arc2 = new Arc(toInt, fromInt, 0, cost, cost, arc1,
                            demandUncertaintyLevel, costUncertaintyLevel);
                    arc1.setInverse(arc2);

                    arcMap.put(Pair.of(fromInt, toInt), arc1);
                    arcMap.put(Pair.of(toInt, fromInt), arc2);
                }
            }

            line = reader.readLine(); // last line: " DEPOSITO :   xx"
            segments = line.split("\\s+");
            int depot = Integer.valueOf(segments[3]);

            return new Instance(new Graph(nodes, arcMap), tasks, depot, capacity,
                    numVehicles, demandUncertaintyLevel, costUncertaintyLevel);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Read a gdb/val/egl data file, which follow the same format.
     * The graph is undirected, so each edge corresponds to two arcs.
     * The nodes are indexed from 1 onward. The number of vehicles is
     * read from the file itself.
     * @param file the data file.
     * @param demandUncertaintyLevel the demand uncertainty level.
     * @param costUncertaintyLevel the deadheading cost uncertainty level.
     * @return the instance.
     */
    public static Instance readFromGVE(File file,
                                       double demandUncertaintyLevel,
                                       double costUncertaintyLevel) {
        String line;
        String[] segments;

        List<Arc> tasks = new LinkedList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            reader.readLine(); // line 1: " NOMBRE : xxx"
            reader.readLine(); // line 2: " COMENTARIO : xxx (cota superior)"
            line = reader.readLine(); // line 3: " VERTICES : xxx"
            segments = line.split("\\s+");
            int numNodes = Integer.valueOf(segments[3]);
            line = reader.readLine(); // line 4: " ARISTAS_REQ : xxx"
            segments = line.split("\\s+");
            int numReq = Integer.valueOf(segments[3]);
            line = reader.readLine(); // line 5: " ARISTAS_NOREQ : xxx"
            segments = line.split("\\s+");
            int numNonReq = Integer.valueOf(segments[3]);
            line = reader.readLine(); // line 6: " VEHICULOS : xxx"
            segments = line.split("\\s+");
            int numVehicles = Integer.valueOf(segments[3]);
            line = reader.readLine(); // line 7: " CAPACIDAD : xxx"
            segments = line.split("\\s+");
            double capacity = Double.valueOf(segments[3]);
            reader.readLine(); // line 8: " TIPO_COSTES_ARISTAS : EXPLICITOS"
            reader.readLine(); // line 9: " COSTE_TOTAL_REQ : xxx"
            reader.readLine(); // line 10: " LISTA_ARISTAS_REQ :"

            List<Integer> nodes = new ArrayList<>();
            for (int i = 1; i < numNodes+1; i++)
                nodes.add(i);
            Map<Pair<Integer, Integer>, Arc> arcMap = new HashMap<>();
            for (int i = 0; i < numReq; i++) {
                line = reader.readLine();
                segments = line.split("[,()\\s]+");
                List<Double> numbers = new ArrayList<>();
                for (String seg : segments) {
                    if (NumberUtils.isNumber(seg))
                        numbers.add(Double.valueOf(seg));
                }

                double from = numbers.get(0);
                double to = numbers.get(1);
                double cost = numbers.get(2);
                double demand = numbers.get(3);
                int fromInt = (int)from;
                int toInt = (int)to;

                Arc arc1 = new Arc(fromInt, toInt, demand, cost, cost, null,
                        demandUncertaintyLevel, costUncertaintyLevel);
                Arc arc2 = new Arc(toInt, fromInt, demand, cost, cost, arc1,
                        demandUncertaintyLevel, costUncertaintyLevel);
                arc1.setInverse(arc2);

                arcMap.put(Pair.of(fromInt, toInt), arc1);
                arcMap.put(Pair.of(toInt, fromInt), arc2);
                tasks.add(arc1);
                tasks.add(arc2);
            }

            if (numNonReq > 0) {
                reader.readLine(); // read " LISTA_ARISTAS_NOREQ :"
                for (int i = 0; i < numNonReq; i++) {
                    line = reader.readLine();
                    segments = line.split("[,()\\s]+");
                    List<Double> numbers = new ArrayList<>();
                    for (String seg : segments) {
                        if (NumberUtils.isNumber(seg))
                            numbers.add(Double.valueOf(seg));
                    }

                    double from = numbers.get(0);
                    double to = numbers.get(1);
                    double cost = numbers.get(2);
                    int fromInt = (int)from;
                    int toInt = (int)to;

                    Arc arc1 = new Arc(fromInt, toInt, 0, cost, cost, null,
                            demandUncertaintyLevel, costUncertaintyLevel);
                    Arc arc2 = new Arc(toInt, fromInt, 0, cost, cost, arc1,
                            demandUncertaintyLevel, costUncertaintyLevel);
                    arc1.setInverse(arc2);

                    arcMap.put(Pair.of(fromInt, toInt), arc1);
                    arcMap.put(Pair.of(toInt, fromInt), arc2);
                }
            }

            line = reader.readLine(); // last line: " DEPOSITO :   xx"
            segments = line.split("\\s+");
            int depot = Integer.valueOf(segments[3]);

            return new Instance(new Graph(nodes, arcMap), tasks, depot, capacity,
                     numVehicles, demandUncertaintyLevel, costUncertaintyLevel);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Instance randomInstance(int numNodes,
                                          RandomDataGenerator rdg) {
        List<Arc> tasks = new LinkedList<>();
        // set the capacity arbitrarily
        double capacity = 500;
        int numVehicles = 10;
        double demandUncertaintyLevel = 0.2;
        double costUncertaintyLevel = 0.2;

        double maxDemand = 2 * capacity / numNodes;
        double maxCost = maxDemand;

        // set the node ids based on numNodes
        List<Integer> nodes = new ArrayList<>();
        for (int i = 1; i < numNodes+1; i++)
            nodes.add(i);

        // randomly generate the arcs
        Map<Pair<Integer, Integer>, Arc> arcMap = new HashMap<>();
        for (int from = 1; from < numNodes; from++) {
            for (int to = from+1; to < numNodes+1; to++) {
                double cost = rdg.nextUniform(1, maxCost);
                double demand = rdg.nextUniform(1, maxDemand);

                Arc arc1 = new Arc(from, to, demand, cost, cost, null,
                        demandUncertaintyLevel, costUncertaintyLevel);
                Arc arc2 = new Arc(to, from, demand, cost, cost, arc1,
                        demandUncertaintyLevel, costUncertaintyLevel);
                arc1.setInverse(arc2);

                arcMap.put(Pair.of(from, to), arc1);
                arcMap.put(Pair.of(to, from), arc2);
                tasks.add(arc1);
                tasks.add(arc2);
            }
        }

        int depot = 1;

        return new Instance(new Graph(nodes, arcMap), tasks, depot, capacity,
                numVehicles, demandUncertaintyLevel, costUncertaintyLevel);
    }

    /**
     * Randomly sample a realised instance for the uncertain CARP instance.
     * @param rdg the random data generator.
     */
    public void sample(RandomDataGenerator rdg) {
        for (Arc arc : graph.getArcMap().values()) {
            double sampledDeadheadingCost = arc.sampleDeadheadingCost(rdg);
            actDeadheadingCostMap.put(arc, sampledDeadheadingCost);
            actDeadheadingCostMap.put(arc.getInverse(), sampledDeadheadingCost);
        }

        for (Arc task : tasks) {
            double sampledDemand = task.sampleDemand(rdg);
            actDemandMap.put(task, sampledDemand);
            actDemandMap.put(task.getInverse(), sampledDemand);
        }

        calcActDistMatrix();
    }

    /**
     * Calculate the actual distance matrix by running Dijkstra's algorithm
     * on the actual cost matrix, starting from each node.
     * This is efficient for sparse graphs.
     */
    public void calcActDistMatrix() {
        List<Integer> nodes = graph.getNodes();
        int maxNodeId = nodes.get(nodes.size()-1); // get the boundaries of the matrices
        actCostMatrix = new double[maxNodeId+1][maxNodeId+1];
        actDistMatrix = new double[maxNodeId+1][maxNodeId+1];

        for (int i = 0; i < nodes.size(); i++) {
            for (int j = 0; j < nodes.size(); j++) {
                actCostMatrix[nodes.get(i)][nodes.get(j)] = Double.POSITIVE_INFINITY;
                actDistMatrix[nodes.get(i)][nodes.get(j)] = Double.POSITIVE_INFINITY;
            }
            actCostMatrix[nodes.get(i)][nodes.get(i)] = 0;
            actDistMatrix[nodes.get(i)][nodes.get(i)] = 0;
        }

        // initialise actCostMatrix with the actual costs
        for (Arc arc : graph.getArcMap().values())
            actCostMatrix[arc.getFrom()][arc.getTo()] = getActDeadheadingCost(arc);

        for (int i = 0; i < nodes.size(); i++) {
            calcActDistancesFrom(nodes.get(i));
        }
    }

    /**
     * Calculate the actual distances from a node to all other nodes.
     * This is done by Dijkstra's algorithm.
     * @param node the starting node.
     */
    private void calcActDistancesFrom(int node) {
        List<Integer> nodes = graph.getNodes();

        // local class for search nodes in the priority queue
        class SearchNode {
            private int node;
            private double pathLength;

            private SearchNode(int node, double pathLength) {
                this.node = node;
                this.pathLength = pathLength;
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
        pq.add(new SearchNode(node, 0));

        // whether each node is visited or not, initially all false
        boolean[] visited = new boolean[nodes.get(nodes.size()-1)+1];

        while (!pq.isEmpty()) {
            SearchNode next = pq.poll();

            if (visited[next.node])
                continue;

            visited[next.node] = true;
            actDistMatrix[node][next.node] = next.pathLength;

            for (Arc arc : graph.getOutNeighbour(next.node)) {
                int neigh = arc.getTo();
                if (visited[neigh])
                    continue;

                double lengthToNeigh = next.pathLength + actCostMatrix[arc.getFrom()][arc.getTo()];
                pq.add(new SearchNode(neigh, lengthToNeigh));
            }
        }
    }

    @Override
    public String toString() {
        String str = graph.toString();
        str = str + "depot = " + depot + " \n";
        str = str + "capacity = " + capacity + " \n";

        return str;
    }

    public Instance clone() {
        return new Instance(graph, tasks, depot, depotLoop, capacity, numVehicles,
                demandUncertaintyLevel, costUncertaintyLevel);
    }
}
