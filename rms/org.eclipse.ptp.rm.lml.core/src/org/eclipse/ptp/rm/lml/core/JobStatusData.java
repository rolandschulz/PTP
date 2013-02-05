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

	public static final String JOB_ID_ATTR = "jobId";//$NON-NLS-1$
	public static final String CONTROL_TYPE_ATTR = "controlType";//$NON-NLS-1$
	public static final String MONITOR_TYPE_ATTR = "monitorType";//$NON-NLS-1$
	public static final String STDOUT_REMOTE_FILE_ATTR = "stdoutRemotePath";//$NON-NLS-1$
	public static final String STDERR_REMOTE_FILE_ATTR = "stderrRemotePath";//$NON-NLS-1$
	public static final String INTERACTIVE_ATTR = "interactive";//$NON-NLS-1$;
	public static final String STATE_ATTR = "state";//$NON-NLS-1$;
	public static final String STATE_DETAIL_ATTR = "stateDetail";//$NON-NLS-1$;
	public static final String OID_ATTR = "oid";//$NON-NLS-1$;
	public static final String QUEUE_NAME_ATTR = "queueName";//$NON-NLS-1$;
	public static final String OWNER_ATTR = "owner";//$NON-NLS-1$;
	public static final String REMOTE_SERVICES_ID_ATTR = "remoteServicesId";//$NON-NLS-1$;
	public static final String CONNECTION_NAME_ATTR = "connectionName";//$NON-NLS-1$;

	private static boolean detailIsFinal(String detail) {
		return CANCELED.equals(detail) || FAILED.equals(detail) || JOB_OUTERR_READY.equals(detail);
	}

	private boolean fOutReady = false;
	private boolean fErrReady = false;
	private boolean fRemoved = false;

	private final Map<String, String> fAttrs = new HashMap<String, String>();

	/**
	 * Store key value pairs for this job
	 */
	private final Map<String, String> jobData = new HashMap<String, String>();

	public JobStatusData(String[][] attrs) {
		setState(SUBMITTED);
		setStateDetail(SUBMITTED);

		for (final String[] attr : attrs) {
			fAttrs.put(attr[0], attr[1]);
		}

		fOutReady = getOutputPath() != null && JOB_OUTERR_READY.equals(getStateDetail());
		fErrReady = getErrorPath() != null && JOB_OUTERR_READY.equals(getStateDetail());
	}

	/**
	 * Store a key value pair in the jobData map.
	 * Store any data for this job, which can be serialized and returned to
	 * the user within the job tables of lml.ui.
	 * 
	 * @param key
	 *            the key of the stored data, e.g. dispatchdate
	 * @param value
	 *            the value of the data, e.g. 21.03.2012
	 */
	public void addInfo(String key, String value) {
		jobData.put(key, value);
	}

	/**
	 * @return list of keys given by the jobData hashmap
	 */
	public Set<String> getAdditionalKeys() {
		return jobData.keySet();
	}

	/**
	 * @return connection name
	 */
	public String getConnectionName() {
		return fAttrs.get(CONNECTION_NAME_ATTR);
	}

	/**
	 * @return control type
	 */
	public String getControlType() {
		return fAttrs.get(CONTROL_TYPE_ATTR);
	}

	/**
	 * @return path to remote error file
	 */
	public String getErrorPath() {
		return fAttrs.get(STDERR_REMOTE_FILE_ATTR);
	}

	/**
	 * @return if error file is ready
	 */
	public boolean getErrReady() {
		return fErrReady;
	}

	/**
	 * Get the value stored for a given key.
	 * 
	 * @param key
	 *            the key, e.g. dispatchdate
	 * @return the stored value or null, if there is no value stored
	 */
	public String getInfo(String key) {
		return jobData.get(key);
	}

	/**
	 * @return job id
	 */
	public String getJobId() {
		return fAttrs.get(JOB_ID_ATTR);
	}

	/**
	 * @return monitor type
	 */
	public String getMonitorType() {
		return fAttrs.get(MONITOR_TYPE_ATTR);
	}

	public String getOid() {
		return fAttrs.get(OID_ATTR);
	}

	/**
	 * @return path to remote output file
	 */
	public String getOutputPath() {
		return fAttrs.get(STDOUT_REMOTE_FILE_ATTR);
	}

	/**
	 * @return if output file is ready
	 */
	public boolean getOutReady() {
		return fOutReady;
	}

	/**
	 * @return job id
	 */
	public String getOwner() {
		return fAttrs.get(OWNER_ATTR);
	}

	/**
	 * @return queue name
	 */
	public String getQueueName() {
		return fAttrs.get(QUEUE_NAME_ATTR);
	}

	/**
	 * @return remote services ID
	 */
	public String getRemoteId() {
		return fAttrs.get(REMOTE_SERVICES_ID_ATTR);
	}

	/**
	 * @return fState, or empty string if none.
	 */
	public String getState() {
		return fAttrs.get(STATE_ATTR);
	}

	/**
	 * @return fState detail, or empty string if none.
	 */
	public String getStateDetail() {
		return fAttrs.get(STATE_DETAIL_ATTR);
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
	 * @param outReady
	 *            output file is ready
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
		fOutReady = getOutputPath() != null && JOB_OUTERR_READY.equals(stateDetail);
		fErrReady = getErrorPath() != null && JOB_OUTERR_READY.equals(stateDetail);
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
