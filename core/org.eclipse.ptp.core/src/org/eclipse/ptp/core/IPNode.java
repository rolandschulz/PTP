package org.eclipse.ptp.core;

/**
 * @author Clement
 *
 */
public interface IPNode extends IPElement {
    public IPProcess[] getProcesses();
    public IPProcess[] getSortedProcesses();
    
    public String getNodeNumber();
	public IPProcess findProcess(String processNumber);

    /* returns the parent machine that comprises this node */
    public IPMachine getMachine();
    
    /* returns an array of jobs that are running on this node - accomplishes this
     * by looking through the processes that are running on this node and seeing
     * which parent jobs they belong to
     */
    public IPJob[] getJobs();
    
    public void setAttrib(String key, Object val);
    
    public Object getAttrib(String key);
}
