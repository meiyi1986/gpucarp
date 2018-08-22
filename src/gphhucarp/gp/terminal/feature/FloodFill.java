package gphhucarp.gp.terminal.feature;

import gphhucarp.core.Arc;
import gphhucarp.decisionprocess.DecisionProcessState;
import gphhucarp.gp.CalcPriorityProblem;
import gphhucarp.gp.terminal.FeatureGPNode;

public class FloodFill extends FeatureGPNode {

    public FloodFill() {
        super();
        name = "FF";
    }

    @Override
    public double value(CalcPriorityProblem calcPriorityProblem) {
        Arc candidate = calcPriorityProblem.getCandidate();
        DecisionProcessState state = calcPriorityProblem.getState();
        return state.isOnFloods(candidate).size() + state.isOnFloods(candidate.getInverse()).size();
    }
}
