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
package org.eclipse.ptp.core;

/**
 *
 */
public interface IPMachine extends IPElement {
    public IPNode[] getNodes();
    public IPNode[] getSortedNodes();
    
    public IPNode findNode(String nodeNumber);
	 
	public IPProcess[] getSortedProcesses();
	public IPProcess[] getProcesses();
	
	public int totalNodes();
	public int totalProcesses();	
	public void removeAllProcesses();
	
	/* returns the parent universe */
	public IPUniverse getUniverse();
	
	/* gets all the jobs that are running on this machine - should do this by
	 * seeing which processes are mapped to the nodes comprised by this machine
	 * and then looking at which jobs they reside under
	 */
	public IPJob[] getJobs();
	
	/*
	public String getOutputStoreDirectory();
	public int getStoreLine();
	*/

	/* returns a String representing the architecture in some form */
	public String getArch();
	/* sets the architecture, should be used by instantiating classes and such */
	public void setArch(String arch);
}
