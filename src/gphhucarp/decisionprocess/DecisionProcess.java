package gphhucarp.decisionprocess;

import gphhucarp.core.Instance;
import gphhucarp.representation.Solution;
import gphhucarp.representation.route.NodeSeqRoute;
import gphhucarp.representation.route.TaskSeqRoute;
import gphhucarp.decisionprocess.proreactive.ProreativeDecisionProcess;
import gphhucarp.decisionprocess.proreactive.event.ProreactiveServingEvent;
import gphhucarp.decisionprocess.reactive.ReactiveDecisionProcess;
import gphhucarp.decisionprocess.reactive.event.ReactiveRefillEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * An abstract of a decision process. A decision process is a process where
 * vehicles make decisions as they go to serve the tasks of the graph.
 * It includes
 *  - A decision process state: the state of the vehicles and the environment
 *  - An event queue: the events to happen
 *  - A routing policy that makes decisions as the vehicles go.
 *  - A task sequence solution as a predefined plan. This is used for proactive-reactive decision process.
 */

public abstract class DecisionProcess {
    protected DecisionProcessState state; // the state
    protected PriorityQueue<DecisionProcessEvent> eventQueue;
    protected RoutingPolicy routingPolicy;
    protected Solution<TaskSeqRoute> plan;

    public DecisionProcess(DecisionProcessState state,
                           PriorityQueue<DecisionProcessEvent> eventQueue,
                           RoutingPolicy routingPolicy,
                           Solution<TaskSeqRoute> plan) {
        this.state = state;
        this.eventQueue = eventQueue;
        this.routingPolicy = routingPolicy;
        this.plan = plan;
    }

    public DecisionProcessState getState() {
        return state;
    }

    public PriorityQueue<DecisionProcessEvent> getEventQueue() {
        return eventQueue;
    }

    public RoutingPolicy getRoutingPolicy() {
        return routingPolicy;
    }

    public void setRoutingPolicy(RoutingPolicy routingPolicy) {
        this.routingPolicy = routingPolicy;
    }

    public Solution<TaskSeqRoute> getPlan() {
        return plan;
    }

    public void setPlan(Solution<TaskSeqRoute> plan) {
        this.plan = plan;
    }

    /**
     * Initialise a reactive decision process from an instance and a routing policy.
     * @param instance the given instance.
     * @param seed the seed to sample the random variables.
     * @param routingPolicy the given policy.
     * @return the initial reactive decision process.
     */
    public static ReactiveDecisionProcess initReactive(Instance instance,
                                                       long seed,
                                                       RoutingPolicy routingPolicy) {
        DecisionProcessState state = new DecisionProcessState(instance, seed);
        PriorityQueue<DecisionProcessEvent> eventQueue = new PriorityQueue<>();
        for (NodeSeqRoute route : state.getSolution().getRoutes())
            eventQueue.add(new ReactiveRefillEvent(0, route));

        return new ReactiveDecisionProcess(state, eventQueue, routingPolicy);
    }

    /**
     * Initialise a proactive-reactive decision process from an instance, a routing policy and a plan.
     * @param instance the given instance.
     * @param routingPolicy the given policy.
     * @param plan the given plan (a task sequence solution).
     * @return the initial proactive-reactive decision process.
     */
    public static ProreativeDecisionProcess initProreactive(Instance instance,
                                                            long seed,
                                                            RoutingPolicy routingPolicy,
                                                            Solution<TaskSeqRoute> plan) {
        DecisionProcessState state = new DecisionProcessState(instance, seed, plan.getRoutes().size());
        PriorityQueue<DecisionProcessEvent> eventQueue = new PriorityQueue<>();
        for (int i = 0; i < plan.getRoutes().size(); i++)
            eventQueue.add(new ProreactiveServingEvent(0,
                    state.getSolution().getRoute(i), plan.getRoute(i), 0));

        return new ProreativeDecisionProcess(state, eventQueue, routingPolicy, plan);
    }

    /**
     * Run the decision process.
     */
    public void run() {
        // first sample the random variables by the seed.
        state.getInstance().setSeed(state.getSeed());

        // trigger the events.
        while (!eventQueue.isEmpty()) {
            DecisionProcessEvent event = eventQueue.poll();
            event.trigger(this);
        }
    }

    /**
     * Reset the decision process.
     * This is done by reseting the decision process state and event queue.
     */
    public abstract void reset();
}
