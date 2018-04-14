package gphhucarp.algorithm.edasls.localsearch;

import gphhucarp.algorithm.edasls.EDASLSEvolutionState;
import gphhucarp.algorithm.edasls.EDASLSProblem;
import gphhucarp.algorithm.edasls.EdgeHistogramMatrix;
import gphhucarp.algorithm.edasls.GiantTaskSequenceIndividual;
import gphhucarp.core.Arc;

import java.util.LinkedList;
import java.util.List;

/**
 * The 2-opt operator defines a move as inversing a subsequence.
 * It pads the sequence by depot loops to address boundary issues.
 *
 */
public class EhmTwoOpt extends EhmLocalSearch {

    @Override
    public GiantTaskSequenceIndividual move(EDASLSEvolutionState state, GiantTaskSequenceIndividual curr) {
        EdgeHistogramMatrix ehm = state.getEhm();
        EDASLSProblem problem = (EDASLSProblem)state.evaluator.p_problem;

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
                problem.evaluate(state, neighbour, 0, 0);

                if (neighbour.fitness.betterThan(curr.fitness))
                    return neighbour;
            }
        }

        return curr;
    }
}
