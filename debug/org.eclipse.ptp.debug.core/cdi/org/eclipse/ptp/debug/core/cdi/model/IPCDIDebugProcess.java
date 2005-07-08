package org.eclipse.ptp.debug.core.cdi.model;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IThread;

public interface IPCDIDebugProcess {
	/**
	 * Modeled after IDebugTarget.getThreads();
	 * 
	 * @return
	 * @throws DebugException
	 */
	public IThread[] getThreads() throws DebugException;
	
	/**
	 * Modeled after IDebugTarget.hasThreads();
	 * 
	 * @return
	 * @throws DebugException
	 */
	public boolean hasThreads() throws DebugException;
}
