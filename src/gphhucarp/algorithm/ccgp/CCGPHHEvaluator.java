package gphhucarp.algorithm.ccgp;

import ec.Evaluator;
import ec.EvolutionState;
import ec.Individual;
import ec.Population;
import ec.coevolve.GroupedProblemForm;
import ec.simple.SimpleBreeder;

/**
 * The CCGPHH evaluator. It evaluates each individual using the context vector.
 * Specifically, for evaluating an individual, it first takes the context vector,
 * and then replace the corresponding index with the individual to be evaluated,
 * then evaluate the modified context vector (using the combiner of the CCGPHHProblem).
 * Finally, it assigns the fitness of the modified context vector to the individual.
 * If better fitness is found, it also updates the context vector.
 */

public class CCGPHHEvaluator extends Evaluator {

    // individuals to evaluate together
    private Individual[] inds = null;
    // which individual should have its fitness updated as a result
    private boolean[] updates = null;

    @Override
    public void evaluatePopulation(EvolutionState state) {
        // determine who needs to be evaluated
        boolean[] preAssessFitness = new boolean[state.population.subpops.length];
        boolean[] postAssessFitness = new boolean[state.population.subpops.length];

        for(int i = 0; i < state.population.subpops.length; i++) {
            postAssessFitness[i] = shouldEvaluateSubpop(state, i, 0);
            preAssessFitness[i] = postAssessFitness[i] || (state.generation == 0);  // always prepare (set up trials) on generation 0
        }


        // do evaluation
        ((GroupedProblemForm)p_problem).preprocessPopulation(state,state.population, preAssessFitness, false);
        performCoevolutionaryEvaluation(state, state.population, (GroupedProblemForm)p_problem );
        ((GroupedProblemForm)p_problem).postprocessPopulation(state, state.population, postAssessFitness, false);
    }

    /** Returns true if the subpopulation should be evaluated.  This will happen if the Breeder
     believes that the subpopulation should be breed afterwards. */
    public boolean shouldEvaluateSubpop(EvolutionState state, int subpop, int threadnum)
    {
        return (state.breeder instanceof SimpleBreeder &&
                ((SimpleBreeder)(state.breeder)).shouldBreedSubpop(state, subpop, threadnum));
    }

    public void performCoevolutionaryEvaluation(EvolutionState state,
                                                Population population,
                                                GroupedProblemForm prob ) {
        CCGPHHEvolutionState ccgpState = (CCGPHHEvolutionState)state;

        int evaluations = 0;

        inds = new Individual[population.subpops.length];
        updates = new boolean[population.subpops.length];

        for(int j = 0; j < state.population.subpops.length; j++) {
            if (!shouldEvaluateSubpop(state, j, 0))
                continue;  // don't evaluate this subpopulation

            // for each individual
            for(int i = 0; i < state.population.subpops[j].individuals.length; i++) {
                Individual individual = state.population.subpops[j].individuals[i];

                // Evaluate this individual using the context vector
                for (int ind = 0; ind < inds.length; ind++) {
                    if (ind == j) {
                        inds[ind] = individual;
                        updates[ind] = true;
                    }
                    else {
                        inds[ind] = ccgpState.getContext(ind);
                        updates[ind] = false;
                    }
                }

                prob.evaluate(state, inds, updates, false, new int[state.population.subpops.length], 0);
                evaluations ++;
            }
        }

        state.output.message("Evaluations: " + evaluations);
    }

    @Override
    public boolean runComplete(EvolutionState state) {
        return false;
    }
}
