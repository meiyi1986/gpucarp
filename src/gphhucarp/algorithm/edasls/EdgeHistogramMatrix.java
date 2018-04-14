package gphhucarp.algorithm.edasls;

import gphhucarp.core.Arc;
import gphhucarp.core.Instance;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.Map;

/**
 * An edge histogram matrix stores the histogram of each pair of tasks.
 */

public class EdgeHistogramMatrix {

    public static final double B_RATIO = 0.005;
    public static final double LEARNING_RATE = 1;

    private Map<Pair<Arc, Arc>, Double> matrix;

    public EdgeHistogramMatrix(Map<Pair<Arc, Arc>, Double> matrix) {
        this.matrix = matrix;
    }

    public EdgeHistogramMatrix() {
        this(new HashMap<>());
    }

    public EdgeHistogramMatrix(Instance instance) {
        matrix = new HashMap<>();

        // initialise all the histograms to be epsilon
        for (int i = 0; i < instance.getTasks().size()-1; i++) {
            Arc task1 = instance.getTasks().get(i);
            for (int j = i+1; j < instance.getTasks().size(); j++) {
                Arc task2 = instance.getTasks().get(j);

                matrix.put(Pair.of(task1, task2), 0d);
                matrix.put(Pair.of(task2, task1), 0d);
            }
        }

        // <null, task> is from the depot to the task
        // <task, null> is from the task to the depot
        for (Arc task : instance.getTasks()) {
            matrix.put(Pair.of(task, instance.getDepotLoop()), 0d);
            matrix.put(Pair.of(instance.getDepotLoop(), task), 0d);
        }
    }

    public Map<Pair<Arc, Arc>, Double> getMatrix() {
        return matrix;
    }

    public void updateBy(GiantTaskSequenceIndividual[] individuals,
                         Instance instance) {
        double epsilon = individuals.length / (individuals[0].size() - 1) * B_RATIO;

        // update by epsilon first
        for (Pair<Arc, Arc> key : matrix.keySet()) {
            double oldValue = matrix.get(key);
            double newValue = (1-LEARNING_RATE) * oldValue + LEARNING_RATE * epsilon;
            matrix.put(key, newValue);
        }

        // update by the individuals
        for (GiantTaskSequenceIndividual individual : individuals) {
            // from depot to the first task
            Arc first = individual.get(0);
            double oldValue = matrix.get(Pair.of(instance.getDepotLoop(), first));
            double newValue = oldValue + LEARNING_RATE;
            matrix.put(Pair.of(instance.getDepotLoop(), first), newValue);
            matrix.put(Pair.of(first.getInverse(), instance.getDepotLoop()), newValue);

            for (int i = 0; i < individual.size()-1; i++) {
                Arc former = individual.get(i);
                Arc latter = individual.get(i+1);

                oldValue = matrix.get(Pair.of(former, latter));
                newValue = oldValue + LEARNING_RATE;
                matrix.put(Pair.of(former, latter), newValue);
                matrix.put(Pair.of(latter.getInverse(), former.getInverse()), newValue);
            }

            Arc last = individual.get(individual.getTaskSequence().size()-1);
            oldValue = matrix.get(Pair.of(last, instance.getDepotLoop()));
            newValue = oldValue + LEARNING_RATE;
            matrix.put(Pair.of(last, instance.getDepotLoop()), newValue);
            matrix.put(Pair.of(instance.getDepotLoop(), last.getInverse()), newValue);
        }
    }

    public double getValue(Arc former, Arc latter) {
        return matrix.get(Pair.of(former, latter));
    }
}
