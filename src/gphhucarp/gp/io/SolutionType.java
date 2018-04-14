package gphhucarp.gp.io;

import java.util.HashMap;
import java.util.Map;

/**
 * Private enumeration of different solution types.
 * Different solution types will be read from the out.stat files differently.
 */

public enum SolutionType {

    SIMPLE_SOLUTION("simple-solution"),
    CC_SOLUTION("cc-solution"),
    RF_SOLUTION("rf-solution");

    private final String name;

    SolutionType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    // Reverse-lookup map
    private static final Map<String, SolutionType> lookup = new HashMap<>();

    static {
        for (SolutionType a : SolutionType.values()) {
            lookup.put(a.getName(), a);
        }
    }

    public static SolutionType get(String name) {
        return lookup.get(name);
    }
}
