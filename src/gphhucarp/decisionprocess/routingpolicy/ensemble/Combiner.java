package gphhucarp.decisionprocess.routingpolicy.ensemble;

import gphhucarp.core.Arc;
import gphhucarp.decisionprocess.DecisionProcessState;
import gphhucarp.representation.route.NodeSeqRoute;

import java.util.List;

/**
 * A combiner combines the decisions made by the policy elements in the ensemble,
 * and returns the final decision.
 */

public abstract class Combiner {

    public abstract Arc next(List<Arc> pool, NodeSeqRoute route, DecisionProcessState state, EnsemblePolicy ensemblePolicy);
}
