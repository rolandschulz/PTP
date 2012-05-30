/*******************************************************************************
 * Copyright (c) 2005, 2006, 2007 Los Alamos National Security, LLC.
 * This material was produced under U.S. Government contract DE-AC52-06NA25396
 * for Los Alamos National Laboratory (LANL), which is operated by the Los Alamos
 * National Security, LLC (LANS) for the U.S. Department of Energy.  The U.S. Government has
 * rights to use, reproduce, and distribute this software. NEITHER THE
 * GOVERNMENT NOR LANS MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified
 * to produce derivative works, such modified software should be clearly marked,
 * so as not to confuse it with the version available from LANL.
 *
 * Additionally, this program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.ptp.core.elements.events;

import java.util.BitSet;

import org.eclipse.ptp.core.elements.IPElement;
import org.eclipse.ptp.core.elements.IPJob;

/**
 * This event is generated when one or more processes are removed. It is a bulk event that is sent to child listeners on the source
 * element. Since processes belong to a job, but can also be associated with a node, the source element can be either a job or node.
 * 
 * @see org.eclipse.ptp.core.elements.listeners.IJobChildListener
 * @see org.eclipse.ptp.core.elements.listeners.INodeChildListener
 */
@Deprecated
public interface IRemoveProcessEvent {

	/**
	 * Get the job that used to own these processes
	 * 
	 * @return the job that used to own these processes
	 * @since 4.0
	 */
	public IPJob getJob();

	/**
	 * Get the removed processes
	 * 
	 * @return the removed process
	 * @since 4.0
	 */
	public BitSet getProcesses();

	/**
	 * Get the event source
	 * 
	 * @return the source of the event
	 */
	public IPElement getSource();

}
