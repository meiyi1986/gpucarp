package gphhucarp.decisionprocess.reactive;

import gphhucarp.decisionprocess.DecisionProcess;
import gphhucarp.decisionprocess.DecisionProcessEvent;
import gphhucarp.decisionprocess.DecisionProcessState;
import gphhucarp.decisionprocess.RoutingPolicy;
import gphhucarp.decisionprocess.reactive.event.ReactiveRefillEvent;
import gphhucarp.representation.route.NodeSeqRoute;

import java.util.PriorityQueue;

/**
 * The reactive decision process builds the solution in real time.
 * It is actually a decision process that adds one task at the end of the current route at a time.
 * It assumes that the actual serving/deadheading cost of an arc is realised after it is served/traversed.
 * The actual demand of a task is realised after it is served.
 *
 * Created by gphhucarp on 25/08/17.
 */
public class ReactiveDecisionProcess extends DecisionProcess {

    public ReactiveDecisionProcess(DecisionProcessState state,
                                   PriorityQueue<DecisionProcessEvent> eventQueue,
                                   RoutingPolicy routingPolicy) {
        super(state, eventQueue, routingPolicy, null);
    }

    @Override
    public void reset() {
        state.reset();
        eventQueue.clear();
        for (NodeSeqRoute route : state.getSolution().getRoutes())
            eventQueue.add(new ReactiveRefillEvent(0, route));
    }

//    public List<DecisionSituation> recordFirstDecisionSituations(int numDecisionSituations) {
//        List<DecisionSituation> decisionSituations = new ArrayList<>();
//
//        while (!eventQueue.isEmpty()) {
//            DecisionProcessEvent event = eventQueue.poll();
//
//            event.trigger(this);
//        }
//    }
}
