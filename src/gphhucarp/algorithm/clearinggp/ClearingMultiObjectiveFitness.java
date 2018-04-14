package gphhucarp.algorithm.clearinggp;

import ec.EvolutionState;
import ec.multiobjective.MultiObjectiveFitness;

public class ClearingMultiObjectiveFitness extends MultiObjectiveFitness {

    // each element of the phenotype vector is
    // the index of the best candidate selected for each decision situation.
    private int[] phenotypeVector;

    private boolean cleared;

    public void clear() {
        for (int i = 0; i < objectives.length; i++) {
            if (maximize[i]) {
                objectives[i] = Double.NEGATIVE_INFINITY;
            }
            else {
                objectives[i] = Double.POSITIVE_INFINITY;
            }
        }

        cleared = true;
    }

    public boolean isCleared() {
        return cleared;
    }

    public void setObjectives(final EvolutionState state, double[] newObjectives) {
        super.setObjectives(state, newObjectives);

        cleared = false;
    }
}
