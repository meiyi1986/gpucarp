package gphhucarp.algorithm.sopoc;

import ec.BreedingPipeline;
import ec.EvolutionState;
import ec.Population;
import ec.simple.SimpleBreeder;

/**
 * The breeder of SoPoC.
 * It only breeds the subpop 1.
 * Subpop 0 (baseline solution) is already bred in the SoPoCEvolutionState
 */

public class SoPoCBreeder extends SimpleBreeder {
    /** A simple breeder that doesn't attempt to do any cross-
     population breeding.  Basically it applies pipelines,
     one per thread, to various subchunks of a new population. */
    public Population breedPopulation(EvolutionState state) {
        Population newpop = null;
        if (clonePipelineAndPopulation)
            newpop = (Population) state.population.emptyClone();
        else
        {
            if (backupPopulation == null)
                backupPopulation = (Population) state.population.emptyClone();
            newpop = backupPopulation;
            newpop.clear();
            backupPopulation = state.population;  // swap in
        }

        // keep subpop0
        newpop.subpops[0] = state.population.subpops[0];

        // maybe resize?
        for(int i = 1; i < state.population.subpops.length; i++)
        {
            if (reduceBy[i] > 0)
            {
                int prospectiveSize = Math.max(
                        Math.max(state.population.subpops[i].individuals.length - reduceBy[i], minimumSize[i]),
                        numElites(state, i));
                if (prospectiveSize < state.population.subpops[i].individuals.length)  // let's resize!
                {
                    state.output.message("Subpop " + i + " reduced " + state.population.subpops[i].individuals.length + " -> " + prospectiveSize);
                    newpop.subpops[i].resize(prospectiveSize);
                }
            }
        }

        // load elites into top of newpop
        loadElites(state, newpop);


        // how many threads do we really need?  No more than the maximum number of individuals in any subpopulation
        int numThreads = 0;
        for(int x = 1; x < state.population.subpops.length; x++)
            numThreads = Math.max(numThreads, state.population.subpops[x].individuals.length);
        numThreads = Math.min(numThreads, state.breedthreads);
        if (numThreads < state.breedthreads)
            state.output.warnOnce("Largest subpopulation size (" + numThreads +") is smaller than number of breedthreads (" + state.breedthreads + "), so fewer breedthreads will be created.");

        int numinds[][] =
                new int[numThreads][state.population.subpops.length];
        int from[][] =
                new int[numThreads][state.population.subpops.length];

        for(int x=1;x<state.population.subpops.length;x++)
        {
            int length = computeSubpopulationLength(state, newpop, x, 0);

            // we will have some extra individuals.  We distribute these among the early subpopulations
            int individualsPerThread = length / numThreads;  // integer division
            int slop = length - numThreads * individualsPerThread;
            int currentFrom = 0;

            for(int y=0;y<numThreads;y++)
            {
                if (slop > 0)
                {
                    numinds[y][x] = individualsPerThread + 1;
                    slop--;
                }
                else
                    numinds[y][x] = individualsPerThread;

                if (numinds[y][x] == 0)
                {
                    state.output.warnOnce("More threads exist than can be used to breed some subpopulations (first example: subpopulation " + x + ")");
                }

                from[y][x] = currentFrom;
                currentFrom += numinds[y][x];
            }
        }

        breedPopChunk(newpop,state,numinds[0],from[0],0);


        return newpop;
    }

    protected void breedPopChunk(Population newpop, EvolutionState state, int[] numinds, int[] from, int threadnum)
    {
        // only for subpop 1
        for(int subpop=1;subpop<newpop.subpops.length;subpop++)
        {
            // if it's subpop's turn and we're doing sequential breeding...
            if (!shouldBreedSubpop(state, subpop, threadnum))
            {
                // instead of breeding, we should just copy forward this subpopulation.  We'll copy the part we're assigned
                for(int ind=from[subpop] ; ind < numinds[subpop] - from[subpop]; ind++)
                    // newpop.subpops[subpop].individuals[ind] = (Individual)(state.population.subpops[subpop].individuals[ind].clone());
                    // this could get dangerous
                    newpop.subpops[subpop].individuals[ind] = state.population.subpops[subpop].individuals[ind];
            }
            else
            {
                // do regular breeding of this subpopulation
                BreedingPipeline bp = null;
                if (clonePipelineAndPopulation)
                    bp = (BreedingPipeline)newpop.subpops[subpop].species.pipe_prototype.clone();
                else
                    bp = (BreedingPipeline)newpop.subpops[subpop].species.pipe_prototype;

                // check to make sure that the breeding pipeline produces
                // the right kind of individuals.  Don't want a mistake there! :-)
                int x;
                if (!bp.produces(state,newpop,subpop,threadnum))
                    state.output.fatal("The Breeding Pipeline of subpopulation " + subpop + " does not produce individuals of the expected species " + newpop.subpops[subpop].species.getClass().getName() + " or fitness " + newpop.subpops[subpop].species.f_prototype );
                bp.prepareToProduce(state,subpop,threadnum);

                // start breedin'!

                x=from[subpop];
                int upperbound = from[subpop]+numinds[subpop];
                while(x<upperbound)
                    x += bp.produce(1,upperbound-x,x,subpop,
                            newpop.subpops[subpop].individuals,
                            state,threadnum);
                if (x>upperbound) // uh oh!  Someone blew it!
                    state.output.fatal("Whoa!  A breeding pipeline overwrote the space of another pipeline in subpopulation " + subpop + ".  You need to check your breeding pipeline code (in produce() ).");

                bp.finishProducing(state,subpop,threadnum);
            }
        }
    }
}
