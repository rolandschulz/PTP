/*******************************************************************************
 * Copyright (c) 2009, 2013 University of Utah School of Computing
 * 50 S Central Campus Dr. 3190 Salt Lake City, UT 84112
 * http://www.cs.utah.edu/formal_verification/
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alan Humphrey - Initial API and implementation
 *    Christopher Derrick - Initial API and implementation
 *    Prof. Ganesh Gopalakrishnan - Project Advisor
 *******************************************************************************/

package org.eclipse.ptp.internal.gem.util;

/**
 * This class represents a CB (completes before) edge. It simply consists of a
 * rank and an index.
 */

public class CB {

	private final int rank;
	private final int index;

	/**
	 * Constructor
	 * 
	 * @param rank
	 *            The rank for this CB.
	 * @param index
	 *            The index for this CB.
	 */
	public CB(int rank, int index) {
		this.rank = rank;
		this.index = index;
	}

	/**
	 * Returns the index of the operation.
	 * 
	 * @param none
	 * @return int The index involved with this CB.
	 */
	public int getIndex() {
		return this.index;
	}

	/**
	 * Returns the rank of the operation.
	 * 
	 * @param none
	 * @return int The rank involved with this CB.
	 */
	public int getRank() {
		return this.rank;
	}

}
