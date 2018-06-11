package gphhucarp.algorithm.edasls;

import ec.EvolutionState;
import ec.Individual;
import ec.Problem;
import ec.multiobjective.MultiObjectiveFitness;
import ec.simple.SimpleProblemForm;
import ec.util.Parameter;
import gphhucarp.core.Instance;
import gphhucarp.core.InstanceSamples;
import gphhucarp.core.Objective;
import gphhucarp.decisionprocess.DecisionProcess;
import gphhucarp.decisionprocess.proreactive.ProreativeDecisionProcess;
import gphhucarp.decisionprocess.routingpolicy.FeasibilityPolicy;
import gphhucarp.gp.evaluation.EvaluationModel;

import java.util.List;

public class EDASLSProblem extends Problem implements SimpleProblemForm {

    public static final String P_EVAL_MODEL = "eval-model";

    protected EvaluationModel evaluationModel;

    public List<Objective> getObjectives() {
        return evaluationModel.getObjectives();
    }

    public EvaluationModel getEvaluationModel() {
        return evaluationModel;
    }

    public void rotateEvaluationModel() {
        evaluationModel.rotateSeeds();
    }

    @Override
    public void setup(final EvolutionState state, final Parameter base) {

        Parameter p = base.push(P_EVAL_MODEL);
        evaluationModel = (EvaluationModel)(
                state.parameters.getInstanceForParameter(
                        p, null, EvaluationModel.class));
        evaluationModel.setup(state, p);
    }

    @Override
    public void evaluate(EvolutionState state,
                         Individual indi,
                         int subpopulation,
                         int threadnum) {
        GiantTaskSequenceIndividual chromosome = (GiantTaskSequenceIndividual)indi;

        double[] fitnesses = new double[evaluationModel.getObjectives().size()];

        int numProcesses = 0;
        for (InstanceSamples iSamples : evaluationModel.getInstanceSamples()) {
            chromosome.split(iSamples.getBaseInstance());

            for (long seed : iSamples.getSeeds()) {
                ProreativeDecisionProcess dp =
                        DecisionProcess.initProreactive(iSamples.getBaseInstance(), seed,
                                new FeasibilityPolicy(), chromosome.getSolution());

                numProcesses ++;

                dp.run();

                for (int j = 0; j < fitnesses.length; j++) {
                    Objective objective = evaluationModel.getObjectives().get(j);
                    double objValue = dp.getState().getSolution().objValue(objective);
                    fitnesses[j] += objValue;
                }
            }
        }

        for (int j = 0; j < fitnesses.length; j++) {
            fitnesses[j] /= numProcesses;
        }

        MultiObjectiveFitness f = (MultiObjectiveFitness)indi.fitness;
        f.setObjectives(state, fitnesses);

        indi.evaluated = true;

        // increment the number of fitness evaluations of the current generation
        EDASLSEvolutionState edaslsState = (EDASLSEvolutionState)state;
        edaslsState.genFEs[edaslsState.generation] ++;
    }
}
