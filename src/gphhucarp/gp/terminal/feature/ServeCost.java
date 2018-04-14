package gphhucarp.gp.terminal.feature;

import gphhucarp.gp.CalcPriorityProblem;
import gphhucarp.gp.terminal.FeatureGPNode;

/**
 * Feature: the serving cost of the candidate task.
 *
 * Created by gphhucarp on 30/08/17.
 */
public class ServeCost extends FeatureGPNode {
    public ServeCost() {
        super();
        name = "SC";
    }

    @Override
    public double value(CalcPriorityProblem calcPriorityProblem) {
        return calcPriorityProblem.getCandidate().getServeCost();
    }
}
