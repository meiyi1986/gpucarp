package gphhucarp.algorithm.sopoc.localsearch;

import ec.Individual;
import gphhucarp.algorithm.edasls.EdgeHistogramMatrix;
import gphhucarp.algorithm.edasls.GiantTaskSequenceIndividual;
import gphhucarp.algorithm.sopoc.SoPoCEvolutionState;
import gphhucarp.algorithm.sopoc.SoPoCProblem;
import gphhucarp.core.Arc;

import java.util.LinkedList;
import java.util.List;

/**
 * The 2-opt operator defines a move as inversing a subsequence.
 * It pads the sequence by depot loops to address boundary issues.
 *
 */
public class SoPoCTwoOpt extends SoPoCLocalSearch {

    @Override
    public GiantTaskSequenceIndividual move(SoPoCEvolutionState state, GiantTaskSequenceIndividual curr) {
        EdgeHistogramMatrix ehm = state.getEhm();
        SoPoCProblem problem = (SoPoCProblem)state.evaluator.p_problem;

        Individual[] inds = new Individual[state.population.subpops.length];
        boolean[] updates = new boolean[state.population.subpops.length];

        // initialise inds as the context vector
        for(int i = 0; i < state.population.subpops.length; i++) {
            inds[i] = state.getContext(i);
            updates[i] = false;
        }

        // evaluate subpop 0: the baseline solution
        updates[0] = true;

        List<Arc> seq = new LinkedList<>(curr.getTaskSequence());

        // pad the sequence by depot loop
        seq.add(state.getUcarpInstance().getDepotLoop());
        seq.add(0, state.getUcarpInstance().getDepotLoop());

        GiantTaskSequenceIndividual neighbour;

        for (int start = 1; start < seq.size()-1; start++) {
            for (int finish = start; finish < seq.size()-1; finish++) {
                Arc pre = seq.get(start-1); // predecessor
                Arc suc = seq.get(finish+1); // successor
                Arc head = seq.get(start); // head of subroute
                Arc tail = seq.get(finish); // tail of subroute

                double oldEhm = ehm.getValue(pre, head) +
                        ehm.getValue(tail, suc);

                // pre-check the delta ehm of inversing the subroute from start to finish
                double newEhm = ehm.getValue(pre, tail.getInverse()) +
                        ehm.getValue(head.getInverse(), suc);

                if (newEhm <= oldEhm)
                    continue;

                // the new ehm is greater than the old ehm, apply the move
                neighbour = curr.clone();
                for (int i = start; i < finish+1; i++)
                    neighbour.getTaskSequence().set(i-1, seq.get(finish+start-i).getInverse());

                inds[0] = neighbour;
                problem.evaluate(state, inds, updates, false, new int[state.population.subpops.length], 0);
                state.EDASLSFEs[state.generation] ++;

                if (neighbour.fitness.betterThan(curr.fitness))
                    return neighbour;
            }
        }

        return curr;
    }
}
