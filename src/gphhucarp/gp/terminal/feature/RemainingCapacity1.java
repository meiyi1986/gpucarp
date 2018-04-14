package gphhucarp.gp.terminal.feature;

import gphhucarp.core.Arc;
import gphhucarp.gp.CalcPriorityProblem;
import gphhucarp.gp.terminal.FeatureGPNode;
import gphhucarp.representation.route.NodeSeqRoute;

/**
 * The remaining capacity of the closest alternative route to the candidate task.
 * If there is no alternative route, return 0.
 */

public class RemainingCapacity1 extends FeatureGPNode {
    public RemainingCapacity1() {
        super();
        name = "RQ1";
    }

    @Override
    public double value(CalcPriorityProblem calcPriorityProblem) {
        Arc candidate = calcPriorityProblem.getCandidate();

        if (calcPriorityProblem.getState()
                .getRouteAdjacencyList(candidate).isEmpty())
            return 0;

        NodeSeqRoute route1 = calcPriorityProblem.getState()
                .getRouteAdjacencyList(candidate).get(0);

        return route1.getCapacity() - route1.getDemand();
    }
}
