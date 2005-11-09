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

public class JobRunConfiguration {
	protected String pathToExec;
	protected String machineName;
	protected int numberOfProcesses;
	protected int numberOfProcessesPerNode;
	protected int firstNodeNumber;
	protected int remoteInfo;
	protected boolean isDebugJob = false;
	
	public JobRunConfiguration(String p, String mn, int np, int npp, int fn, boolean debug)
	{
		pathToExec = p;
		machineName = mn;
		numberOfProcesses = np;
		numberOfProcessesPerNode = npp;
		firstNodeNumber = fn;
		isDebugJob = debug;
		remoteInfo = 0;
	}
	
	public String getPathToExec()
	{
		return pathToExec;
	}
	
	public String getMachineName()
	{
		return machineName;
	}
	
	public int getNumberOfProcesses()
	{
		return numberOfProcesses;
	}
	
	public int getNumberOfProcessesPerNode()
	{
		return numberOfProcessesPerNode;
	}
	
	public int getFirstNodeNumber()
	{
		return firstNodeNumber;
	}
	
	public int getRemoteInfo()
	{
		return remoteInfo;
	}
	
	public boolean isDebug()
	{
		return isDebugJob;
	}
	
	public void setDebug()
	{
		isDebugJob = true;
	}
	
	public void setRemoteInfo(int val)
	{
		remoteInfo = val;
	}
	
	public String toString()
	{
		return "path:\t\t"+pathToExec+"\n"+
		       "machineName:\t"+machineName+"\n"+
		       "#procs:\t\t"+numberOfProcesses+"\n"+
		       "#proc/node:\t"+numberOfProcessesPerNode+"\n"+
		       "firstNode#:\t"+firstNodeNumber+"\n"+
		       "isDebug?\t\t"+isDebugJob;
	}
}
