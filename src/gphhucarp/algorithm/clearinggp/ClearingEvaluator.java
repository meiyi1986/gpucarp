//package gphhucarp.algorithm.clearinggp;
//
//import ec.EvolutionState;
//import ec.simple.SimpleEvaluator;
//import ec.util.Parameter;
//import gphhucarp.decisionprocess.reactive.ReactiveDecisionSituation;
//
//import java.util.List;
//
//public class ClearingEvaluator extends SimpleEvaluator {
//
//    public static final String P_RADIUS = "radius";
//    public static final String P_CAPACITY = "capacity";
//
//    protected boolean clear = true;
//
//    protected double radius;
//    protected int capacity;
//
//    protected List<ReactiveDecisionSituation> reactiveDecisionSituations;
//
//    public double getRadius() {
//        return radius;
//    }
//
//    public int getCapacity() {
//        return capacity;
//    }
//
//    public List<ReactiveDecisionSituation> getReactiveDecisionSituations() {
//        return reactiveDecisionSituations;
//    }
//
//    public void setup(final EvolutionState state, final Parameter base) {
//        super.setup(state, base);
//
//        radius = state.parameters.getDoubleWithDefault(
//                base.push(P_RADIUS), null, 0.0);
//        capacity = state.parameters.getIntWithDefault(
//                base.push(P_CAPACITY), null, 1);
//
//        phenoCharacterisation =
//                PhenoCharacterisation.defaultPhenoCharacterisation();
//    }
//
//    @Override
//    public void evaluatePopulation(final EvolutionState state) {
//        super.evaluatePopulation(state);
//
//        if (clear) {
//            Clearing.clearPopulation(state, radius, capacity,
//                    phenoCharacterisation);
//        }
//    }
//
//    public void setClear(boolean clear) {
//        this.clear = clear;
//    }
//}
