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

import org.eclipse.ptp.core.elements.attributes.ProcessAttributes;


public interface IPProcess extends IPElement {
	
	/**
	 * sets the node that this process is running on 
	 * 
	 * @param node
	 */
	public void addNode(IPNode node);

	/**
	 * @param output
	 */
	public void addOutput(String output);
	
	/**
	 * 
	 */
	public void clearOutput();
	
	/**
	 * @return
	 */
	public String getContents();
	
	/**
	 * @return
	 */
	public int getExitCode();
	
	/**
	 * Find the parent job that this process is encompassed by 
	 * 
	 * @return
	 */
	public IPJob getJob();
	
	/**
	 * Find the node that this process is running on
	 * 
	 * @return
	 */
	public IPNode getNode();
	
	/**
	 * @return
	 */
	public String[] getOutputs();
	
	/**
	 * @return
	 */
	public int getPid();
	
	/**
	 * @return
	 */
	public String getSignalName();
	
	/**
	 * @return
	 */
	public ProcessAttributes.State getState();
	
	/**
	 * @return
	 */
	public String getProcessNumber();
	
	/**
	 * @return
	 */
	public boolean isTerminated();
	
	/**
	 * Remove us from node we are running on
	 */
	public void removeNode();
	
	/**
	 * @param status
	 */
	public void setState(ProcessAttributes.State state);
	
	/**
	 * @param isTerminate
	 */
	public void setTerminated(boolean isTerminate);
}
