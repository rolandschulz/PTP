package org.eclipse.ptp.internal.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ptp.core.IPElement;
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.core.IPMachine;
import org.eclipse.ptp.core.IPNode;
import org.eclipse.ptp.core.IPProcess;
import org.eclipse.ptp.core.IPUniverse;

public class PJob extends Parent implements IPJob {
    protected String NAME_TAG = "root ";
    
	public PJob(IPUniverse uni, String name) {
		super(uni, name, P_JOB);
	}
	
	/* returns the Machines that this job is running on.  this is accomplished by
	 * drilling down to the processes, finding the nodes they are running on, 
	 * and then seeing which machines those nodes are part of
	 */
	public IPMachine[] getMachines() {
		IPNode[] nodes = getNodes();
		List array = new ArrayList(0);
		for(int i=0; i<nodes.length; i++) {
			if(!array.contains(nodes[i].getMachine())) {
				array.add(nodes[i].getMachine());
			}
		}
		
		return (IPMachine[])array.toArray(new IPMachine[array.size()]);
	}
	
	public synchronized IPNode[] getSortedNodes() {
	    IPNode[] nodes = getNodes();
	    sort(nodes);
	    return nodes;
	}

	public synchronized IPNode[] getNodes() {
		IPProcess[] processes = getProcesses();
		List array = new ArrayList(0);
		for(int i=0; i<processes.length; i++) {
			if(!array.contains(processes[i].getNode())) {
				array.add(processes[i].getNode());
			}
		}
		
		return (IPNode[])array.toArray(new IPNode[array.size()]);
	}
	
	/* returns all the processes in this job, which are the children of
	 * the job
	 */
	public synchronized IPProcess[] getProcesses() {
	    return (IPProcess[])getCollection().toArray(new IPProcess[size()]);
	}
	
	public synchronized IPProcess[] getSortedProcesses() {
	    IPProcess[] processes = getProcesses();
	    sort(processes);
	    return processes;
	}
	
	public synchronized IPProcess findProcess(String processNumber) {
		IPElement element = findChild(processNumber);
		if(element != null)
			return (IPProcess)element;
		return null;
	}
	
	/* returns the number of nodes that this job is running on by
	 * counting each node that each process in this job is running
	 * on
	 */
	public int totalNodes() {
		return getNodes().length;
	}
	
	public int totalProcesses() {
		return size();
	}
	
	public void removeAllProcesses() {
	    IPProcess[] processes = getProcesses();
        for (int i=0; i<processes.length; i++)
            processes[i].clearOutput();
        
        removeChildren();
	}

	public IPUniverse getUniverse() {
		IPElement current = this;
		do {
			if (current instanceof IPUniverse) return (IPUniverse) current;
		} while ((current = current.getParent()) != null);
		return null;
	}
}
