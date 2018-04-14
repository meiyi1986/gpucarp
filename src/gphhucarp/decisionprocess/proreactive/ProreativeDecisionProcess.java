package gphhucarp.decisionprocess.proreactive;

import gphhucarp.representation.Solution;
import gphhucarp.representation.route.TaskSeqRoute;
import gphhucarp.decisionprocess.DecisionProcess;
import gphhucarp.decisionprocess.DecisionProcessEvent;
import gphhucarp.decisionprocess.DecisionProcessState;
import gphhucarp.decisionprocess.RoutingPolicy;
import gphhucarp.decisionprocess.proreactive.event.ProreactiveServingEvent;

import java.util.PriorityQueue;

/**
 * A proactive-reactive decision process produces an actual solution based on
 * a planned solution (task sequence routes) and a routing policy.
 * Whenever a vehicle finishes a task, the routing policy takes the next task
 * and decides whether to continue the service or go back to the depot to refill.
 */

public class ProreativeDecisionProcess extends DecisionProcess {

    public ProreativeDecisionProcess(DecisionProcessState state,
                                     PriorityQueue<DecisionProcessEvent> eventQueue,
                                     RoutingPolicy routingPolicy,
                                     Solution<TaskSeqRoute> plan) {
        super(state, eventQueue, routingPolicy, plan);
    }

    @Override
    public void reset() {
        state.reset();
        eventQueue.clear();
        for (int i = 0; i < plan.getRoutes().size(); i++)
            eventQueue.add(new ProreactiveServingEvent(0,
                    state.getSolution().getRoute(i), plan.getRoute(i), 0));
    }
}
