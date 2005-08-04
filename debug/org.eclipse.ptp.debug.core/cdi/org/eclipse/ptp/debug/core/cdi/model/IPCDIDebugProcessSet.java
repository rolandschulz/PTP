package org.eclipse.ptp.debug.core.cdi.model;

public interface IPCDIDebugProcessSet extends IPCDIDebugFocus {
	public IPCDIDebugProcess[] getProcesses();
	public IPCDIDebugProcess getProcess(int number);
	public void addProcess(IPCDIDebugProcess proc);
	public void removeProcess(IPCDIDebugProcess proc);
}
