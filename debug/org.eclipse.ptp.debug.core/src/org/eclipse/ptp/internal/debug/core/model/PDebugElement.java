/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.debug.core.model;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.debug.core.IPDebugConstants;
import org.eclipse.ptp.debug.core.IPSession;
import org.eclipse.ptp.debug.core.TaskSet;
import org.eclipse.ptp.debug.core.model.IPDebugElement;
import org.eclipse.ptp.debug.core.model.IPDebugElementStatus;
import org.eclipse.ptp.debug.core.model.IPDebugTarget;
import org.eclipse.ptp.debug.core.model.PDebugElementState;
import org.eclipse.ptp.debug.core.pdi.IPDISession;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.model.IPDITarget;
import org.eclipse.ptp.internal.debug.core.PDebugModel;
import org.eclipse.ptp.internal.debug.core.PDebugUtils;
import org.eclipse.ptp.internal.debug.core.messages.Messages;

public abstract class PDebugElement extends PlatformObject implements IPDebugElement, IPDebugElementStatus {
	/**
	 * @param message
	 * @throws DebugException
	 */
	public static void notSupported(String message) throws DebugException {
		throwDebugException(message, DebugException.NOT_SUPPORTED, null);
	}

	/**
	 * @param message
	 * @param e
	 * @throws DebugException
	 */
	public static void requestFailed(String message, Exception e) throws DebugException {
		requestFailed(message, e, DebugException.REQUEST_FAILED);
	}

	/**
	 * @param message
	 * @param e
	 * @param code
	 * @throws DebugException
	 */
	public static void requestFailed(String message, Throwable e, int code) throws DebugException {
		throwDebugException(message, code, e);
	}

	/**
	 * @param message
	 * @param e
	 * @throws DebugException
	 */
	public static void targetRequestFailed(String message, PDIException e) throws DebugException {
		requestFailed(NLS.bind(Messages.PDebugElement_0, new Object[] { message }), e, DebugException.TARGET_REQUEST_FAILED);
	}

	/**
	 * @param message
	 * @param e
	 * @throws DebugException
	 */
	public static void targetRequestFailed(String message, Throwable e) throws DebugException {
		throwDebugException(NLS.bind(Messages.PDebugElement_0, new Object[] { message }), DebugException.TARGET_REQUEST_FAILED, e);
	}

	/**
	 * @param message
	 * @param code
	 * @param exception
	 * @throws DebugException
	 */
	protected static void throwDebugException(String message, int code, Throwable exception) throws DebugException {
		throw new DebugException(new Status(IStatus.ERROR, PDebugModel.getPluginIdentifier(), code, message, exception));
	}

	private int fSeverity = IPDebugElementStatus.OK;

	private String fMessage = null;
	private PDebugElementState fState = PDebugElementState.UNDEFINED;
	private PDebugElementState fOldState = PDebugElementState.UNDEFINED;
	private Object fCurrentStateInfo = null;
	protected final IPSession fSession;
	protected final TaskSet tasks;

	public PDebugElement(IPSession session, TaskSet tasks) {
		fSession = session;
		this.tasks = tasks;
	}

	/**
	 * @param detail
	 * @return
	 */
	public DebugEvent createChangeEvent(int detail) {
		return new DebugEvent(this, DebugEvent.CHANGE, detail);
	}

	/**
	 * @return
	 */
	public DebugEvent createCreateEvent() {
		return new DebugEvent(this, DebugEvent.CREATE);
	}

	/**
	 * @param detail
	 * @return
	 */
	public DebugEvent createResumeEvent(int detail) {
		return new DebugEvent(this, DebugEvent.RESUME, detail);
	}

	/**
	 * @param detail
	 * @return
	 */
	public DebugEvent createSuspendEvent(int detail) {
		return new DebugEvent(this, DebugEvent.SUSPEND, detail);
	}

	/**
	 * @return
	 */
	public DebugEvent createTerminateEvent() {
		return new DebugEvent(this, DebugEvent.TERMINATE);
	}

	/**
	 * @param detail
	 */
	public void fireChangeEvent(int detail) {
		fireEvent(new DebugEvent(this, DebugEvent.CHANGE, detail));
	}

	/**
	 * 
	 */
	public void fireCreationEvent() {
		fireEvent(new DebugEvent(this, DebugEvent.CREATE));
	}

	/**
	 * @param detail
	 */
	public void fireResumeEvent(int detail) {
		fireEvent(new DebugEvent(this, DebugEvent.RESUME, detail));
	}

	/**
	 * @param detail
	 */
	public void fireSuspendEvent(int detail) {
		fireEvent(new DebugEvent(this, DebugEvent.SUSPEND, detail));
	}

	/**
	 * 
	 */
	public void fireTerminateEvent() {
		fireEvent(new DebugEvent(this, DebugEvent.TERMINATE));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.PlatformObject#getAdapter(java.lang.Class)
	 */
	@Override
	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapter) {
		if (adapter.equals(IDebugElement.class))
			return this;
		if (adapter.equals(IPDebugElement.class))
			return this;
		if (adapter.equals(IPDebugElementStatus.class))
			return this;
		if (adapter.equals(IPSession.class))
			return fSession;
		if (adapter.equals(IPDebugTarget.class))
			return getDebugTarget();
		if (adapter.equals(IDebugTarget.class))
			return getDebugTarget();
		if (adapter.equals(ILaunch.class))
			return getLaunch();

		return super.getAdapter(adapter);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.model.IPDebugElement#getCurrentStateInfo()
	 */
	public synchronized Object getCurrentStateInfo() {
		return fCurrentStateInfo;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IDebugElement#getDebugTarget()
	 */
	public IDebugTarget getDebugTarget() {
		// return fSession.findDebugTarget(getTasks());
		return fSession.getLaunch().getDebugTarget(getID());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.model.IPDebugElement#getID()
	 */
	public int getID() {
		return tasks.nextSetBit(0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IDebugElement#getLaunch()
	 */
	public ILaunch getLaunch() {
		return fSession.getLaunch();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.model.IPDebugElementStatus#getMessage()
	 */
	public String getMessage() {
		return fMessage;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IDebugElement#getModelIdentifier()
	 */
	public String getModelIdentifier() {
		return PDebugModel.getPluginIdentifier();
	}

	/**
	 * @return
	 */
	public IPDISession getPDISession() {
		return fSession.getPDISession();
	}

	/**
	 * @return
	 * @throws PDIException
	 */
	public IPDITarget getPDITarget() throws PDIException {
		IPDebugTarget debugTarget = fSession.findDebugTarget(getTasks());
		if (debugTarget == null) {
			throw new PDIException(getTasks(), Messages.PDebugElement_2);
		}
		return debugTarget.getPDITarget();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.model.IPDebugElement#getSession()
	 */
	public IPSession getSession() {
		return fSession;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.model.IPDebugElementStatus#getSeverity()
	 */
	public synchronized int getSeverity() {
		return fSeverity;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.model.IPDebugElement#getState()
	 */
	public PDebugElementState getState() {
		synchronized (fState) {
			return fState;
		}
	}

	/**
	 * @return
	 */
	public TaskSet getTasks() {
		return tasks;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.model.IPDebugElementStatus#isOK()
	 */
	public synchronized boolean isOK() {
		return (fSeverity == IPDebugElementStatus.OK);
	}

	/**
	 * @param event
	 */
	protected void fireEvent(DebugEvent event) {
		DebugPlugin.getDefault().fireDebugEventSet(new DebugEvent[] { event });
	}

	/**
	 * @param events
	 */
	protected void fireEventSet(DebugEvent[] events) {
		DebugPlugin.getDefault().fireDebugEventSet(events);
	}

	/**
	 * @param e
	 */
	protected void infoMessage(Throwable e) {
		IStatus newStatus = new Status(IStatus.INFO, PDebugModel.getPluginIdentifier(), IPDebugConstants.STATUS_CODE_INFO,
				e.getMessage(), null);
		PDebugUtils.info(newStatus, getDebugTarget());
	}

	/**
	 * @param e
	 */
	protected void logError(Exception e) {
		DebugPlugin.log(e);
	}

	/**
	 * @param message
	 */
	protected void logError(String message) {
		DebugPlugin.logMessage(message, null);
	}

	/**
	 * 
	 */
	protected synchronized void resetStatus() {
		fSeverity = IPDebugElementStatus.OK;
		fMessage = null;
	}

	/**
	 * 
	 */
	protected void restoreState() {
		synchronized (fState) {
			fState = fOldState;
		}
	}

	/**
	 * @param currentStateInfo
	 */
	protected synchronized void setCurrentStateInfo(Object currentStateInfo) {
		fCurrentStateInfo = currentStateInfo;
	}

	/**
	 * @param state
	 * @throws IllegalArgumentException
	 */
	protected void setState(PDebugElementState state) throws IllegalArgumentException {
		synchronized (fState) {
			fOldState = fState;
			fState = state;
		}
	}

	/**
	 * @param severity
	 * @param message
	 */
	protected synchronized void setStatus(int severity, String message) {
		fSeverity = severity;
		fMessage = message;
		if (fMessage != null) {
			fMessage.trim();
		}
	}
}
