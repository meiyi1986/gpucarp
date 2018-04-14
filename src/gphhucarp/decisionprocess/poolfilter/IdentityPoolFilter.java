package gphhucarp.decisionprocess.poolfilter;

import gphhucarp.core.Arc;
import gphhucarp.representation.route.NodeSeqRoute;
import gphhucarp.decisionprocess.DecisionProcessState;
import gphhucarp.decisionprocess.PoolFilter;

import java.util.List;

/**
 * The identity pool filter does nothing, but simply returns the pool.
 * It is called "identity" since the filtered pool is the same as the given pool.
 */

public class IdentityPoolFilter extends PoolFilter {

    @Override
    public List<Arc> filter(List<Arc> pool,
                            NodeSeqRoute route,
                            DecisionProcessState state) {
        return pool;
    }
}
