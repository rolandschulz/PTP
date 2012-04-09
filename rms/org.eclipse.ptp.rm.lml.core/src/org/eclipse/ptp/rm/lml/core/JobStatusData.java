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

	private static boolean detailIsFinal(String detail) {
		return CANCELED.equals(detail) || FAILED.equals(detail) || JOB_OUTERR_READY.equals(detail);
	}

	private final String fConfigName;
	private final String fRemoteServicesId;
	private final String fConnectionName;
	private final String fJobId;
	private final String fOutputPath;
	private final String fErrorPath;
	private final String fQueueName;
	private final String fOwner;
	private final boolean fInteractive;

	private String fState = SUBMITTED;
	private String fStateDetail = SUBMITTED;
	private boolean fOutReady = false;
	private boolean fErrReady = false;

	private String fOid;

	private boolean fRemoved = false;

	/**
	 * Incoming constructor. For saving active status.
	 * 
	 * @param status
	 *            to persist
	 */
	public JobStatusData(String jobId, String configName, String remoteServicesId, String connectionName, String queueName,
			String owner, String outputPath, String errorPath, boolean interactive) {
		fConfigName = configName;
		fRemoteServicesId = remoteServicesId;
		fConnectionName = connectionName;
		fJobId = jobId;
		fQueueName = queueName;
		fOwner = owner;
		fOutputPath = outputPath;
		fErrorPath = errorPath;
		fInteractive = interactive;
	}

	public JobStatusData(String jobId, String configName, String remoteServicesId, String connectionName, String state,
			String stateDetail, String outputPath, String errorPath, boolean interactive, String queueName, String owner, String oid) {
		this(jobId, configName, remoteServicesId, connectionName, queueName, owner, outputPath, errorPath, interactive);
		fState = state;
		fStateDetail = stateDetail;
		fOid = oid;
		fOutReady = fOutputPath != null && JOB_OUTERR_READY.equals(fStateDetail);
		fErrReady = fErrorPath != null && JOB_OUTERR_READY.equals(fStateDetail);
	}

	/**
	 * 
	 * @return configuration name
	 */
	public String getConfigurationName() {
		return fConfigName;
	}

	/**
	 * 
	 * @return connection name
	 */
	public String getConnectionName() {
		return fConnectionName;
	}

	/**
	 * @return path to remote error file
	 */
	public String getErrorPath() {
		return fErrorPath;
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

	public String getOid() {
		return fOid;
	}

	/**
	 * @return path to remote output file
	 */
	public String getOutputPath() {
		return fOutputPath;
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
		return fOwner;
	}

	/**
	 * @return queue name
	 */
	public String getQueueName() {
		return fQueueName;
	}

	/**
	 * 
	 * @return remote services ID
	 */
	public String getRemoteServicesId() {
		return fRemoteServicesId;
	}

	/**
	 * @return fState, or empty string if none.
	 */
	public String getState() {
		return fState;
	}

	/**
	 * @return fState detail, or empty string if none.
	 */
	public String getStateDetail() {
		return fStateDetail;
	}

	/**
	 * @return job is completed
	 */
	public boolean isCompleted() {
		return fState.equals(COMPLETED);
	}

	/**
	 * @return job was submitted interactively
	 */
	public boolean isInteractive() {
		return fInteractive;
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
		fOid = oid;
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
		fState = state;
	}

	/**
	 * @param stateDetail
	 *            the state detail
	 */
	public void setStateDetail(String stateDetail) {
		fStateDetail = stateDetail;
		fOutReady = fOutputPath != null && JOB_OUTERR_READY.equals(stateDetail);
		fErrReady = fErrorPath != null && JOB_OUTERR_READY.equals(stateDetail);
	}

	/**
	 * Replace with updated status.
	 * 
	 * @param status
	 */
	public void updateState(String state, String stateDetail) {
		if (!detailIsFinal(fStateDetail)) {
			fState = state;
			setStateDetail(stateDetail);
		}
	}
}
