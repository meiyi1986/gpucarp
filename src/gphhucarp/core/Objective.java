package gphhucarp.core;

import gphhucarp.decisionprocess.RoutingPolicy;
import gphhucarp.decisionprocess.routingpolicy.PathScanning5Policy;

import java.util.HashMap;
import java.util.Map;

/**
 * An enumeration for all the possible objectives.
 *
 * Created by gphhucarp on 31/08/17.
 */
public enum Objective {

    TOTAL_COST("total-cost"),
    MAX_ROUTE_COST("max-route-cost");

    private final String name;

    Objective(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    // Reverse-lookup map
    private static final Map<String, Objective> lookup = new HashMap<>();

    static {
        for (Objective a : Objective.values()) {
            lookup.put(a.getName(), a);
        }
    }

    public static Objective get(String name) {
        return lookup.get(name);
    }

    /**
     * The reference reactive routing policy to calculate the reference objective values.
     * @return the reference reactive routing policy.
     */
    public static RoutingPolicy refReactiveRoutingPolicy() {
        return new PathScanning5Policy();
    }
}
