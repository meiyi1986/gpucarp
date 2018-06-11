package gphhucarp.algorithm.edasls;

import ec.Evaluator;
import ec.EvolutionState;
import ec.Evolve;
import ec.Fitness;
import ec.multiobjective.MultiObjectiveFitness;
import ec.util.Parameter;
import ec.util.ParameterDatabase;
import gphhucarp.core.Instance;
import gphhucarp.core.InstanceSamples;
import gphhucarp.core.Objective;
import gphhucarp.decisionprocess.DecisionProcess;
import gphhucarp.decisionprocess.proreactive.ProreativeDecisionProcess;
import gphhucarp.decisionprocess.routingpolicy.FeasibilityPolicy;
import gphhucarp.gp.evaluation.EvaluationModel;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EDASLSTest {

    public static final String P_TRAIN_PATH = "train-path"; // path of the out.stat files
    public static final String P_NUM_TRAINS = "num-trains"; // number of trains (out.stat files)

    public static void main(String[] args) {
        ParameterDatabase parameters = Evolve.loadParameterDatabase(args);

        EvolutionState state = Evolve.initialize(parameters, 0);

        Parameter p;

        // setup the evaluator, essentially the test evaluation model
        p = new Parameter(EvolutionState.P_EVALUATOR);
        state.evaluator = (Evaluator)
                (parameters.getInstanceForParameter(p, null, Evaluator.class));
        state.evaluator.setup(state, p);

        // read the path of the training out.stat files.
        p = new Parameter(P_TRAIN_PATH);
        String trainPath = parameters.getStringWithDefault(p, null, "");
        // read the number of trains, i.e. the number of out.stat files
        p = new Parameter(P_NUM_TRAINS);
        int numTrains = parameters.getIntWithDefault(p, null, 1);

        // the fields for testing
        EDASLSProblem testProblem = (EDASLSProblem)state.evaluator.p_problem;
        EvaluationModel testEvaluationModel = testProblem.evaluationModel;

        // read the giant task sequences
        List<EDASLSResult> results = new ArrayList<>();

        // start testing the rules
        System.out.println("Test rules from path " + trainPath);

        for (int i = 0; i < numTrains; i++) {
            System.out.println("Testing run " + i);

            File sourceFile = new File(trainPath + "job." + i + ".out.stat");

            // read the individuals to a result class
            EDASLSResult result = EDASLSResult.readFromFile(sourceFile, state.evaluator.p_problem);

            // read the time from the .stat.csv file
            File timeFile = new File(trainPath + "job." + i + ".stat.csv");
            result.setTimeStat(EDASLSResult.readTimeFromFile(timeFile));

            // test the rules for each generation
            long start = System.currentTimeMillis();

            for (int j = 0; j < result.getSolutions().size(); j++) {
                GiantTaskSequenceIndividual chromosome = result.getSolutionAtGen(j);

                // evaluate by the test problem
                double[] fitnesses = new double[testEvaluationModel.getObjectives().size()];

                int numProcesses = 0;
                for (InstanceSamples iSamples : testEvaluationModel.getInstanceSamples()) {
                    chromosome.split(iSamples.getBaseInstance());

                    for (long seed : iSamples.getSeeds()) {
                        ProreativeDecisionProcess dp =
                                DecisionProcess.initProreactive(iSamples.getBaseInstance(), seed,
                                        new FeasibilityPolicy(), chromosome.getSolution());

                        numProcesses ++;

                        dp.run();

                        for (int f = 0; f < fitnesses.length; f++) {
                            Objective objective = testEvaluationModel.getObjectives().get(f);
                            double objValue = dp.getState().getSolution().objValue(objective);
                            fitnesses[f] += objValue;
                        }
                    }
                }

                for (int f = 0; f < fitnesses.length; f++) {
                    fitnesses[f] /= numProcesses;
                }

                MultiObjectiveFitness f = (MultiObjectiveFitness)result.getTestFitnessAtGen(j);
                f.setObjectives(state, fitnesses);

                System.out.println("Generation " + j + ": test fitness = " +
                        result.getTestFitnessAtGen(j).fitness());
            }

            long finish = System.currentTimeMillis();
            long duration = finish - start;
            System.out.println("Duration = " + duration + " ms.");

            results.add(result);
        }

        // write to csv file
        File writtenPath = new File(trainPath + "test");
        if (!writtenPath.exists()) {
            writtenPath.mkdirs();
        }

        String writtenFileName = testFileName(testEvaluationModel);
        File csvFile = new File(writtenPath + "/" + writtenFileName + ".csv");

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile.getAbsoluteFile()));
            // write the title
            writer.write(csvTitle());
            writer.newLine();
            for (int i = 0; i < numTrains; i++) {
                EDASLSResult result = results.get(i);

                // write the test results for each generation
                for (int j = 0; j < result.getSolutions().size(); j++) {
                    writer.write(i + "," + j + "," +
                            fitnessString(result, j) +
                            result.getTimeAtGen(j));
                    writer.newLine();
                }
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static String testFileName(EvaluationModel testEvaluationModel) {
        String str = "";
        for (Objective objective : testEvaluationModel.getObjectives())
            str += objective.getName() + "-";

        Instance instance = testEvaluationModel.getInstanceSamples().get(0).getBaseInstance();
        str += instance.getName() + "-" + instance.getNumVehicles() + "-"
                + instance.getDemandUncertaintyLevel() + "-"
                + instance.getCostUncertaintyLevel();

        return str;
    }

    private static String csvTitle() {
        String s = "Run,Generation,Obj,TrainFitness,TestFitness,Time";

        return s;
    }

    private static String fitnessString(EDASLSResult result, int gen) {
        String s = "";

        Fitness trainFit = result.getBestTrainFitness();
        Fitness testFit = result.getBestTestFitness();

        if (gen != -1) {
            trainFit = result.getTrainFitnessAtGen(gen);
            testFit = result.getTestFitnessAtGen(gen);
        }

        MultiObjectiveFitness simpleTrainFit = (MultiObjectiveFitness)trainFit;
        MultiObjectiveFitness simpleTestFit = (MultiObjectiveFitness)testFit;
        for (int k = 0; k < simpleTrainFit.objectives.length; k++) {
            s += k + "," + simpleTrainFit.getObjective(k) + "," +
                    simpleTestFit.getObjective(k) + ",";
        }

        return s;
    }
}
