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

import org.eclipse.ptp.rm.jaxb.core.ICommandJobRemoteOutputHandler;
import org.eclipse.ptp.rm.jaxb.core.ICommandJobStatus;
import org.eclipse.ptp.rm.jaxb.core.ICommandJobStreamsProxy;
import org.eclipse.ptp.rm.jaxb.core.IFileReadyListener;
import org.eclipse.ptp.rm.jaxb.core.runnable.command.CommandJobStatus;
import org.eclipse.ptp.rm.jaxb.ui.IJAXBUINonNLSConstants;
import org.eclipse.ui.IMemento;

/**
 * Wrapper for command job status which extracts the persistent properties and
 * saves them or reloads them from a memento.
 * 
 * @author arossi
 * 
 */
public class PersistentCommandJobStatus implements IJAXBUINonNLSConstants {

	private final ICommandJobStatus status;
	private boolean outReady;
	private boolean errReady;

	/**
	 * Incoming constructor. For saving active status.
	 * 
	 * @param status
	 *            to persist
	 */
	public PersistentCommandJobStatus(ICommandJobStatus status) {
		this.status = status;
		outReady = false;
		errReady = false;
	}

	/**
	 * Outgoing constructor for reloading.
	 * 
	 * @param memento
	 *            child node for a single jobId
	 */
	public PersistentCommandJobStatus(IMemento memento) {
		String rmId = memento.getString(RM_ID);
		String jobId = memento.getString(JOB_ID);
		String stdoutPath = memento.getString(STDOUT_REMOTE_FILE);
		String stderrPath = memento.getString(STDERR_REMOTE_FILE);
		this.status = new CommandJobStatus(rmId, jobId, stdoutPath, stderrPath);
		outReady = false;
		errReady = false;
	}

	/**
	 * Wrapper method.
	 * 
	 * @return remote error file handler
	 */
	public ICommandJobRemoteOutputHandler getErrorHandler() {
		return getHandler(true);
	}

	/**
	 * @return if error file is ready
	 */
	public boolean getErrReady() {
		return errReady;
	}

	/**
	 * Wrapper method.
	 * 
	 * @return jobId, or empty string if none.
	 */
	public String getJobId() {
		if (status == null) {
			return ZEROSTR;
		}
		return status.getJobId();
	}

	/**
	 * Wrapper method.
	 * 
	 * @return remote output file handler
	 */
	public ICommandJobRemoteOutputHandler getOutputHandler() {
		return getHandler(false);
	}

	/**
	 * @return if output file is ready
	 */
	public boolean getOutReady() {
		return outReady;
	}

	/**
	 * Wrapper method.
	 * 
	 * @return state, or empty string if none.
	 */
	public String getState() {
		if (status == null) {
			return ZEROSTR;
		}
		return status.getState();
	}

	/**
	 * Wrapper method.
	 * 
	 * @return state detail, or empty string if none.
	 */
	public String getStateDetail() {
		if (status == null) {
			return ZEROSTR;
		}
		return status.getStateDetail();
	}

	/**
	 * 
	 * @return underlying status object
	 */
	public ICommandJobStatus getStatus() {
		return status;
	}

	/**
	 * Checks to see if there are handlers and adds listener if so.
	 * 
	 * @param listener
	 */
	public void maybeAddListener(IFileReadyListener listener) {
		ICommandJobRemoteOutputHandler h = getOutputHandler();
		if (h != null) {
			h.addFileReadyListener(listener);
		}
		h = getErrorHandler();
		if (h != null) {
			h.addFileReadyListener(listener);
		}
	}

	/**
	 * Checks to see if there are handlers and removes listener if so.
	 * 
	 * @param listener
	 */
	public void maybeRemoveListener(IFileReadyListener listener) {
		ICommandJobRemoteOutputHandler h = getOutputHandler();
		if (h != null) {
			h.removeFileReadyListener(listener);
		}
		h = getErrorHandler();
		if (h != null) {
			h.removeFileReadyListener(listener);
		}
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
		if (status == null) {
			return;
		}
		IMemento jobMemento = memento.createChild(JOB_ID, status.getJobId());
		jobMemento.putString(JOB_ID, status.getJobId());
		jobMemento.putString(RM_ID, status.getRmUniqueName());
		ICommandJobStreamsProxy proxy = (ICommandJobStreamsProxy) status.getStreamsProxy();
		if (proxy == null) {
			return;
		}
		proxy.getOutputStreamMonitor();
		ICommandJobRemoteOutputHandler handler = proxy.getRemoteOutputHandler();
		if (handler != null) {
			jobMemento.putString(STDOUT_REMOTE_FILE, handler.getRemoteFilePath());
		}
		handler = proxy.getRemoteErrorHandler();
		if (handler != null) {
			jobMemento.putString(STDERR_REMOTE_FILE, handler.getRemoteFilePath());
		}
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
	 * @param error
	 *            get handler for error file; if false, output handler is
	 *            returned.
	 * @return remote file handler
	 */
	private ICommandJobRemoteOutputHandler getHandler(boolean error) {
		if (status == null) {
			return null;
		}

		ICommandJobStreamsProxy proxy = (ICommandJobStreamsProxy) status.getStreamsProxy();
		if (proxy == null) {
			return null;
		}

		ICommandJobRemoteOutputHandler handler = null;
		if (error) {
			handler = proxy.getRemoteErrorHandler();
		} else {
			handler = proxy.getRemoteOutputHandler();
		}

		return handler;
	}

	/**
	 * Convenience method for reloading all saved job status data.
	 * 
	 * @param memento
	 * @return list of job status objects
	 */
	public static List<PersistentCommandJobStatus> reload(IMemento memento) {
		List<PersistentCommandJobStatus> jobs = new ArrayList<PersistentCommandJobStatus>();
		if (memento != null) {
			IMemento[] children = memento.getChildren(JOB_ID);
			for (IMemento child : children) {
				jobs.add(new PersistentCommandJobStatus(child));
			}
		}
		return jobs;
	}
}
