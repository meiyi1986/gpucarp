package gphhucarp.core;

import gphhucarp.representation.route.NodeSeqRoute;

/**
 * A route with distance to a node.
 * The distance is from the current node of the route to the node.
 * The natural comparator prefers shorter distance.
 */

public class RouteWithDistance implements Comparable<RouteWithDistance> {
    private NodeSeqRoute route;
    private double distance;

    public RouteWithDistance(NodeSeqRoute route, double distance) {
        this.route = route;
        this.distance = distance;
    }

    public NodeSeqRoute getRoute() {
        return route;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    @Override
    public int compareTo(RouteWithDistance o) {
        return Double.compare(distance, o.distance);
    }
}
