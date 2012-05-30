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

import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.core.elements.IPElement;
import org.eclipse.ptp.core.elements.IPJob;

/**
 * This event is generated when the attributes on one or more processes have changed. It is a bulk event that is sent to child
 * listeners on the source job.
 * 
 * @see org.eclipse.ptp.core.elements.listeners.IJobChildListener
 */
@Deprecated
public interface IChangedProcessEvent {

	/**
	 * Get the attributes that have changed
	 * 
	 * @return changed attributes
	 * @since 4.0
	 */
	public AttributeManager getAttributes();

	/**
	 * Get the job that owns these changed processes
	 * 
	 * @return the job that owns these changed processes
	 * @since 4.0
	 */
	public IPJob getJob();

	/**
	 * Get the processes that have changed
	 * 
	 * @return processes that have changed
	 * @since 4.0
	 */
	public BitSet getProcesses();

	/**
	 * Get the source of the event. This event can come from either a job or a node.
	 * 
	 * @return the source of the event
	 */
	public IPElement getSource();
}
