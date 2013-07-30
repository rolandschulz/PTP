/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.lml.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Wrapper for job status which extracts the persistent properties and saves them or reloads them from a memento.
 * 
 * @author arossi
 * 
 */

public class JobStatusData {
	private static final String JOB_OUTERR_READY = "JOB_OUTERR_READY";//$NON-NLS-1$

	public static final String SUBMITTED = "SUBMITTED";//$NON-NLS-1$
	public static final String RUNNING = "RUNNING";//$NON-NLS-1$
	public static final String COMPLETED = "COMPLETED";//$NON-NLS-1$
	public static final String FAILED = "FAILED";//$NON-NLS-1$
	public static final String CANCELED = "CANCELED";//$NON-NLS-1$

	public static final String SYSTEM_TYPE_ATTR = "systemType";//$NON-NLS-1$
	public static final String STDOUT_REMOTE_FILE_ATTR = "stdoutRemotePath";//$NON-NLS-1$
	public static final String STDERR_REMOTE_FILE_ATTR = "stderrRemotePath";//$NON-NLS-1$
	public static final String INTERACTIVE_ATTR = "interactive";//$NON-NLS-1$;
	public static final String STATE_ATTR = "state";//$NON-NLS-1$;
	public static final String STATE_DETAIL_ATTR = "stateDetail";//$NON-NLS-1$;
	public static final String OID_ATTR = "oid";//$NON-NLS-1$;
	public static final String QUEUE_NAME_ATTR = "queueName";//$NON-NLS-1$;
	public static final String OWNER_ATTR = "owner";//$NON-NLS-1$;
	public static final String CONTROL_ID_ATTR = "controlId";//$NON-NLS-1$

	private static boolean detailIsFinal(String detail) {
		return CANCELED.equals(detail) || FAILED.equals(detail) || JOB_OUTERR_READY.equals(detail);
	}

	private boolean fOutReady = false;
	private boolean fErrReady = false;
	private boolean fRemoved = false;

	private final Map<String, String> fAttrs = new HashMap<String, String>();

	private final String fJobId;

	public JobStatusData(String jobId, String[][] attrs) {
		fJobId = jobId;

		setState(SUBMITTED);
		setStateDetail(SUBMITTED);

		for (final String[] attr : attrs) {
			fAttrs.put(attr[0], attr[1]);
		}
	}

	/**
	 * @return if error file is ready
	 */
	public boolean getErrReady() {
		return fErrReady;
	}

	/**
	 * @return job id
	 */
	public String getJobId() {
		return fJobId;
	}

	public Set<String> getKeys() {
		return fAttrs.keySet();
	}

	/**
	 * @return if output file is ready
	 */
	public boolean getOutReady() {
		return fOutReady;
	}

	/**
	 * @return fState, or empty string if none.
	 */
	public String getState() {
		return fAttrs.get(STATE_ATTR);
	}

	/**
	 * @return fState, or empty string if none.
	 */
	public String getStateDetail() {
		return fAttrs.get(STATE_DETAIL_ATTR);
	}

	/**
	 * @param key
	 * @return
	 */
	public String getString(String key) {
		return fAttrs.get(key);
	}

	/**
	 * @return job is completed
	 */
	public boolean isCompleted() {
		return getState().equals(COMPLETED);
	}

	/**
	 * @return job was submitted interactively
	 */
	public boolean isInteractive() {
		final String interactive = fAttrs.get(INTERACTIVE_ATTR);
		if (interactive != null) {
			try {
				return Boolean.parseBoolean(interactive);
			} catch (final Exception e) {
				// Default to false
			}
		}
		return false;
	}

	/**
	 * @return job has been removed by user
	 */
	public boolean isRemoved() {
		return fRemoved;
	}

	/**
	 * @param key
	 * @param value
	 */
	public void putString(String key, String value) {
		fAttrs.put(key, value);
	}

	/**
	 * @param errReady
	 *            error file is ready
	 */
	public void setErrReady(boolean errReady) {
		fErrReady = errReady;
	}

	/**
	 * @param oid
	 *            the oid
	 */
	public void setOid(String oid) {
		fAttrs.put(OID_ATTR, oid);
	}

	/**
	 * @param outReady
	 *            output file is ready
	 */
	public void setOutReady(boolean outReady) {
		fOutReady = outReady;
	}

	/**
	 * Set job as removed
	 */
	public void setRemoved() {
		fRemoved = true;
	}

	/**
	 * @param state
	 *            the state
	 */
	public void setState(String state) {
		fAttrs.put(STATE_ATTR, state);
	}

	/**
	 * @param stateDetail
	 *            the state detail
	 */
	public void setStateDetail(String stateDetail) {
		fAttrs.put(STATE_DETAIL_ATTR, stateDetail);
	}

	/**
	 * Replace with updated status.
	 * 
	 * @param status
	 */
	public void updateState(String state, String stateDetail) {
		if (!detailIsFinal(getStateDetail())) {
			setState(state);
			setStateDetail(stateDetail);
		}
	}
}
