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
	
	public void setUser(String user);

    public String getUser();

    public boolean isCurrentUser();

    public String getGroup();

    public void setGroup(String group);

    public String getMode();

    public void setMode(String mode);

    public String getState();

    public void setState(String state);
    
    /* returns the parent machine that comprises this node */
    public IPMachine getPMachine();
    
    /* returns an array of jobs that are running on this node - accomplishes this
     * by looking through the processes that are running on this node and seeing
     * which parent jobs they belong to
     */
    public IPJob[] getJobs();
}
