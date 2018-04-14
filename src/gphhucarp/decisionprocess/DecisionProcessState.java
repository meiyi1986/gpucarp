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

    // the task-to-task map: for each task, the outgoing remaining tasks are sorted
    // in the increasing order of distance from the task
    private Map<Arc, List<Arc>> taskToTaskMap;

    // the route-to-task map: for each task, the routes are sorted
    // in the increasing order of distance to the task
    private Map<Arc, List<NodeSeqRoute>> routeToTaskMap;

    public DecisionProcessState(Instance instance,
                                long seed,
                                List<Arc> remainingTasks,
                                List<Arc> unassignedTasks,
                                Solution<NodeSeqRoute> solution) {
        this.instance = instance;
        this.seed = seed;
        this.remainingTasks = remainingTasks;
        this.unassignedTasks = unassignedTasks;
        this.solution = solution;

        initTaskToTaskMap();
        initRouteToTaskMap();
    }

    public DecisionProcessState(Instance instance, long seed, int numRoutes) {
        this.instance = instance;
        this.seed = seed;
        remainingTasks = new LinkedList<>(instance.getTasks());
        for (Arc task : remainingTasks)
            task.setRemainingDemandFraction(1);
        unassignedTasks = new LinkedList<>(remainingTasks);
        solution = Solution.initialNodeSeqSolution(instance, numRoutes);

        initTaskToTaskMap();
        initRouteToTaskMap();
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
        for (Arc task : instance.getTasks())
            taskToTaskMap.put(task, new LinkedList<>(instance.getTaskToTaskMap().get(task)));
    }

    /**
     * Reset the task-to-task map. Clear the map instead of creating a new one.
     */
    public void resetTaskToTaskMap() {
        taskToTaskMap.clear();
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
     * Reset the route-to-task map. Clear the map without creating a new one.
     */
    public void resetRouteToTaskMap() {
        routeToTaskMap.clear();
        for (Arc task : instance.getTasks()) {
            List<NodeSeqRoute> routeAdjacencyList = new LinkedList<>();
            routeToTaskMap.put(task, routeAdjacencyList);
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

            routeAdjacencyList.remove(currRoute);

            Collections.sort(routeAdjacencyList,
                    (o1, o2) -> Double.compare(graph.getEstDistance(o1.currNode(), task.getFrom()),
                            graph.getEstDistance(o2.currNode(), task.getFrom())));
        }
    }

    /**
     * Reset a decision process state.
     */
    public void reset() {
        remainingTasks = new LinkedList<>(instance.getTasks());
        for (Arc task : remainingTasks)
            task.setRemainingDemandFraction(1);
        unassignedTasks = new LinkedList<>(remainingTasks);
        solution.reset(instance);

        resetTaskToTaskMap();
        resetRouteToTaskMap();
    }

    public DecisionProcessState clone() {
        List<Arc> clonedRemTasks = new LinkedList<>(remainingTasks);
        List<Arc> clonedUasTasks = new LinkedList<>(unassignedTasks);
        Solution<NodeSeqRoute> clonedSol = solution.clone();


        return new DecisionProcessState(instance, seed, clonedRemTasks, clonedUasTasks, clonedSol);
    }
}
