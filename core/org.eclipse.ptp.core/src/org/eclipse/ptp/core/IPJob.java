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

public interface IPJob extends IPElement {
	/* helper functions to get the nodes that this job is running on.  This should
	 * be accomplished by going through all the processes of this job and seeing
	 * which nodes they are running on
	 */
    public IPNode[] getNodes();
    public IPNode[] getSortedNodes();

    public IPProcess findProcess(String processNumber);
    public IPProcess findProcessByName(String pname);
	
    public String getJobNumber();
    
	public IPProcess[] getSortedProcesses();
	public IPProcess[] getProcesses();
	
	public int totalNodes();
	public int totalProcesses();	
	public void removeAllProcesses();
	
	/* returns an array of machines that this job is running on.  For many cases
	 * this will be a single element array as a job often resides on a single
	 * machine
	 */
	public IPMachine[] getMachines();
	
	/* gets the parent universe that this job is running inside of */
	public IPUniverse getUniverse();
}
