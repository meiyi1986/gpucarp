package gphhucarp.gp.terminal.feature;

import gphhucarp.core.Arc;
import gphhucarp.gp.CalcPriorityProblem;
import gphhucarp.gp.terminal.FeatureGPNode;
import gphhucarp.representation.route.NodeSeqRoute;

/**
 * The cost from the closest alternative route to the candidate task.
 * If there is no alternative route, return 0.
 */

public class CostFromRoute1 extends FeatureGPNode {
    public CostFromRoute1() {
        super();
        name = "CFR1";
    }

    @Override
    public double value(CalcPriorityProblem calcPriorityProblem) {
        Arc candidate = calcPriorityProblem.getCandidate();

        if (calcPriorityProblem.getState()
                .getRouteAdjacencyList(candidate).isEmpty())
            return 0;

        NodeSeqRoute route1 = calcPriorityProblem.getState()
                .getRouteAdjacencyList(candidate).get(0);

        return calcPriorityProblem.getState().getInstance().getGraph().getEstDistance(route1.currNode(), candidate.getFrom());
    }
}
