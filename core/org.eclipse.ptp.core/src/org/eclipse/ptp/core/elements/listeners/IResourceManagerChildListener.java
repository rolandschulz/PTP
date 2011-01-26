/*******************************************************************************
 * Copyright (c) 2006 The Regents of the University of California. 
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
/**
 * 
 */
package org.eclipse.ptp.core.elements.listeners;

import org.eclipse.ptp.core.elements.events.IChangedJobEvent;
import org.eclipse.ptp.core.elements.events.IChangedMachineEvent;
import org.eclipse.ptp.core.elements.events.IChangedQueueEvent;
import org.eclipse.ptp.core.elements.events.INewJobEvent;
import org.eclipse.ptp.core.elements.events.INewMachineEvent;
import org.eclipse.ptp.core.elements.events.INewQueueEvent;
import org.eclipse.ptp.core.elements.events.IRemoveJobEvent;
import org.eclipse.ptp.core.elements.events.IRemoveMachineEvent;
import org.eclipse.ptp.core.elements.events.IRemoveQueueEvent;

/**
 * @author rsqrd
 * 
 */
public interface IResourceManagerChildListener {

	/**
	 * @param e
	 * @since 5.0
	 */
	public void handleEvent(IChangedJobEvent e);

	/**
	 * @param e
	 */
	public void handleEvent(IChangedMachineEvent e);

	/**
	 * @param e
	 */
	public void handleEvent(IChangedQueueEvent e);

	/**
	 * @param e
	 * @since 5.0
	 */
	public void handleEvent(INewJobEvent e);

	/**
	 * @param e
	 */
	public void handleEvent(INewMachineEvent e);

	/**
	 * @param e
	 */
	public void handleEvent(INewQueueEvent e);

	/**
	 * @param e
	 * @since 5.0
	 */
	public void handleEvent(IRemoveJobEvent e);

	/**
	 * @param e
	 */
	public void handleEvent(IRemoveMachineEvent e);

	/**
	 * @param e
	 */
	public void handleEvent(IRemoveQueueEvent e);

}
