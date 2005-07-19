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
package org.eclipse.ptp.debug.internal.core;

import java.util.HashMap;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.ptp.debug.core.PCDIDebugModel;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIDebugProcessGroup;
import org.eclipse.ptp.debug.internal.core.model.PDebugTarget;

/**
 * Manages the process groups
 */
public class PGroupManager {

	/**
	 * The debug target associated with this manager.
	 */
	private PDebugTarget fDebugTarget;

	private HashMap fProcessGroups = null;
	
	/**
	 * The dispose flag.
	 */
	private boolean fIsDisposed = false;

	/**
	 * Constructor for CSignalManager.
	 */
	public PGroupManager( PDebugTarget target ) {
		fDebugTarget = target;
		fProcessGroups = new HashMap();
	}

	public void newProcessGroup(String name) {
		IPCDIDebugProcessGroup grp;
		grp = getDebugTarget().getCDITarget().newProcessGroup(name);
		fProcessGroups.put(name, grp);
	}
	
	public void delProcessGroup(String name) {
		getDebugTarget().getCDITarget().delProcessGroup(name);
		fProcessGroups.remove(name);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.core.CUpdateManager#dispose()
	 */
	public void dispose() {
		fProcessGroups.clear();
		fProcessGroups = null;
		fIsDisposed = true;
	}

	protected boolean isDisposed() {
		return fIsDisposed;
	}

	/**
	 * Throws a debug exception with the given message, error code, and underlying exception.
	 */
	protected void throwDebugException( String message, int code, Throwable exception ) throws DebugException {
		throw new DebugException( new Status( IStatus.ERROR, PCDIDebugModel.getPluginIdentifier(), code, message, exception ) );
	}

	protected PDebugTarget getDebugTarget() {
		return fDebugTarget;
	}
}