package gphhucarp.gp.terminal.feature;

import gphhucarp.core.Arc;
import gphhucarp.gp.CalcPriorityProblem;
import gphhucarp.gp.terminal.FeatureGPNode;

import java.util.List;

/**
 * The cost from the candidate to its closest remaining task.
 * If there is no remaining task, return 0.
 */

public class CostToTask1 extends FeatureGPNode {
    public CostToTask1() {
        super();
        name = "CTT1";
    }

    @Override
    public double value(CalcPriorityProblem calcPriorityProblem) {
        Arc candidate = calcPriorityProblem.getCandidate();
        List<Arc> taskAdjacentList = calcPriorityProblem.getState()
                .getTaskAdjacencyList(candidate);

        if (taskAdjacentList.isEmpty())
            return 0;

        Arc task1 = taskAdjacentList.get(0);

        return calcPriorityProblem.getState().getInstance().getGraph().getEstDistance(candidate, task1);
    }
}
