package gphhucarp.gp.terminal.feature;

import gphhucarp.core.Arc;
import gphhucarp.core.Instance;
import gphhucarp.representation.route.NodeSeqRoute;
import gphhucarp.gp.CalcPriorityProblem;
import gphhucarp.gp.terminal.FeatureGPNode;

/**
 * Feature: the cost from the tail node of the task to the depot.
 *
 * Created by gphhucarp on 31/08/17.
 */
public class CostToDepot extends FeatureGPNode {

    public CostToDepot() {
        super();
        name = "CTD";
    }

    @Override
    public double value(CalcPriorityProblem calcPriorityProblem) {
        Instance instance = calcPriorityProblem.getState().getInstance();
        NodeSeqRoute route = calcPriorityProblem.getRoute();
        Arc candidate = calcPriorityProblem.getCandidate();
        return instance.getGraph().getEstDistance(candidate.getTo(), instance.getDepot());
    }
}
