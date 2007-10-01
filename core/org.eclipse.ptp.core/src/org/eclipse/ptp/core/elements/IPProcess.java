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

import org.eclipse.ptp.core.attributes.StringAttributeDefinition;
import org.eclipse.ptp.core.elements.attributes.ProcessAttributes;
import org.eclipse.ptp.core.elements.listeners.IProcessListener;

public interface IPProcess extends IPElement {
	
	/**
	 * Add listener for element events.
	 * 
	 * @param listener
	 */
	public void addElementListener(IProcessListener listener);

	/**
	 * Clear the local cache of process output
	 */
	public void clearOutput();

	/**
	 * Get the value of the exit code attribute. This is the exit code
	 * that the process returned on termination.
	 * 
	 * @return value of the exit code attribute
	 */
	public int getExitCode();
	
	/**
	 * Find the parent job that this process is encompassed by 
	 * 
	 * @return parent job
	 */
	public IPJob getJob();
	
	/**
	 * Find the node that this process is running on
	 * 
	 * @return node that the process is running on
	 */
	public IPNode getNode();
	
	/**
	 * Get the value of the PID attribute
	 * 
	 * @return value of the PID attribute
	 */
	public int getPid();
	
	/**
	 * Returns a zero-based index of the process in a job.
	 * 
	 * @return process index
	 */
	public String getProcessIndex();
	
	/**
	 * @param attrDef
	 * @return
	 */
	public String getSavedOutput(StringAttributeDefinition attrDef);
	
	/**
	 * Get the value of the signal name attribute. If the process was
	 * terminated by a signal, this is the name of the signal that caused
	 * the termination.
	 * 
	 * @return value of the signal name attribute
	 */
	public String getSignalName();
	
	/**
	 * Get the state of the process
	 * 
	 * @return process state
	 */
	public ProcessAttributes.State getState();
	
	/**
	 * Check if process is terminated
	 * 
	 * @return true if process terminated
	 */
	public boolean isTerminated();
	
	/**
	 * Remove listener for element events.
	 * 
	 * @param listener
	 */
	public void removeElementListener(IProcessListener listener);
	
	/**
	 * Set the state attribute on the process
	 * 
	 * @param state of the process
	 */
	public void setState(ProcessAttributes.State state);
	
	/**
	 * Set the value of the terminated attribute on the process
	 * 
	 * @param boolean representing termination status of the process
	 */
	public void setTerminated(boolean isTerminate);
}
