package org.eclipse.ptp.core;

import org.eclipse.pdt.mi.MISession;

/**
 * @author Clement
 *
 */
public interface IPRoot extends IPElement {
    public IPNode[] getNodes();
    public IPNode[] getSortedNodes();
    
    public IPNode findNode(String nodeNumber);
	public MISession getMISession();
	
	public IPProcess findProcess(String nodeNumber, String processNumber);
	public IPProcess findProcess(String processNumber);
	
	public IPProcess[] getSortedProcesses();
	public IPProcess[] getProcesses();
	
	public int totalNodes();
	public int totalProcesses();	
	public void removeAllProcesses();
	
	public String getOutputStoreDirectory();
	public int getStoreLine();	
}
