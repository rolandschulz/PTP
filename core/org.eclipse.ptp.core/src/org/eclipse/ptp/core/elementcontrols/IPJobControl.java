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
package org.eclipse.ptp.core.elementcontrols;

import java.util.Collection;

import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.elements.IPJob;

public interface IPJobControl extends IPElementControl, IPJob {

	/**
	 * Add attributes to a collection of processes.
	 * 
	 * @param processes collection of IPProcessControl
	 * @param attrs array of attributes to add to each process
	 */
	public void addProcessAttributes(Collection<IPProcessControl> processes, IAttribute<?,?,?>[] attrs);

	/**
	 * Add the collection of processes to the job.
	 * 
	 * @param processes collection of IPProcessControls
	 */
	public void addProcesses(Collection<IPProcessControl> processes);
	
	/**
	 * Get all the processes known by this job
	 * 
	 * @return collection of processes
	 */
	public Collection<IPProcessControl> getProcessControls();

	/**
	 * Get the parent of this job
	 * 
	 * @return parent queue
	 */
	public IPQueueControl getQueueControl();
	
	/**
	 * Remove processes from the job
	 * 
	 * @param processes to remove
	 */
	public void removeProcesses(Collection<IPProcessControl> processes);
}
