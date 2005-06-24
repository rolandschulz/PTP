package org.eclipse.ptp.launch.internal;

public class JobRunConfiguration {
	private String pathToExec;
	private int numberOfProcesses;
	private int numberOfProcessesPerNode;
	private int firstNodeNumber;
	
	public JobRunConfiguration(String p, int np, int npp, int fn)
	{
		pathToExec = p;
		numberOfProcesses = np;
		numberOfProcessesPerNode = npp;
		firstNodeNumber = fn;
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
}
