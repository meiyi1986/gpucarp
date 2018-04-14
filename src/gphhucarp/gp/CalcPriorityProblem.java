package gphhucarp.gp;

import ec.EvolutionState;
import ec.Individual;
import ec.Problem;
import ec.simple.SimpleProblemForm;
import gphhucarp.core.Arc;
import gphhucarp.representation.route.NodeSeqRoute;
import gphhucarp.decisionprocess.DecisionProcessState;

/**
 * The problem for calculating the priority of a candidate task.
 *
 * Created by YiMei on 27/09/16.
 */
public class CalcPriorityProblem extends Problem implements SimpleProblemForm {

    private Arc candidate;
    private NodeSeqRoute route;
    private DecisionProcessState state;

    public CalcPriorityProblem(Arc candidate,
                               NodeSeqRoute route,
                               DecisionProcessState state) {
        this.candidate = candidate;
        this.route = route;
        this.state = state;
    }

    public Arc getCandidate() {
        return candidate;
    }

    public NodeSeqRoute getRoute() {
        return route;
    }

    public DecisionProcessState getState() {
        return state;
    }

    @Override
    public void evaluate(EvolutionState state, Individual ind,
                         int subpopulation, int threadnum) {
    }
}
