package gphhucarp.decisionprocess.poolfilter;

import gphhucarp.core.Arc;
import gphhucarp.representation.route.NodeSeqRoute;
import gphhucarp.decisionprocess.DecisionProcessState;
import gphhucarp.decisionprocess.PoolFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * A feasible pool filter filters the candidate tasks from the pool by selecting
 * only the tasks that are expected to be feasible to be added, i.e.
 * the tasks whose expected demands do not exceed the remaining capacity.
 */

public class ExpFeasiblePoolFilter extends PoolFilter {

    @Override
    public List<Arc> filter(List<Arc> pool,
                            NodeSeqRoute route,
                            DecisionProcessState state) {
        double remainingCapacity = route.getCapacity() - route.getDemand();

        List<Arc> filtered = new ArrayList<>();
        for (Arc candidate : pool) {
            // check if the task is expected to be feasible or not
            if (candidate.getExpectedDemand() > remainingCapacity)
                continue;

            filtered.add(candidate);
        }

        return filtered;
    }
}
