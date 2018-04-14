package gphhucarp.decisionprocess;

import gphhucarp.core.Arc;
import gphhucarp.representation.route.NodeSeqRoute;

import java.util.List;

/**
 * A pool filter uses some criteria to filter out tasks from a pool given a state.
 * This is a preprocessing to help improve the effectiveness and efficiency of
 * decision making of routing policy during the decision making process.
 */

public abstract class PoolFilter {

    public abstract List<Arc> filter(List<Arc> pool,
                                     NodeSeqRoute route,
                                     DecisionProcessState state);
}
