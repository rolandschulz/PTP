package org.eclipse.ptp.debug.core.cdi.model;

import org.eclipse.debug.core.DebugException;
import org.eclipse.ptp.core.IPProcess;

public interface IPCDIDebugProcess extends IPCDIDebugFocus {
	/**
	 * Modeled after IDebugTarget.getThreads();
	 * 
	 * @return
	 * @throws DebugException
	 */
	public Thread[] getThreads() throws DebugException;
	
	/**
	 * Modeled after IDebugTarget.hasThreads();
	 * 
	 * @return
	 * @throws DebugException
	 */
	public boolean hasThreads() throws DebugException;
	
	public IPProcess getPProcess();
	
	public int getPProcessNumber();
}
