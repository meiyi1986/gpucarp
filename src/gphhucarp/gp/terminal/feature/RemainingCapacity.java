package gphhucarp.gp.terminal.feature;

import gphhucarp.representation.route.NodeSeqRoute;
import gphhucarp.gp.CalcPriorityProblem;
import gphhucarp.gp.terminal.FeatureGPNode;

/**
 * Feature: the remaining capacity of the route.
 *
 * Created by gphhucarp on 31/08/17.
 */
public class RemainingCapacity extends FeatureGPNode {

    public RemainingCapacity() {
        super();
        name = "RQ";
    }

    @Override
    public double value(CalcPriorityProblem calcPriorityProblem) {
        NodeSeqRoute route = calcPriorityProblem.getRoute();
        return route.getCapacity() - route.getDemand();
    }
}
