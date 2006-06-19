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
package org.eclipse.ptp.debug.external.core.cdi.event;

import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.cdi.IPCDISession;
import org.eclipse.ptp.debug.core.cdi.IPCDISessionObject;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIExitedEvent;


public class InferiorExitedEvent extends AbstractEvent implements IPCDIExitedEvent {
	private int exitStatus = -1;
	private String signalName;
	private String signalMeaning;
	
	public InferiorExitedEvent(IPCDISession session, BitList tasks, int exitStatus) {
		super(session, tasks);
		this.exitStatus = exitStatus;
	}
	public InferiorExitedEvent(IPCDISession session, BitList tasks, String signalName, String signalMeaning) {
		super(session, tasks);
		this.signalName = signalName;
		this.signalMeaning = signalMeaning;
	}
	public String getSignalName() {
		return this.signalName;
	}
	public String getSignalMeaning() {
		return this.signalMeaning;
	}
	
	public int getExitStatus() {
		return exitStatus;
	}
	
	public IPCDISessionObject getReason() {
		return null;
	}
}
