package gputils.terminal;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.*;
import ec.util.Code;
import ec.util.DecodeReturn;
import gputils.DoubleData;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * A double ERC, basically copied from ec.app.regression.func.RegERC.
 * The most important part is the resetNode() method,
 * which may need to be adjusted.
 *
 * Created by YiMei on 2/10/16.
 */
public class DoubleERC extends ERC {

    public DoubleERC() {
        children = new GPNode[0];
    }

    public double value;

    public void resetNode(final EvolutionState state, final int thread) {
//        value = state.random[thread].nextDouble() * 2 - 1.0;
        value = state.random[thread].nextDouble();
    }

    public int nodeHashCode() {
        // a reasonable hash code
        long l = Double.doubleToLongBits(value);
        int iUpper = (int)(l & 0x00000000FFFFFFFF);
        int iLower = (int)(l >>> 32);
        return this.getClass().hashCode() + iUpper + iLower;
    }

    public boolean nodeEquals(final GPNode node) {
        // check first to see if we're the same kind of ERC --
        // won't work for subclasses; in that case you'll need
        // to change this to isAssignableTo(...)
        if (this.getClass() != node.getClass()) return false;
        // now check to see if the ERCs hold the same value
        return (((DoubleERC)node).value == value);
    }

    public void readNode(final EvolutionState state, final DataInput dataInput) throws IOException {
        value = dataInput.readDouble();
    }

    public void writeNode(final EvolutionState state, final DataOutput dataOutput) throws IOException {
        dataOutput.writeDouble(value);
    }

    public String encode() {
        return Code.encode(value);
    }

    public boolean decode(DecodeReturn dret) {
        // store the position and the string in case they
        // get modified by Code.java
        int pos = dret.pos;
        String data = dret.data;

        // decode
        Code.decode(dret);

        if (dret.type != DecodeReturn.T_DOUBLE) // uh oh!
        {
            // restore the position and the string; it was an error
            dret.data = data;
            dret.pos = pos;
            return false;
        }

        // store the data
        value = dret.d;
        return true;
    }

    public String toStringForHumans() {
        return "" + value;
    }

    @Override
    public String toString() {
        return toStringForHumans();
    }

    @Override
    public void eval(final EvolutionState state,
                     final int thread,
                     final GPData input,
                     final ADFStack stack,
                     final GPIndividual individual,
                     final Problem problem) {
        DoubleData a = ((DoubleData)(input));
        a.value = value;
    }
}
