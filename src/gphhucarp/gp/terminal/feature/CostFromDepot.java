package gphhucarp.gp.terminal.feature;

import gphhucarp.core.Arc;
import gphhucarp.core.Instance;
import gphhucarp.gp.CalcPriorityProblem;
import gphhucarp.gp.terminal.FeatureGPNode;
import gphhucarp.representation.route.NodeSeqRoute;

/**
 * Feature: the cost from the depot to the head node of the task.
 *
 * Created by gphhucarp on 31/08/17.
 */
public class CostFromDepot extends FeatureGPNode {

    public CostFromDepot() {
        super();
        name = "CFD";
    }

    @Override
    public double value(CalcPriorityProblem calcPriorityProblem) {
        Instance instance = calcPriorityProblem.getState().getInstance();
        NodeSeqRoute route = calcPriorityProblem.getRoute();
        Arc candidate = calcPriorityProblem.getCandidate();
        return instance.getGraph().getEstDistance(instance.getDepot(), candidate.getFrom());
    }
}
