package gphhucarp.decisionprocess.routingpolicy.ensemble;

import gphhucarp.core.Arc;
import gphhucarp.decisionprocess.DecisionProcessState;
import gphhucarp.decisionprocess.PoolFilter;
import gphhucarp.decisionprocess.RoutingPolicy;
import gphhucarp.decisionprocess.TieBreaker;
import gphhucarp.decisionprocess.reactive.ReactiveDecisionSituation;
import gphhucarp.representation.route.NodeSeqRoute;

import java.util.List;

public class EnsemblePolicy extends RoutingPolicy {

    private RoutingPolicy[] policies; // the element policies in the ensemble
    private double[] weights; // the weights for the element policies
    private Combiner combiner; // the combiner

    public EnsemblePolicy(PoolFilter poolFilter, TieBreaker tieBreaker, RoutingPolicy[] policies, double[] weights, Combiner combiner) {
        super(poolFilter, tieBreaker);
        this.policies = policies;
        this.weights = weights;
        this.combiner = combiner;
    }

    public EnsemblePolicy(PoolFilter poolFilter, RoutingPolicy[] policies, double[] weights, Combiner combiner) {
        super(poolFilter);
        this.policies = policies;
        this.weights = weights;
        this.combiner = combiner;
    }

    public EnsemblePolicy(TieBreaker tieBreaker, RoutingPolicy[] policies, double[] weights, Combiner combiner) {
        super(tieBreaker);
        this.policies = policies;
        this.weights = weights;
        this.combiner = combiner;
    }

    public EnsemblePolicy(PoolFilter poolFilter, TieBreaker tieBreaker, RoutingPolicy[] policies, Combiner combiner) {
        super(poolFilter, tieBreaker);
        this.policies = policies;
        this.combiner = combiner;
        weights = new double[policies.length];
        for (int i = 0; i < weights.length; i++)
            weights[i] = 1;
    }

    public EnsemblePolicy(PoolFilter poolFilter, RoutingPolicy[] policies, Combiner combiner) {
        super(poolFilter);
        this.policies = policies;
        this.combiner = combiner;
        weights = new double[policies.length];
        for (int i = 0; i < weights.length; i++)
            weights[i] = 1;
    }

    public EnsemblePolicy(TieBreaker tieBreaker, RoutingPolicy[] policies, Combiner combiner) {
        super(tieBreaker);
        this.policies = policies;
        this.combiner = combiner;
        weights = new double[policies.length];
        for (int i = 0; i < weights.length; i++)
            weights[i] = 1;
    }

    public RoutingPolicy[] getPolicies() {
        return policies;
    }

    public void setPolicies(RoutingPolicy[] policies) {
        this.policies = policies;
    }

    public RoutingPolicy getPolicy(int index) {
        return policies[index];
    }

    public void setPolicy(int index, RoutingPolicy policy) {
        policies[index] = policy;
    }

    public double[] getWeights() {
        return weights;
    }

    public double getWeight(int index) {
        return weights[index];
    }

    public Combiner getCombiner() {
        return combiner;
    }

    public void setCombiner(Combiner combiner) {
        this.combiner = combiner;
    }

    public int size() {
        return policies.length;
    }

    @Override
    public Arc next(ReactiveDecisionSituation rds) {
        List<Arc> pool = rds.getPool();
        NodeSeqRoute route = rds.getRoute();
        DecisionProcessState state = rds.getState();

        List<Arc> filteredPool = poolFilter.filter(pool, route, state);

        if (filteredPool.isEmpty())
            return null;

        Arc next = combiner.next(pool, route, state, this);

        return next;
    }

    @Override
    /**
     * For ensemble routing policies, this function is not used.
     */
    public double priority(Arc candidate, NodeSeqRoute route, DecisionProcessState state) {
        return 0;
    }
}
