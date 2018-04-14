package gphhucarp.representation;

import gphhucarp.core.Instance;
import gphhucarp.core.Objective;
import gphhucarp.representation.route.NodeSeqRoute;
import gphhucarp.representation.route.Route;
import gphhucarp.representation.route.TaskSeqRoute;

import java.util.ArrayList;
import java.util.List;

/**
 * A solution is represented as a list of routes.
 * The routes can be represented in different ways.
 * Therefore, the solution is a generic type with respect to the route parameter.
 *
 * Created by gphhucarp on 25/08/17.
 */

public class Solution<T extends Route> {
    private List<T> routes;

    public Solution(List<T> routes) {
        this.routes = routes;
    }

    /**
     * Construct empty routes.
     */
    public Solution() {
        this(new ArrayList<>());
    }

    public List<T> getRoutes() {
        return routes;
    }

    public void setRoutes(List<T> routes) {
        this.routes = routes;
    }

    public T getRoute(int index) {
        return routes.get(index);
    }

    /**
     * Add a route into the solution.
     * @param route the added route.
     */
    public void addRoute(T route) {
        routes.add(route);
    }

    /**
     * Remove the route with an index.
     * @param index the index of the route to be removed.
     */
    public void removeRoute(int index) {
        routes.remove(index);
    }

    /**
     * Initialise a task sequence solution of an instance.
     * Each route is an initial task sequence route.
     * @param instance the instance.
     * @return the initial task sequence solution.
     */
    public static Solution<TaskSeqRoute> initialTaskSeqSolution(Instance instance) {
        Solution<TaskSeqRoute> solution = new Solution<>();
        for (int i = 0; i < instance.getNumVehicles(); i++)
            solution.addRoute(TaskSeqRoute.initial(instance));

        return solution;
    }

    /**
     * Initialise a node sequence solution of an instance.
     * Each route is an initial node sequence route.
     * @param instance the instance.
     * @param numRoutes the number of routes.
     * @return the initial node sequence solution.
     */
    public static Solution<NodeSeqRoute> initialNodeSeqSolution(Instance instance,
                                                                int numRoutes) {
        Solution<NodeSeqRoute> solution = new Solution<>();
        for (int i = 0; i < numRoutes; i++)
            solution.addRoute(NodeSeqRoute.initial(instance));

        return solution;
    }

    /**
     * Initialise a node sequence solution of an instance.
     * Each route is an initial node sequence route.
     * @param instance the instance.
     * @return the initial node sequence solution.
     */
    public static Solution<NodeSeqRoute> initialNodeSeqSolution(Instance instance) {
        return initialNodeSeqSolution(instance, instance.getNumVehicles());
    }

    /**
     * Reset this solution under an instance. This is done by reseting each route.
     * @param instance the given instance.
     */
    public void reset(Instance instance) {
        for (int i = 0; i < instance.getNumVehicles(); i++)
            routes.get(i).reset(instance);
    }

    /**
     * Calculate the total cost, which is the sum of the route costs.
     * @return the total cost.
     */
    public double totalCost() {
        double result = 0;
        for (T route : routes)
            result += route.getCost();

        return result;
    }

    /**
     * Calculate the maximal route cost, i.e. makespan.
     * @return the maximal route cost.
     */
    public double maxRouteCost() {
        double result = -1;
        for (T route : routes) {
            if (result < route.getCost())
                result = route.getCost();
        }

        return result;
    }

    /**
     * Return the value of an objective, NaN if the objective cannot calculated.
     * @param objective the objective.
     * @return the objective value of the solution.
     */
    public double objValue(Objective objective) {
        switch (objective) {
            case TOTAL_COST:
                return totalCost();
            case MAX_ROUTE_COST:
                return maxRouteCost();
            default:
                return Double.NaN;
        }
    }

    @Override
    public String toString() {
        String str = "Solution: \n";
        for (int i = 0; i < routes.size(); i++) {
            str = str + "route " + i + ": ";
            str = str + routes.get(i).toString() + " \n";
        }

        return str;
    }

    public Solution<T> clone() {
        Solution<T> clonedSol = new Solution<>();
        for (T route : routes)
            clonedSol.addRoute((T)route.clone());

        return clonedSol;
    }
}
