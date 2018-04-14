package gphhucarp.algorithm.edasls.localsearch;

import gphhucarp.algorithm.edasls.EDASLSEvolutionState;
import gphhucarp.algorithm.edasls.EDASLSProblem;
import gphhucarp.algorithm.edasls.EdgeHistogramMatrix;
import gphhucarp.algorithm.edasls.GiantTaskSequenceIndividual;
import gphhucarp.core.Arc;

import java.util.LinkedList;
import java.util.List;

/**
 * The swap operator defines a move as swapping two tasks.
 * It pads the sequence by depot loops to address boundary issues.
 *
 */
public class EhmSwap extends EhmLocalSearch {

    @Override
    public GiantTaskSequenceIndividual move(EDASLSEvolutionState state, GiantTaskSequenceIndividual curr) {
        EdgeHistogramMatrix ehm = state.getEhm();
        EDASLSProblem problem = (EDASLSProblem)state.evaluator.p_problem;

        List<Arc> seq = new LinkedList<>(curr.getTaskSequence());

        // pad the sequence by depot loop
        seq.add(state.getUcarpInstance().getDepotLoop());
        seq.add(0, state.getUcarpInstance().getDepotLoop());

        GiantTaskSequenceIndividual neighbour;

        for (int origPos = 1; origPos < seq.size()-3; origPos++) {
            Arc task1 = seq.get(origPos);
            Arc invTask1 = task1.getInverse();

            Arc origPre = seq.get(origPos-1); // original predecessor
            Arc origSuc = seq.get(origPos+1); // original successor

            // avoid swapping adjacent tasks, which is essentially the same as single insertion
            for (int targPos = origPos+2; targPos < seq.size()-1; targPos++) {
                Arc task2 = seq.get(targPos);
                Arc invTask2 = task2.getInverse();

                Arc targPre = seq.get(targPos-1); // target predecessor
                Arc targSuc = seq.get(targPos+1); // target successor

                double oldEhm = ehm.getValue(origPre, task1) +
                        ehm.getValue(task1, origSuc) +
                        ehm.getValue(targPre, task2) +
                        ehm.getValue(task2, targSuc);

                // pre-check the delta ehm of swapping task1 and task2
                double newEhm = ehm.getValue(origPre, task2) +
                        ehm.getValue(task2, origSuc) +
                        ehm.getValue(targPre, task1) +
                        ehm.getValue(task1, targSuc);

                if (newEhm <= oldEhm)
                    continue;

                // the new ehm is greater than the old ehm, apply the move
                neighbour = curr.clone();
                neighbour.getTaskSequence().set(origPos-1, task2);
                neighbour.getTaskSequence().set(targPos-1, task1);
                problem.evaluate(state, neighbour, 0, 0);

                if (neighbour.fitness.betterThan(curr.fitness))
                    return neighbour;

                // pre-check the delta ehm of swaping (task1, invTask2)
                newEhm = ehm.getValue(origPre, invTask2) +
                        ehm.getValue(invTask2, origSuc) +
                        ehm.getValue(targPre, task1) +
                        ehm.getValue(task1, targSuc);

                if (newEhm <= oldEhm)
                    continue;

                // the new ehm is greater than the old ehm, apply the move
                neighbour = curr.clone();
                neighbour.getTaskSequence().set(origPos-1, invTask2);
                neighbour.getTaskSequence().set(targPos-1, task1);
                problem.evaluate(state, neighbour, 0, 0);

                if (neighbour.fitness.betterThan(curr.fitness))
                    return neighbour;

                // pre-check the delta ehm of swaping (invTask1, task2)
                newEhm = ehm.getValue(origPre, task2) +
                        ehm.getValue(task2, origSuc) +
                        ehm.getValue(targPre, invTask1) +
                        ehm.getValue(invTask1, targSuc);

                if (newEhm <= oldEhm)
                    continue;

                // the new ehm is greater than the old ehm, apply the move
                neighbour = curr.clone();
                neighbour.getTaskSequence().set(origPos-1, task2);
                neighbour.getTaskSequence().set(targPos-1, invTask1);
                problem.evaluate(state, neighbour, 0, 0);

                if (neighbour.fitness.betterThan(curr.fitness))
                    return neighbour;

                // pre-check the delta ehm of swaping (invTask1, invTask2)
                newEhm = ehm.getValue(origPre, invTask2) +
                        ehm.getValue(invTask2, origSuc) +
                        ehm.getValue(targPre, invTask1) +
                        ehm.getValue(invTask1, targSuc);

                if (newEhm <= oldEhm)
                    continue;

                // the new ehm is greater than the old ehm, apply the move
                neighbour = curr.clone();
                neighbour.getTaskSequence().set(origPos-1, invTask2);
                neighbour.getTaskSequence().set(targPos-1, invTask1);
                problem.evaluate(state, neighbour, 0, 0);

                if (neighbour.fitness.betterThan(curr.fitness))
                    return neighbour;
            }
        }

        return curr;
    }
}
