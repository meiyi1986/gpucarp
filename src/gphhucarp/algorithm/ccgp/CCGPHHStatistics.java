package gphhucarp.algorithm.ccgp;

import ec.EvolutionState;
import ec.Individual;
import ec.simple.SimpleProblemForm;
import ec.simple.SimpleStatistics;

/**
 * The CCGPHH statistics.
 * It prints out the context vector for each generation.
 */

public class CCGPHHStatistics extends SimpleStatistics {

    @Override
    public void postEvaluationStatistics(final EvolutionState state) {

        CCGPHHEvolutionState ccgpState = (CCGPHHEvolutionState)state;

        // for now we just print the best fitness per subpopulation.
        Individual[] best_i = new Individual[state.population.subpops.length];  // quiets compiler complaints
        for(int x=0;x<state.population.subpops.length;x++)
        {
            best_i[x] = state.population.subpops[x].individuals[0];
            for(int y=1;y<state.population.subpops[x].individuals.length;y++)
            {
                if (state.population.subpops[x].individuals[y] == null)
                {

                }
                else if (best_i[x] == null || state.population.subpops[x].individuals[y].fitness.betterThan(best_i[x].fitness))
                    best_i[x] = state.population.subpops[x].individuals[y];
                if (best_i[x] == null)
                {

                }
            }

            // now demo to see if it's the new best_of_run
            if (best_of_run[x]==null || best_i[x].fitness.betterThan(best_of_run[x].fitness))
                best_of_run[x] = (Individual)(best_i[x].clone());
        }

        // print the best-of-generation individual
        if (doGeneration)
            state.output.println("\nGeneration: " + state.generation,statisticslog);

        if (doGeneration)
            state.output.println("Best Individual:",statisticslog);

        for(int x = 0; x < state.population.subpops.length; x++) {
            if (doGeneration)
                state.output.println("Subpopulation " + x + ":",statisticslog);

            if (doGeneration)
                ccgpState.getContext(x).printIndividualForHumans(state,statisticslog);

            if (doMessage && !silentPrint) state.output.message("Subpop " + x + " best fitness of generation" +
                    (ccgpState.getContext(x).evaluated ? " " : " (evaluated flag not set): ") +
                    ccgpState.getContext(x).fitness.fitnessToStringForHumans());

            // describe the winner if there is a description
            if (doGeneration && doPerGenerationDescription) {
                if (state.evaluator.p_problem instanceof SimpleProblemForm)
                    ((SimpleProblemForm)(state.evaluator.p_problem.clone())).describe(state, ccgpState.getContext(x), x, 0, statisticslog);
            }
        }
    }
}
