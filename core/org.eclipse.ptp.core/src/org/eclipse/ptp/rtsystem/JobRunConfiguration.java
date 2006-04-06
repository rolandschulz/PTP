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

/**
 * This is a complete description needed to run or debug a parallel job
 * 
 * execName					The name of the executable (can be a relative path).
 * pathToExec				When appended to execName, gives the absolute path to the executable. 
 * 							Can be null.
 * machineName				The name of the machine (from model) on which to run the job.
 * numberOfProcesses			Number of processes to start.
 * numberOfProcessesPerNode	Number of processes to start on each node.
 * firsNodeNumber			Node number (from model) of first node to use in the allocation.
 * workingDir				The working directory for the job. If pathToExec is null, this will be used
 * 							locate the executable.
 * arguments					The arguments passed to the program. Can be null.
 * environment				The environment passed to the program. Can be null.
 * debuggerPath				Name (or path) to the debugger executable.
 * debuggerArgs				Arguments to be passed to the debugger.
 * isDebugJob				True if this is a debug job.
 */
public class JobRunConfiguration {
	protected String execName;
	protected String pathToExec;
	protected String machineName;
	protected int numberOfProcesses;
	protected int numberOfProcessesPerNode;
	protected int firstNodeNumber;
	protected String workingDir;
	protected String[] arguments;
	protected String[] environment;
	protected String debuggerPath;
	protected String[] debuggerArgs;
	protected boolean isDebugJob;
	
	public JobRunConfiguration(String exe, String exePath, String machine, int nprocs, int npernode, int first, String[] args, String[] env, String dir)
	{
		execName = exe;
		pathToExec = exePath;
		machineName = machine;
		numberOfProcesses = nprocs;
		numberOfProcessesPerNode = npernode;
		firstNodeNumber = first;
		workingDir = dir;
		arguments = args;
		environment = env;
		isDebugJob = false;
		debuggerPath = null;
		debuggerArgs = null;
	}
	
	public String getExecName()
	{
		return execName;
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

	public String getWorkingDir()
	{
		return workingDir;
	}

	public String[] getArguments()
	{
		return arguments;
	}

	public String[] getEnvironment()
	{
		return environment;
	}

	public String getDebuggerPath()
	{
		return debuggerPath;
	}
	
	public String[] getDebuggerArgs()
	{
		return debuggerArgs;
	}
	
	public boolean isDebug()
	{
		return isDebugJob;
	}
	
	public void setDebug()
	{
		isDebugJob = true;
	}
	
	public void setDebuggerPath(String path)
	{
		debuggerPath = path;
	}
	
	public void setDebuggerArgs(String args)
	{
		debuggerArgs = args.split(" ");
	}
	
	public String toString()
	{
		return "name:\t\t"+execName+"\n"+
				"path:\t\t"+pathToExec+"\n"+
				"cwd:\t\t"+workingDir+"\n"+
				"machineName:\t"+machineName+"\n"+
				"#procs:\t\t"+numberOfProcesses+"\n"+
				"#proc/node:\t"+numberOfProcessesPerNode+"\n"+
				"firstNode#:\t"+firstNodeNumber+"\n"+
				"isDebug?\t\t"+isDebugJob;
	}
}
