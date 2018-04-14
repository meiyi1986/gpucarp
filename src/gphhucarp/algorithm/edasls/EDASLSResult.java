package gphhucarp.algorithm.edasls;

import ec.Fitness;
import ec.Problem;
import ec.multiobjective.MultiObjectiveFitness;
import gphhucarp.core.Arc;
import gphhucarp.core.Instance;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EDASLSResult {

    private List<String> expressions;
    private List<GiantTaskSequenceIndividual> solutions;
    private List<Fitness> trainFitnesses;
    private List<Fitness> testFitnesses;
    private String bestExpression;
    private GiantTaskSequenceIndividual bestSolution;
    private Fitness bestTrainFitness;
    private Fitness bestTestFitness;
    private DescriptiveStatistics timeStat;

    public EDASLSResult() {
        expressions = new ArrayList<>();
        solutions = new ArrayList<>();
        trainFitnesses = new ArrayList<>();
        testFitnesses = new ArrayList<>();
    }

    public List<String> getExpressions() {
        return expressions;
    }

    public void setExpressions(List<String> expressions) {
        this.expressions = expressions;
    }

    public String getBestExpression() {
        return bestExpression;
    }

    public void setBestExpression(String bestExpression) {
        this.bestExpression = bestExpression;
    }

    public List<GiantTaskSequenceIndividual> getSolutions() {
        return solutions;
    }

    public void setSolutions(List<GiantTaskSequenceIndividual> solutions) {
        this.solutions = solutions;
    }

    public List<Fitness> getTrainFitnesses() {
        return trainFitnesses;
    }

    public void setTrainFitnesses(List<Fitness> trainFitnesses) {
        this.trainFitnesses = trainFitnesses;
    }

    public List<Fitness> getTestFitnesses() {
        return testFitnesses;
    }

    public void setTestFitnesses(List<Fitness> testFitnesses) {
        this.testFitnesses = testFitnesses;
    }

    public GiantTaskSequenceIndividual getBestSolution() {
        return bestSolution;
    }

    public void setBestSolution(GiantTaskSequenceIndividual bestSolution) {
        this.bestSolution = bestSolution;
    }

    public Fitness getBestTrainFitness() {
        return bestTrainFitness;
    }

    public void setBestTrainFitness(Fitness bestTrainFitness) {
        this.bestTrainFitness = bestTrainFitness;
    }

    public Fitness getBestTestFitness() {
        return bestTestFitness;
    }

    public void setBestTestFitness(Fitness bestTestFitness) {
        this.bestTestFitness = bestTestFitness;
    }

    public DescriptiveStatistics getTimeStat() {
        return timeStat;
    }

    public void setTimeStat(DescriptiveStatistics timeStat) {
        this.timeStat = timeStat;
    }

    public GiantTaskSequenceIndividual getSolutionAtGen(int gen) {
        return solutions.get(gen);
    }

    public Fitness getTrainFitnessAtGen(int gen) {
        return trainFitnesses.get(gen);
    }

    public Fitness getTestFitnessAtGen(int gen) {
        return testFitnesses.get(gen);
    }

    public double getTimeAtGen(int gen) {
        return timeStat.getElement(gen);
    }

    public void addExpression(String expression) {
        expressions.add(expression);
    }

    public void addSolution(GiantTaskSequenceIndividual solution) {
        solutions.add(solution);
    }

    public void addTrainFitness(Fitness fitness) {
        trainFitnesses.add(fitness);
    }

    public void addTestFitness(Fitness fitness) {
        testFitnesses.add(fitness);
    }

    public static EDASLSResult readFromFile(File file,
                                        Problem problem) {
        EDASLSProblem prob = (EDASLSProblem)problem;

        EDASLSResult result = new EDASLSResult();

        String line;
        Fitness fitness = null;
        GiantTaskSequenceIndividual solution = null;
        String expression = "";

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            while (!(line = br.readLine()).equals("Best Individual of Run:")) {
                if (line.startsWith("Generation")) {
                    br.readLine();
                    br.readLine();
                    br.readLine();
                    line = br.readLine();
                    fitness = readSimpleFitnessFromLine(line);
                    expression = br.readLine();

                    result.addExpression(expression);

                    GiantTaskSequenceIndividual indi = readGTSExpression(expression,
                            prob.getEvaluationModel().getInstanceSamples().get(0).getBaseInstance());

                    result.addSolution(indi);
                    result.addTrainFitness(fitness);
                    result.addTestFitness((Fitness)fitness.clone());

                    solution = indi;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Set the best solution as the solution in the last generation
        result.setBestExpression(expression);
        result.setBestSolution(solution);
        result.setBestTrainFitness(fitness);
        result.setBestTestFitness((Fitness)fitness.clone());

        return result;
    }

    private static Fitness readSimpleFitnessFromLine(String line) {
        String[] segments = line.split("\\[|\\]");
        double fitness = Double.valueOf(segments[1]);
        MultiObjectiveFitness f = new MultiObjectiveFitness();
        f.objectives = new double[1];
        f.objectives[0] = fitness;

        return f;
    }

    /**
     * Read a giant task sequence from an expression string, whose format is given by out.stat.
     * @param expression the expression string.
     * @param instance the instance as the database.
     * @return the giant task sequence.
     */
    private static GiantTaskSequenceIndividual readGTSExpression(String expression,
                                                                 Instance instance) {
        GiantTaskSequenceIndividual indi = new GiantTaskSequenceIndividual();

        String seq = expression.trim();

        int leftIdx = 0;
        while (seq.charAt(leftIdx) == '(') {
            // there are still tasks
            int commaIdx = seq.indexOf(',', leftIdx+1);
            int from = Integer.valueOf(seq.substring(leftIdx+1, commaIdx));

            int rightIdx = seq.indexOf(')', commaIdx);
            int to = Integer.valueOf(seq.substring(commaIdx+2, rightIdx));

            Arc arc = instance.getGraph().getArc(from, to);
            indi.add(arc);

            leftIdx = rightIdx + 2;

            if (leftIdx > seq.length())
                break;
        }

        return indi;
    }

    public static DescriptiveStatistics readTimeFromFile(File file) {
        DescriptiveStatistics generationalTimeStat = new DescriptiveStatistics();

        String line;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            br.readLine();
            while(true) {
                line = br.readLine();

                if (line == null)
                    break;

                String[] commaSegments = line.split(",");
                generationalTimeStat.addValue(Double.valueOf(commaSegments[1]));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return generationalTimeStat;
    }

}
