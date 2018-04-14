package gphhucarp.gp.terminal.feature;

import gphhucarp.core.Instance;
import gphhucarp.representation.route.NodeSeqRoute;
import gphhucarp.gp.CalcPriorityProblem;
import gphhucarp.gp.terminal.FeatureGPNode;

/**
 * Feature: the cost for refill, i.e. the cost from the current node to the depot.
 *
 * Created by gphhucarp on 31/08/17.
 */
public class CostRefill extends FeatureGPNode {

    public CostRefill() {
        super();
        name = "CR";
    }

    @Override
    public double value(CalcPriorityProblem calcPriorityProblem) {
        Instance instance = calcPriorityProblem.getState().getInstance();
        NodeSeqRoute route = calcPriorityProblem.getRoute();
        return instance.getGraph().getEstDistance(route.currNode(), instance.getDepot());
    }
}
