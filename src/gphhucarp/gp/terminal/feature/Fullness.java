package gphhucarp.gp.terminal.feature;

import gphhucarp.representation.route.NodeSeqRoute;
import gphhucarp.gp.CalcPriorityProblem;
import gphhucarp.gp.terminal.FeatureGPNode;

/**
 * Feature: the fullness of the route. 0 if totally empty, 1 totally full.
 */

public class Fullness extends FeatureGPNode {

    public Fullness() {
        super();
        name = "FULL";
    }

    @Override
    public double value(CalcPriorityProblem calcPriorityProblem) {
        NodeSeqRoute route = calcPriorityProblem.getRoute();
        return route.getDemand() / route.getCapacity();
    }
}
