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
 * Wrapper for job status which extracts the persistent properties and saves
 * them or reloads them from a memento.
 * 
 * @author arossi
 * 
 */

public class JobStatusData {
	//	private static final String JOB_ID_ATTR = "job_id";//$NON-NLS-1$
	//	private static final String RM_ID_ATTR = "rm_id";//$NON-NLS-1$
	//	private static final String STDOUT_REMOTE_FILE_ATTR = "stdout_remote_path";//$NON-NLS-1$
	//	private static final String STDERR_REMOTE_FILE_ATTR = "stderr_remote_path";//$NON-NLS-1$
	//	private static final String INTERACTIVE_ATTR = "interactive";//$NON-NLS-1$;
	//	private static final String STATE_ATTR = "state";//$NON-NLS-1$;
	//	private static final String STATE_DETAIL_ATTR = "state_detail";//$NON-NLS-1$;
	//	private static final String OID_ATTR = "oid";//$NON-NLS-1$;
	//	private static final String QUEUE_NAME_ATTR = "queue_name";//$NON-NLS-1$;
	//	private static final String OWNER_ATTR = "owner";//$NON-NLS-1$;

	private static final String JOB_OUTERR_READY = "JOB_OUTERR_READY";//$NON-NLS-1$

	public static final String SUBMITTED = "SUBMITTED";//$NON-NLS-1$
	public static final String RUNNING = "RUNNING";//$NON-NLS-1$
	public static final String COMPLETED = "COMPLETED";//$NON-NLS-1$
	public static final String FAILED = "FAILED";//$NON-NLS-1$
	public static final String CANCELED = "CANCELED";//$NON-NLS-1$

	// /**
	// * Convenience method for reloading all saved job status data.
	// *
	// * @param memento
	// * @return list of job status objects
	// */
	// public static List<JobStatusData> reload(IMemento memento) {
	// final List<JobStatusData> jobs = new ArrayList<JobStatusData>();
	// if (memento != null) {
	// final IMemento[] children = memento.getChildren(JOB_ID_ATTR);
	// for (final IMemento child : children) {
	// jobs.add(new JobStatusData(child));
	// }
	// }
	// return jobs;
	// }

	private static boolean detailIsFinal(String detail) {
		return CANCELED.equals(detail) || FAILED.equals(detail)
				|| JOB_OUTERR_READY.equals(detail);
	}

	private final String fRmId;
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

	// /**
	// * Outgoing constructor for reloading.
	// *
	// * @param memento
	// * child node for a single fJobId
	// */
	// public JobStatusData(IMemento memento) {
	// fJobId = memento.getID();
	// fRmId = memento.getString(RM_ID_ATTR);
	// fState = memento.getString(STATE_ATTR);
	// fStateDetail = memento.getString(STATE_DETAIL_ATTR);
	// fOutputPath = memento.getString(STDOUT_REMOTE_FILE_ATTR);
	// fErrorPath = memento.getString(STDERR_REMOTE_FILE_ATTR);
	// fInteractive = memento.getBoolean(INTERACTIVE_ATTR);
	// fQueueName = memento.getString(QUEUE_NAME_ATTR);
	// fOwner = memento.getString(OWNER_ATTR);
	// fOid = memento.getString(OID_ATTR);
	// fOutReady = fOutputPath != null
	// && JOB_OUTERR_READY.equals(fStateDetail);
	// fErrReady = fErrorPath != null && JOB_OUTERR_READY.equals(fStateDetail);
	// }

	/**
	 * Incoming constructor. For saving active status.
	 * 
	 * @param status
	 *            to persist
	 */
	public JobStatusData(String rmId, String jobId, String queueName,
			String owner, String outputPath, String errorPath,
			boolean interactive) {
		fRmId = rmId;
		fJobId = jobId;
		fQueueName = queueName;
		fOwner = owner;
		fOutputPath = outputPath;
		fErrorPath = errorPath;
		fInteractive = interactive;
	}

	public JobStatusData(String jobId, String rmId, String state,
			String stateDetail, String outputPath, String errorPath,
			boolean interactive, String queueName, String owner, String oid) {
		this(rmId, jobId, queueName, owner, outputPath, errorPath, interactive);
		fState = state;
		fStateDetail = stateDetail;
		fOid = oid;
		fOutReady = fOutputPath != null
				&& JOB_OUTERR_READY.equals(fStateDetail);
		fErrReady = fErrorPath != null && JOB_OUTERR_READY.equals(fStateDetail);
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
	 * @return unique name of resource manager
	 */
	public String getRmId() {
		return fRmId;
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

	// /**
	// * Save for restart.
	// *
	// * @param memento
	// */
	// public void save(IMemento memento) {
	// if (memento == null) {
	// return;
	// }
	// final IMemento jobMemento = memento.createChild(JOB_ID_ATTR, fJobId);
	// jobMemento.putString(RM_ID_ATTR, fRmId);
	// jobMemento.putString(STATE_ATTR, fState);
	// jobMemento.putString(STATE_DETAIL_ATTR, fStateDetail);
	// jobMemento.putString(STDOUT_REMOTE_FILE_ATTR, fOutputPath);
	// jobMemento.putString(STDERR_REMOTE_FILE_ATTR, fErrorPath);
	// jobMemento.putBoolean(INTERACTIVE_ATTR, fInteractive);
	// jobMemento.putString(QUEUE_NAME_ATTR, fQueueName);
	// jobMemento.putString(OWNER_ATTR, fOwner);
	// jobMemento.putString(OID_ATTR, fOid);
	// }

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
