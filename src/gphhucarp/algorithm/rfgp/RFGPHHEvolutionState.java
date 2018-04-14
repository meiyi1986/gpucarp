package gphhucarp.algorithm.rfgp;

import ec.EvolutionState;
import ec.Individual;
import ec.gp.GPNode;
import ec.util.Checkpoint;
import ec.util.Parameter;
import gphhucarp.gp.GPHHEvolutionState;
import gphhucarp.gp.UCARPPrimitiveSet;
import gphhucarp.gp.ReactiveGPHHProblem;
import gputils.TerminalERCEvolutionState;
import gputils.terminal.PrimitiveSet;

import java.util.*;

/**
 * The Random Forest GP (RFGP) evolution state class.
 * In the forest, each element is a GP tree.
 * The GP process evolves each element one by one, just like random forest training.
 * For evolving each element, a subset of terminal set is randomly sampled from the original terminal set.
 * This can potentially increase the diversity of the elements in the forest.
 * The size of the subset depends on a parameter 0 < sampleRatio <= 1.
 * If sampleRatio = 1, then the original terminal set is taken.
 *
 */

public class RFGPHHEvolutionState extends GPHHEvolutionState {

    public static final String P_FOREST_SIZE = "forest-size";

    // back up the original terminal sets for future sampling
    protected List<PrimitiveSet> originalTerminalSets;

    protected int forestSize;

    protected List<Individual> ensemble;
    protected int numGenerationsPerElement;

    protected Random rdg; // used for sampling the terminal sets

    /**
     * Randomly sample a subset of terminals for each subpopulation.
     */
    public void sampleTerminalSets() {
        // this makes sure each terminal has 99% probability to appear in at least one element.
        double sampleRatio = 1;// - Math.pow(0.01, 1.0 / subpops);

        for (int i = 0; i < subpops; i++) {
            PrimitiveSet originalTerminalSet = originalTerminalSets.get(i);

            int numTerminals = originalTerminalSet.size();

            int numSampledTerminals = (int) (sampleRatio * numTerminals);

            List<GPNode> tmpTerminals = new LinkedList<>(originalTerminalSet.getList());
            Collections.shuffle(tmpTerminals, rdg);
            List<GPNode> sampledTerminals = tmpTerminals.subList(0, numSampledTerminals);

            terminalSets.set(i, new PrimitiveSet(sampledTerminals));
        }
    }



    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);

        Parameter p = new Parameter(P_FOREST_SIZE);
        forestSize = state.parameters.getIntWithDefault(p, null, 1);

        ensemble = new ArrayList<>();

        numGenerationsPerElement = numGenerations / forestSize;

        rdg = new Random(jobSeed);

        // back up the original terminal sets
        originalTerminalSets = new ArrayList<>();
        for (PrimitiveSet terminalSet : ((TerminalERCEvolutionState)state).getTerminalSets()) {
            List<GPNode> originalTerminalSet = new LinkedList<>(terminalSet.getList());
            originalTerminalSets.add(new PrimitiveSet(originalTerminalSet));
        }

        sampleTerminalSets();
    }

    @Override
    public int evolve() {
        long start = util.Timer.getCpuTime();

        if (generation > 0)
            output.message("Generation " + generation);

        // EVALUATION
        statistics.preEvaluationStatistics(this);
        evaluator.evaluatePopulation(this);
        statistics.postEvaluationStatistics(this);

        writeToStatFile();

        // each element is given numGenerationsPerElement generations to evolve.
        if (generation > 0 && generation % numGenerationsPerElement == 0) {
            // finish evolving the current element
            // add the current best individual to the ensemble
            ensemble.add(bestIndi(0));

            // re-sample the terminal sets
            sampleTerminalSets();

//            population.subpops[0].individuals[0].printIndividualForHumans(this, 0);

            // re-initialise the individuals
            population = initializer.initialPopulation(this, 0);

//            population.subpops[0].individuals[0].printIndividualForHumans(this, 0);
        }

        // SHOULD WE QUIT?
        if (evaluator.runComplete(this) && quitOnRunComplete) {
            output.message("Found Ideal Individual");
            return R_SUCCESS;
        }

        // SHOULD WE QUIT?
        if (generation == numGenerations-1) {
            return R_FAILURE;
        }

        // PRE-BREEDING EXCHANGING
        statistics.prePreBreedingExchangeStatistics(this);
        population = exchanger.preBreedingExchangePopulation(this);
        statistics.postPreBreedingExchangeStatistics(this);

        String exchangerWantsToShutdown = exchanger.runComplete(this);
        if (exchangerWantsToShutdown!=null)
        {
            output.message(exchangerWantsToShutdown);
	        /*
	         * Don't really know what to return here.  The only place I could
	         * find where runComplete ever returns non-null is
	         * IslandExchange.  However, that can return non-null whether or
	         * not the ideal individual was found (for example, if there was
	         * a communication error with the server).
	         *
	         * Since the original version of this code didn't care, and the
	         * result was initialized to R_SUCCESS before the while loop, I'm
	         * just going to return R_SUCCESS here.
	         */

            return R_SUCCESS;
        }

        // BREEDING
        statistics.preBreedingStatistics(this);

        population = breeder.breedPopulation(this);

        // POST-BREEDING EXCHANGING
        statistics.postBreedingStatistics(this);

        // POST-BREEDING EXCHANGING
        statistics.prePostBreedingExchangeStatistics(this);
        population = exchanger.postBreedingExchangePopulation(this);
        statistics.postPostBreedingExchangeStatistics(this);

        // Generate new instances if needed
        if (rotateEvalModel) {
            ReactiveGPHHProblem problem = (ReactiveGPHHProblem)evaluator.p_problem;
            problem.rotateEvaluationModel();
        }



        long finish = util.Timer.getCpuTime();
        duration = (finish - start) / 1000000000;

        output.message("Generation " + generation + " elapsed " + duration + " seconds.");

        // INCREMENT GENERATION AND CHECKPOINT
        generation++;
        if (checkpoint && generation%checkpointModulo == 0)
        {
            output.message("Checkpointing");
            statistics.preCheckpointStatistics(this);
            Checkpoint.setCheckpoint(this);
            statistics.postCheckpointStatistics(this);
        }

        return R_NOTDONE;
    }

}
