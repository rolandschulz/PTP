/******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 *****************************************************************************/
package org.eclipse.ptp.utils.ui.swt;

/**
 * @author Richard Maciel, Daniel Ferber
 *
 */
public class SpinnerMold extends GenericControlMold {

	int min = 0;
	int max = 100;
	int increment = 10;
	
	public SpinnerMold(int bitmask, String label, int min, int max, int inc) {
		super(bitmask, label);
		this.min = min;
		this.max = max;
		increment = inc;		
	}

	/** Increment. */
	public int getIncrement() {
		return increment;
	}

	/** Increment. */
	public void setIncrement(int increment) {
		this.increment = increment;
	}

	/** Maximum value. */
	public int getMax() {
		return max;
	}

	/** Maximum value. */
	public void setMax(int max) {
		this.max = max;
	}

	/** Minimum value. */
	public int getMin() {
		return min;
	}

	/** Minimum value. */
	public void setMin(int min) {
		this.min = min;
	}

}
