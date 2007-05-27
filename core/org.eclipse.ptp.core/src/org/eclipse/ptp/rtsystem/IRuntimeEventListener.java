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
package org.eclipse.ptp.rtsystem;

import org.eclipse.ptp.rtsystem.events.IRuntimeAttributeDefinitionEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeConnectedStateEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeMessageEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeJobChangeEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeMachineChangeEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeNewJobEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeNewMachineEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeNewNodeEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeNewProcessEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeNewQueueEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeNodeChangeEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeProcessChangeEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeQueueChangeEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeRunningStateEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeShutdownStateEvent;

public interface IRuntimeEventListener {
	public void handleRuntimeAttributeDefinitionEvent(IRuntimeAttributeDefinitionEvent e);
	public void handleRuntimeConnectedStateEvent(IRuntimeConnectedStateEvent e);
	public void handleRuntimeMessageEvent(IRuntimeMessageEvent e);
	public void handleRuntimeJobChangeEvent(IRuntimeJobChangeEvent e);
	public void handleRuntimeMachineChangeEvent(IRuntimeMachineChangeEvent e);
	public void handleRuntimeNewJobEvent(IRuntimeNewJobEvent e);
	public void handleRuntimeNewMachineEvent(IRuntimeNewMachineEvent e);
	public void handleRuntimeNewNodeEvent(IRuntimeNewNodeEvent e);
	public void handleRuntimeNewQueueEvent(IRuntimeNewQueueEvent e);
	public void handleRuntimeNewProcessEvent(IRuntimeNewProcessEvent e);
	public void handleRuntimeNodeChangeEvent(IRuntimeNodeChangeEvent e);
	public void handleRuntimeProcessChangeEvent(IRuntimeProcessChangeEvent e);
	public void handleRuntimeQueueChangeEvent(IRuntimeQueueChangeEvent e);
	public void handleRuntimeRunningStateEvent(IRuntimeRunningStateEvent e);
	public void handleRuntimeShutdownStateEvent(IRuntimeShutdownStateEvent e);
}
