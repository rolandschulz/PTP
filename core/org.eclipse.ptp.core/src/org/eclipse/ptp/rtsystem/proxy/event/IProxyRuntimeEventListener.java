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

package org.eclipse.ptp.rtsystem.proxy.event;

public interface IProxyRuntimeEventListener {
	public void handleProxyRuntimeMessageEvent(IProxyRuntimeMessageEvent e);
	public void handleProxyRuntimeAttributeDefEvent(IProxyRuntimeAttributeDefEvent e);
	public void handleProxyRuntimeNewJobEvent(IProxyRuntimeNewJobEvent e);
	public void handleProxyRuntimeNewMachineEvent(IProxyRuntimeNewMachineEvent e);
	public void handleProxyRuntimeNewNodeEvent(IProxyRuntimeNewNodeEvent e);
	public void handleProxyRuntimeNewProcessEvent(IProxyRuntimeNewProcessEvent e);
	public void handleProxyRuntimeNewQueueEvent(IProxyRuntimeNewQueueEvent e);
	public void handleProxyRuntimeJobChangeEvent(IProxyRuntimeJobChangeEvent e);
	public void handleProxyRuntimeMachineChangeEvent(IProxyRuntimeMachineChangeEvent e);
	public void handleProxyRuntimeNodeChangeEvent(IProxyRuntimeNodeChangeEvent e);
	public void handleProxyRuntimeProcessChangeEvent(IProxyRuntimeProcessChangeEvent e);	
	public void handleProxyRuntimeQueueChangeEvent(IProxyRuntimeQueueChangeEvent e);	
	public void handleProxyRuntimeConnectedStateEvent(IProxyRuntimeConnectedStateEvent e);
	public void handleProxyRuntimeRunningStateEvent(IProxyRuntimeRunningStateEvent e);
	public void handleProxyRuntimeShutdownStateEvent(IProxyRuntimeShutdownStateEvent e);
}
