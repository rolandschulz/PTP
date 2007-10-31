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
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.model.IPSignal;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.model.IPDISignal;
import org.eclipse.ptp.debug.internal.core.PSession;

/**
 * @author Clement chu
 */
public class PSignal extends PDebugElement implements IPSignal {
	private IPDISignal pdiSignal;

	public PSignal(PSession session, BitList tasks, IPDISignal pdiSignal) {
		super(session, tasks);
		this.pdiSignal = pdiSignal;
	}
	public String getDescription() throws DebugException {
		return getPDISignal().getDescription();
	}
	public String getName() throws DebugException {
		return getPDISignal().getName();
	}
	public boolean isPassEnabled() throws DebugException {
		return !getPDISignal().isIgnore();
	}
	public boolean isStopEnabled() throws DebugException {
		return getPDISignal().isStopSet();
	}
	public void setPassEnabled(boolean enable) throws DebugException {
		handle(enable, isStopEnabled());
	}
	public void setStopEnabled(boolean enable) throws DebugException {
		handle(isPassEnabled(), enable);
	}
	public void dispose() {
	}
	public void signal() throws DebugException {
		try {
			getPDISession().resume(getTasks(), getPDISignal());
		} catch (PDIException e) {
			targetRequestFailed(e.getMessage(), null);
		}
	}
	protected IPDISignal getPDISignal() {
		return pdiSignal;
	}
	private void handle(boolean pass, boolean stop) throws DebugException {
		try {
			getPDISignal().handle(!pass, stop);
		} catch (PDIException e) {
			targetRequestFailed(e.getMessage(), null);
		}
	}
	public boolean canModify() {
		// TODO add canModify method to IPDISignal
		return true;
	}
}
