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


/**
 * A Job can be a parallel or sequential job, consisting of one or more
 * processes which are residing on Nodes. Jobs may span Machines, though this
 * may not be very common in reality, but the model allows for this. Finally, a
 * Job may or may not belong to the current user, it just means that if it is
 * visible then this user can see the Job in his or her Universe. This may have
 * ramifications on what the user can do to the Job if it is not owned by them.
 * 
 * @author Nathan DeBardeleben
 */
public interface IPJob extends IPElement {
	
	/**
	 * Returns true if all processes in the job have terminated
	 * 
	 * @return
	 */
	public boolean isTerminated();
	
	/**
	 * Find a Process in this Job by its ID. Returns the Process
	 * object if found, else returns <code>null</code>.
	 * 
	 * @param processNumber
	 *            The Process number to search for
	 * @return The Process object if found, else <code>null</code>
	 * @see IPProcess
	 */
	public IPProcess getProcessById(String id);

	/**
	 * Finds a Process in by its task ID. Returns the Process
	 * object if found, else returns <code>null</code>.
	 * 
	 * @param taskId
	 *            The Process task ID to search for
	 * @return The Process object if found, else <code>null</code>
	 */
	public IPProcess getProcessByTaskId(int taskId);

	/**
	 * Returns an array of the Processes comprised by this Job. Might return
	 * <code>null</code> if no Processes have yet been assigned.
	 * 
	 * @return The Processes in this Job.
	 */
	public IPProcess[] getProcesses();

	/**
	 * Returns parent queue for this job.
	 * 
	 * @return IPQueue
	 */
	public IPQueue getQueue();

	/**
	 * Returns a sorted array of the Processes comprised by this Job. Might
	 * return <code>null</code> if no Processes have yet been assigned.
	 * 
	 * @return
	 */
	public IPProcess[] getSortedProcesses();

	/**
	 * Returns true/false regarding whether this Job is a debug job 
	 * 
	 * @return True if this job is a debug job
	 */
	public boolean isDebug();
	
	/**
	 * Remove all processes from this job
	 *
	 */
	public void removeAllProcesses();
	
	/**
	 * Sets this job to be a debug job
	 *
	 */
	public void setDebug();

	/**
	 * Returns the number of Processes in this Job.
	 * 
	 * @return The number of Processes in this Job.
	 */
	public int totalProcesses();
}
