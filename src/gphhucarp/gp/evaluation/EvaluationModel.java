package gphhucarp.gp.evaluation;

import ec.EvolutionState;
import ec.Fitness;
import ec.multiobjective.MultiObjectiveFitness;
import ec.util.Parameter;
import gphhucarp.core.Instance;
import gphhucarp.core.InstanceSamples;
import gphhucarp.core.Objective;
import gphhucarp.representation.Solution;
import gphhucarp.representation.route.NodeSeqRoute;
import gphhucarp.representation.route.TaskSeqRoute;
import gphhucarp.decisionprocess.DecisionProcess;
import gphhucarp.decisionprocess.RoutingPolicy;
import gphhucarp.decisionprocess.reactive.ReactiveDecisionProcess;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The evaluation model for evaluating individuals in GPHH.
 */

public abstract class EvaluationModel {
    public static final long SEED_GAP_INSTANCE = 935627; // seed gap between instances
    public static final long SEED_GAP_ROTATION = 6125; // seed gap for each rotation

    public static final String P_OBJECTIVES = "objectives";
    public static final String P_INSTANCES = "instances";
    public static final String P_FILE = "file"; // the file of the instance
    public static final String P_SAMPLES = "samples"; // the number of samples
    public static final String P_DEM_ULEVEL = "demand-uncertainty-level";
    public static final String P_COST_ULEVEL = "cost-uncertainty-level";
    public static final String P_VEHICLES = "vehicles"; // nubmer of vehicles
    public static final String P_SEED = "seed"; // the seed for the first instance

    protected List<Objective> objectives;
    protected List<InstanceSamples> instanceSamples; // the instance samples used for evaluation
    protected Map<Pair<Integer, Objective>, Double> objRefValueMap;

    public List<Objective> getObjectives() {
        return objectives;
    }

    public List<InstanceSamples> getInstanceSamples() {
        return instanceSamples;
    }

    /**
     * Get the objective reference value of a particular decision process and an objective.
     * @param index the index of the decision process.
     * @param objective the objective.
     * @return the corresponding objective reference value.
     */
    public double getObjRefValue(int index, Objective objective) {
        return objRefValueMap.get(Pair.of(index, objective));
    }

    public void setup(final EvolutionState state, final Parameter base) {
        // get the objectives
        Parameter p = base.push(P_OBJECTIVES);
        int numObjectives = state.parameters.getIntWithDefault(p, null, 0);

        if (numObjectives == 0) {
            System.err.println("ERROR:");
            System.err.println("No objective is specified.");
            System.exit(1);
        }

        objectives = new ArrayList<>();
        for (int i = 0; i < numObjectives; i++) {
            p = base.push(P_OBJECTIVES).push("" + i);
            String objectiveName = state.parameters.getStringWithDefault(p, null, "");
            Objective objective = Objective.get(objectiveName);

            objectives.add(objective);
        }

        // setup the instances
        p = base.push(P_INSTANCES);
        int numInstances = state.parameters.getIntWithDefault(p, null, 0);

        if (numInstances == 0) {
            System.err.println("ERROR:");
            System.err.println("No instance is provided.");
            System.exit(1);
        }

        // the seed for the first instance
        p = base.push(P_SEED);
        long initSeed = state.parameters.getLongWithDefault(p, null, 0);
        long currSeed = initSeed;

        instanceSamples = new ArrayList<>();
        for (int i = 0; i < numInstances; i++) {
            Parameter b = base.push(P_INSTANCES).push("" + i);
            // the file of the instance
            p = b.push(P_FILE);
            String file = state.parameters.getStringWithDefault(p, null, null);
            // the number of samples for this instance
            p = b.push(P_SAMPLES);
            int samples = state.parameters.getIntWithDefault(p, null, 1);
            // the demand uncertainty level
            p = b.push(P_DEM_ULEVEL);
            double demULevel = state.parameters.getDoubleWithDefault(p, null, 0);
            // the cost uncertainty level
            p = b.push(P_COST_ULEVEL);
            double costULevel = state.parameters.getDoubleWithDefault(p, null, 0);
            // the number of vehicles/routes
            p = b.push(P_VEHICLES);
            String numVehiclesStr = state.parameters.getStringWithDefault(p, null, "from-file");

            Instance baseInstance = null;

            if (numVehiclesStr.equals("from-file")) {
                baseInstance = Instance.readFromGVE(
                        new File("data/" + file),
                        demULevel, costULevel);
            }
            else if (NumberUtils.isNumber(numVehiclesStr)) {
                int numVehicles = Integer.valueOf(numVehiclesStr);

                baseInstance = Instance.readFromGVE(
                        new File("data/" + file),
                        numVehicles, demULevel, costULevel);
            }
            else {
                System.err.println("Unknown number of vehicles: " + numVehiclesStr);
                System.exit(1);
            }

            String fileName = file.substring(file.lastIndexOf("/")+1, file.length()-4);
            baseInstance.setName(fileName);

            InstanceSamples iSamples = new InstanceSamples(baseInstance);
            for (int s = 0; s < samples; s++) {
                iSamples.addSeed(currSeed);
                currSeed += SEED_GAP_INSTANCE;
            }

            instanceSamples.add(iSamples);
        }

        // calculate the initial objective reference values
        objRefValueMap = new HashMap<>();
        calcObjRefValueMap();
    }

    /**
     * Rotate the seeds of the instances.
     * For each instance, the seed is incremented by SEED_GAP_ROTATION.
     */
    public void rotateSeeds() {
        for (InstanceSamples iSamples : instanceSamples) {
            for (int i = 0; i < iSamples.getSeeds().size(); i++) {
                long seed = iSamples.getSeed(i);
                iSamples.setSeed(i, seed + SEED_GAP_ROTATION);
            }
        }

        // recalculate the objective reference values after rotation
        calcObjRefValueMap();
    }

    /**
     * Calculate the objective reference values.
     */
    public void calcObjRefValueMap() {
        int index = 0;
        for (InstanceSamples iSamples : instanceSamples) {
            for (long seed : iSamples.getSeeds()) {
                // create a new reactive decision process from the based intance and the seed.
                ReactiveDecisionProcess dp =
                        DecisionProcess.initReactive(iSamples.getBaseInstance(),
                                seed, Objective.refReactiveRoutingPolicy());

                // get the objective reference values by applying the reference routing policy.
                dp.run();
                Solution<NodeSeqRoute> solution = dp.getState().getSolution();
                for (Objective objective : objectives) {
                    double objValue = solution.objValue(objective);
                    objRefValueMap.put(Pair.of(index, objective), objValue);
                    index ++;
                }
                dp.reset();
            }
        }
    }

    /**
     * Evaluate an individual (a policy plus a plan) using this evaluation model.
     * @param policy the policy to be evaluated.
     * @param plan the plan to be evaluated -- null if the policy is reactive.
     * @param fitness the fitness of the individual.
     * @param state the evolution state.
     */
    public abstract void evaluate(RoutingPolicy policy, Solution<TaskSeqRoute> plan,
                         Fitness fitness, EvolutionState state);

    /**
     * Evaluate an individual (a policy plus a plan) using this evaluation model.
     * The fitness is original --- without normalisation.
     * @param policy the policy to be evaluated.
     * @param plan the plan to be evaluated -- null if the policy is reactive.
     * @param fitness the fitness of the individual.
     * @param state the evolution state.
     */
    public abstract void evaluateOriginal(RoutingPolicy policy,
                                 Solution<TaskSeqRoute> plan,
                         Fitness fitness, EvolutionState state);
}
