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
package org.eclipse.ptp.debug.internal.core.model;

import org.eclipse.debug.core.DebugException;
import org.eclipse.ptp.debug.core.cdi.PCDIException;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIEvent;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIEventListener;
import org.eclipse.ptp.debug.core.cdi.model.IPCDISignal;
import org.eclipse.ptp.debug.core.model.IPSignal;

/**
 * @author Clement chu
 */
public class PSignal extends PDebugElement implements IPSignal, IPCDIEventListener {
	private IPCDISignal fCDISignal;

	/**
	 * Constructor for PSignal.
	 * 
	 * @param target
	 */
	public PSignal(PDebugTarget target, IPCDISignal cdiSignal) {
		super(target);
		fCDISignal = cdiSignal;
		getCDISession().getEventManager().addEventListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.model.IPSignal#getDescription()
	 */
	public String getDescription() throws DebugException {
		return getCDISignal().getDescription();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.model.IPSignal#getName()
	 */
	public String getName() throws DebugException {
		return getCDISignal().getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.model.IPSignal#isPassEnabled()
	 */
	public boolean isPassEnabled() throws DebugException {
		return !getCDISignal().isIgnore();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.model.IPSignal#isStopEnabled()
	 */
	public boolean isStopEnabled() throws DebugException {
		return getCDISignal().isStopSet();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.model.IPSignal#setPassEnabled(boolean)
	 */
	public void setPassEnabled(boolean enable) throws DebugException {
		handle(enable, isStopEnabled());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.model.IPSignal#setStopEnabled(boolean)
	 */
	public void setStopEnabled(boolean enable) throws DebugException {
		handle(isPassEnabled(), enable);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.cdi.event.IPCDIEventListener#handleDebugEvents(IPCDIEvent)
	 */
	public void handleDebugEvents(IPCDIEvent[] events) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.model.IPSignal#dispose()
	 */
	public void dispose() {
		getCDISession().getEventManager().removeEventListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.model.IPSignal#signal()
	 */
	public void signal() throws DebugException {
		try {
			getCDITarget().resume(getCDISignal());
		} catch (PCDIException e) {
			targetRequestFailed(e.getMessage(), null);
		}
	}

	protected IPCDISignal getCDISignal() {
		return fCDISignal;
	}

	private void handle(boolean pass, boolean stop) throws DebugException {
		try {
			getCDISignal().handle(!pass, stop);
		} catch (PCDIException e) {
			targetRequestFailed(e.getMessage(), null);
		}
	}

	public boolean canModify() {
		// TODO add canModify method to IPCDISignal
		return true;
	}
}
