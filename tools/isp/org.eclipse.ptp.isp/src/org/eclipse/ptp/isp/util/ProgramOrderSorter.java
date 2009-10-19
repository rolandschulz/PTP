/*******************************************************************************
 * Copyright (c) 2009 University of Utah School of Computing
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

package org.eclipse.ptp.isp.util;

import java.util.Comparator;

/**
 * java.lang.Comparator: int compare(Envelope env1, env2)
 * 
 * This method compares two Envelopes (env1 and env2) by program order.
 * (Integer) casts are made to use the Integer class autobox/unbox feature as
 * well as its compareTo method.
 * 
 * Returned int value has the following meanings:
 * 
 * 1. positiveâ env1 is greater than env2 
 * 2. zeroâ env1 equals to env2 
 * 3. negativeâ env1 is less than env2
 */
public class ProgramOrderSorter implements Comparator<Envelope> {

	/**
	 * Compares the two Envelopes passed in by Program order
	 * 
	 * @param Envelope
	 *            env1, the first Envelope
	 * @param Envelope
	 *            env1, the second Envelope
	 * @return the comparison of the two parameters
	 */
	public int compare(Envelope env1, Envelope env2) {
		if (env1.getOrderIndex() == env2.getOrderIndex()) {
			if (env1.getRank() != env2.getRank()) {
				return ((Integer) env1.getRank()).compareTo((Integer) env2
						.getRank());
			} else {
				return ((Integer) env1.getIndex()).compareTo((Integer) env2
						.getIndex());
			}
		} else {
			return ((Integer) env1.getOrderIndex()).compareTo((Integer) env2
					.getOrderIndex());
		}
	}

}
