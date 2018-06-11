package gphhucarp.decisionprocess.poolfilter;

import gphhucarp.core.Arc;
import gphhucarp.core.Graph;
import gphhucarp.decisionprocess.DecisionProcessState;
import gphhucarp.decisionprocess.PoolFilter;
import gphhucarp.representation.route.NodeSeqRoute;

import java.util.ArrayList;
import java.util.List;

/**
 * This filter selects only the tasks that are expected to be feasible.
 * It also filters out the tasks if the way from the current node to them
 * passes the depot. The motivation is that this can be replaced by
 * refill-then-serve.
 */

public class ExpFeasibleNoRefillPoolFilter extends PoolFilter {

    @Override
    public List<Arc> filter(List<Arc> pool,
                            NodeSeqRoute route,
                            DecisionProcessState state) {
        int currNode = route.currNode();
        int depot = state.getInstance().getDepot();
        Graph graph = state.getInstance().getGraph();
        double remainingCapacity = route.getCapacity() - route.getDemand();

        // if just refilled, then all the tasks are eligible
        if (currNode == depot)
            return new ArrayList<>(pool);

        List<Arc> filtered = new ArrayList<>();
        for (Arc candidate : pool) {
            // check if the task is expected to be feasible or not
            if (candidate.getExpectedDemand() > remainingCapacity)
                continue;
            
            // check if the way to the candidate passes the depot
            if (graph.getEstDistance(currNode, candidate.getFrom()) ==
                    graph.getEstDistance(currNode, depot) +
                            graph.getEstDistance(depot, candidate.getFrom()))
                continue;

            filtered.add(candidate);
        }

        return filtered;
    }
}
