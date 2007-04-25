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

package org.eclipse.ptp.core.elements;

import org.eclipse.ptp.rmsystem.IResourceManager;


/**
 * A Universe represents a user's view of the world. It is comprised of a set of
 * Machines and Jobs. This class has helper methods to get access to those sets
 * as well as other things down inside those sets (such as finding a node, which
 * may require searching all machines).
 * 
 * @author Nathan DeBardeleben
 * @see IPMachine
 * @see IPJob
 */
public interface IPUniverse /*extends IPElement*/ {
	public void deleteJob(IPJob job);

	/**
	 * @param job_id
	 * @return
	 */
	public IPJob findJobById(String job_id);

	/**
	 * @param machine_id
	 * @return
	 */
	public IPMachine findMachineByGlobalId(String machin_id);
		
	/**
	 * @param id
	 * @return
	 */
	public IPQueue findQueueById(String id);

	/**
	 * @param id
	 * @return
	 */
	public IResourceManager findResourceManagerById(String id);

	/**
	 * Returns all the Jobs that are visible by this Universe, or null if there
	 * are none.
	 * 
	 * @return An array of all Jobs in this Universe, null if there are none
	 */
	public IPJob[] getJobs();
	
	/**
	 * Returns all the Machines that are visible by this Universe, or null if
	 * there are none.
	 * 
	 * @return An array of all Machines in this Universe, null if there are none
	 */
	public IPMachine[] getMachines();
	
	/**
	 * @return
	 */
	public IPQueue[] getQueues();
	
	/**
	 * @return
	 */
	public IResourceManager[] getResourceManagers();

}
