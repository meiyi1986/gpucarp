package gphhucarp.algorithm.pilotsearch;

import ec.EvolutionState;
import ec.Fitness;
import ec.multiobjective.MultiObjectiveFitness;
import ec.util.Parameter;
import gphhucarp.core.InstanceSamples;
import gphhucarp.core.Objective;
import gphhucarp.decisionprocess.DecisionProcess;
import gphhucarp.decisionprocess.RoutingPolicy;
import gphhucarp.decisionprocess.reactive.ReactiveDecisionProcess;
import gphhucarp.gp.evaluation.EvaluationModel;
import gphhucarp.representation.Solution;
import gphhucarp.representation.route.NodeSeqRoute;
import gphhucarp.representation.route.TaskSeqRoute;

public class PilotSearchEvaluationModel extends EvaluationModel {
    public static final String PILOT_SEARCHER = "pilot-searcher";

    private PilotSearcher pilotSearcher;

    @Override
    public void setup(final EvolutionState state, final Parameter base) {
        super.setup(state, base);

        // read the pilot searcher from parameter
        Parameter p = base.push(PILOT_SEARCHER);
        pilotSearcher = (PilotSearcher)(state.parameters.getInstanceForParameter(p,
                null, PilotSearcher.class));
    }

    @Override
    public void evaluate(RoutingPolicy policy, Solution<TaskSeqRoute> plan,
                         Fitness fitness, EvolutionState state) {
        double[] fitnesses = new double[objectives.size()];

        int numdps = 0;
        for (InstanceSamples iSamples : instanceSamples) {
            for (long seed : iSamples.getSeeds()) {
                // create a new reactive decision process from the based intance and the seed.
                ReactiveDecisionProcess dp =
                        DecisionProcess.initPilotSearch(iSamples.getBaseInstance(),
                                seed, policy, pilotSearcher);

                dp.run();
                Solution<NodeSeqRoute> solution = dp.getState().getSolution();
                for (int j = 0; j < fitnesses.length; j++) {
                    Objective objective = objectives.get(j);
                    double normObjValue =
                            solution.objValue(objective); // / getObjRefValue(i, objective);
                    fitnesses[j] += normObjValue;
                }
                dp.reset();

                numdps ++;
            }
        }

        for (int j = 0; j < fitnesses.length; j++) {
            fitnesses[j] /= numdps;
        }

        MultiObjectiveFitness f = (MultiObjectiveFitness)fitness;
        f.setObjectives(state, fitnesses);
    }

    @Override
    public void evaluateOriginal(RoutingPolicy policy,
                                 Solution<TaskSeqRoute> plan,
                                 Fitness fitness, EvolutionState state) {
        double[] fitnesses = new double[objectives.size()];

        int numdps = 0;
        for (InstanceSamples iSamples : instanceSamples) {
            for (long seed : iSamples.getSeeds()) {
                // create a new reactive decision process from the based intance and the seed.
                ReactiveDecisionProcess dp =
                        DecisionProcess.initPilotSearch(iSamples.getBaseInstance(),
                                seed, policy, pilotSearcher);

                dp.run();
                Solution<NodeSeqRoute> solution = dp.getState().getSolution();
                for (int j = 0; j < fitnesses.length; j++) {
                    Objective objective = objectives.get(j);
                    double normObjValue =
                            solution.objValue(objective);
                    fitnesses[j] += normObjValue;
                }
                dp.reset();

                numdps ++;
            }
        }

        for (int j = 0; j < fitnesses.length; j++) {
            fitnesses[j] /= numdps;
        }

        MultiObjectiveFitness f = (MultiObjectiveFitness)fitness;
        f.setObjectives(state, fitnesses);
    }
}
