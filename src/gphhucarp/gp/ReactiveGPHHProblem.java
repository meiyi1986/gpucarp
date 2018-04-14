package gphhucarp.gp;

import ec.EvolutionState;
import ec.Individual;
import ec.gp.GPIndividual;
import ec.gp.GPProblem;
import ec.simple.SimpleProblemForm;
import ec.util.Parameter;
import gphhucarp.core.Objective;
import gphhucarp.decisionprocess.TieBreaker;
import gphhucarp.decisionprocess.routingpolicy.GPRoutingPolicy;
import gphhucarp.decisionprocess.PoolFilter;
import gphhucarp.gp.evaluation.EvaluationModel;

import java.util.List;

/**
 * A reactive GPHH problem to evaluate a reactive routing policy during the GPHH.
 * The evaluationg model is a reactive evaluation model.
 * It also includes a pool filter specifying how to filter out the pool of candidate tasks.
 */

public class ReactiveGPHHProblem extends GPProblem implements SimpleProblemForm {

    public static final String P_EVAL_MODEL = "eval-model";
    public static final String P_POOL_FILTER = "pool-filter";
    public static final String P_TIE_BREAKER = "tie-breaker";

    protected EvaluationModel evaluationModel;
    protected PoolFilter poolFilter;
    protected TieBreaker tieBreaker;

    public List<Objective> getObjectives() {
        return evaluationModel.getObjectives();
    }

    public EvaluationModel getEvaluationModel() {
        return evaluationModel;
    }

    public PoolFilter getPoolFilter() {
        return poolFilter;
    }

    public TieBreaker getTieBreaker() {
        return tieBreaker;
    }

    public void rotateEvaluationModel() {
        evaluationModel.rotateSeeds();
    }

    @Override
    public void setup(final EvolutionState state, final Parameter base) {
        super.setup(state, base);

        Parameter p = base.push(P_EVAL_MODEL);
        evaluationModel = (EvaluationModel)(
                state.parameters.getInstanceForParameter(
                        p, null, EvaluationModel.class));
        evaluationModel.setup(state, p);

        p = base.push(P_POOL_FILTER);
        poolFilter = (PoolFilter)(
                state.parameters.getInstanceForParameter(
                        p, null, PoolFilter.class));

        p = base.push(P_TIE_BREAKER);
        tieBreaker = (TieBreaker)(
                state.parameters.getInstanceForParameter(
                        p, null, TieBreaker.class));
    }

    @Override
    public void evaluate(EvolutionState state,
                         Individual indi,
                         int subpopulation,
                         int threadnum) {
        GPRoutingPolicy policy =
                new GPRoutingPolicy(poolFilter, ((GPIndividual)indi).trees[0]);

        // the evaluation model is reactive, so no plan is specified.
        evaluationModel.evaluate(policy, null, indi.fitness, state);

        indi.evaluated = true;
    }
}
