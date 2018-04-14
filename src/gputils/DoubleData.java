/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package gputils;

import ec.gp.*;

/**
 * The GPData that stores double value.
 *
 * @author gphhucarp
 *
 */

public class DoubleData extends GPData {

    /**
	 *
	 */
	private static final long serialVersionUID = 1L;


	public double value;    // return value

    public void copyTo(final GPData gpd) {
    	((DoubleData)gpd).value = value;
    }
}


