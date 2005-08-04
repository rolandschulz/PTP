package org.eclipse.ptp.rtsystem;

public class JobRunConfiguration {
	protected String pathToExec;
	protected int numberOfProcesses;
	protected int numberOfProcessesPerNode;
	protected int firstNodeNumber;
	protected boolean isDebugJob = false;
	
	public JobRunConfiguration(String p, int np, int npp, int fn, boolean debug)
	{
		pathToExec = p;
		numberOfProcesses = np;
		numberOfProcessesPerNode = npp;
		firstNodeNumber = fn;
		isDebugJob = debug;
	}
	
	public String getPathToExec()
	{
		return pathToExec;
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
	
	public String toString()
	{
		return "path:\t\t"+pathToExec+"\n"+
		       "#procs:\t\t"+numberOfProcesses+"\n"+
		       "#proc/node:\t"+numberOfProcessesPerNode+"\n"+
		       "firstNode#:\t"+firstNodeNumber+"\n";
	}
	
	public boolean isDebug()
	{
		return isDebugJob;
	}
	
	public void setDebug()
	{
		isDebugJob = true;
	}
}
