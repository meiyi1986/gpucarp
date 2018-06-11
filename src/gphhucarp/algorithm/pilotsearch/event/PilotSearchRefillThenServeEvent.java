package gphhucarp.algorithm.pilotsearch.event;

import gphhucarp.algorithm.pilotsearch.PilotSearcher;
import gphhucarp.core.Arc;
import gphhucarp.core.Graph;
import gphhucarp.core.Instance;
import gphhucarp.decisionprocess.DecisionProcess;
import gphhucarp.decisionprocess.DecisionProcessEvent;
import gphhucarp.decisionprocess.DecisionProcessState;
import gphhucarp.decisionprocess.RoutingPolicy;
import gphhucarp.representation.route.NodeSeqRoute;

/**
 * This is the same as ReactiveRefillThenServeEvent, but interact with other
 * pilot search events.
 */

public class PilotSearchRefillThenServeEvent extends DecisionProcessEvent {

    private NodeSeqRoute route;
    private Arc nextTask;
    private PilotSearcher pilotSearcher;

    public PilotSearchRefillThenServeEvent(double time, NodeSeqRoute route,
                                           Arc nextTask, PilotSearcher pilotSearcher) {
        super(time);
        this.route = route;
        this.nextTask = nextTask;
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

            // now going back to continue the failed service
            // this is essentially a serving event
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
                    new PilotSearchRefillThenServeEvent(route.getCost(), route, nextTask, pilotSearcher));
        }
    }
}
