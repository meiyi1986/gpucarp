package gphhucarp.decisionprocess.routingpolicy;

import ec.gp.GPTree;
import gphhucarp.core.Arc;
import gphhucarp.representation.route.NodeSeqRoute;
import gphhucarp.decisionprocess.DecisionProcessState;
import gphhucarp.decisionprocess.PoolFilter;
import gphhucarp.decisionprocess.RoutingPolicy;
import gphhucarp.decisionprocess.poolfilter.IdentityPoolFilter;
import gphhucarp.gp.CalcPriorityProblem;
import gputils.DoubleData;

/**
 * A GP-evolved routing policy.
 *
 * Created by gphhucarp on 30/08/17.
 */
public class GPRoutingPolicy extends RoutingPolicy {

    private GPTree gpTree;

    public GPRoutingPolicy(PoolFilter poolFilter, GPTree gpTree) {
        super(poolFilter);
        name = "\"GPRoutingPolicy\"";
        this.gpTree = gpTree;
    }

    public GPRoutingPolicy(GPTree gpTree) {
        this(new IdentityPoolFilter(), gpTree);
    }

    public GPTree getGPTree() {
        return gpTree;
    }

    public void setGPTree(GPTree gpTree) {
        this.gpTree = gpTree;
    }

    @Override
    public double priority(Arc candidate, NodeSeqRoute route, DecisionProcessState state) {
        CalcPriorityProblem calcPrioProb =
                new CalcPriorityProblem(candidate, route, state);

        DoubleData tmp = new DoubleData();
        gpTree.child.eval(null, 0, tmp, null, null, calcPrioProb);

        return tmp.value;
    }
}
