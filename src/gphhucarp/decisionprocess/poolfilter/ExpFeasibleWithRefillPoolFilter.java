package gphhucarp.decisionprocess.poolfilter;

import gphhucarp.core.Arc;
import gphhucarp.core.Graph;
import gphhucarp.decisionprocess.DecisionProcessState;
import gphhucarp.decisionprocess.PoolFilter;
import gphhucarp.representation.route.NodeSeqRoute;

import java.util.ArrayList;
import java.util.List;

/**
 * This pool filter only selects the tasks that are expected to be feasible to be added,
 * i.e. the tasks whose expected demands do not exceed the remaining capacity.
 * In addition, it will also consider the tasks that can be served if refill can be done on the way.
 * That is, dist(curr, depot) + dist(depot, task.from) = dist(curr, task.from).
 */

public class ExpFeasibleWithRefillPoolFilter extends PoolFilter {

    @Override
    public List<Arc> filter(List<Arc> pool,
                            NodeSeqRoute route,
                            DecisionProcessState state) {
        int currNode = route.currNode();
        int depot = state.getInstance().getDepot();
        Graph graph = state.getInstance().getGraph();
        double remainingCapacity = route.getCapacity() - route.getDemand();

        // if just refilled, then all the candidates are feasible
        if (currNode == depot)
            return pool;

        List<Arc> filtered = new ArrayList<>();
        for (Arc candidate : pool) {
            if (candidate.getExpectedDemand() <= remainingCapacity) {
                // select if the demand does not exceed remaining capacity
                filtered.add(candidate);
            } else if (graph.getEstDistance(currNode, candidate.getFrom()) ==
                    graph.getEstDistance(currNode, depot) +
                    graph.getEstDistance(depot, candidate.getFrom())) {
                // select if depot is passed along the way
                filtered.add(candidate);
            }
        }

        return filtered;
    }
}
