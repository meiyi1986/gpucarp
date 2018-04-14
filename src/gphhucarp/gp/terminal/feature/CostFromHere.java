package gphhucarp.gp.terminal.feature;

import gphhucarp.core.Arc;
import gphhucarp.core.Instance;
import gphhucarp.representation.route.NodeSeqRoute;
import gphhucarp.gp.CalcPriorityProblem;
import gphhucarp.gp.terminal.FeatureGPNode;

/**
 * Feature: the cost from here (the current node) to the head node of the task.
 *
 * Created by gphhucarp on 30/08/17.
 */
public class CostFromHere extends FeatureGPNode {

    public CostFromHere() {
        super();
        name = "CFH";
    }

    @Override
    public double value(CalcPriorityProblem calcPriorityProblem) {
        Instance instance = calcPriorityProblem.getState().getInstance();
        NodeSeqRoute route = calcPriorityProblem.getRoute();
        Arc candidate = calcPriorityProblem.getCandidate();
        return instance.getGraph().getEstDistance(route.currNode(), candidate.getFrom());
    }
}
