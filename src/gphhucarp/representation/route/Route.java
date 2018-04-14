package gphhucarp.representation.route;

import gphhucarp.core.Instance;

/**
 * An abstract class of a route.
 * A route should start and end at the depot.
 * It cannot serve the demand more than its capacity.
 *
 * Created by gphhucarp on 25/08/17.
 */
public abstract class Route {

    protected double capacity; // the capacity of the route
    protected double demand; // the total demand served by the route
    protected double cost; // the total cost/time of the route

    public Route(double capacity, double demand, double cost) {
        this.capacity = capacity;
        this.demand = demand;
        this.cost = cost;
    }

    public double getCapacity() {
        return capacity;
    }

    public double getDemand() {
        return demand;
    }

    public double getCost() {
        return cost;
    }

    public void setCapacity(double capacity) {
        this.capacity = capacity;
    }

    public void setDemand(double demand) {
        this.demand = demand;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    /**
     * Return the current node of the route, i.e. the current location of the vehicle.
     * It is essentially the last node in the sequence.
     * @return the current node of the route.
     */
    public abstract int currNode();

    /**
     * Reset the node sequence route under an instance.
     * @param instance the instance.
     */
    public abstract void reset(Instance instance);

    /**
     * Clone the route.
     * @return the cloned route.
     */
    public abstract Route clone();
}
