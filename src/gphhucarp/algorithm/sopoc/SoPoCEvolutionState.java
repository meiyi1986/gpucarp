package gphhucarp.algorithm.sopoc;

import ec.*;
import ec.gp.GPIndividual;
import ec.gp.GPSpecies;
import ec.multiobjective.MultiObjectiveFitness;
import ec.simple.SimpleEvolutionState;
import ec.util.Checkpoint;
import ec.util.Parameter;
import gphhucarp.algorithm.edasls.EDASLSProblem;
import gphhucarp.algorithm.edasls.EdgeHistogramMatrix;
import gphhucarp.algorithm.edasls.GiantTaskSequenceIndividual;
import gphhucarp.core.Instance;
import gphhucarp.gp.GPHHEvolutionState;
import gphhucarp.gp.UCARPPrimitiveSet;
import gputils.LispUtils;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * The Solution-Policy Co-evolver evolution state.
 * It co-evolves two sub-populations.
 * Sub-population 0: the giant sequence evolved by EDASLS.
 * Sub-population 1: the policy to decide whether to continue service, evolved by GP.
 */

public class SoPoCEvolutionState extends GPHHEvolutionState {
    // the context vector
    // index 0: baseline solution; index 1: policy
    private Individual[] contextVector;
    private MultiObjectiveFitness contextFitness;

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

    public MultiObjectiveFitness getContextFitness() {
        return contextFitness;
    }

    public void setContextFitness(MultiObjectiveFitness contextFitness) {
        this.contextFitness = contextFitness;
    }

    /**
     * Statistics
     */
    public static final String POP_FITNESS = "pop-fitness";

    /**
     * Parameters for EDASLS
     */
    public static final String P_EDASLS_GEN_FES = "edasls-gen-fes";

    /**
     * Parameters for GP
     */
    /**
     * Statistics to store.
     */
    public static final String POP_PROG_SIZE = "pop-prog-size";

    /**
     * Read the file to specify the terminals.
     */
    public static final String P_TERMINALS_FROM = "terminals-from";
    public static final String P_INCLUDE_ERC = "include-erc";

    /**
     * Whether to rotate the evaluation model or not.
     */
    public static final String P_ROTATE_EVAL_MODEL = "rotate-eval-model";

    protected Instance ucarpInstance;
    protected EdgeHistogramMatrix ehm;
    protected GiantTaskSequenceIndividual bestIndi;

    // parameters for EDASLS
    protected int numFEsPerGen; // maximal number of generations per generation
    protected int[] genFEs; // the fitness evaluations in each generation

    // parameters for GP
    protected String terminalFrom; // where the terminals are from
    protected boolean includeErc; // whether to include ERC

    // general parameters
    protected boolean rotateEvalModel; // whether to rotate the evaluation model or not

    protected long jobSeed;
    protected RandomDataGenerator rdg; // used in the initialisation and edge historial matrix update

    protected Map<String, DescriptiveStatistics> statisticsMap;
    protected File statFile;
    protected long start, finish;
    protected double duration;

    public Instance getUcarpInstance() {
        return ucarpInstance;
    }

    public EdgeHistogramMatrix getEhm() {
        return ehm;
    }

    public long getJobSeed() {
        return jobSeed;
    }

    public RandomDataGenerator getRdg() {
        return rdg;
    }

    public void initStatFile() {
        statFile = new File("job." + jobSeed + ".stat.csv");
        if (statFile.exists()) {
            statFile.delete();
        }

        writeStatFileTitle();
    }

    public void writeStatFileTitle() {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(statFile));
            writer.write("Gen,Time,FitMean,FitStd");
            writer.newLine();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeToStatFile() {
        calcStatistics();

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(statFile, true));
            writer.write(generation + "," + duration +
                    "," + statisticsMap.get(POP_FITNESS).getMean() +
                    "," + statisticsMap.get(POP_FITNESS).getStandardDeviation()
            );
            writer.newLine();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setupStatistics() {
        statisticsMap = new HashMap<>();
        statisticsMap.put(POP_FITNESS, new DescriptiveStatistics());
    }

    public void calcStatistics() {
        statisticsMap.get(POP_FITNESS).clear();

        for (Individual indi : population.subpops[0].individuals) {
            double fitness = indi.fitness.fitness();
            statisticsMap.get(POP_FITNESS).addValue(fitness);
        }
    }

    // the best individual in subpopulation
    public Individual bestIndi(int subpop) {
        int best = 0;
        for(int x = 1; x < population.subpops[subpop].individuals.length; x++)
            if (population.subpops[subpop].individuals[x].fitness.betterThan(population.subpops[subpop].individuals[best].fitness))
                best = x;

        return population.subpops[subpop].individuals[best];
    }

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(this, base);

        Parameter p;

        // get the job seed
        p = new Parameter("seed").push(""+0);
        jobSeed = parameters.getLongWithDefault(p, null, 0);

        /**
         * Read parameters of EDASLS
         */
        p = new Parameter(P_EDASLS_GEN_FES);
        numFEsPerGen = parameters.getIntWithDefault(p, null, 1024);

        /**
         * Read parameters of GP
         */
        // get the job seed
        p = new Parameter("seed").push(""+0);
        jobSeed = parameters.getLongWithDefault(p, null, 0);

        // get the source of the terminal sets
        p = new Parameter(P_TERMINALS_FROM);
        terminalFrom = parameters.getStringWithDefault(p, null, "basic");

        // get whether to include the double ERC in the terminal sets or not
        p = new Parameter(P_INCLUDE_ERC);
        includeErc = parameters.getBoolean(p, null, false);

        // get whether to rotate the evaluation model per generation or not
        p = new Parameter(P_ROTATE_EVAL_MODEL);
        rotateEvalModel = parameters.getBoolean(p, null, false);

        // get the number of subpopulations
        p = new Parameter(Initializer.P_POP).push(Population.P_SIZE);
        subpops = parameters.getInt(p,null,1);

        initTerminalSets();

        // get whether to rotate the evaluation model per generation or not
        p = new Parameter(P_ROTATE_EVAL_MODEL);
        rotateEvalModel = parameters.getBoolean(p, null, false);

        rdg = new RandomDataGenerator();
        rdg.reSeed(jobSeed);

        // setup the UCARP instance and the edge histogram matrix
        SoPoCProblem problem = (SoPoCProblem)evaluator.p_problem;
        ucarpInstance = problem.evaluationModel.getInstanceSamples().get(0).getBaseInstance();
        ehm = new EdgeHistogramMatrix(ucarpInstance);

        genFEs = new int[numGenerations];

        // initialise the context vector: the policy is initialised as RQ-DEM
        contextVector = new Individual[2];
        GPIndividual policy = (GPIndividual)(population.subpops[1].species.newIndividual(this, 0));
        policy.trees[0] = LispUtils.parseExpression("(- RQ DEM)", UCARPPrimitiveSet.wholePrimitiveSet());
        contextVector[1] = policy;

        // initialise the context fitness to be infinity (to be minimised)
        contextFitness = new MultiObjectiveFitness();
        contextFitness.objectives = new double[problem.getObjectives().size()];
        for (int i = 0; i < contextFitness.objectives.length; i++)
            contextFitness.objectives[i] = Double.POSITIVE_INFINITY;
    }

    @Override
    public void run(int condition) {
        if (condition == C_STARTED_FRESH) {
            startFresh();
        }
        else {
            startFromCheckpoint();
        }

        initStatFile();
        setupStatistics();

        start = util.Timer.getCpuTime();

        // evaluate the initial population
        // this is a steady-state algorithm without rotating the instances
        // so no need to re-evaluate the entire population every generation
        evaluator.evaluatePopulation(this);

        // sort the population according to fitness
        // sorting by fitness is a natural comparator for individuals
        Arrays.sort(population.subpops[0].individuals);

        // update the best individual
        bestIndi = (GiantTaskSequenceIndividual)population.subpops[0].individuals[0];

        int result = R_NOTDONE;
        while ( result == R_NOTDONE ) {
            result = evolve();
        }

        finish(result);
    }

    @Override
    public int evolve() {
//        for (Pair<Arc, Arc> key : ehm.getMatrix().keySet()) {
//            if (ehm.getMatrix().get(key) > 10) {
//                System.out.println(key.getLeft().toSimpleString() + " -> " + key.getRight().toSimpleString() + ": " + ehm.getMatrix().get(key));
//            }
//        }

        if (generation > 0)
            output.message("Generation " + generation);

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

        while (genFEs[generation] < numFEsPerGen) {
            // select the first half as the promising individuals
            int numPromisingIndividuals = population.subpops[0].individuals.length / 2;
            GiantTaskSequenceIndividual[] promisingIndividuals =
                    new GiantTaskSequenceIndividual[numPromisingIndividuals];

            for (int i = 0; i < numPromisingIndividuals; i++)
                promisingIndividuals[i] =
                        (GiantTaskSequenceIndividual)population.subpops[0].individuals[i];

            // update the edge histogram matrix
            ehm.updateBy(promisingIndividuals, ucarpInstance);

            // BREEDING
            statistics.preBreedingStatistics(this);

            population = breeder.breedPopulation(this);

            // POST-BREEDING EXCHANGING
            statistics.postBreedingStatistics(this);

            // POST-BREEDING EXCHANGING
            statistics.prePostBreedingExchangeStatistics(this);
            population = exchanger.postBreedingExchangePopulation(this);
            statistics.postPostBreedingExchangeStatistics(this);

            // sort the population according to fitness
            // sorting by fitness is a natural comparator for individuals
            Arrays.sort(population.subpops[0].individuals);

            // update the best individual
            bestIndi = (GiantTaskSequenceIndividual)population.subpops[0].individuals[0];
        }

        statistics.preEvaluationStatistics(this);
        statistics.postEvaluationStatistics(this);

        finish = util.Timer.getCpuTime();
        duration = (finish - start) / 1000000000;

        output.message("Generation " + generation + " elapsed " + duration + " seconds.");

        writeToStatFile();

        start = util.Timer.getCpuTime();

        // INCREMENT GENERATION AND CHECKPOINT
        generation++;
        if (checkpoint && generation%checkpointModulo == 0)
        {
            output.message("Checkpointing");
            statistics.preCheckpointStatistics(this);
            Checkpoint.setCheckpoint(this);
            statistics.postCheckpointStatistics(this);
        }

        // Generate new instances if needed
        if (rotateEvalModel) {
            EDASLSProblem problem = (EDASLSProblem)evaluator.p_problem;
            problem.rotateEvaluationModel();

            // re-evaluate the population after the evaluation model rotation
            evaluator.evaluatePopulation(this);
        }

        return R_NOTDONE;
    }
}
