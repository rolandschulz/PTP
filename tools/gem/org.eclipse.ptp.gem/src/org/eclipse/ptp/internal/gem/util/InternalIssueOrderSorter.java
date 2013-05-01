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

import java.util.Comparator;

/**
 * java.lang.Comparator: int compare(Envelope env1, env2)
 * 
 * This method compares two Envelopes (env1 and env2) by internal issue order.
 * (Integer) casts are made to use the Integer class autobox/unbox feature as
 * well as its compareTo() method.
 * 
 * Returned int value has the following meanings:
 * 
 * 1. positive: env1 is greater than env2 2. zero: env1 equals to env2 3.
 * negative: env1 is less than env2
 */
public class InternalIssueOrderSorter implements Comparator<Envelope> {

	/**
	 * Compares the two Envelopes passed in by Issue Order.
	 * 
	 * @param env1
	 *            The first Envelope.
	 * @param env2
	 *            The second Envelope.
	 * @return int The comparison of the two parameters... positive: env1 is
	 *         greater than env2, zero: env1 equals to env2 negative: env1 is
	 *         less than env2
	 */
	public int compare(Envelope env1, Envelope env2) {
		if (env1.getIssueIndex() == -1 && env2.getIssueIndex() == -1) {
			return ((Integer) env1.getOrderIndex()).compareTo(env2.getOrderIndex());
		} else if (env1.getIssueIndex() == -1) {
			return 1;
		} else if (env2.getIssueIndex() == -1) {
			return -1;
		} else {
			return ((Integer) env1.getIssueIndex()).compareTo(env2.getIssueIndex());
		}
	}

}
