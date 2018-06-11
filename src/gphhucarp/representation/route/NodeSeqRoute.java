package gphhucarp.representation.route;

import gphhucarp.core.Arc;
import gphhucarp.core.Instance;

import java.util.LinkedList;
import java.util.List;

/**
 * A node sequence route is a sequence of nodes
 * plus a sequence indicating the fraction of demand served (0 if not served).
 * For example:
 * ----------------------------------
 * Node sequence:     [0,1,5,3,4,2,0]
 * Fraction sequence: [ 0,1,0,0,1,1 ]
 * ----------------------------------
 * means the route serves (1,5), (4,2) and (2,0).
 *
 * Note that the indicating vector can be double, i.e. a fraction of demand is served.
 *
 * Created by gphhucarp on 25/08/17.
 */
public class NodeSeqRoute extends Route {
    private List<Integer> nodeSequence;
    private List<Double> fracSequence;

    // fields used during the decision process
    private Arc nextTask; // the next task to serve (depot loop if refilling)

    public NodeSeqRoute(double capacity, double demand, double cost,
                        List<Integer> nodeSequence, List<Double> fracSequence) {
        super(capacity, demand, cost);
        this.nodeSequence = nodeSequence;
        this.fracSequence = fracSequence;
    }

    public NodeSeqRoute(double capacity) {
        this(capacity, 0, 0, new LinkedList<>(), new LinkedList<>());
    }

    public List<Integer> getNodeSequence() {
        return nodeSequence;
    }

    public List<Double> getFracSequence() {
        return fracSequence;
    }

    public int getNode(int index) {
        return nodeSequence.get(index);
    }

    public double getFraction(int index) {
        return fracSequence.get(index);
    }

    public Arc getNextTask() {
        return nextTask;
    }

    public void setNextTask(Arc nextTask) {
        this.nextTask = nextTask;
    }

    /**
     * Add a node of an instance in a pilot search (not knowing the actual demand and cost)
     * @param node the node to be added.
     * @param fraction
     * @param instance
     */
    public void addPilot(int node, double fraction, Instance instance) {
        Arc arc = instance.getGraph().getArc(currNode(), node);

        nodeSequence.add(node);
        fracSequence.add(fraction);
        demand += arc.getExpectedDemand() * fraction;
        cost += arc.getServeCost() * fraction + arc.getExpectedDeadheadingCost() * (1-fraction);
    }

    /**
     * Add a node of an instance with a possible service.
     * @param node the node.
     * @param fraction the fraction of demand to be served (1 if fully served, 0 if not served).
     */
    public void add(int node, double fraction, Instance instance) {
        Arc arc = instance.getGraph().getArc(currNode(), node);

        nodeSequence.add(node);
        fracSequence.add(fraction);
        demand += instance.getActDemand(arc) * fraction;
        cost += arc.getServeCost() * fraction + instance.getActDeadheadingCost(arc) * (1-fraction);
    }

    /**
     * An initial node sequence route for an instance.
     * It starts from the depot.
     * @param instance the instance.
     * @return An initial node sequence route starting from the depot.
     */
    public static NodeSeqRoute initial(Instance instance) {
        NodeSeqRoute initialRoute = new NodeSeqRoute(instance.getCapacity());
        initialRoute.nodeSequence.add(instance.getDepot());

        return initialRoute;
    }

    @Override
    public void reset(Instance instance) {
        demand = 0;
        cost = 0;
        nodeSequence.clear();
        fracSequence.clear();
        nodeSequence.add(instance.getDepot());
    }

    @Override
    public int currNode() {
        return nodeSequence.get(nodeSequence.size()-1);
    }

    @Override
    public String toString() {
        String str = "" + nodeSequence.get(0);
        for (int i = 0; i < fracSequence.size(); i++) {
            if (fracSequence.get(i) == 0) {
                // simply traverse without serving
                str += " -> " + nodeSequence.get(i+1);
            }
            else {
                // serving the arc/task with the fraction of demand
                str += " (" + fracSequence.get(i) + ") " + nodeSequence.get(i+1);
            }
        }


        return str;
    }

    /**
     * Clone the node sequence route.
     * @return the cloned route.
     */
    @Override
    public Route clone() {
        List<Integer> clonedNodeSeq = new LinkedList<>(nodeSequence);
        List<Double> clonedFracSeq = new LinkedList<>(fracSequence);

        NodeSeqRoute cloned = new NodeSeqRoute(capacity, demand, cost, clonedNodeSeq, clonedFracSeq);
        cloned.setNextTask(nextTask);

        return cloned;
    }
}
