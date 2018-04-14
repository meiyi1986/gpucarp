package gphhucarp.algorithm.edasls;

import ec.EvolutionState;
import ec.Fitness;
import ec.Individual;
import ec.Species;
import ec.util.Parameter;
import gphhucarp.core.Arc;
import gphhucarp.core.Instance;
import gphhucarp.core.InstanceSamples;
import gphhucarp.decisionprocess.DecisionProcess;
import gphhucarp.decisionprocess.RoutingPolicy;

import java.util.LinkedList;
import java.util.List;

public class GiantTaskSequenceSpecies extends Species {

    public static final String P_SPECIES = "species";

    @Override
    public Parameter defaultBase()
    {
        return EDASLSDefaults.base().push(P_SPECIES);
    }

    public void setup(final EvolutionState state, final Parameter base) {
        super.setup(state, base);

        // check to make sure that our individual prototype is a GiantTaskSequenceIndividual
        if (!(i_prototype instanceof GiantTaskSequenceIndividual))
            state.output.fatal("The Individual class for the Species " + getClass().getName() + " is must be a subclass of GiantTaskSequenceIndividual.", base);
    }

    public Individual newIndividual(EvolutionState state, int thread) {
        EDASLSEvolutionState edaslsState = (EDASLSEvolutionState)state;
        EDASLSProblem problem = (EDASLSProblem)state.evaluator.p_problem;

        GiantTaskSequenceIndividual newind = new GiantTaskSequenceIndividual();

        // Randomly select an instance
        List<InstanceSamples> instanceSamples = problem.getEvaluationModel().getInstanceSamples();
        int x = edaslsState.getRdg().nextInt(0, instanceSamples.size()-1);
        Instance instance = instanceSamples.get(x).getBaseInstance();

        List<Arc> remainingTasks = new LinkedList<>(instance.getTasks());

        while (!remainingTasks.isEmpty()) {
            int idx = state.random[thread].nextInt(remainingTasks.size());
            Arc nextTask = remainingTasks.get(idx);

            newind.add(nextTask);
            remainingTasks.remove(nextTask);
            remainingTasks.remove(nextTask.getInverse());
        }

        // Set the fitness
        newind.fitness = (Fitness)(f_prototype.clone());
        newind.evaluated = false;

        // Set the species to me
        newind.species = this;

        // ...and we're ready!
        return newind;
    }

    /**
     * Construct a new giant task sequence individual by applying a routing policy
     * (i.e. constructive heuristic) on an instance.
     * @param instance the instance.
     * @param seed the seed to sample the random variables.
     * @param policy the routing policy (i.e. constructive heuristic).
     * @return the constructed giant task sequence individual.
     */
    public GiantTaskSequenceIndividual construct(Instance instance,
                                                 long seed,
                                                 RoutingPolicy policy) {
        DecisionProcess dp = DecisionProcess.initReactive(instance, seed, policy);
        dp.run();

//        System.out.println(dp.getState().getSolution().toString());

        GiantTaskSequenceIndividual newind =
                GiantTaskSequenceIndividual.fromSolution(
                        dp.getState().getSolution(), instance);

        // Set the fitness
        newind.fitness = (Fitness)(f_prototype.clone());
        newind.evaluated = false;

        // Set the species to me
        newind.species = this;

        // ...and we're ready!
        return newind;
    }

}