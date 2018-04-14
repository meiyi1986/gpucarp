package gputils;

import ec.gp.GPNode;
import ec.simple.SimpleEvolutionState;
import gphhucarp.gp.UCARPPrimitiveSet;
import gputils.terminal.PrimitiveSet;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * The evolution state that represents terminals as ERC.
 * This way, it is convenient to handle a variable number (maybe a lot) of terminals.
 * The evolution state stores an array of terminal sets, each for a subpopulation.
 *
 * Created by gphhucarp on 31/03/17.
 */
public abstract class TerminalERCEvolutionState extends SimpleEvolutionState {
    protected int subpops; // the actual number of subpopulations,
                           // the subpops set in the params file becomes the maximal possible number of subpopulations
    protected List<PrimitiveSet> terminalSets;

    public List<PrimitiveSet> getTerminalSets() {
        return terminalSets;
    }

    public void setTerminalSets(List<PrimitiveSet> terminalSets) {
        this.terminalSets = terminalSets;
    }

    /**
     * Get the terminal set of a particular subpopulation.
     * @param index the index of the subpopulation.
     * @return the terminal set of the subpopulation.
     */
    public PrimitiveSet getTerminalSet(int index) {
        return terminalSets.get(index);
    }

    /**
     * Set the terminal set of a particular subpopulation.
     * @param index the index of the subpopulation.
     * @param terminalSet the set terminal set.
     */
    public void setTerminalSet(int index, UCARPPrimitiveSet terminalSet) {
        this.terminalSets.set(index, terminalSet);
    }

    /**
     * Initialize the terminal sets.
     */
    public abstract void initTerminalSets();

    /**
     * Initialise the terminal sets with the names of the variables from a csv file.
     * @param csvFile the csv file, separated by comma. Each line represents the terminal set of a subpopulation.
     * @param dictionary the dictionary of the terminals to be read from the file.
     */
    public void initTerminalSetsFromCsv(File csvFile, UCARPPrimitiveSet dictionary) {
        terminalSets = new ArrayList<>();
        for (int i = 0; i < population.subpops.length; i++)
            terminalSets.add(new UCARPPrimitiveSet());

        BufferedReader br = null;
        String line = "";
        int subpop = 0;

        try {
            br = new BufferedReader(new FileReader(csvFile));
            while ((line = br.readLine()) != null) {
                String[] elements = line.split(",");
                for (String e : elements) {
                    String key = e.trim();
                    terminalSets.get(subpop).add(dictionary.get(key));
                }

                subpop ++;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Uniformly pick a terminal from a particular terminal set.
     * @param index the index of the terminal set.
     * @return the selected terminal, which is a GPNode.
     */
    public GPNode pickTerminalUniform(int index) {
        PrimitiveSet terminalSet = terminalSets.get(index);
        int k = random[0].nextInt(terminalSet.size());
        return terminalSet.get(k);
    }
}
