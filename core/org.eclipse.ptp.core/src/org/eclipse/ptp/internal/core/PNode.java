package org.eclipse.ptp.internal.core;

import org.eclipse.ptp.core.IPElement;
import org.eclipse.ptp.core.IPNode;
import org.eclipse.ptp.core.IPProcess;
import org.eclipse.ptp.core.IPMachine;


/**
 * @author Clement
 *
 */
public class PNode extends Parent implements IPNode {
    protected String NAME_TAG = "node ";
    private String user = null;
    private String group = null;
    private String state = null;
    private String mode = null;
    	
    public PNode(IPElement element, String nodeNumber) {
		this(element, nodeNumber, null, null, null, null);
	}
    
    public PNode(IPElement element, String nodeNumber, String user, String group, String state, String mode) {
        super(element, nodeNumber, P_NODE);
        this.user = user;
        this.group = group;
        this.state = state;
        this.mode = mode;
    }
	
	public IPMachine getPMachine() {
		IPElement current = this;
		do {
			if (current instanceof IPMachine) return (IPMachine) current;
		} while ((current = current.getParent()) != null);
		return null;
	}
    
	public String getNodeNumber() {
	    return getKey();
	}
	
    public IPProcess[] getProcesses() {
        return (IPProcess[])getCollection().toArray(new IPProcess[size()]);
	}
    
	public IPProcess[] getSortedProcesses() {
	    IPProcess[] processes = getProcesses();
	    sort(processes);
	    return processes;
	}
    
    public IPProcess findProcess(String processNumber) {
        IPElement element = findChild(processNumber);
        if (element != null)
            return (IPProcess)element;
        return null;
    }
    
    public String getElementName() {
        return NAME_TAG + getKey();
    }
    
    public void setUser(String user) {
        this.user = user;
    }
    public String getUser() {
        return user;
    }
    public boolean isCurrentUser() {
        return user.equals(System.getProperty("user.name"));
    }
    public String getGroup() {
        return group;
    }
    public void setGroup(String group) {
        this.group = group;
    }
    public String getMode() {
        return mode;
    }
    public void setMode(String mode) {
        this.mode = mode;
    }
    public String getState() {
        return state;
    }
    public void setState(String state) {
        this.state = state;
    }
}
