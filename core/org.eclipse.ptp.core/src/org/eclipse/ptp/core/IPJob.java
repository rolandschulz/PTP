package org.eclipse.ptp.core;

import org.eclipse.pdt.mi.MISession;

/**
 * @author Clement
 *
 */
public interface IPJob extends IPElement {
	/* helper functions to get the nodes that this job is running on.  This should
	 * be accomplished by going through all the processes of this job and seeing
	 * which nodes they are running on
	 */
    public IPNode[] getNodes();
    public IPNode[] getSortedNodes();

    public IPProcess findProcess(String processNumber);
	
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
