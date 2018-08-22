package gphhucarp.algorithm.edasls.localsearch;

import gphhucarp.algorithm.edasls.EDASLSEvolutionState;
import gphhucarp.algorithm.edasls.GiantTaskSequenceIndividual;

/**
 * The Edge Histogram Matrix (ehm)-based stochastic local search.
 * At each move, it first checks the delta ehm to filter out the neighbours
 * whose total ehm is smaller than the current solution.
 *
 * In this way, one can effectively reduce the effort of evaluating unpromising
 * neighbours.
 */

public abstract class EDASLSLocalSearch {

    /**
     * Move one step foward, and return a neighbour no worse than the current solution.
     * @param state the evolution state.
     * @param curr the current solution.
     * @return a neighbour no worse than the current solution.
     */
    public abstract GiantTaskSequenceIndividual move(
            EDASLSEvolutionState state, GiantTaskSequenceIndividual curr);
}
