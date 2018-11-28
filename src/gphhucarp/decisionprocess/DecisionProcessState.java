package gphhucarp.decisionprocess;

import gphhucarp.core.*;
import gphhucarp.representation.Solution;
import gphhucarp.representation.route.NodeSeqRoute;

import java.util.*;

/**
 * The state for the decision making process of a reactive solution builder.
 * It includes
 *  - the given UCARP instance, including relevant information such as the graph,
 *  - a list of remaining unserved tasks,
 *  - a list of unassigned tasks,
 *  - a partial node sequence solution.
 * Created by gphhucarp on 27/08/17.
 */
public class DecisionProcessState {

    private Instance instance; // the UCARP instance
    private long seed; // the seed to sample the random variables in the UCARP instance
    private List<Arc> remainingTasks;
    private List<Arc> unassignedTasks;
    private Solution<NodeSeqRoute> solution;

    private Map<Arc, Double> taskRemainingDemandFrac;

    // the task-to-task map: for each task, the outgoing remaining tasks are sorted
    // in the increasing order of distance from the task
    private Map<Arc, List<Arc>> taskToTaskMap;

    // the route-to-task map: for each task, the routes are sorted
    // in the increasing order of the distance from its next decision node to the task
    private Map<Arc, List<NodeSeqRoute>> routeToTaskMap;

    // in this map, the key is the unassigned tasks
    // the value for eack task is a list of tasks which are on the flood of the key task
    private Map<Arc, List<Arc>> floodMap;

    // in this map, the key is the unassigned tasks
    // the value for each task is a list of tasks where the key task is on their flood
    private Map<Arc, List<Arc>> onFloodMap;


    public DecisionProcessState(Instance instance,
                                long seed,
                                List<Arc> remainingTasks,
                                List<Arc> unassignedTasks,
                                Solution<NodeSeqRoute> solution,
                                Map<Arc, Double> taskRemainingDemandFrac) {
        this.instance = instance;
        this.seed = seed;
        this.remainingTasks = remainingTasks;
        this.unassignedTasks = unassignedTasks;
        this.solution = solution;
        this.taskRemainingDemandFrac = taskRemainingDemandFrac;

        initTaskToTaskMap();
        initRouteToTaskMap();
        initFloodMaps();
    }

    /**
     * Construct the state by an instance.
     * Initially, all the routes starts from the depot,
     * and all the tasks are unserved.
     * @param instance the instance.
     * @param seed the seed.
     * @param numRoutes the number of routes.
     */
    public DecisionProcessState(Instance instance, long seed, int numRoutes) {
        this.instance = instance;
        this.seed = seed;
        remainingTasks = new LinkedList<>(instance.getTasks());
        unassignedTasks = new LinkedList<>(remainingTasks);
        solution = Solution.initialNodeSeqSolution(instance, numRoutes);
        for (NodeSeqRoute route : solution.getRoutes())
            route.setNextTask(instance.getDepotLoop());
        taskRemainingDemandFrac = new HashMap<>();
        for (Arc task : remainingTasks)
            taskRemainingDemandFrac.put(task, 1.0);

        initTaskToTaskMap();
        initRouteToTaskMap();
        initFloodMaps();
    }

    public DecisionProcessState(Instance instance, long seed) {
        this(instance, seed, instance.getNumVehicles());
    }

    public Instance getInstance() {
        return instance;
    }

    public long getSeed() {
        return seed;
    }

    public void setSeed(long seed) {
        this.seed = seed;
    }

    public List<Arc> getRemainingTasks() {
        return remainingTasks;
    }

    public List<Arc> getUnassignedTasks() {
        return unassignedTasks;
    }

    public Solution<NodeSeqRoute> getSolution() {
        return solution;
    }

    public Map<Arc, Double> getTaskRemainingDemandFracMap() {
        return taskRemainingDemandFrac;
    }

    public double getTaskRemainingDemandFrac(Arc task) {
        return taskRemainingDemandFrac.get(task);
    }

    public void setTaskRemainingDemandFrac(Arc task, double frac) {
        taskRemainingDemandFrac.put(task, frac);
    }

    public Map<Arc, List<Arc>> getTaskToTaskMap() {
        return taskToTaskMap;
    }

    public Map<Arc, List<NodeSeqRoute>> getRouteToTaskMap() {
        return routeToTaskMap;
    }

    public List<Arc> getTaskAdjacencyList(Arc task) {
        return taskToTaskMap.get(task);
    }

    public List<NodeSeqRoute> getRouteAdjacencyList(Arc task) {
        return routeToTaskMap.get(task);
    }

    public Map<Arc, List<Arc>> getFloodMap() {
        return floodMap;
    }

    public Map<Arc, List<Arc>> getOnFloodMap() {
        return onFloodMap;
    }

    public List<Arc> getFloodOfTask(Arc task) {
        return floodMap.get(task);
    }

    public List<Arc> isOnFloods(Arc task) {
        return onFloodMap.get(task);
    }

    /**
     * Remove a remaining task and its inverse.
     * @param task the removed task.
     */
    public void removeRemainingTasks(Arc task) {
        remainingTasks.remove(task);
        remainingTasks.remove(task.getInverse());
    }

    /**
     * Remove an unassign task and its inverse.
     * @param task the removed task.
     */
    public void removeUnassignedTasks(Arc task) {
        unassignedTasks.remove(task);
        unassignedTasks.remove(task.getInverse());
    }

    /**
     * Initialise the task-to-task map by deep cloning from the graph.
     */
    public void initTaskToTaskMap() {
        taskToTaskMap = new HashMap<>();
        taskToTaskMap.put(instance.getDepotLoop(),
                instance.getTaskToTaskMap().get(instance.getDepotLoop()));
        for (Arc task : instance.getTasks())
            taskToTaskMap.put(task, new LinkedList<>(instance.getTaskToTaskMap().get(task)));
    }

    /**
     * Reset the task-to-task map. Clear the map instead of creating a new one.
     */
    public void resetTaskToTaskMap() {
        taskToTaskMap.clear();
        taskToTaskMap.put(instance.getDepotLoop(),
                instance.getTaskToTaskMap().get(instance.getDepotLoop()));
        for (Arc task : instance.getTasks())
            taskToTaskMap.put(task, new LinkedList<>(instance.getTaskToTaskMap().get(task)));
    }

    /**
     * Initialise the route-to-task map.
     */
    public void initRouteToTaskMap() {
        routeToTaskMap = new HashMap<>();
        for (Arc task : instance.getTasks()) {
            List<NodeSeqRoute> routeAdjacencyList = new LinkedList<>();
            routeToTaskMap.put(task, routeAdjacencyList);
        }
    }

    /**
     * Reset the route-to-task map. Clear the current map instead of creating a new one.
     */
    public void resetRouteToTaskMap() {
        routeToTaskMap.clear();
        for (Arc task : instance.getTasks()) {
            List<NodeSeqRoute> routeAdjacencyList = new LinkedList<>();
            routeToTaskMap.put(task, routeAdjacencyList);
        }
    }

    /**
     * Initialise the flood maps.
     */
    public void initFloodMaps() {
        floodMap = new HashMap<>();
        onFloodMap = new HashMap<>();

        for (Arc task : instance.getTasks()) {
            floodMap.put(task, new LinkedList<>());
            onFloodMap.put(task, new LinkedList<>());
        }

        for (Arc task : instance.getTasks()) {
            int curr = task.getTo();
            while (curr != instance.getDepot()) {
                int next = instance.getGraph().getPathTo(curr, instance.getDepot());

                Arc floodTask = instance.getGraph().getArc(curr, next);

                if (floodTask != null && instance.getTasks().contains(floodTask) && !floodTask.equals(task.getInverse())) {
                    floodMap.get(task).add(floodTask);
                    onFloodMap.get(floodTask).add(task);
                }

                curr = next;
            }
        }
    }

    /**
     * Reset the flood maps.
     */
    public void resetFloodMaps() {
        floodMap.clear();
        onFloodMap.clear();

        for (Arc task : instance.getTasks()) {
            floodMap.put(task, new LinkedList<>());
            onFloodMap.put(task, new LinkedList<>());
        }

        for (Arc task : instance.getTasks()) {
            int curr = task.getTo();
            while (curr != instance.getDepot()) {
                int next = instance.getGraph().getPathTo(curr, instance.getDepot());

                Arc floodTask = instance.getGraph().getArc(curr, next);

                if (floodTask != null && !floodTask.equals(task.getInverse())) {
                    floodMap.get(task).add(floodTask);
                    onFloodMap.get(floodTask).add(task);
                }

                curr = next;
            }
        }
    }

    /**
     * Update the task-to-task map and route-to-task map when a task is completed.
     * First, remove the task and its inverse from the task-to-task and route-to-task maps.
     * Then, for each remaining task, remove the task and its inverse from its adjacency list.
     * @param task the completed task.
     */
    public void completeTask(Arc task) {
        taskToTaskMap.remove(task);
        taskToTaskMap.remove(task.getInverse());
        routeToTaskMap.remove(task);
        routeToTaskMap.remove(task.getInverse());

        for (Arc anotherTask : taskToTaskMap.keySet()) {
            taskToTaskMap.get(anotherTask).remove(task);
            taskToTaskMap.get(anotherTask).remove(task.getInverse());
        }

        for (Arc floodTask : floodMap.get(task))
            onFloodMap.get(floodTask).remove(task);

        for (Arc floodTask : floodMap.get(task.getInverse()))
            onFloodMap.get(floodTask).remove(task.getInverse());

        floodMap.remove(task);
        floodMap.remove(task.getInverse());

    }

    /**
     * Calculate the route-to-task map with the current route.
     * For each remaining task, the current route is excluded from the map and will be treated separately.
     * @param currRoute the current route.
     */
    public void calcRouteToTaskMap(NodeSeqRoute currRoute) {
        Graph graph = instance.getGraph();

        for (Arc task : routeToTaskMap.keySet()) {
            List<NodeSeqRoute> routeAdjacencyList = routeToTaskMap.get(task);

            routeAdjacencyList.clear();

            for (NodeSeqRoute route : solution.getRoutes()) {
                routeAdjacencyList.add(route);
            }

            // exclude the current route id
            routeAdjacencyList.remove(currRoute);

            // sort the routes with the increasing order of
            // the distance from the next decision node to the task
            Collections.sort(routeAdjacencyList,
                    (o1, o2) -> Double.compare(graph.getEstDistance(o1.getNextTask().getTo(), task.getFrom()),
                            graph.getEstDistance(o2.getNextTask().getTo(), task.getFrom())));
        }
    }

    public void reset(DecisionProcessState initialState) {
        remainingTasks.clear();
        for (Arc task : initialState.remainingTasks) {
        }
    }

    /**
     * Reset a decision process state as the initial state.
     */
    public void reset() {
        remainingTasks = new LinkedList<>(instance.getTasks());
        taskRemainingDemandFrac.clear();
        for (Arc task : remainingTasks)
            taskRemainingDemandFrac.put(task, 1.0);
        unassignedTasks = new LinkedList<>(remainingTasks);
        solution.reset(instance);

        resetTaskToTaskMap();
        resetRouteToTaskMap();

    }

    public DecisionProcessState clone() {
        List<Arc> clonedRemTasks = new LinkedList<>(remainingTasks);
        List<Arc> clonedUasTasks = new LinkedList<>(unassignedTasks);
        Solution<NodeSeqRoute> clonedSol = solution.clone();
        Map<Arc, Double> clonedTRDF = new HashMap<>(taskRemainingDemandFrac);

        return new DecisionProcessState(instance, seed,
                clonedRemTasks, clonedUasTasks, clonedSol, clonedTRDF);
    }
}
