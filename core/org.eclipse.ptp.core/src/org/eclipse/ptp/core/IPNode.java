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
}
