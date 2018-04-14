package gphhucarp.core;

import org.apache.commons.math3.random.RandomDataGenerator;
import util.random.AbstractRealSampler;
import util.random.NormalSampler;

/**
 * An arc, which is a directed edge of the graph.
 * It has
 *  - (from, to) nodes,
 *  - serving cost,
 *  - inverser arc,
 *  - random demand (represented by a demand sampler)
 *  - random deadheading cost (represented by a cost sampler)
 *
 * Natural comparison: (a1, b1) < (a2, b2) if a1 < a2 or a1 == a2 and b1 < b2.
 *
 * In addition, it has two fields for decision making process
 *  - remaining demand fraction
 *  - priority
 *
 * Created by gphhucarp on 14/06/17.
 */

public class Arc implements Comparable<Arc> {
    private int from; // from node id
    private int to; // to node id
    private double serveCost; // serve cost >= 0

    private Arc inverse; // the inverse arc is (to, from)

    // In UCARP, the demand and costs are random variables.
    // Their distributions are known in advance (e.g. estimated from history).
    private AbstractRealSampler demandSampler; // the sampler for the demand.
    private AbstractRealSampler costSampler; // the sampler for the deadheading cost.

    private double remainingDemandFraction; // the remaining demand fraction to be served for tasks.
                                            // 0 = completely served; 1 = not served at all.

    private double priority; // the priority for decision making process.

    public Arc(int from, int to, double demand,
               double serveCost, double deadheadingCost,
               Arc inverse, double demandUncertaintyLevel, double costUncertaintyLevel) {
        this.from = from;
        this.to = to;
        this.serveCost = serveCost;
        this.inverse = inverse;

        this.demandSampler = new NormalSampler(demand, demandUncertaintyLevel * demand);
        this.costSampler = new NormalSampler(deadheadingCost, costUncertaintyLevel * deadheadingCost);
    }

    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }

    public double getServeCost() {
        return serveCost;
    }

    public Arc getInverse() {
        return inverse;
    }

    public double getExpectedDemand() {
        return demandSampler.getMean();
    }

    public double getExpectedDeadheadingCost() {
        return costSampler.getMean();
    }

    public double getRemainingDemandFraction() {
        return remainingDemandFraction;
    }

    public double getPriority() {
        return priority;
    }

    public void setServeCost(double serveCost) {
        this.serveCost = serveCost;
    }

    public void setInverse(Arc inverse) {
        this.inverse = inverse;
    }

    public void setRemainingDemandFraction(double remainingDemandFraction) {
        this.remainingDemandFraction = remainingDemandFraction;
    }

    public void setPriority(double priority) {
        this.priority = priority;
    }

    /**
     * Whether the arc is a task, i.e. is required to be served.
     * @return true if the arc is a task, false otherwise.
     */
    public boolean isTask() {
        return demandSampler.getMean() > 0;
    }

    /**
     * Sample an actual demand based on the sampler and a random data generator.
     * If the task is undirected, then set its inverse as well.
     * @param rdg the random data generator.
     * @return the sampled demand.
     */
    public double sampleDemand(RandomDataGenerator rdg) {
        double sampledDemand = demandSampler.next(rdg);

        if (sampledDemand < 0)
            sampledDemand = 0;

        return sampledDemand;
    }

    /**
     * Sample an actual deadheading cost based on the sampler and a random data generator.
     * @param rdg the random data generator.
     * @return the sampled deadheading cost.
     */
    public double sampleDeadheadingCost(RandomDataGenerator rdg) {
        double sampledDeadheadingCost = costSampler.next(rdg);

        // set the deadheading cost to infinity (the arc becomes temporarily unavailable
        // if the sampled value is negative.
        if (sampledDeadheadingCost < 0)
            sampledDeadheadingCost = Double.POSITIVE_INFINITY;

        return sampledDeadheadingCost;
    }

    /**
     * Whether this arc is prior to another arc.
     * An arc is prior to another arc if
     *   (1) it has a smaller priority value, or
     *   (2) they have the same priority, and this arc is better than the other arc.
     * @param o the other arc.
     * @return true if this arc is prior to the other, and false otherwise.
     */
    public boolean priorTo(Arc o) {
        if (Double.compare(priority, o.priority) < 0)
            return true;

        if (Double.compare(priority, o.priority) > 0)
            return false;

        return compareTo(o) < 0;
    }

    @Override
    public String toString() {
        return "(" + from + ", " + to + "), dem = " + demandSampler.getMean()
                + ", sc = " + serveCost + ", dc = " + costSampler.getMean() + " \n";
    }

    public String toSimpleString() {
        return "(" + from + ", " + to + ")";
    }

    @Override
    public int compareTo(Arc o) {
        if (from < o.from)
            return -1;

        if (from > o.from)
            return 1;

        if (to < o.to)
            return -1;

        if (to > o.to)
            return 1;

        return 0;
    }
}
