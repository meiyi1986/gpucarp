package gphhucarp.gp;

import ec.Evaluator;
import ec.EvolutionState;
import ec.Evolve;
import ec.gp.GPNode;
import ec.util.Parameter;
import ec.util.ParameterDatabase;
import gphhucarp.gp.io.FitnessType;
import gphhucarp.gp.io.GPResult;
import gphhucarp.gp.io.SolutionType;
import gphhucarp.gp.terminal.FeatureGPNode;
import gputils.LispUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GPAnalysis {
    public static final String P_ANALYSIS = "analysis"; // the analysis to do
    public static final String P_TRAIN_PATH = "train-path"; // path of the out.stat files of the training
    public static final String P_SOLUTION_TYPE = "solution-type"; // solution type, e.g. a single routing policy
    public static final String P_FITNESS_TYPE = "fitness-type"; // fitness type, e.g. multiobjective fitness
    public static final String P_NUM_TRAINS = "num-trains"; // number of trains (out.stat files)

    public String analysis;
    public String trainPath;
    public SolutionType solutionType;
    public FitnessType fitnessType;
    public int numTrains;

    Map<String, int[]> terminalFrequencyMap = new HashMap<>();

    public void terminalFrequencyAnalysis(List<GPResult> results) {
        for (GPNode terminal : UCARPPrimitiveSet.wholeTerminalSet().getList()) {
            terminalFrequencyMap.put(((FeatureGPNode)terminal).getName(), new int[numTrains]);
        }

        for (int i = 0; i < results.size(); i++) {
            GPResult result = results.get(i);
            String bestExp = result.getBestExpression();
            List<String> terminals = LispUtils.terminals(bestExp);

            for (String terminal : terminals) {
                terminalFrequencyMap.get(terminal)[i] ++;
            }
        }

        // write to csv file
        File writtenPath = new File(trainPath + "analysis");
        if (!writtenPath.exists()) {
            writtenPath.mkdirs();
        }

        File csvFile = new File(writtenPath + "/terminal-run-frequency.csv");

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile.getAbsoluteFile()));
            // write the title
            writer.write("Terminal,Run,Frequency");
            writer.newLine();
            for (String terminal : terminalFrequencyMap.keySet()) {
                for (int i = 0; i < numTrains; i++) {
                    writer.write(terminal + "," + i + "," + terminalFrequencyMap.get(terminal)[i]);
                    writer.newLine();
                }

            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        GPAnalysis gpAnalysis = new GPAnalysis();

        ParameterDatabase parameters = Evolve.loadParameterDatabase(args);

        EvolutionState state = Evolve.initialize(parameters, 0);

        Parameter p;

        // read the analysis to do
        p = new Parameter(P_ANALYSIS);
        gpAnalysis.analysis = parameters.getStringWithDefault(p, null, "");
        // read the path of the training out.stat files.
        p = new Parameter(P_TRAIN_PATH);
        gpAnalysis.trainPath = parameters.getStringWithDefault(p, null, "");
        // read the solution type, e.g. a single routing policy or ensemble
        p = new Parameter(P_SOLUTION_TYPE);
        String stString = parameters.getStringWithDefault(p, null, "");
        gpAnalysis.solutionType = SolutionType.get(stString);
        // read the fitness type, e.g. a multiobjective fitness
        p = new Parameter(P_FITNESS_TYPE);
        String ftString = parameters.getStringWithDefault(p, null, "");
        gpAnalysis.fitnessType = FitnessType.get(ftString);
        // read the number of trains, i.e. the number of out.stat files
        p = new Parameter(P_NUM_TRAINS);
        gpAnalysis.numTrains = parameters.getIntWithDefault(p, null, 1);

        // setup the evaluator, essentially the test evaluation model
        p = new Parameter(EvolutionState.P_EVALUATOR);
        state.evaluator = (Evaluator)
                (parameters.getInstanceForParameter(p, null, Evaluator.class));
        state.evaluator.setup(state, p);

        // read the results from the training files
        List<GPResult> results = new ArrayList<>();
        for (int i = 0; i < gpAnalysis.numTrains; i++) {
            File sourceFile = new File(gpAnalysis.trainPath + "job." + i + ".out.stat");

            // read the rules to a result class
            GPResult result = GPResult.readFromFile(sourceFile, state.evaluator.p_problem, gpAnalysis.solutionType, gpAnalysis.fitnessType);
            results.add(result);
        }

        System.out.println("Analysing from path " + gpAnalysis.trainPath);

        switch (gpAnalysis.analysis) {
            case "terminal-frequency":
                gpAnalysis.terminalFrequencyAnalysis(results);
        }
    }
}
