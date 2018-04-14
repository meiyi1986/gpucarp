package gphhucarp.algorithm.edasls;

import ec.DefaultsForm;
import ec.util.Parameter;

public class EDASLSDefaults implements DefaultsForm {

    public static final String P_EDASLS = "edasls";

    /** Returns the default base. */
    public static final Parameter base() {
        return new Parameter(P_EDASLS);
    }
}
