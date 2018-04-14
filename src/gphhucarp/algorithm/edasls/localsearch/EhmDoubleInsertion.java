package gphhucarp.algorithm.edasls.localsearch;

import gphhucarp.algorithm.edasls.EDASLSEvolutionState;
import gphhucarp.algorithm.edasls.EDASLSProblem;
import gphhucarp.algorithm.edasls.EdgeHistogramMatrix;
import gphhucarp.algorithm.edasls.GiantTaskSequenceIndividual;
import gphhucarp.core.Arc;

import java.util.LinkedList;
import java.util.List;

/**
 * The double insertion operator defines a move as selecting two consecutive tasks,
 * and insert them into another position.
 * It pads the sequence by depot loops to address boundary issues.
 *
 */
public class EhmDoubleInsertion extends EhmLocalSearch {

    @Override
    public GiantTaskSequenceIndividual move(EDASLSEvolutionState state, GiantTaskSequenceIndividual curr) {
        EdgeHistogramMatrix ehm = state.getEhm();
        EDASLSProblem problem = (EDASLSProblem)state.evaluator.p_problem;

        List<Arc> seq = new LinkedList<>(curr.getTaskSequence());

        // pad the sequence by depot loop
        seq.add(state.getUcarpInstance().getDepotLoop());
        seq.add(0, state.getUcarpInstance().getDepotLoop());

        GiantTaskSequenceIndividual neighbour;

        for (int origPos = 1; origPos < seq.size()-2; origPos++) {
            Arc task1 = seq.get(origPos);
            Arc task2 = seq.get(origPos+1);
            Arc invTask1 = task1.getInverse();
            Arc invTask2 = task2.getInverse();

            Arc origPre = seq.get(origPos-1); // original predecessor
            Arc origSuc = seq.get(origPos+2); // original successor

            // insert into a position before the original position
            // the target predecessor and successor is different
            for (int targPos = 1; targPos < origPos; targPos++) {
                Arc targPre = seq.get(targPos-1); // target predecessor
                Arc targSuc = seq.get(targPos); // target successor

                // pre-check the delta ehm of inserting (task1, task2)
                double oldEhm = ehm.getValue(origPre, task1) +
                        ehm.getValue(task2, origSuc) +
                        ehm.getValue(targPre, targSuc);

                double newEhm = ehm.getValue(origPre, origSuc) +
                        ehm.getValue(targPre, task1) +
                        ehm.getValue(task2, targSuc);

                if (newEhm <= oldEhm)
                    continue;

                // the new ehm is greater than the old ehm, apply the move
                neighbour = curr.clone();
                neighbour.getTaskSequence().remove(origPos-1);
                neighbour.getTaskSequence().remove(origPos-1);
                neighbour.getTaskSequence().add(targPos-1, task2);
                neighbour.getTaskSequence().add(targPos-1, task1);
                problem.evaluate(state, neighbour, 0, 0);

                if (neighbour.fitness.betterThan(curr.fitness))
                    return neighbour;

                // pre-check the delta ehm of inserting (invTask2, invTask1)
                newEhm = ehm.getValue(origPre, origSuc) +
                        ehm.getValue(targPre, invTask2) +
                        ehm.getValue(invTask1, targSuc);

                if (newEhm <= oldEhm)
                    continue;

                // the new ehm is greater than the old ehm, apply the move
                neighbour = curr.clone();
                neighbour.getTaskSequence().remove(origPos-1);
                neighbour.getTaskSequence().remove(origPos-1);
                neighbour.getTaskSequence().add(targPos-1, invTask1);
                neighbour.getTaskSequence().add(targPos-1, invTask2);
                problem.evaluate(state, neighbour, 0, 0);

                if (neighbour.fitness.betterThan(curr.fitness))
                    return neighbour;
            }

            // insert into a position after the original position
            // the target predecessor and successor is different
            for (int targPos = origPos+1; targPos < seq.size()-2; targPos++) {
                Arc targPre = seq.get(targPos+1); // target predecessor
                Arc targSuc = seq.get(targPos+2); // target successor

                // pre-check the delta ehm of inserting (task1, task2)
                double oldEhm = ehm.getValue(origPre, task1) +
                        ehm.getValue(task2, origSuc) +
                        ehm.getValue(targPre, targSuc);

                double newEhm = ehm.getValue(origPre, origSuc) +
                        ehm.getValue(targPre, task1) +
                        ehm.getValue(task2, targSuc);

                if (newEhm <= oldEhm)
                    continue;

                // the new ehm is greater than the old ehm, apply the move
                neighbour = curr.clone();
                neighbour.getTaskSequence().remove(origPos-1);
                neighbour.getTaskSequence().remove(origPos-1);
                neighbour.getTaskSequence().add(targPos-1, task2);
                neighbour.getTaskSequence().add(targPos-1, task1);
                problem.evaluate(state, neighbour, 0, 0);

                if (neighbour.fitness.betterThan(curr.fitness))
                    return neighbour;

                // pre-check the delta ehm of inserting (invTask2, invTask1)
                newEhm = ehm.getValue(origPre, origSuc) +
                        ehm.getValue(targPre, invTask2) +
                        ehm.getValue(invTask1, targSuc);

                if (newEhm <= oldEhm)
                    continue;

                // the new ehm is greater than the old ehm, apply the move
                neighbour = curr.clone();
                neighbour.getTaskSequence().remove(origPos-1);
                neighbour.getTaskSequence().remove(origPos-1);
                neighbour.getTaskSequence().add(targPos-1, invTask1);
                neighbour.getTaskSequence().add(targPos-1, invTask2);
                problem.evaluate(state, neighbour, 0, 0);

                if (neighbour.fitness.betterThan(curr.fitness))
                    return neighbour;
            }
        }

        return curr;
    }
}
