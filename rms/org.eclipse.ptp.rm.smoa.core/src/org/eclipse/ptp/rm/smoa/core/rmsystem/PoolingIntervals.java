/*******************************************************************************
 * Copyright (c) 2010 Poznan Supercomputing and Networking Center
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jan Konczak (PSNC) - initial implementation
 ******************************************************************************/

package org.eclipse.ptp.rm.smoa.core.rmsystem;

/**
 * Static class holding information about pooling intervals of task and output
 * status.
 */

/*
 * When this functionality has been part of SMOAResourceManager class, a
 * ClassCircularityExcepthion has been thrown under certain circumstances.
 */
public class PoolingIntervals {

	/** Default interval for checking job state. */
	public static final int DEFAULT_POOLING_STATE = 10000;
	/** Default interval for checking job out and err state. */
	public static final int DEFAULT_POOLING_OUT = 15000;

	/** Interval for checking job state. Global for all jobs. */
	private static int PoolingIntervalState = DEFAULT_POOLING_STATE;
	/** Interval for checking job out and err state. Global for all jobs. */
	private static int PoolingIntervalOut = DEFAULT_POOLING_OUT;

	/** Retrieves value of the output checking interval */
	public static int getPoolingIntervalOut() {
		return PoolingIntervalOut;
	}

	/** Retrieves value of the job state checking interval */
	public static int getPoolingIntervalTask() {
		return PoolingIntervalState;
	}

	/** How often the output will be checked (but not less than 0.1s) */
	public static void setPoolingIntervalOut(int newVal) {
		if (newVal >= 100) {
			PoolingIntervalOut = newVal;
		}
	}

	/** How often the job state will be checked (but not less than 0.1s) */
	public static void setPoolingIntervalTask(int newVal) {
		if (newVal >= 100) {
			PoolingIntervalState = newVal;
		}
	}

}
