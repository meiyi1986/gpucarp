package gphhucarp.demo;

import gphhucarp.decisionprocess.DecisionProcess;
import gphhucarp.decisionprocess.RoutingPolicy;
import gphhucarp.decisionprocess.routingpolicy.*;
import org.apache.commons.math3.random.RandomDataGenerator;
import gphhucarp.core.Instance;
import gphhucarp.decisionprocess.reactive.ReactiveDecisionProcess;
import util.Timer;

import java.io.File;

/**
 * A demo for a reactive decision process.
 * First, an instances is read from a data file, e.g. data/gdb/gdb23.dat.
 * Then, given a routing policy
 * Created by gphhucarp on 29/08/17.
 */
public class ReactiveDecisionProcessDemo {

    public static void main(String[] args) {
        long seed = 0;
        double demULevel = 0.3;
        double costULevel = 0.2;

        // read an instance from a data file
        Instance instance = Instance.readFromGVE(
                new File("data/val/val10C.dat"), 4,
                demULevel, costULevel);

        // specify a routing policy
//        RoutingPolicy policy = new NearestNeighbourPolicy(new ExpFeasiblePoolFilter());
        RoutingPolicy policy = new PathScanning5Policy();

        // initialise a reactive decision process
        ReactiveDecisionProcess rdp = DecisionProcess.initReactive(instance, seed, policy);

        // run the decision process
        // these should give the same results
        long start = Timer.getCpuTime();
        rdp.run();
        long end = Timer.getCpuTime();
        double duration = (end - start) / 1000000;

        System.out.println(rdp.getState().getSolution().toString());
        System.out.println(rdp.getState().getSolution().totalCost());
        System.out.println("elapsed " + duration + " ms.");

        // rerun the decision process for a number of times.
        // the instance and routing policy do not change,
        // so all the reruns will give the same results.
        int maxReruns = 10;
        for (int rerun = 0; rerun < maxReruns; rerun++) {
            // before rerunning, need to reset the decision process
            rdp.reset();
            start = Timer.getCpuTime();
            rdp.run();
            end = Timer.getCpuTime();
            duration = (end - start) / 1000000;

            System.out.println(rdp.getState().getSolution().toString());
            System.out.println(rdp.getState().getSolution().totalCost());
            System.out.println("elapsed " + duration + " ms.");
        }
    }
}
