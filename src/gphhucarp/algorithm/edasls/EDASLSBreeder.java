package gphhucarp.algorithm.edasls;

import ec.EvolutionState;
import ec.Individual;
import ec.Population;
import ec.Subpopulation;
import ec.simple.SimpleBreeder;
import ec.util.Parameter;
import gphhucarp.algorithm.edasls.localsearch.*;
import gphhucarp.core.Arc;
import org.apache.commons.math3.random.RandomDataGenerator;

import java.util.LinkedList;
import java.util.List;

/**
 * The breeder of EDASLS.
 * It randomly selects an individual from the population.
 * Then it generates a new individual by applying the EHBSA.
 * Then it applies the stochastic local search by some probability.
 */

public class EDASLSBreeder extends SimpleBreeder {

    // the local search probability
    public static final String P_LS_PROB = "ls-prob";

    private double lsProb;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);

        // read the local search probability
        Parameter p = base.push(P_LS_PROB);
        lsProb = state.parameters.getDoubleWithDefault(p, null, 0);
    }

    @Override
    public Population breedPopulation(EvolutionState state) {
        EDASLSEvolutionState edaslsState = (EDASLSEvolutionState)state;
        RandomDataGenerator rdg = edaslsState.getRdg();
        EDASLSProblem problem = (EDASLSProblem)state.evaluator.p_problem;

        Population newpop = state.population;
        Subpopulation subpop = newpop.subpops[0];

        // randomly select an individual as the template
        int idx = edaslsState.getRdg().nextInt(0, subpop.individuals.length-1);
        GiantTaskSequenceIndividual template =
                (GiantTaskSequenceIndividual)subpop.individuals[idx];

        // first do the edge histogram sampling
        GiantTaskSequenceIndividual child =
                edgeHistogramSampling(edaslsState, template);

        // do local search with some probability
        double r = rdg.nextUniform(0, 1);
        if (r < lsProb) {
            GiantTaskSequenceIndividual lsChild =
                    stochasticLocalSearch(edaslsState, child);

            if (lsChild.fitness.betterThan(template.fitness) &&
                    !isDuplicate(lsChild, subpop.individuals))
                // replace the template with the child
                subpop.individuals[idx] = lsChild;
        }
        else {
            // evaluate the child
            problem.evaluate(state, child, 0, 0);

            if (child.fitness.betterThan(template.fitness) &&
                    !isDuplicate(child, subpop.individuals))
                // replace the template with the child
                subpop.individuals[idx] = child;
        }

        return newpop;
    }

    /**
     * The sampling based on edge histogram
     * @param state the evolution state
     * @param template the template individual
     * @return the randomly sampled individual
     */
    public GiantTaskSequenceIndividual edgeHistogramSampling(EDASLSEvolutionState state,
                                                             GiantTaskSequenceIndividual template) {
        GiantTaskSequenceIndividual newIndi = template.clone();

        EdgeHistogramMatrix ehm = state.getEhm();
        RandomDataGenerator rdg = state.getRdg();
        List<Arc> taskSequence = template.getTaskSequence();
        // split into 2 (set in the paper) segments
        int[] splitIdx = new int[3];
        splitIdx[0] = 0;
        splitIdx[1] = rdg.nextInt(1, taskSequence.size()-1);
        splitIdx[2] = taskSequence.size();

        // randomly choose one segment
        int segStart = rdg.nextInt(0, 1);
        // the segment starts from splitIdx[segStart] and end at splitIdx[segStart+1]-1

        // regenerate the tasks in this segment
        List<Arc> remainingTasks = new LinkedList<>();
        for (int i = splitIdx[segStart]; i < splitIdx[segStart+1]; i++) {
            remainingTasks.add(taskSequence.get(i));
            remainingTasks.add(taskSequence.get(i).getInverse());
        }

        Arc curr = state.getUcarpInstance().getDepotLoop();
        if (segStart > 0)
            curr = taskSequence.get(splitIdx[segStart]-1);

        for (int i = splitIdx[segStart]; i < splitIdx[segStart+1]; i++) {
            // roulette wheel selection for the next task
            double totalHistogram = 0;

            for (Arc task : remainingTasks) {
                totalHistogram += ehm.getValue(curr, task);
            }

            double r = -1;

            if (totalHistogram > 0)
                r = rdg.nextUniform(0, totalHistogram);

            int idx = 0;
            while (idx < remainingTasks.size()) {
//                System.out.println(curr.toSimpleString() + " -> " + remainingTasks.get(idx).toSimpleString() + ": " + ehm.getValue(curr, remainingTasks.get(idx)));
                double histogram = ehm.getValue(curr, remainingTasks.get(idx));

                if (r < histogram)
                    break;

                r -= histogram;
                idx ++;
            }

            Arc next = remainingTasks.get(idx);

            // add next to the sequence of newIndi and set it as the current
            newIndi.getTaskSequence().set(i, next);
            curr = next;

            // remove next and its inverse from the remaining tasks
            remainingTasks.remove(next);
            remainingTasks.remove(next.getInverse());
        }

        return newIndi;
    }

    /**
     * The stochastic local search
     * @param state the evolution state
     * @param curr the current individual
     * @return the individual after local search
     */
    public GiantTaskSequenceIndividual stochasticLocalSearch(EDASLSEvolutionState state,
                                                             GiantTaskSequenceIndividual curr) {
        EDASLSLocalSearch si = new EDASLSSingleInsertion();
        EDASLSLocalSearch di = new EDASLSDoubleInsertion();
        EDASLSLocalSearch swap = new EDASLSSwap();
        EDASLSLocalSearch twoOpt = new EDASLSTwoOpt();

        while (true) {
            GiantTaskSequenceIndividual newIndi = curr;

            GiantTaskSequenceIndividual siNeighbour = si.move(state, curr);
            GiantTaskSequenceIndividual diNeighbour = di.move(state, curr);
            GiantTaskSequenceIndividual swapNeighbour = swap.move(state, curr);
            GiantTaskSequenceIndividual twoOptNeighbour = twoOpt.move(state, curr);

            if (siNeighbour.fitness.betterThan(newIndi.fitness))
                newIndi = siNeighbour;

            if (diNeighbour.fitness.betterThan(newIndi.fitness))
                newIndi = diNeighbour;

            if (swapNeighbour.fitness.betterThan(newIndi.fitness))
                newIndi = swapNeighbour;

            if (twoOptNeighbour.fitness.betterThan(newIndi.fitness))
                newIndi = twoOptNeighbour;

            // no improvement is found
            if (!newIndi.fitness.betterThan(curr.fitness))
                break;

            curr = newIndi;
        }

        return curr;
    }

    /**
     * Check whether an individual is a duplicate of any individuals from a set of individuals.
     * @param indi the checked individual.
     * @param individuals the individual pool.
     * @return true inf indi is a duplicate from individuals, and false otherwise.
     */
    boolean isDuplicate(GiantTaskSequenceIndividual indi,
                        Individual[] individuals) {
        for (Individual x : individuals) {
            if (indi.equals(x))
                return true;
        }

        return false;
    }
}
