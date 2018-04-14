package gphhucarp.decisionprocess.proreactive.event;

import gphhucarp.core.Arc;
import gphhucarp.core.Graph;
import gphhucarp.core.Instance;
import gphhucarp.representation.route.NodeSeqRoute;
import gphhucarp.representation.route.TaskSeqRoute;
import gphhucarp.decisionprocess.DecisionProcess;
import gphhucarp.decisionprocess.DecisionProcessEvent;
import gphhucarp.decisionprocess.DecisionProcessState;
import gphhucarp.decisionprocess.RoutingPolicy;

public class ProreactiveRefillThenServeEvent extends DecisionProcessEvent {
    private NodeSeqRoute route;
    private TaskSeqRoute plan;
    private int nextTaskIndex; // the index of the next task in the plan

    public ProreactiveRefillThenServeEvent(double time,
                                           NodeSeqRoute route, TaskSeqRoute plan, int nextTaskIndex) {
        super(time);
        this.route = route;
        this.plan = plan;
        this.nextTaskIndex = nextTaskIndex;
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
            // this is essentially a reactive serving event
            decisionProcess.getEventQueue().add(
                    new ProreactiveServingEvent(route.getCost(), route, plan, nextTaskIndex));
        }
        else {
            // continue going to the depot if not arrived yet
            int nextNode = graph.getPathTo(currNode, depot);

            // check the accessibility of all the arcs going out from the arrived node.
            boolean edgeFailure = false; // edge failure: next node is not accessible.
            for (Arc arc : graph.getOutNeighbour(currNode)) {
                if (instance.getActDeadheadingCost(arc) == Double.POSITIVE_INFINITY) {
                    graph.updateEstCostMatrix(arc.getFrom(), arc.getTo(), Double.POSITIVE_INFINITY);

                    if (arc.getTo() == nextNode)
                        edgeFailure = true;
                }
            }

            // recalculate the shortest path based on the new cost matrix.
            // update the next node in the new shortest path.
            if (edgeFailure) {
                graph.recalcEstDistanceBetween(currNode, depot);
                nextNode = graph.getPathTo(currNode, depot);
            }

            // add the traverse to the next node
            route.add(nextNode, 0, instance);
            // add a new event
            decisionProcess.getEventQueue().add(
                    new ProreactiveRefillThenServeEvent(route.getCost(), route, plan, nextTaskIndex));
        }
    }
}
