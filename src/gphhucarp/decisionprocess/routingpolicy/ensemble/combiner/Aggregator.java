package gphhucarp.decisionprocess.routingpolicy.ensemble.combiner;

import gphhucarp.core.Arc;
import gphhucarp.decisionprocess.DecisionProcessState;
import gphhucarp.decisionprocess.RoutingPolicy;
import gphhucarp.decisionprocess.routingpolicy.ensemble.EnsemblePolicy;
import gphhucarp.decisionprocess.routingpolicy.ensemble.Combiner;
import gphhucarp.representation.route.NodeSeqRoute;

import java.util.List;

/**
 * The aggregator combiner simply sums up the weighted priority calculated by all the elements,
 * and set the final priority as the weighted sum.
 */

public class Aggregator extends Combiner {

    @Override
    public Arc next(List<Arc> pool, NodeSeqRoute route, DecisionProcessState state, EnsemblePolicy ensemblePolicy) {
        Arc next = pool.get(0);
        next.setPriority(priority(next, route, state, ensemblePolicy));

        for (int i = 1; i < pool.size(); i++) {
            Arc tmp = pool.get(i);
            tmp.setPriority(priority(tmp, route, state, ensemblePolicy));

            if (Double.compare(tmp.getPriority(), next.getPriority()) < 0 ||
                    (Double.compare(tmp.getPriority(), next.getPriority()) == 0 &&
                            ensemblePolicy.getTieBreaker().breakTie(tmp, next) < 0))
                next = tmp;
        }

        return next;
    }

    /**
     * Calculate the priority of a candidate arc by an ensemble policy.
     * @param arc the arc whose priority is to be calculated.
     * @param route the route.
     * @param state the decision process state.
     * @param ensemblePolicy the ensemble policy.
     * @return the priority of the arc calculated by the ensemble policy.
     */
    private double priority(Arc arc, NodeSeqRoute route, DecisionProcessState state, EnsemblePolicy ensemblePolicy) {
        double priority = 0;
        for (int i = 0; i < ensemblePolicy.size(); i++) {
            priority += ensemblePolicy.getPolicy(i).priority(arc, route, state) *
                    ensemblePolicy.getWeight(i);
        }

        return priority;
    }
}
