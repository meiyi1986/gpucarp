package gphhucarp.gp.io;

import java.util.HashMap;
import java.util.Map;

/**
 * Private enumeration of different fitness.
 * Different fitness types will be read from the out.stat file differently.
 */

public enum FitnessType {
    SIMPLE_FITNESS("simple-fitness"),
    DIMENSION_AWARE_FITNESS("dimension-aware-fitness");

    private final String name;

    FitnessType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    // Reverse-lookup map
    private static final Map<String, FitnessType> lookup = new HashMap<>();

    static {
        for (FitnessType a : FitnessType.values()) {
            lookup.put(a.getName(), a);
        }
    }

    public static FitnessType get(String name) {
        return lookup.get(name);
    }
}
