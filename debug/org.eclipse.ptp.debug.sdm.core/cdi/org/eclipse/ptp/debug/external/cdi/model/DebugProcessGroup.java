package org.eclipse.ptp.debug.external.cdi.model;

import java.util.ArrayList;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIDebugProcess;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIDebugProcessSet;

public class DebugProcessGroup implements IPCDIDebugProcessSet {
	
	private ArrayList pGroup;
	private String pGroupName;

	public DebugProcessGroup(String name) {
		pGroup = new ArrayList();
		pGroupName = name;
	}
	
	public IPCDIDebugProcess[] getProcesses() {
		// Auto-generated method stub
		System.out.println("DebugProcessSet.getProcesses()");
		
		IPCDIDebugProcess[] result = new IPCDIDebugProcess[pGroup.size()];
		pGroup.toArray(result);
		return result;
	}
	
	public IPCDIDebugProcess getProcess(int number) {
		return (IPCDIDebugProcess) pGroup.get(number);
	}
	
	public void addProcess(IPCDIDebugProcess proc) {
		if(!pGroup.contains(proc))
			pGroup.add(proc);
	}
	
	public void removeProcess(IPCDIDebugProcess proc) {
		if(pGroup.contains(proc))
			pGroup.remove(proc);
	}
	
	public String getName() {
		return pGroupName;
	}
}
