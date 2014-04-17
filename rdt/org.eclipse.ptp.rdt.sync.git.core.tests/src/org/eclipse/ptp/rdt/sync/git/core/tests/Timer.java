/*******************************************************************************
 * Copyright (c) 2014 The University of Tennessee and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Eblen - initial implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.sync.git.core.tests;

/**
 * Simple class for measuring elapsed time.
 */
public class Timer {
	private long startTime;

	/**
	 * Constructor - set start time to current time
	 */
	public Timer() {
		reset();
	}

	/**
	 * Set start time to current time
	 */
	public void reset() {
		startTime = System.nanoTime();
	}

	/**
	 * Report elapsed time since start in nanoseconds
	 * @return elapsed time
	 */
	public long getElapsed() {
		return System.nanoTime() - startTime;
	}
}