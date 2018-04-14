package gphhucarp.algorithm.ccgp;

import ec.EvolutionState;
import ec.Individual;
import ec.Population;
import ec.coevolve.GroupedProblemForm;
import ec.gp.GPIndividual;
import ec.multiobjective.MultiObjectiveFitness;
import ec.util.Parameter;
import gphhucarp.decisionprocess.routingpolicy.GPRoutingPolicy;
import gphhucarp.decisionprocess.routingpolicy.ensemble.Combiner;
import gphhucarp.decisionprocess.routingpolicy.ensemble.EnsemblePolicy;
import gphhucarp.gp.ReactiveGPHHProblem;

/**
 * The CCGPHH problem.
 * The problem evalutes a set of individuals together by forming an ensemble policy.
 * Then it sets the fitness for all the indices to be updated.
 * Finally, it updates the context vector and its fitness if better context vector is found.
 *
 */

public class CCGPHHProblem extends ReactiveGPHHProblem implements GroupedProblemForm {

    public static final String P_SHOULD_SET_CONTEXT = "set-context";
    public static final String P_COMBINER = "combiner";

    boolean shouldSetContext;
    private Combiner combiner;

    public Combiner getCombiner() {
        return combiner;
    }

    public void setup(final EvolutionState state, final Parameter base) {
        super.setup(state, base);

        // load whether we should set context or not
        shouldSetContext = state.parameters.getBoolean(base.push(P_SHOULD_SET_CONTEXT), null, true);

        // load the combiner for the ensember routing policy
        combiner = (Combiner)(
                state.parameters.getInstanceForParameter(
                        base.push(P_COMBINER), null, Combiner.class));
    }

    @Override
    public void preprocessPopulation(final EvolutionState state, Population pop, boolean[] prepareForAssessment, boolean countVictoriesOnly) {
    }

    @Override
    public void postprocessPopulation(final EvolutionState state, Population pop, boolean[] assessFitness, boolean countVictoriesOnly) {
    }

    @Override
    public void evaluate(final EvolutionState state,
                         final Individual[] ind,  // the individuals to evaluate together
                         final boolean[] updateFitness,  // should this individuals' fitness be updated?
                         final boolean countVictoriesOnly, // can be neglected in cooperative coevolution
                         int[] subpops,
                         final int threadnum) {
        if (ind.length == 0)
            state.output.fatal("Number of individuals provided to CoevolutionaryECSuite is 0!");

        if (ind.length == 1)
            state.output.warnOnce("Coevolution used, but number of individuals provided to CoevolutionaryECSuite is 1.");

        for(int i = 0 ; i < ind.length; i++)
            if ( ! ( ind[i] instanceof GPIndividual) )
                state.output.error( "Individual " + i + "in coevolution is not a GPIndividual." );

        state.output.exitIfErrors();

        // create an ensemble routing policy based on the individuals
        GPRoutingPolicy[] policies = new GPRoutingPolicy[ind.length];
        for (int i = 0; i < policies.length; i++)
            policies[i] = new GPRoutingPolicy(poolFilter, ((GPIndividual)ind[i]).trees[0]);

        EnsemblePolicy ensemblePolicy = new EnsemblePolicy(poolFilter, policies, combiner);

        MultiObjectiveFitness trialFit = (MultiObjectiveFitness)ind[0].fitness.clone();

        evaluationModel.evaluate(ensemblePolicy, null, trialFit, state);

        // update the fitness of the evaluated individuals
        for (int i = 0; i < ind.length; i++) {
            if (updateFitness[i])
                ((MultiObjectiveFitness)(ind[i].fitness)).setObjectives(state, trialFit.objectives);
        }

        // update the context vector if the trial fitness is better
        CCGPHHEvolutionState ccgpState = (CCGPHHEvolutionState)state;
        MultiObjectiveFitness contextFitness =
                (MultiObjectiveFitness)ccgpState.getContext(0).fitness;
        if (trialFit.betterThan(contextFitness)) {
            for (int i = 0; i < ind.length; i++) {
                ((MultiObjectiveFitness)ind[i].fitness).setObjectives(state, trialFit.objectives);
                ccgpState.setContext(i, ind[i]);
            }
        }
    }

    /**
     * Evaluate the context vector.
     * @param state the evolution state.
     * @param ind the context vector as an array of individuals.
     */
    public void evaluateContextVector(final EvolutionState state,
                                      final Individual[] ind) {
        // create an ensemble routing policy based on the individuals
        GPRoutingPolicy[] policies = new GPRoutingPolicy[ind.length];
        for (int i = 0; i < policies.length; i++)
            policies[i] = new GPRoutingPolicy(poolFilter, ((GPIndividual)ind[i]).trees[0]);

        EnsemblePolicy ensemblePolicy = new EnsemblePolicy(poolFilter, policies, combiner);

        MultiObjectiveFitness trialFit = (MultiObjectiveFitness)ind[0].fitness.clone();

        evaluationModel.evaluate(ensemblePolicy, null, trialFit, state);

        for (Individual i : ind)
            ((MultiObjectiveFitness)(i.fitness)).setObjectives(state, trialFit.objectives);
    }
}
