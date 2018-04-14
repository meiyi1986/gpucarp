package gphhucarp.gp.terminal.feature;

import gphhucarp.decisionprocess.DecisionProcessState;
import gphhucarp.gp.CalcPriorityProblem;
import gphhucarp.gp.terminal.FeatureGPNode;

/**
 * Feature: the fraction of remaining tasks (unserved)
 * Created by gphhucarp on 31/08/17.
 */
public class FractionRemainingTasks extends FeatureGPNode {

    public FractionRemainingTasks() {
        super();
        name = "FRT";
    }

    @Override
    public double value(CalcPriorityProblem calcPriorityProblem) {
        DecisionProcessState state = calcPriorityProblem.getState();
        return 1.0 * state.getRemainingTasks().size() /
                state.getInstance().getTasks().size();
    }
}
