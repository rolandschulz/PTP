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

package org.eclipse.ptp.rtmodel;

public interface IRuntimeModel {
	/* constructor will establish a new parallel session / a handle */
	public NamedEntity[] getMachines();
	public NamedEntity[] getNodes(String machineName);
	public NamedEntity[] getJobs();
	public NamedEntity[] getProcesses(String jobName);
	
	public String getProcessNodeName(String procName);
	public String getNodeMachineName(String nodeName);
	public String getProcessStatus(String procName);
	public String getProcessExitCode(String procName);
	public String getProcessSignal(String procName);
	
	public String getNodeAttribute(String nodeName, String attrib);
	
	/* returns the new job name string that it spawns, unique */
	public NamedEntity run(String[] args);
	public NamedEntity abortJob();
	
	/* event stuff */
	public void addRuntimeListener(IRuntimeListener listener);
	public void removeRuntimeListener(IRuntimeListener listener);
	public void shutdown();
}
