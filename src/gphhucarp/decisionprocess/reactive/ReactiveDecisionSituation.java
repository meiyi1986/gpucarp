package gphhucarp.decisionprocess.reactive;

import gphhucarp.core.Arc;
import gphhucarp.decisionprocess.DecisionProcessState;
import gphhucarp.decisionprocess.DecisionSituation;
import gphhucarp.representation.route.NodeSeqRoute;

import java.util.LinkedList;
import java.util.List;

/**
 * The decision situation for reactive decision process.
 */

public class ReactiveDecisionSituation extends DecisionSituation {

    private List<Arc> pool;
    private NodeSeqRoute route;
    private DecisionProcessState state;

    public ReactiveDecisionSituation(List<Arc> pool, NodeSeqRoute route, DecisionProcessState state) {
        this.pool = pool;
        this.route = route;
        this.state = state;
    }

    public List<Arc> getPool() {
        return pool;
    }

    public NodeSeqRoute getRoute() {
        return route;
    }

    public DecisionProcessState getState() {
        return state;
    }

    public ReactiveDecisionSituation clone() {
        List<Arc> clonedPool = new LinkedList<>(pool);
        NodeSeqRoute clonedRoute = (NodeSeqRoute)route.clone();
        DecisionProcessState clonedState = state.clone();

        return new ReactiveDecisionSituation(clonedPool, clonedRoute, clonedState);
    }
}
