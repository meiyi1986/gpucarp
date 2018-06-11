package gphhucarp.algorithm.pilotsearch.pilotsearcher;

import gphhucarp.algorithm.pilotsearch.PilotSearcher;
import gphhucarp.core.Arc;
import gphhucarp.decisionprocess.DecisionProcessEvent;
import gphhucarp.decisionprocess.DecisionProcessState;
import gphhucarp.decisionprocess.RoutingPolicy;
import gphhucarp.decisionprocess.reactive.ReactiveDecisionProcess;
import gphhucarp.decisionprocess.reactive.ReactiveDecisionSituation;
import gphhucarp.decisionprocess.reactive.event.ReactiveRefillEvent;
import gphhucarp.decisionprocess.reactive.event.ReactiveServingEvent;
import gphhucarp.representation.route.NodeSeqRoute;

import java.util.PriorityQueue;

/**
 * The sequential pilot searcher starts from the current decision state,
 * and grow the routes one by one, until all the routes have been closed.
 */

public class SeqPilotSearcher extends PilotSearcher {
    @Override
    public Arc next(ReactiveDecisionSituation rds, RoutingPolicy routingPolicy) {
        DecisionProcessState state = rds.getState().clone();

        int decisionRouteId = rds.getState().getSolution().getRoutes().indexOf(rds.getRoute());

        int routeId = 0; // start from the first route
        PriorityQueue<DecisionProcessEvent> queue = new PriorityQueue<>();
        // build the routes before the decision route first
        while (routeId < decisionRouteId) {
            NodeSeqRoute route = state.getSolution().getRoute(routeId);
            Arc routeNextTask = route.getNextTask();
            if (routeNextTask.equals(state.getInstance().getDepotLoop())) {
                queue.add(new ReactiveRefillEvent(route.getCost(), route));
            } else {
                queue.add(new ReactiveServingEvent(route.getCost(), route, routeNextTask));
            }

            ReactiveDecisionProcess dp = new ReactiveDecisionProcess(state, queue, routingPolicy);

            while (!queue.isEmpty()) {
                DecisionProcessEvent event = queue.poll();
                event.trigger(dp);

                if (route.currNode() == state.getInstance().getDepot())
                    queue.clear();
            }

            routeId ++;
        }

        // get the next task of the current route
        NodeSeqRoute route = state.getSolution().getRoute(routeId);
        Arc routeNextTask = route.getNextTask();
        DecisionProcessEvent event;
        if (routeNextTask.equals(state.getInstance().getDepotLoop())) {
            event = new ReactiveRefillEvent(route.getCost(), route);
        } else {
            event = new ReactiveServingEvent(route.getCost(), route, routeNextTask);
        }

        ReactiveDecisionProcess dp = new ReactiveDecisionProcess(state, queue, routingPolicy);
        // trigger the current event to get a new next task
        event.trigger(dp);

        return route.getNextTask();
    }
}
