package org.eclipse.ptp.core;

/**
 * @author Clement
 *
 */
public interface IPMachine extends IPElement {
    public IPNode[] getNodes();
    public IPNode[] getSortedNodes();
    
    public IPNode findNode(String nodeNumber);
	
	public IPProcess findProcess(String nodeNumber, String processNumber);
	public IPProcess findProcess(String processNumber);
	 
	public IPProcess[] getSortedProcesses();
	public IPProcess[] getProcesses();
	
	public int totalNodes();
	public int totalProcesses();	
	public void removeAllProcesses();
	
	/* returns the parent universe */
	public IPUniverse getPUniverse();
	
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
