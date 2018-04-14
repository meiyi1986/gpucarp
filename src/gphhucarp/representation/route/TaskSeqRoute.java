package gphhucarp.representation.route;

import gphhucarp.core.Arc;
import gphhucarp.core.Instance;

import java.util.LinkedList;
import java.util.List;

/**
 * A task sequence route is a sequence of tasks.
 * It must start and end at the depot.
 * Its total demand cannot exceed its capacity.
 *
 * Created by gphhucarp on 25/08/17.
 */
public class TaskSeqRoute extends Route {
    protected List<Arc> taskSequence;
    protected int currNode; // the current node, which is the depot if the task sequence is empty

    public TaskSeqRoute(double capacity, double demand, double cost,
                        List<Arc> taskSequence, int currNode) {
        super(capacity, demand, cost);
        this.taskSequence = taskSequence;
        this.currNode = currNode;
    }

    public List<Arc> getTaskSequence() {
        return taskSequence;
    }

    public Arc get(int index) {
        return taskSequence.get(index);
    }

    public int size() {
        return taskSequence.size();
    }

    /**
     * Add a task of an instance to the end of the task sequence route.
     * @param task the task to be added.
     * @param instance the instance providing the distance matrix.
     */
    public void add(Arc task, Instance instance) {
        taskSequence.add(task);
        demand += instance.getActDemand(task);
        cost += instance.getActDistance(currNode, task.getFrom()) + task.getServeCost();
        currNode = task.getTo();
    }

    /**
     * An initial task sequence route for an instance.
     * It starts from the depot loop.
     * @param instance the instance.
     * @return An initial task sequence route starting from the depot.
     */
    public static TaskSeqRoute initial(Instance instance) {
        TaskSeqRoute initialRoute = new TaskSeqRoute(instance.getCapacity(), 0, 0,
                new LinkedList<>(), instance.getDepot());
        initialRoute.add(instance.getDepotLoop(), instance);

        return initialRoute;
    }

    @Override
    public void reset(Instance instance) {
        demand = 0;
        cost = 0;
        taskSequence.clear();
        add(instance.getDepotLoop(), instance);
    }

    @Override
    public int currNode() {
        return currNode;
    }

    public void setCurrNode(int node) {
        this.currNode = node;
    }

    @Override
    public String toString() {
        String str = taskSequence.get(0).toSimpleString();

        for (int j = 1; j < taskSequence.size(); j++) {
            str += " -> " + taskSequence.get(j).toSimpleString();
        }

        return str;
    }

    @Override
    public Route clone() {
        return new TaskSeqRoute(capacity, demand, cost,
                new LinkedList<>(taskSequence), currNode);
    }
}
