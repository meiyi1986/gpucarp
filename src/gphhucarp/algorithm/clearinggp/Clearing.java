//package gphhucarp.algorithm.clearinggp;
//
//import gphhucarp.core.Instance;
//import gphhucarp.decisionprocess.DecisionProcess;
//import gphhucarp.decisionprocess.RoutingPolicy;
//import gphhucarp.decisionprocess.reactive.ReactiveDecisionProcess;
//import gphhucarp.decisionprocess.reactive.ReactiveDecisionSituation;
//import gphhucarp.decisionprocess.routingpolicy.PathScanning5Policy;
//import org.apache.commons.math3.random.RandomDataGenerator;
//
//import java.util.List;
//
//public class Clearing {
//
//    public static List<ReactiveDecisionSituation> randomDecisionSituations(
//            int numDecisionSituations) {
//        long seed = 8295342;
//        RandomDataGenerator rdg = new RandomDataGenerator();
//        rdg.reSeed(seed);
//
//        Instance randomInstance = Instance.randomInstance(100, rdg);
//        randomInstance.sample(RandomDataGenerator rdg);
//
//        RoutingPolicy policy = new PathScanning5Policy();
//
//        ReactiveDecisionProcess rdp = DecisionProcess.initReactive(randomInstance, policy);
//
//        DynamicSimulation simulation = DynamicSimulation.standardFull(0, refRule,
//                10, 500, 0, 0.95, 4.0);
//
//        List<DecisionSituation> situations = simulation.decisionSituations(minQueueLength);
//        Collections.shuffle(situations, new Random(shuffleSeed));
//
//        situations = situations.subList(0, numDecisionSituations);
//        return new PhenoCharacterisation(situations, refRule);
//    }
//}
