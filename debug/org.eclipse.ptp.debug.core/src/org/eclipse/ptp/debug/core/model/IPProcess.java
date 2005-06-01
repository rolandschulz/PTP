package org.eclipse.ptp.debug.core.model;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IThread;

public interface IPProcess extends IProcess {
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
