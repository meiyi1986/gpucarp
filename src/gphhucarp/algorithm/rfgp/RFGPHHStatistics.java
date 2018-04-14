package gphhucarp.algorithm.rfgp;

import ec.EvolutionState;
import ec.simple.SimpleProblemForm;
import ec.simple.SimpleStatistics;

/**
 * The statistics for RFGPHH. Finally, it will return all the elements in the forest (ensemble)
 * as the best individuals of the run.
 */

public class RFGPHHStatistics extends SimpleStatistics {

    /** Logs the best individual of the run. */
    public void finalStatistics(final EvolutionState state, final int result) {
        RFGPHHEvolutionState rfgpState = (RFGPHHEvolutionState)state;

        if (doFinal) state.output.println("\nBest Individual of Run:",statisticslog);
        for(int x = 0;x < rfgpState.ensemble.size(); x++) {
            if (doFinal) state.output.println("Subpopulation " + x + ":",statisticslog);
            if (doFinal) rfgpState.ensemble.get(x).printIndividualForHumans(state,statisticslog);
            if (doMessage && !silentPrint) state.output.message("Subpop " + x + " best fitness of run: " + rfgpState.ensemble.get(x).fitness.fitnessToStringForHumans());

            // finally describe the winner if there is a description
            if (doFinal && doDescription)
                if (state.evaluator.p_problem instanceof SimpleProblemForm)
                    ((SimpleProblemForm)(state.evaluator.p_problem.clone())).describe(state, rfgpState.ensemble.get(x), x, 0, statisticslog);
        }
    }
}
