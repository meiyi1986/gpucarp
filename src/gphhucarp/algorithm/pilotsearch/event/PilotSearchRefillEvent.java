package gphhucarp.algorithm.pilotsearch.event;

import gphhucarp.algorithm.pilotsearch.PilotSearcher;
import gphhucarp.core.Arc;
import gphhucarp.core.Graph;
import gphhucarp.core.Instance;
import gphhucarp.decisionprocess.DecisionProcess;
import gphhucarp.decisionprocess.DecisionProcessEvent;
import gphhucarp.decisionprocess.DecisionProcessState;
import gphhucarp.decisionprocess.RoutingPolicy;
import gphhucarp.decisionprocess.reactive.ReactiveDecisionSituation;
import gphhucarp.representation.route.NodeSeqRoute;

import java.util.LinkedList;
import java.util.List;

/**
 * This is the same as ReactiveRefillEvent, but interact with other pilot search events.
 */

public class PilotSearchRefillEvent extends DecisionProcessEvent {

    private NodeSeqRoute route;
    private PilotSearcher pilotSearcher;

    public PilotSearchRefillEvent(double time, NodeSeqRoute route,
                                  PilotSearcher pilotSearcher) {
        super(time);
        this.route = route;
        this.pilotSearcher = pilotSearcher;
    }

    @Override
    public void trigger(DecisionProcess decisionProcess) {
        RoutingPolicy policy = decisionProcess.getRoutingPolicy();
        DecisionProcessState state = decisionProcess.getState();
        Instance instance = state.getInstance();
        Graph graph = instance.getGraph();
        int depot = instance.getDepot();

        int currNode = route.currNode();

        if (currNode == depot) {
            // refill when arriving the depot
            route.setDemand(0);

            if (state.getUnassignedTasks().isEmpty()) {
                // if there is no unassigned tasks, then no need to go out again.
                // stay at the depot and close the route
                return;
            }

            // calculate the route-to-task map
            state.calcRouteToTaskMap(route);

            // decide which task to serve next by pilot search
            ReactiveDecisionSituation rds = new ReactiveDecisionSituation(
                    null, route, state);

            Arc nextTask = pilotSearcher.next(rds, policy);

            if (nextTask == null || nextTask.equals(instance.getDepotLoop())) {
                return;
            }

            state.removeUnassignedTasks(nextTask);
            route.setNextTask(nextTask);

            decisionProcess.getEventQueue().add(
                    new PilotSearchServingEvent(route.getCost(), route, nextTask, pilotSearcher));
        }
        else {
            // continue going to the depot if not arrived yet
            int nextNode = graph.getPathTo(currNode, depot);

            // there is no edge failure in expectation

            // add the traverse to the next node
            route.addPilot(nextNode, 0, instance);
            // add a new event
            decisionProcess.getEventQueue().add(
                    new PilotSearchRefillEvent(route.getCost(), route, pilotSearcher));
        }
    }
}
