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

/**
 * A proactive-reactive serving event occurs whenever a vehicle arrives a node
 * on the way to serve the next task.
 * It follows the plan, and the next task has the index nextTaskIndex in the plan.
 */

public class ProreactiveServingEvent extends DecisionProcessEvent {
    private NodeSeqRoute route;
    private TaskSeqRoute plan;
    private int nextTaskIndex; // the index of the next task in the plan

    public ProreactiveServingEvent(double time,
                                   NodeSeqRoute route, TaskSeqRoute plan, int nextTaskIndex) {
        super(time);
        this.route = route;
        this.plan = plan;
        this.nextTaskIndex = nextTaskIndex;
    }

    @Override
    public void trigger(DecisionProcess decisionProcess) {
        Arc nextTask = plan.get(nextTaskIndex);
        RoutingPolicy policy = decisionProcess.getRoutingPolicy();
        DecisionProcessState state = decisionProcess.getState();
        Instance instance = state.getInstance();
        Graph graph = instance.getGraph();
        int depot = instance.getDepot();

        int currNode = route.currNode();

        // refill the capacity if the current node is the depot
        if (currNode == depot)
            route.setDemand(0);

        if (currNode == nextTask.getFrom()) {
            // skip serving the depot loop, direct serve the next task
            if (currNode == depot && nextTask.getTo() == depot) {
                if (nextTaskIndex == 0) {
                    nextTaskIndex ++;

                    decisionProcess.getEventQueue().add(
                            new ProreactiveServingEvent(route.getCost(), route, plan, nextTaskIndex));

                }

                return;
            }

            // start serving the next task if it arrives its head node
            double remainingCapacity = instance.getCapacity() - route.getDemand();
            double remainingDemand = instance.getActDemand(nextTask) * nextTask.getRemainingDemandFraction();

            if (remainingDemand > remainingCapacity) {
                // a route failure occurs, refill and then come back
                double servedFraction = remainingCapacity / remainingDemand;

                // add the partial service to the route
                route.add(nextTask.getTo(), servedFraction, instance);
                // update the remaining demand fraction of the task
                nextTask.setRemainingDemandFraction(nextTask.getRemainingDemandFraction()-servedFraction);
                // add a new event: go to the depot to refill, and come back to
                // continue the failed service.
                decisionProcess.getEventQueue().add(
                        new ProreactiveRefillThenServeEvent(route.getCost(), route, plan, nextTaskIndex));
            }
            else {
                // no route failure occurs, complete the service successfully
                route.add(nextTask.getTo(), nextTask.getRemainingDemandFraction(), instance);
                // remove the task from the remaining tasks
                decisionProcess.getState().removeRemainingTasks(nextTask);

                // if all the planned tasks have been completed, close this route
                if (nextTaskIndex == plan.size()-1)
                    return;

                // go ahead to the next task
                nextTaskIndex ++;
                nextTask = plan.get(nextTaskIndex);

                boolean continueService = policy.continueService(nextTask, route, state);

                if (continueService) {
                    // if continue the service, then go to the next task
                    decisionProcess.getEventQueue().add(
                            new ProreactiveServingEvent(route.getCost(), route, plan, nextTaskIndex));
                }
                else {
                    // go back to refill, and then go to serve the next task
                    decisionProcess.getEventQueue().add(
                            new ProreactiveRefillThenServeEvent(route.getCost(), route, plan, nextTaskIndex));
                }
            }
        }
        else {
            // go to the next node if has not arrived the target node yet
            int nextNode = graph.getPathTo(currNode, nextTask.getFrom());

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
                graph.recalcEstDistanceBetween(currNode, nextTask.getFrom());
                nextNode = graph.getPathTo(currNode, nextTask.getFrom());
            }

            // add the traverse to the next node
            route.add(nextNode, 0, instance);
            // add a new event
            decisionProcess.getEventQueue().add(
                    new ProreactiveServingEvent(route.getCost(), route, plan, nextTaskIndex));
        }
    }
}
