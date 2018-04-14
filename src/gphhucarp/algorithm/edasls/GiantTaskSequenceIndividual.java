package gphhucarp.algorithm.edasls;

import ec.Individual;
import ec.util.Parameter;
import gphhucarp.core.Arc;
import gphhucarp.core.Graph;
import gphhucarp.core.Instance;
import gphhucarp.representation.Solution;
import gphhucarp.representation.route.NodeSeqRoute;
import gphhucarp.representation.route.TaskSeqRoute;

import java.util.LinkedList;
import java.util.List;

/**
 * A giant task sequence individual is a task sequence without delimiter.
 * It is usually used as a chromosome in EDASLS.
 * Given an instance, it will be turned into a task sequence solution
 * by Ulusoy's split algorithm that minimise the actual total cost.
 */

public class GiantTaskSequenceIndividual extends Individual {
    private List<Arc> taskSequence;
    private Solution<TaskSeqRoute> solution;

    public GiantTaskSequenceIndividual(List<Arc> taskSequence) {
        this.taskSequence = taskSequence;
        this.solution = new Solution<>();
    }

    public GiantTaskSequenceIndividual() {
        this(new LinkedList<>());
    }

    public List<Arc> getTaskSequence() {
        return taskSequence;
    }

    public Solution<TaskSeqRoute> getSolution() {
        return solution;
    }

    public Arc get(int index) {
        return taskSequence.get(index);
    }

    public void add(Arc task) {
        taskSequence.add(task);
    }

    @Override
    public long size() {
        return taskSequence.size();
    }

    public static GiantTaskSequenceIndividual fromSolution(Solution<NodeSeqRoute> solution,
                                                           Instance instance) {
        Graph graph = instance.getGraph();

        GiantTaskSequenceIndividual individual = new GiantTaskSequenceIndividual();
        List<Arc> remainingTasks = new LinkedList<>(instance.getTasks());

        for (NodeSeqRoute route : solution.getRoutes()) {
            for (int i = 0; i < route.getFracSequence().size(); i++) {
                if (route.getFraction(i) == 0)
                    continue;

                Arc task = graph.getArc(route.getNode(i), route.getNode(i+1));

                if (remainingTasks.contains(task)) {
                    individual.add(task);

                    remainingTasks.remove(task);
                    remainingTasks.remove(task.getInverse());
                }

            }
        }

        return individual;
    }

    /**
     * Split the chromosome into a feasible task sequence solution which has the minimal total cost.
     * The actual demand and deadheading cost are used.
     * @param instance the given instance.
     * @param seed the seed to sample the random variables.
     */
    public void split(Instance instance, long seed) {
        instance.setSeed(seed);

        int numTasks = taskSequence.size();
        double capacity = instance.getCapacity();

        double[] minCostFromStart = new double[numTasks+1]; // all zeros initially
        int[] minNumRoutesFromStart = new int[numTasks+1]; // all zeros initially
        int[] routeFromIndex = new int[numTasks]; // the best route of this task starts from which index

        for (int i = 1; i < minCostFromStart.length; i++)
            minCostFromStart[i] = Double.POSITIVE_INFINITY;

        for (int i = 1; i < minCostFromStart.length; i++) {
            int startIndex = i-1; // the index of the starting task
            Arc startTask = taskSequence.get(startIndex);

            // initially, open a new route (depot -> startTask -> depot)
            double load = instance.getActDemand(startTask);
            double cost = instance.getActDistance(instance.getDepot(), startTask.getFrom()) +
                    startTask.getServeCost() +
                    instance.getActDistance(startTask.getTo(), instance.getDepot());

            // scan the subsequent tasks in the sequence
            for (int j = startIndex + 1; j < numTasks; j++) {
                Arc prev = taskSequence.get(j-1);
                Arc curr = taskSequence.get(j);
                load += instance.getActDemand(curr);

                if (load > capacity)
                    break;

                cost += instance.getActDistance(prev.getTo(), curr.getFrom()) +
                        curr.getServeCost() +
                        instance.getActDistance(curr.getTo(), instance.getDepot()) -
                        instance.getActDistance(prev.getTo(), instance.getDepot());

                double tmpMinCostFromStart = minCostFromStart[i-1] + cost;
                if (minCostFromStart[j] > tmpMinCostFromStart ||
                        (minCostFromStart[j] == tmpMinCostFromStart &&
                                minNumRoutesFromStart[i-1] + 1 < minNumRoutesFromStart[j])) {
                    // append the jth task in the sequence to the route
                    minCostFromStart[j] = tmpMinCostFromStart;
                    minNumRoutesFromStart[j] = minNumRoutesFromStart[i-1] + 1;
                    routeFromIndex[j] = startIndex;
                }
            }
        }

        int numRoutes = solution.getRoutes().size();
        int currRouteId = 0;

        // reversely check the start index of the best routes from the last task
        int endIndex = taskSequence.size()-1;

        while (currRouteId < numRoutes && endIndex >= 0) {
            int beginIndex = routeFromIndex[endIndex];

            TaskSeqRoute route = solution.getRoute(currRouteId);
            route.reset(instance);
            route.setCapacity(instance.getCapacity());
            route.setCurrNode(instance.getDepot());

            for (int i = beginIndex; i <= endIndex; i++)
                route.add(taskSequence.get(i), instance);
            route.add(instance.getDepotLoop(), instance);

            currRouteId ++;
            endIndex = beginIndex-1;
        }

        while (endIndex >= 0) {
            int beginIndex = routeFromIndex[endIndex];

            TaskSeqRoute route = TaskSeqRoute.initial(instance);

            for (int i = beginIndex; i <= endIndex; i++)
                route.add(taskSequence.get(i), instance);
            route.add(instance.getDepotLoop(), instance);

            solution.addRoute(route);
            currRouteId ++;

            endIndex = beginIndex-1;
        }

        if (currRouteId < solution.getRoutes().size()-1) {
            solution.setRoutes(solution.getRoutes().subList(0, currRouteId+1));
        }
    }

    /**
     * Split the chromosome into a feasible task sequence solution which has the minimal total cost.
     * The expected demand and deadheading cost are used.
     * @param instance the UCARP instance.
     */
    public void split(Instance instance) {
        int numTasks = taskSequence.size();
        double capacity = instance.getCapacity();

        double[] minCostFromStart = new double[numTasks+1]; // all zeros initially
        int[] minNumRoutesFromStart = new int[numTasks+1]; // all zeros initially
        int[] routeFromIndex = new int[numTasks]; // the best route of this task starts from which index

        for (int i = 1; i < minCostFromStart.length; i++)
            minCostFromStart[i] = Double.POSITIVE_INFINITY;

        for (int i = 1; i < minCostFromStart.length; i++) {
            int startIndex = i-1; // the index of the starting task
            Arc startTask = taskSequence.get(startIndex);

            // initially, open a new route (depot -> startTask -> depot)
            double load = startTask.getExpectedDemand();
            double cost = instance.getGraph().getEstDistance(instance.getDepot(), startTask.getFrom()) +
                    startTask.getServeCost() +
                    instance.getGraph().getEstDistance(startTask.getTo(), instance.getDepot());

            // scan the subsequent tasks in the sequence
            for (int j = startIndex + 1; j < numTasks; j++) {
                Arc prev = taskSequence.get(j-1);
                Arc curr = taskSequence.get(j);
                load += curr.getExpectedDemand();

                if (load > capacity)
                    break;

                cost += instance.getGraph().getEstDistance(prev.getTo(), curr.getFrom()) +
                        curr.getServeCost() +
                        instance.getGraph().getEstDistance(curr.getTo(), instance.getDepot()) -
                        instance.getGraph().getEstDistance(prev.getTo(), instance.getDepot());

                double tmpMinCostFromStart = minCostFromStart[i-1] + cost;
                if (minCostFromStart[j] > tmpMinCostFromStart ||
                        (minCostFromStart[j] == tmpMinCostFromStart &&
                                minNumRoutesFromStart[i-1] + 1 < minNumRoutesFromStart[j])) {
                    // append the jth task in the sequence to the route
                    minCostFromStart[j] = tmpMinCostFromStart;
                    minNumRoutesFromStart[j] = minNumRoutesFromStart[i-1] + 1;
                    routeFromIndex[j] = startIndex;
                }
            }
        }

        int numRoutes = solution.getRoutes().size();
        int currRouteId = 0;

        // reversely check the start index of the best routes from the last task
        int endIndex = taskSequence.size()-1;

        while (currRouteId < numRoutes && endIndex >= 0) {
            int beginIndex = routeFromIndex[endIndex];

            TaskSeqRoute route = solution.getRoute(currRouteId);
            route.reset(instance);
            route.setCapacity(instance.getCapacity());
            route.setCurrNode(instance.getDepot());

            for (int i = beginIndex; i <= endIndex; i++)
                route.add(taskSequence.get(i), instance);
            route.add(instance.getDepotLoop(), instance);

            currRouteId ++;
            endIndex = beginIndex-1;
        }

        while (endIndex >= 0) {
            int beginIndex = routeFromIndex[endIndex];

            TaskSeqRoute route = TaskSeqRoute.initial(instance);

            for (int i = beginIndex; i <= endIndex; i++)
                route.add(taskSequence.get(i), instance);
            route.add(instance.getDepotLoop(), instance);

            solution.addRoute(route);
            currRouteId ++;

            endIndex = beginIndex-1;
        }

        if (currRouteId < solution.getRoutes().size()-1) {
            solution.setRoutes(solution.getRoutes().subList(0, currRouteId+1));
        }
    }

    @Override
    public boolean equals(Object ind) {
        if (ind == null)
            return false;

        if (!(this.getClass().equals(ind.getClass())))
            return false;

        GiantTaskSequenceIndividual i = (GiantTaskSequenceIndividual)ind;

        // check whether the to giant task sequences have the same length
        if (taskSequence.size() != i.taskSequence.size())
            return false;

        for (int x = 0; x < taskSequence.size(); x++) {
            // check whether each element in the sequence is the same
            if (!taskSequence.get(x).equals(i.taskSequence.get(x)))
                return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public Parameter defaultBase() {
        return null;
    }

    @Override
    public GiantTaskSequenceIndividual clone() {
        GiantTaskSequenceIndividual cloned = (GiantTaskSequenceIndividual)(super.clone());

        cloned.taskSequence = new LinkedList<>(taskSequence);
        return cloned;
    }

    public GiantTaskSequenceIndividual lightClone() {
        GiantTaskSequenceIndividual cloned = (GiantTaskSequenceIndividual)(super.clone());

        cloned.taskSequence = new LinkedList<>();
        return cloned;
    }


    /**
     * A string representation of the task sequence.
     */
    @Override
    public String toString() {
        String s = "";
        for (Arc task : taskSequence) {
            s += task.toSimpleString() + " ";
        }

        return s;
    }
}
