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
import org.eclipse.ptp.rtsystem.events.IRuntimeErrorStateEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeJobChangeEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeMachineChangeEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeMessageEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeNewJobEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeNewMachineEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeNewNodeEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeNewProcessEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeNewQueueEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeNodeChangeEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeProcessChangeEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeQueueChangeEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeRMChangeEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeRemoveAllEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeRemoveJobEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeRemoveMachineEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeRemoveNodeEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeRemoveProcessEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeRemoveQueueEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeRunningStateEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeShutdownStateEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeStartupErrorEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeSubmitJobErrorEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeTerminateJobErrorEvent;

@Deprecated
public interface IRuntimeEventListener {
	/**
	 * @param e
	 */
	public void handleEvent(IRuntimeAttributeDefinitionEvent e);

	/**
	 * @param e
	 */
	public void handleEvent(IRuntimeConnectedStateEvent e);

	/**
	 * @param e
	 */
	public void handleEvent(IRuntimeErrorStateEvent e);

	/**
	 * @param e
	 */
	public void handleEvent(IRuntimeJobChangeEvent e);

	/**
	 * @param e
	 */
	public void handleEvent(IRuntimeMachineChangeEvent e);

	/**
	 * @param e
	 */
	public void handleEvent(IRuntimeMessageEvent e);

	/**
	 * @param e
	 */
	public void handleEvent(IRuntimeNewJobEvent e);

	/**
	 * @param e
	 */
	public void handleEvent(IRuntimeNewMachineEvent e);

	/**
	 * @param e
	 */
	public void handleEvent(IRuntimeNewNodeEvent e);

	/**
	 * @param e
	 */
	public void handleEvent(IRuntimeNewProcessEvent e);

	/**
	 * @param e
	 */
	public void handleEvent(IRuntimeNewQueueEvent e);

	/**
	 * @param e
	 */
	public void handleEvent(IRuntimeNodeChangeEvent e);

	/**
	 * @param e
	 */
	public void handleEvent(IRuntimeProcessChangeEvent e);

	/**
	 * @param e
	 */
	public void handleEvent(IRuntimeQueueChangeEvent e);

	/**
	 * @param e
	 */
	public void handleEvent(IRuntimeRemoveAllEvent e);

	/**
	 * @param e
	 */
	public void handleEvent(IRuntimeRemoveJobEvent e);

	/**
	 * @param e
	 */
	public void handleEvent(IRuntimeRemoveMachineEvent e);

	/**
	 * @param e
	 */
	public void handleEvent(IRuntimeRemoveNodeEvent e);

	/**
	 * @param e
	 */
	public void handleEvent(IRuntimeRemoveProcessEvent e);

	/**
	 * @param e
	 */
	public void handleEvent(IRuntimeRemoveQueueEvent e);

	/**
	 * @param e
	 */
	public void handleEvent(IRuntimeRMChangeEvent e);

	/**
	 * @param e
	 */
	public void handleEvent(IRuntimeRunningStateEvent e);

	/**
	 * @param e
	 */
	public void handleEvent(IRuntimeShutdownStateEvent e);

	/**
	 * @param e
	 */
	public void handleEvent(IRuntimeStartupErrorEvent e);

	/**
	 * @param e
	 */
	public void handleEvent(IRuntimeSubmitJobErrorEvent e);

	/**
	 * @param e
	 */
	public void handleEvent(IRuntimeTerminateJobErrorEvent e);
}
