/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.ui.data;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ptp.rmsystem.IJobStatus;
import org.eclipse.ui.IMemento;

/**
 * Wrapper for job status which extracts the persistent properties and saves
 * them or reloads them from a memento.
 * 
 * @author arossi
 * 
 */
public class JobStatusData {
	public static final String ZEROSTR = "";//$NON-NLS-1$

	private final String rmId;
	private final String jobId;
	private String state;
	private String stateDetail;
	private final String outputPath;
	private final String errorPath;
	private final boolean interactive;
	private boolean outReady;
	private boolean errReady;

	private IJobStatus status; // for retrieving file contents

	/**
	 * Incoming constructor. For saving active status.
	 * 
	 * @param status
	 *            to persist
	 */
	public JobStatusData(IJobStatus status) {
		this.status = status;
		rmId = status.getRmUniqueName();
		jobId = status.getJobId();
		state = status.getState();
		stateDetail = status.getStateDetail();
		outputPath = status.getOutputPath();
		errorPath = status.getErrorPath();
		interactive = status.isInteractive();
		outReady = false;
		errReady = false;
	}

	/**
	 * Outgoing constructor for reloading.
	 * 
	 * @param memento
	 *            child node for a single jobId
	 */
	public JobStatusData(IMemento memento) {
		rmId = memento.getString(IJobStatus.RM_ID);
		jobId = memento.getString(IJobStatus.JOB_ID);
		state = memento.getString(IJobStatus.STATE);
		stateDetail = memento.getString(IJobStatus.STATE_DETAIL);
		outputPath = memento.getString(IJobStatus.STDOUT_REMOTE_FILE);
		errorPath = memento.getString(IJobStatus.STDERR_REMOTE_FILE);
		interactive = memento.getBoolean(IJobStatus.INTERACTIVE);
		outReady = false;
		errReady = false;
	}

	/**
	 * @return path to remote error file
	 */
	public String getErrorPath() {
		return errorPath;
	}

	/**
	 * @return if error file is ready
	 */
	public boolean getErrReady() {
		return errReady;
	}

	/**
	 * @return jobId
	 */
	public String getJobId() {
		return jobId;
	}

	/**
	 * @return path to remote output file
	 */
	public String getOutputPath() {
		return outputPath;
	}

	/**
	 * @return if output file is ready
	 */
	public boolean getOutReady() {
		return outReady;
	}

	/**
	 * 
	 * @return unique name of resource manager
	 */
	public String getRmId() {
		return rmId;
	}

	/**
	 * @return state, or empty string if none.
	 */
	public String getState() {
		return state;
	}

	/**
	 * @return state detail, or empty string if none.
	 */
	public String getStateDetail() {
		return stateDetail;
	}

	/**
	 * @return underlying status object.
	 */
	public IJobStatus getStatus() {
		return status;
	}

	/**
	 * @return job was submitted interactively
	 */
	public boolean isInteractive() {
		return interactive;
	}

	/**
	 * Save for restart.
	 * 
	 * @param memento
	 */
	public void save(IMemento memento) {
		if (memento == null) {
			return;
		}
		IMemento jobMemento = memento.createChild(IJobStatus.JOB_ID, jobId);
		jobMemento.putString(IJobStatus.JOB_ID, jobId);
		jobMemento.putString(IJobStatus.RM_ID, rmId);
		jobMemento.putString(IJobStatus.STATE, state);
		jobMemento.putString(IJobStatus.STATE_DETAIL, stateDetail);
		jobMemento.putString(IJobStatus.STDOUT_REMOTE_FILE, outputPath);
		jobMemento.putString(IJobStatus.STDERR_REMOTE_FILE, errorPath);
		jobMemento.putBoolean(IJobStatus.INTERACTIVE, interactive);
	}

	/**
	 * @param errReady
	 *            error file is ready
	 */
	public void setErrReady(boolean errReady) {
		this.errReady = errReady;
	}

	/**
	 * @param outReady
	 *            output file is ready
	 */
	public void setOutReady(boolean outReady) {
		this.outReady = outReady;
	}

	/**
	 * Replace with updated status.
	 * 
	 * @param status
	 */
	public void updateState(IJobStatus status) {
		this.status = status;
		state = status.getState();
		stateDetail = status.getStateDetail();
	}

	/**
	 * Convenience method for reloading all saved job status data.
	 * 
	 * @param memento
	 * @return list of job status objects
	 */
	public static List<JobStatusData> reload(IMemento memento) {
		List<JobStatusData> jobs = new ArrayList<JobStatusData>();
		if (memento != null) {
			IMemento[] children = memento.getChildren(IJobStatus.JOB_ID);
			for (IMemento child : children) {
				jobs.add(new JobStatusData(child));
			}
		}
		return jobs;
	}
}
