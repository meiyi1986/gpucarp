package gphhucarp.algorithm.sopoc;

import ec.*;
import ec.gp.GPIndividual;
import ec.multiobjective.MultiObjectiveFitness;
import ec.util.Checkpoint;
import ec.util.Parameter;
import gphhucarp.algorithm.edasls.EdgeHistogramMatrix;
import gphhucarp.algorithm.edasls.GiantTaskSequenceIndividual;
import gphhucarp.algorithm.sopoc.localsearch.*;
import gphhucarp.core.Arc;
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
import java.util.*;

/**
 * The Solution-Policy Co-evolver evolution state.
 * It co-evolves two sub-populations.
 * Sub-population 0: the giant sequence evolved by EDASLS.
 * Sub-population 1: the policy to decide whether to continue service, evolved by GP.
 */

public class SoPoCEvolutionState extends GPHHEvolutionState {
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

    // the context vector
    // index 0: baseline solution; index 1: policy
    private Individual[] contextVector;
    private MultiObjectiveFitness contextFitness;

    protected Instance ucarpInstance;
    protected EdgeHistogramMatrix ehm;

    // parameters for EDASLS
    public int EDASLSGenFEs; // number of EDASLS evaluations per generation
    public int[] EDASLSFEs; // the actual EDASLS fitness evaluations in each generation

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
        EDASLSGenFEs = parameters.getIntWithDefault(p, null, 1024);

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

        EDASLSFEs = new int[numGenerations];
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

        // sort the individuals in the first subpopulation for EDASLS
        Arrays.sort(population.subpops[0].individuals);

        int result = R_NOTDONE;
        while ( result == R_NOTDONE ) {
            result = evolve();
        }

        finish(result);
    }

    @Override
    public int evolve() {
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

            return R_SUCCESS;
        }

        // BREEDING
        statistics.preBreedingStatistics(this);
        /**
         * Breed the first sub-population by EDASLS
         */
        while (EDASLSFEs[generation] < EDASLSGenFEs) {
            // select the first half as the promising individuals
            int numPromisingIndividuals = population.subpops[0].individuals.length / 2;
            GiantTaskSequenceIndividual[] promisingIndividuals =
                    new GiantTaskSequenceIndividual[numPromisingIndividuals];

            for (int i = 0; i < numPromisingIndividuals; i++)
                promisingIndividuals[i] =
                        (GiantTaskSequenceIndividual)population.subpops[0].individuals[i];

            // update the edge histogram matrix
            ehm.updateBy(promisingIndividuals, ucarpInstance);

            // EDASLS breeding
            SoPoCProblem problem = (SoPoCProblem)evaluator.p_problem;
            Subpopulation subpop = population.subpops[0];

            // randomly select an individual as the template
            int idx = rdg.nextInt(0, subpop.individuals.length-1);
            GiantTaskSequenceIndividual template =
                    (GiantTaskSequenceIndividual)subpop.individuals[idx];

            // first do the edge histogram sampling
            GiantTaskSequenceIndividual child =
                    edgeHistogramSampling(template);

            // do local search with some probability
            double r = rdg.nextUniform(0, 1);
            if (r < 0.1) {
                GiantTaskSequenceIndividual lsChild = stochasticLocalSearch(child);

                if (lsChild.fitness.betterThan(template.fitness) &&
                        !isDuplicate(lsChild, subpop.individuals))
                    // replace the template with the child
                    subpop.individuals[idx] = lsChild;
            }
            else {
                // evaluate the child
                Individual[] inds = new Individual[population.subpops.length];
                boolean[] updates = new boolean[population.subpops.length];

                // initialise inds as the context vector
                for(int i = 0; i < population.subpops.length; i++) {
                    inds[i] = contextVector[i];
                    updates[i] = false;
                }

                // evaluate subpop 0: the baseline solution
                inds[0] = child;
                updates[0] = true;

                problem.evaluate(this, inds, updates, false, new int[population.subpops.length], 0);
                EDASLSFEs[generation] ++;

                if (child.fitness.betterThan(template.fitness) &&
                        !isDuplicate(child, subpop.individuals))
                    // replace the template with the child
                    subpop.individuals[idx] = child;
            }

            // sort the population according to fitness
            // sorting by fitness is a natural comparator for individuals
            Arrays.sort(population.subpops[0].individuals);
        }

        /**
         * Evolve the second subpopulation (policy) by SoPoCBreeder
         */
        population = breeder.breedPopulation(this);

        // POST-BREEDING EXCHANGING
        statistics.postBreedingStatistics(this);

        // POST-BREEDING EXCHANGING
        statistics.prePostBreedingExchangeStatistics(this);
        population = exchanger.postBreedingExchangePopulation(this);
        statistics.postPostBreedingExchangeStatistics(this);

        output.message("Generation " + generation + " elapsed " + duration + " seconds.");

        writeToStatFile();

        finish = util.Timer.getCpuTime();
        duration = 1.0 * (finish - start) / 1000000000;

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
            SoPoCProblem problem = (SoPoCProblem)evaluator.p_problem;
            problem.rotateEvaluationModel();

            // re-evaluate the population after the evaluation model rotation
            evaluator.evaluatePopulation(this);
        }

        return R_NOTDONE;
    }

    /**
     * The sampling based on edge histogram
     * @param template the template individual
     * @return the randomly sampled individual
     */
    public GiantTaskSequenceIndividual edgeHistogramSampling(GiantTaskSequenceIndividual template) {
        GiantTaskSequenceIndividual newIndi = template.clone();

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

        Arc curr = ucarpInstance.getDepotLoop();
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
     * @param curr the current individual
     * @return the individual after local search
     */
    public GiantTaskSequenceIndividual stochasticLocalSearch(GiantTaskSequenceIndividual curr) {
        SoPoCLocalSearch si = new SoPoCSingleInsertion();
        SoPoCLocalSearch di = new SoPoCDoubleInsertion();
        SoPoCLocalSearch swap = new SoPoCSwap();
        SoPoCLocalSearch twoOpt = new SoPoCTwoOpt();

        while (true) {
            GiantTaskSequenceIndividual newIndi = curr;

            GiantTaskSequenceIndividual siNeighbour = si.move(this, curr);
            GiantTaskSequenceIndividual diNeighbour = di.move(this, curr);
            GiantTaskSequenceIndividual swapNeighbour = swap.move(this, curr);
            GiantTaskSequenceIndividual twoOptNeighbour = twoOpt.move(this, curr);

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
