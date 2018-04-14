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
 * The path scanning 2 policy first selects the nearest neighbours.
 * Among multiple nearest neighbours,
 * it minimises the cost to depot
 */

public class PathScanning2Policy extends RoutingPolicy {
    // a sufficiently large coefficient to guarantee the priority of cost from here
    public static final double ALPHA = 10000;

    public PathScanning2Policy(PoolFilter poolFilter, TieBreaker tieBreaker) {
        super(poolFilter, tieBreaker);
        name = "\"PS2\"";
    }

    public PathScanning2Policy(TieBreaker tieBreaker) {
        this(new ExpFeasiblePoolFilter(), tieBreaker);
    }

    public PathScanning2Policy() {
        this(new SimpleTieBreaker());
    }

    @Override
    public double priority(Arc candidate, NodeSeqRoute route, DecisionProcessState state) {
        Instance instance = state.getInstance();
        Graph graph = instance.getGraph();
        double costFromHere = graph.getEstDistance(route.currNode(), candidate.getFrom());
        double costToDepot = graph.getEstDistance(candidate.getTo(), instance.getDepot());

        return ALPHA * costFromHere + costToDepot;
    }
}
