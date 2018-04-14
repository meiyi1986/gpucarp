package gphhucarp.decisionprocess.routingpolicy;

import gphhucarp.core.Arc;
import gphhucarp.core.Graph;
import gphhucarp.core.Instance;
import gphhucarp.decisionprocess.DecisionProcessState;
import gphhucarp.decisionprocess.PoolFilter;
import gphhucarp.decisionprocess.RoutingPolicy;
import gphhucarp.decisionprocess.TieBreaker;
import gphhucarp.decisionprocess.poolfilter.ExpFeasiblePoolFilter;
import gphhucarp.decisionprocess.tiebreaker.SimpleTieBreaker;
import gphhucarp.representation.route.NodeSeqRoute;

/**
 * The path scanning 3 policy first selects the nearest neighbours.
 * Among multiple nearest neighbours,
 * it maximises the yield = demand/servCost
 */

public class PathScanning3Policy extends RoutingPolicy {
    // a sufficiently large coefficient to guarantee the priority of cost from here
    public static final double ALPHA = 10000;

    public PathScanning3Policy(PoolFilter poolFilter, TieBreaker tieBreaker) {
        super(poolFilter, tieBreaker);
        name = "\"PS3\"";
    }

    public PathScanning3Policy(TieBreaker tieBreaker) {
        this(new ExpFeasiblePoolFilter(), tieBreaker);
    }

    public PathScanning3Policy() {
        this(new SimpleTieBreaker());
    }

    @Override
    public double priority(Arc candidate, NodeSeqRoute route, DecisionProcessState state) {
        Instance instance = state.getInstance();
        Graph graph = instance.getGraph();
        double costFromHere = graph.getEstDistance(route.currNode(), candidate.getFrom());
        double yield = state.getInstance().getActDemand(candidate) / candidate.getServeCost();

        return ALPHA * costFromHere - yield;
    }
}
