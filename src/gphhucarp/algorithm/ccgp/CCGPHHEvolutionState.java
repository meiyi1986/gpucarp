package gphhucarp.algorithm.ccgp;

import ec.EvolutionState;
import ec.Individual;
import ec.util.Checkpoint;
import ec.util.Parameter;
import gphhucarp.gp.GPHHEvolutionState;
import gphhucarp.gp.ReactiveGPHHProblem;

/**
 * The Cooperative Co-evolution GPHH (CCGPHH) evolution state.
 * CCGPHH coevolves a set of heuristics as an ensemble, using a set of subpopulations.
 * Each subpopulation evolves a heuristic.
 *
 * The final result is an ensemble (group) of heuristics.
 *
 * The ensemble makes decisions by the Combiner class provided in the CCGPHHProblem.
 *
 * The Combiner can be Aggregator (summing up the values of all the heuristics), MajorityVoter, etc.
 *
 * The context vector is used for evaluation.
 * At first, the context vector is randomly initialised.
 * Then, for evaluating each subpopulation, the individual replaces the corresponding index
 * in the context vector, and the modified context vector is evaluated. The fitness is
 * then given to the evaluated individual.
 *
 */

public class CCGPHHEvolutionState extends GPHHEvolutionState {

    private Individual[] contextVector;

    public Individual[] getContextVector() {
        return contextVector;
    }

    public void setContextVector(Individual[] contextVector) {
        this.contextVector = contextVector;
    }

    public Individual getContext(int index) {
        return contextVector[index];
    }

    public void setContext(int index, Individual individual) {
        contextVector[index] = individual;
    }

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);

        contextVector = new Individual[subpops];
    }

    @Override
    public void startFresh() {
        super.startFresh();

        // initially set the context vector arbitrarily
        // here set to the first individual of each subpopulation
        for (int i = 0; i < population.subpops.length; i++)
            contextVector[i] = population.subpops[i].individuals[0];
    }

    @Override
    public int evolve() {
        long start = util.Timer.getCpuTime();

        if (generation > 0)
            output.message("Generation " + generation);

        // evaluate the context vector
        CCGPHHProblem prob = (CCGPHHProblem)evaluator.p_problem;
        prob.evaluateContextVector(this, contextVector);

        // EVALUATION
        statistics.preEvaluationStatistics(this);
        evaluator.evaluatePopulation(this);
        statistics.postEvaluationStatistics(this);

        writeToStatFile();

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
