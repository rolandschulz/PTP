package org.eclipse.ptp.core;

import org.eclipse.pdt.mi.MISession;

/**
 * @author Clement
 *
 */
public interface IPMachine extends IPElement {
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
	
	/* returns a String representing the architecture in some form */
	public String getArch();
	/* sets the architecture, should be used by instantiating classes and such */
	public void setArch(String arch);
}
