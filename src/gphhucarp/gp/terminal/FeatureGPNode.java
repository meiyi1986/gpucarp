package gphhucarp.gp.terminal;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;
import gphhucarp.gp.CalcPriorityProblem;
import gputils.DoubleData;
import org.apache.commons.math3.random.RandomDataGenerator;

/**
 * A feature GP node will be used as a terminal of GP.
 *
 * Created by gphhucarp on 30/08/17.
 */
public abstract class FeatureGPNode extends GPNode {
    protected String name;

    public FeatureGPNode() {
        super();
        children = new GPNode[0];
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int expectedChildren() {
        return 0;
    }

    @Override
    public void eval(EvolutionState state, int thread, GPData input,
                     ADFStack stack, GPIndividual individual, Problem problem) {
        // The problem is essentially a priority calculation.
        CalcPriorityProblem calcPrioProb = ((CalcPriorityProblem)problem);

        DoubleData data = ((DoubleData)input);
        data.value = value(calcPrioProb);
    }

    public abstract double value(CalcPriorityProblem calcPriorityProblem);
}
