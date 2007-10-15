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

package org.eclipse.ptp.proxy.runtime.client;

import org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeAttributeDefEvent;
import org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeConnectedStateEvent;
import org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeErrorStateEvent;
import org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeJobChangeEvent;
import org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeMachineChangeEvent;
import org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeMessageEvent;
import org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeNewJobEvent;
import org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeNewMachineEvent;
import org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeNewNodeEvent;
import org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeNewProcessEvent;
import org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeNewQueueEvent;
import org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeNodeChangeEvent;
import org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeProcessChangeEvent;
import org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeQueueChangeEvent;
import org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeRemoveAllEvent;
import org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeRemoveJobEvent;
import org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeRemoveMachineEvent;
import org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeRemoveNodeEvent;
import org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeRemoveProcessEvent;
import org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeRemoveQueueEvent;
import org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeRunningStateEvent;
import org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeShutdownStateEvent;
import org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeStartupErrorEvent;
import org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeSubmitJobErrorEvent;
import org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeTerminateJobErrorEvent;

public interface IProxyRuntimeEventListener {
	/**
	 * @param e
	 */
	public void handleEvent(IProxyRuntimeAttributeDefEvent e);
	
	/**
	 * @param e
	 */
	public void handleEvent(IProxyRuntimeConnectedStateEvent e);
	
	/**
	 * @param e
	 */
	public void handleEvent(IProxyRuntimeErrorStateEvent e);
	
	/**
	 * @param e
	 */
	public void handleEvent(IProxyRuntimeJobChangeEvent e);
	
	/**
	 * @param e
	 */
	public void handleEvent(IProxyRuntimeMachineChangeEvent e);
	
	/**
	 * @param e
	 */
	public void handleEvent(IProxyRuntimeMessageEvent e);
	
	/**
	 * @param e
	 */
	public void handleEvent(IProxyRuntimeNewJobEvent e);
	
	/**
	 * @param e
	 */
	public void handleEvent(IProxyRuntimeNewMachineEvent e);
	
	/**
	 * @param e
	 */
	public void handleEvent(IProxyRuntimeNewNodeEvent e);
	
	/**
	 * @param e
	 */
	public void handleEvent(IProxyRuntimeNewProcessEvent e);
	
	/**
	 * @param e
	 */
	public void handleEvent(IProxyRuntimeNewQueueEvent e);
	
	/**
	 * @param e
	 */
	public void handleEvent(IProxyRuntimeNodeChangeEvent e);	
	
	/**
	 * @param e
	 */
	public void handleEvent(IProxyRuntimeProcessChangeEvent e);	
	
	/**
	 * @param e
	 */
	public void handleEvent(IProxyRuntimeQueueChangeEvent e);
	
	/**
	 * @param e
	 */
	public void handleEvent(IProxyRuntimeRemoveAllEvent e);
	
	/**
	 * @param e
	 */
	public void handleEvent(IProxyRuntimeRemoveJobEvent e);
	
	/**
	 * @param e
	 */
	public void handleEvent(IProxyRuntimeRemoveMachineEvent e);
	
	/**
	 * @param e
	 */
	public void handleEvent(IProxyRuntimeRemoveNodeEvent e);
	
	/**
	 * @param e
	 */
	public void handleEvent(IProxyRuntimeRemoveProcessEvent e);
	
	/**
	 * @param e
	 */
	public void handleEvent(IProxyRuntimeRemoveQueueEvent e);
	
	/**
	 * @param e
	 */
	public void handleEvent(IProxyRuntimeRunningStateEvent e);
	
	/**
	 * @param e
	 */
	public void handleEvent(IProxyRuntimeShutdownStateEvent e);
	
	/**
	 * @param e
	 */
	public void handleEvent(IProxyRuntimeStartupErrorEvent e);
	
	/**
	 * @param e
	 */
	public void handleEvent(IProxyRuntimeSubmitJobErrorEvent e);
	
	/**
	 * @param e
	 */
	public void handleEvent(IProxyRuntimeTerminateJobErrorEvent e);
}
