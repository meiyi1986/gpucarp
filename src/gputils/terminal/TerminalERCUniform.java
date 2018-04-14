package gputils.terminal;

import ec.EvolutionState;
import ec.gp.ERC;
import ec.util.Parameter;
import gputils.TerminalERCEvolutionState;
import gputils.terminal.TerminalERC;

/**
 * The terminal ERC with uniform selection.
 *
 * @author gphhucarp
 */

public class TerminalERCUniform extends TerminalERC {

    @Override
    public void setup(final EvolutionState state, final Parameter base) {
        super.setup(state, base);

        terminal = null;
    }

    @Override
    public void resetNode(EvolutionState state, int thread) {
        terminal = ((TerminalERCEvolutionState)state).pickTerminalUniform(subpop);

        if (terminal instanceof ERC) {
            terminal = terminal.lightClone();
            terminal.resetNode(state, thread);
        }
    }

    @Override
    public void mutateERC(EvolutionState state, int thread) {
        resetNode(state, thread);
    }
}
