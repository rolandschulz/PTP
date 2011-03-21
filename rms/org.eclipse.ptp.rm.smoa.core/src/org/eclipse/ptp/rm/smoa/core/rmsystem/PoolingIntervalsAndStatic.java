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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ptp.core.attributes.StringAttributeDefinition;

import com.smoa.comp.sdk.types.SMOAActivityStatus;

/**
 * Static class holding information about pooling intervals of task and output
 * status.
 * 
 * This class also holds some static methods i order to make
 * {@link SMOAResourceManager} thinner.
 */

/*
 * When this functionality has been part of SMOAResourceManager class, a
 * ClassCircularityExcepthion has been thrown under certain circumstances.
 */
public class PoolingIntervalsAndStatic {

	/** All possible states returned by getBESState and getBESSubState */
	public static enum SMOAJobState {
		Cancelled, Finished, Failed, Executing, Stage_in, Stage_out, Queued, Held, Suspended
	}

	/** Default interval for checking job state. */
	public static final int DEFAULT_POOLING_STATE = 10000;

	/** Default interval for checking job out and err state. */
	public static final int DEFAULT_POOLING_OUT = 15000;
	/** Interval for checking job state. Global for all jobs. */
	private static int PoolingIntervalState = DEFAULT_POOLING_STATE;

	/** Interval for checking job out and err state. Global for all jobs. */
	private static int PoolingIntervalOut = DEFAULT_POOLING_OUT;

	/** Definition of a attribute holding error message */
	public static StringAttributeDefinition exceptionAttrDef = new StringAttributeDefinition(
			"exception", Messages.PoolingIntervalsAndStatic_JobSubmissionFailed, Messages.PoolingIntervalsAndStatic_JobSubmissionFailed, //$NON-NLS-1$
			true, ""); //$NON-NLS-1$

	private static final Map<String, SMOAJobState> besStateNameToEnum = new HashMap<String, SMOAJobState>();

	private static final Map<String, SMOAJobState> besSubStateNameToEnum = new HashMap<String, SMOAJobState>();

	static {
		besStateNameToEnum.put(
				SMOAActivityStatus.SMOA_ACTIVITY_STATE_CANCELLED,
				SMOAJobState.Cancelled);
		besStateNameToEnum.put(SMOAActivityStatus.SMOA_ACTIVITY_STATE_FAILED,
				SMOAJobState.Failed);
		besStateNameToEnum.put(SMOAActivityStatus.SMOA_ACTIVITY_STATE_FINISHED,
				SMOAJobState.Finished);
	}

	static {
		besSubStateNameToEnum.put(
				SMOAActivityStatus.SMOA_ACTIVITY_STATE_EXECUTING,
				SMOAJobState.Executing);
		besSubStateNameToEnum.put(SMOAActivityStatus.SMOA_ACTIVITY_STATE_HELD,
				SMOAJobState.Held);
		besSubStateNameToEnum.put(
				SMOAActivityStatus.SMOA_ACTIVITY_STATE_QUEUED,
				SMOAJobState.Queued);
		besSubStateNameToEnum.put(
				SMOAActivityStatus.SMOA_ACTIVITY_STATE_STAGE_IN,
				SMOAJobState.Stage_in);
		besSubStateNameToEnum.put(
				SMOAActivityStatus.SMOA_ACTIVITY_STATE_STAGE_OUT,
				SMOAJobState.Stage_out);
		besSubStateNameToEnum.put(
				SMOAActivityStatus.SMOA_ACTIVITY_STATE_SUSPENDED,
				SMOAJobState.Suspended);
	}

	/** Maps BES state name to SMOA JobState */
	public static SMOAJobState getEquivalentJobState(SMOAActivityStatus status) {
		final SMOAJobState result = besStateNameToEnum.get(status.getBESStateName());
		if (result != null) {
			return result;
		}
		return besSubStateNameToEnum.get(status.getBESSubStateName());
	}

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
