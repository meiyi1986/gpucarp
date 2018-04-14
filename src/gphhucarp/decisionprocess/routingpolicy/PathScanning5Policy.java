package gphhucarp.decisionprocess.routingpolicy;

import gphhucarp.core.Arc;
import gphhucarp.core.Graph;
import gphhucarp.core.Instance;
import gphhucarp.decisionprocess.TieBreaker;
import gphhucarp.decisionprocess.poolfilter.ExpFeasiblePoolFilter;
import gphhucarp.decisionprocess.tiebreaker.SimpleTieBreaker;
import gphhucarp.representation.route.NodeSeqRoute;
import gphhucarp.decisionprocess.DecisionProcessState;
import gphhucarp.decisionprocess.PoolFilter;
import gphhucarp.decisionprocess.RoutingPolicy;

/**
 * The path scanning 5 policy first selects the nearest neighbours.
 * Among multiple nearest neighbours,
 * it maximises the distance to the depot if less than half full,
 * and minimises the distance to the depot if more than half full.
 */

public class PathScanning5Policy extends RoutingPolicy {
    // a sufficiently large coefficient to guarantee the priority of cost from here
    public static final double ALPHA = 10000;

    public PathScanning5Policy(PoolFilter poolFilter, TieBreaker tieBreaker) {
        super(poolFilter, tieBreaker);
        name = "\"PS5\"";
    }

    public PathScanning5Policy(TieBreaker tieBreaker) {
        this(new ExpFeasiblePoolFilter(), tieBreaker);
    }

    public PathScanning5Policy() {
        this(new SimpleTieBreaker());
    }

    @Override
    public double priority(Arc candidate, NodeSeqRoute route, DecisionProcessState state) {
        Instance instance = state.getInstance();
        Graph graph = instance.getGraph();
        double costFromHere = graph.getEstDistance(route.currNode(), candidate.getFrom());
        double costToDepot = graph.getEstDistance(candidate.getTo(), instance.getDepot());
        double fullness = route.getDemand() / route.getCapacity();

        int fullnessCoefficient = 1;
        if (fullness < 0.5)
            fullnessCoefficient = -1; // maximise cost to depot if less than half full

        return ALPHA * costFromHere + fullnessCoefficient * costToDepot;
    }
}
