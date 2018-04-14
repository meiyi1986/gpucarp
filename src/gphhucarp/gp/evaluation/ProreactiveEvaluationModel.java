package gphhucarp.gp.evaluation;

import ec.EvolutionState;
import ec.Fitness;
import ec.multiobjective.MultiObjectiveFitness;
import gphhucarp.core.Instance;
import gphhucarp.core.InstanceSamples;
import gphhucarp.core.Objective;
import gphhucarp.decisionprocess.DecisionProcess;
import gphhucarp.decisionprocess.RoutingPolicy;
import gphhucarp.decisionprocess.proreactive.ProreativeDecisionProcess;
import gphhucarp.decisionprocess.reactive.ReactiveDecisionProcess;
import gphhucarp.decisionprocess.routingpolicy.FeasibilityPolicy;
import gphhucarp.representation.Solution;
import gphhucarp.representation.route.NodeSeqRoute;
import gphhucarp.representation.route.TaskSeqRoute;

/**
 * A proactive-reactive evaluation model is a set of proactive-reactive decision process.
 * It evaluates a proactive-reactive routing policy and a task sequence plan,
 * by applying them on each decision process,
 * and returning the average normalised objective values across the processes.
 *
 * It includes
 *  - A list of proactive-reactive decision processes.
 *  - The reference objective value map, indicating the reference value
 *    of a given decision process and a given objective.
 *
 * Created by gphhucarp on 31/08/17.
 */

public class ProreactiveEvaluationModel extends EvaluationModel {

    @Override
    public void evaluate(RoutingPolicy policy, Solution<TaskSeqRoute> plan,
                         Fitness fitness, EvolutionState state) {
        double[] fitnesses = new double[objectives.size()];

        int numdps = 0;
        for (InstanceSamples iSamples : instanceSamples) {
            for (long seed : iSamples.getSeeds()) {
                ProreativeDecisionProcess dp = DecisionProcess.initProreactive(
                        iSamples.getBaseInstance(), seed,
                        new FeasibilityPolicy(), null);

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
                ProreativeDecisionProcess dp = DecisionProcess.initProreactive(
                        iSamples.getBaseInstance(), seed,
                        new FeasibilityPolicy(), null);

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
