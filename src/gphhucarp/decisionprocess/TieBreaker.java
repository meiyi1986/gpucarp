package gphhucarp.decisionprocess;

import gphhucarp.core.Arc;

/**
 * A tie breaker breaks the tie between two arcs when they have the same priority.
 */

public abstract class TieBreaker {

    public abstract int breakTie(Arc arc1, Arc arc2);
}
