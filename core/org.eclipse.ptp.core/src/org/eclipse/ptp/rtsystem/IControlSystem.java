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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.core.IPProcess;
import org.eclipse.ptp.rtsystem.JobRunConfiguration;

/**
 * A Control System is a portion of a runtime system that handles controlling jobs.  This includes
 * starting new jobs, terminating jobs, getting information about running jobs and processes, etc.
 * Control Systems also can fire events, specifically {@link RuntimeEvent}s.
 * 
 * @author Nathan DeBardeleben
 */

public interface IControlSystem {	
	/**
	 * The control system can still be available even if the connection to the outside world
	 * has been severed.  This method returns if that connection is alive or not.
	 */
	public boolean isHealthy();
	
	/**
	 * Performs a job run given the jobRunConfig.  The JobRunConfiguration contains specifics
	 * about how the user wants to run the job, such as the program name, number of
	 * processes, etc.
	 * 
	 * @param jobRunConfig the configuration information about the job
	 * @return the job identifier of the created job or -1 on error
	 * @throws CoreException 
	 * @see JobRunConfiguration
	 */
	public int run(JobRunConfiguration jobRunConfig) throws CoreException;

	/**
	 * Terminates a running job.  The {@link IPJob} contains the job identifier used to 
	 * locate the job by the control system.
	 * 
	 * @param job the job to terminate
	 * @throws CoreException 
	 */
	public void terminateJob(IPJob job) throws CoreException;
	
	/**
	 * Returns an array of {@link String}s of the form [ job1", "job2", "job10" ].
	 * The jobs should be a concat of the string "job" and the identifier.
	 * 
	 * @return the names of the known jobs
	 * @throws CoreException 
	 */
	public String[] getJobs() throws CoreException;

	/**
	 * Returns an array of {@link String}s of the form [ "jobXprocessA", "jobXprocessB" ] where
	 * each process is part of the specified job.
	 * 
	 * @param job the job to look for processes on 
	 * @return the names of the processes
	 * @throws CoreException 
	 */
	public String[] getProcesses(IPJob job) throws CoreException;
	
	/**
	 * Gets the specified attributes for all processes of the specified job.  The
	 * attributes are space separated in a string of the form "PID NODE_NUMBER".  The
	 * attributes are defined in {@link org.eclipse.ptp.core.AttributeConstants}.  The return array has a size
	 * of num_procs * num_attribs and is of the form [ proc0attrib0, proc0attrib1, ... proc0attribN,
	 * proc1attrib0 ...]
	 * 
	 * @param job the job to get process attributes from
	 * @param array of attribs to fetch
	 * @return an array of the attribute values for each process
	 * @throws CoreException 
	 * @see org.eclipse.ptp.core.AttributeConstants
	 */
	public String[] getAllProcessesAttributes(IPJob job, String[] attribs) throws CoreException;
	
	/**
	 * Gets the specified attributes for a specific process in a specific job.  The
	 * attributes are space separated in a string of the form "PID NODE_NUMBER".  The
	 * attributes are defined in {@link org.eclipse.ptp.core.AttributeConstants}.  The
	 * return array's length is equal to the number of attributes requested.
	 * 
	 * @param proc the process contained in the job to get the process attributes from
	 * @param array of attribs to fetch
	 * @return an array of the attribute values
	 * @throws CoreException 
	 */
	public String[] getProcessAttributes(IPProcess proc, String[] attribs) throws CoreException;

	/**
	 * Adds a listener to the control system.  The control system may fire {@link RuntimeEvent}s
	 * and will use this list of listeners to determine who to send these events to.
	 * 
	 * @param listener someone that wants to listener to {@link RuntimeEvent}s
	 * @see RuntimeEvent
	 */
	public void addRuntimeListener(IRuntimeListener listener);

	/**
	 * Removes a listener from the list of things listening to {@link RuntimeEvent}s on
	 * this control system.
	 * 
	 * @param listener the listener to remove
	 * @see RuntimeEvent
	 */
	public void removeRuntimeListener(IRuntimeListener listener);

	/**
	 * Called after the control system is contructed.  The implementation can use this
	 * to handle any startup functionality.
	 */
	public void startup();
	
	/**
	 * Called when the control system is being shutdown.  Usually this is either at
	 * close of the PTP plug-in or when switching control systems.  The implementation
	 * needs to handle any cleanup that is required at this time.
	 */
	public void shutdown();
}
