package gphhucarp.gp.terminal.feature;

import gphhucarp.gp.CalcPriorityProblem;
import gphhucarp.gp.terminal.FeatureGPNode;

/**
 * Feature: the deadheading cost of the candidate task.
 *
 * Created by gphhucarp on 30/08/17.
 */
public class DeadheadingCost extends FeatureGPNode {
    public DeadheadingCost() {
        super();
        name = "DC";
    }

    @Override
    public double value(CalcPriorityProblem calcPriorityProblem) {
        return calcPriorityProblem.getCandidate().getExpectedDeadheadingCost();
    }
}
