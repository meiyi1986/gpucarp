package gphhucarp.algorithm.sopoc;

import ec.Evaluator;
import ec.EvolutionState;
import ec.Individual;

public class SoPoCEvaluator extends Evaluator {
    @Override
    public void evaluatePopulation(EvolutionState state) {
        SoPoCEvolutionState soPoCEvolutionState = (SoPoCEvolutionState)state;
        SoPoCProblem prob = (SoPoCProblem)p_problem;

        // do evaluation
        Individual[] inds = new Individual[state.population.subpops.length];
        boolean[] updates = new boolean[state.population.subpops.length];

        // initialise inds as the context vector
        for(int i = 0; i < state.population.subpops.length; i++) {
            inds[i] = soPoCEvolutionState.getContext(i);
            updates[i] = false;
        }

        for(int i = 0; i < state.population.subpops.length; i++) {
            updates[i] = true;

            int evaluations = 0;

            for(int j = 0; j < state.population.subpops[i].individuals.length; j++) {
                Individual individual = state.population.subpops[i].individuals[j];

                // Evaluate this individual using the context vector
                inds[i] = individual;

                prob.evaluate(state, inds, updates, false, new int[state.population.subpops.length], 0);
                evaluations ++;
            }

            state.output.message("Evaluations: " + evaluations);
        }


    }

    @Override
    public boolean runComplete(EvolutionState state) {
        return false;
    }
}
