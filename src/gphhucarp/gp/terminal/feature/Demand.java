package gphhucarp.gp.terminal.feature;

import gphhucarp.gp.CalcPriorityProblem;
import gphhucarp.gp.terminal.FeatureGPNode;

/**
 * Feature: the expected demand of the candidate task.
 *
 * Created by gphhucarp on 30/08/17.
 */
public class Demand extends FeatureGPNode {
    public Demand() {
        super();
        name = "DEM";
    }

    @Override
    public double value(CalcPriorityProblem calcPriorityProblem) {
        return calcPriorityProblem.getCandidate().getExpectedDemand();
    }
}
