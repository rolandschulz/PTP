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
package org.eclipse.ptp.debug.internal.core;

import java.util.ArrayList;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.ptp.debug.core.PCDIDebugModel;
import org.eclipse.ptp.debug.core.cdi.PCDIException;
import org.eclipse.ptp.debug.core.cdi.model.IPCDISignal;
import org.eclipse.ptp.debug.core.model.IPSignal;
import org.eclipse.ptp.debug.internal.core.model.PDebugTarget;
import org.eclipse.ptp.debug.internal.core.model.PSignal;

/**
 * @author Clement chu
 */
public class PSignalManager implements IAdaptable {
	/**
	 * The debug target associated with this manager.
	 */
	private PDebugTarget fDebugTarget;

	/**
	 * The list of signals.
	 */
	private IPSignal[] fSignals = null;

	/**
	 * The dispose flag.
	 */
	private boolean fIsDisposed = false;

	/**
	 * Constructor for PSignalManager.
	 */
	public PSignalManager(PDebugTarget target) {
		fDebugTarget = target;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICSignalManager#getSignals()
	 */
	public IPSignal[] getSignals() throws DebugException {
		if (!isDisposed() && fSignals == null) {
			try {
				IPCDISignal[] cdiSignals = getDebugTarget().getCDITarget().getSignals();
				ArrayList<PSignal> list = new ArrayList<PSignal>(cdiSignals.length);
				for(int i = 0; i < cdiSignals.length; ++i) {
					list.add(new PSignal(getDebugTarget(), cdiSignals[i]));
				}
				fSignals = (IPSignal[])list.toArray(new IPSignal[list.size()]);
			}
			catch(PCDIException e) {
				throwDebugException(e.getMessage(), DebugException.TARGET_REQUEST_FAILED, e);
			}
		}
		return (fSignals != null) ? fSignals : new IPSignal[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.core.CUpdateManager#dispose()
	 */
	public void dispose() {
		if (fSignals != null)
			for(int i = 0; i < fSignals.length; ++i) {
				((PSignal)fSignals[i]).dispose();
			}
		fSignals = null;
		fIsDisposed = true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		if (adapter.equals(PSignalManager.class)) {
			return this;
		}
		if (adapter.equals(PDebugTarget.class)) {
			return getDebugTarget();
		}
		return null;
	}

	public void signalChanged(IPCDISignal cdiSignal) {
		PSignal signal = find(cdiSignal);
		if (signal != null) {
			signal.fireChangeEvent(DebugEvent.STATE);
		}
	}

	private PSignal find(IPCDISignal cdiSignal) {
		try {
			IPSignal[] signals = getSignals();
			for(int i = 0; i < signals.length; ++i)
				if (signals[i].getName().equals(cdiSignal.getName()))
					return (PSignal)signals[i];
		}
		catch(DebugException e) {
		}
		return null;
	}

	protected boolean isDisposed() {
		return fIsDisposed;
	}

	/**
	 * Throws a debug exception with the given message, error code, and underlying exception.
	 */
	protected void throwDebugException(String message, int code, Throwable exception) throws DebugException {
		throw new DebugException(new Status(IStatus.ERROR, PCDIDebugModel.getPluginIdentifier(), code, message, exception));
	}

	protected PDebugTarget getDebugTarget() {
		return fDebugTarget;
	}
}
