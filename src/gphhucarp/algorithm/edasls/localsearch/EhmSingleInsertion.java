package gphhucarp.algorithm.edasls.localsearch;

import gphhucarp.algorithm.edasls.EDASLSEvolutionState;
import gphhucarp.algorithm.edasls.EDASLSProblem;
import gphhucarp.algorithm.edasls.EdgeHistogramMatrix;
import gphhucarp.algorithm.edasls.GiantTaskSequenceIndividual;
import gphhucarp.core.Arc;

import java.util.LinkedList;
import java.util.List;

/**
 * The single insertion operator defines a move as selecting a task, and insert it
 * into another position.
 * It pads the sequence by depot loops to address boundary issues.
 *
 */
public class EhmSingleInsertion extends EhmLocalSearch {

    @Override
    public GiantTaskSequenceIndividual move(EDASLSEvolutionState state, GiantTaskSequenceIndividual curr) {
        EdgeHistogramMatrix ehm = state.getEhm();
        EDASLSProblem problem = (EDASLSProblem)state.evaluator.p_problem;

        List<Arc> seq = new LinkedList<>(curr.getTaskSequence());

        // pad the sequence by depot loop
        seq.add(state.getUcarpInstance().getDepotLoop());
        seq.add(0, state.getUcarpInstance().getDepotLoop());

        GiantTaskSequenceIndividual neighbour;

        for (int origPos = 1; origPos < seq.size()-1; origPos++) {
            Arc task = seq.get(origPos);
            Arc invTask = task.getInverse();

            Arc origPre = seq.get(origPos-1); // original predecessor
            Arc origSuc = seq.get(origPos+1); // original successor

            // insert into a position before the original position
            // the target predecessor and successor is different
            for (int targPos = 1; targPos < origPos; targPos++) {
                Arc targPre = seq.get(targPos-1); // target predecessor
                Arc targSuc = seq.get(targPos); // target successor

                // pre-check the delta ehm of inserting task
                double oldEhm = ehm.getValue(origPre, task) +
                        ehm.getValue(task, origSuc) +
                        ehm.getValue(targPre, targSuc);

                double newEhm = ehm.getValue(origPre, origSuc) +
                        ehm.getValue(targPre, task) +
                        ehm.getValue(task, targSuc);

                if (newEhm <= oldEhm)
                    continue;

                // the new ehm is greater than the old ehm, apply the move
                neighbour = curr.clone();
                neighbour.getTaskSequence().remove(origPos-1);
                neighbour.getTaskSequence().add(targPos-1, task);
                problem.evaluate(state, neighbour, 0, 0);

                if (neighbour.fitness.betterThan(curr.fitness))
                    return neighbour;

                // pre-check the delta ehm of inserting the inversed task
                newEhm = ehm.getValue(origPre, origSuc) +
                        ehm.getValue(targPre, invTask) +
                        ehm.getValue(invTask, targSuc);

                if (newEhm <= oldEhm)
                    continue;

                // the new ehm is greater than the old ehm, apply the move
                neighbour = curr.clone();
                neighbour.getTaskSequence().remove(origPos-1);
                neighbour.getTaskSequence().add(targPos-1, invTask);
                problem.evaluate(state, neighbour, 0, 0);

                if (neighbour.fitness.betterThan(curr.fitness))
                    return neighbour;
            }

            // insert into a position after the original position
            // the target predecessor and successor is different
            for (int targPos = origPos+1; targPos < seq.size()-1; targPos++) {
                Arc targPre = seq.get(targPos); // target predecessor
                Arc targSuc = seq.get(targPos+1); // target successor

                // pre-check the delta ehm of inserting task
                double oldEhm = ehm.getValue(origPre, task) +
                        ehm.getValue(task, origSuc) +
                        ehm.getValue(targPre, targSuc);

                double newEhm = ehm.getValue(origPre, origSuc) +
                        ehm.getValue(targPre, task) +
                        ehm.getValue(task, targSuc);

                if (newEhm <= oldEhm)
                    continue;

                // the new ehm is greater than the old ehm, apply the move
                neighbour = curr.clone();
                neighbour.getTaskSequence().remove(origPos-1);
                neighbour.getTaskSequence().add(targPos-1, task);
                problem.evaluate(state, neighbour, 0, 0);

                if (neighbour.fitness.betterThan(curr.fitness))
                    return neighbour;

                // pre-check the delta ehm of inserting the inversed task
                newEhm = ehm.getValue(origPre, origSuc) +
                        ehm.getValue(targPre, invTask) +
                        ehm.getValue(invTask, targSuc);

                if (newEhm <= oldEhm)
                    continue;

                // the new ehm is greater than the old ehm, apply the move
                neighbour = curr.clone();
                neighbour.getTaskSequence().remove(origPos-1);
                neighbour.getTaskSequence().add(targPos-1, invTask);
                problem.evaluate(state, neighbour, 0, 0);

                if (neighbour.fitness.betterThan(curr.fitness))
                    return neighbour;
            }
        }

        return curr;
    }
}
