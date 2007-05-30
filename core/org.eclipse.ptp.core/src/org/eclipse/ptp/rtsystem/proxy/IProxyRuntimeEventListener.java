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

package org.eclipse.ptp.rtsystem.proxy;

import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeAttributeDefEvent;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeConnectedStateEvent;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeJobChangeEvent;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeMachineChangeEvent;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeMessageEvent;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeNewJobEvent;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeNewMachineEvent;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeNewNodeEvent;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeNewProcessEvent;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeNewQueueEvent;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeNodeChangeEvent;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeProcessChangeEvent;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeQueueChangeEvent;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeRemoveAllEvent;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeRemoveJobEvent;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeRemoveMachineEvent;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeRemoveNodeEvent;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeRemoveProcessEvent;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeRemoveQueueEvent;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeRunningStateEvent;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeShutdownStateEvent;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeStartupErrorEvent;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeSubmitJobErrorEvent;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeTerminateJobErrorEvent;

public interface IProxyRuntimeEventListener {
	public void handleProxyRuntimeAttributeDefEvent(IProxyRuntimeAttributeDefEvent e);
	public void handleProxyRuntimeConnectedStateEvent(IProxyRuntimeConnectedStateEvent e);
	public void handleProxyRuntimeJobChangeEvent(IProxyRuntimeJobChangeEvent e);
	public void handleProxyRuntimeMachineChangeEvent(IProxyRuntimeMachineChangeEvent e);
	public void handleProxyRuntimeMessageEvent(IProxyRuntimeMessageEvent e);
	public void handleProxyRuntimeNewJobEvent(IProxyRuntimeNewJobEvent e);
	public void handleProxyRuntimeNewMachineEvent(IProxyRuntimeNewMachineEvent e);
	public void handleProxyRuntimeNewNodeEvent(IProxyRuntimeNewNodeEvent e);
	public void handleProxyRuntimeNewProcessEvent(IProxyRuntimeNewProcessEvent e);
	public void handleProxyRuntimeNewQueueEvent(IProxyRuntimeNewQueueEvent e);
	public void handleProxyRuntimeNodeChangeEvent(IProxyRuntimeNodeChangeEvent e);	
	public void handleProxyRuntimeProcessChangeEvent(IProxyRuntimeProcessChangeEvent e);	
	public void handleProxyRuntimeQueueChangeEvent(IProxyRuntimeQueueChangeEvent e);
	public void handleProxyRuntimeRemoveAllEvent(IProxyRuntimeRemoveAllEvent e);
	public void handleProxyRuntimeRemoveJobEvent(IProxyRuntimeRemoveJobEvent e);
	public void handleProxyRuntimeRemoveMachineEvent(IProxyRuntimeRemoveMachineEvent e);
	public void handleProxyRuntimeRemoveNodeEvent(IProxyRuntimeRemoveNodeEvent e);
	public void handleProxyRuntimeRemoveProcessEvent(IProxyRuntimeRemoveProcessEvent e);
	public void handleProxyRuntimeRemoveQueueEvent(IProxyRuntimeRemoveQueueEvent e);
	public void handleProxyRuntimeRunningStateEvent(IProxyRuntimeRunningStateEvent e);
	public void handleProxyRuntimeShutdownStateEvent(IProxyRuntimeShutdownStateEvent e);
	public void handleProxyRuntimeStartupErrorEvent(IProxyRuntimeStartupErrorEvent e);
	public void handleProxyRuntimeSubmitJobErrorEvent(IProxyRuntimeSubmitJobErrorEvent e);
	public void handleProxyRuntimeTerminateJobErrorEvent(IProxyRuntimeTerminateJobErrorEvent e);
}
