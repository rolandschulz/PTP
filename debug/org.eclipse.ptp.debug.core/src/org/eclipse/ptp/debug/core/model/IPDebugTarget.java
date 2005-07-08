package org.eclipse.ptp.debug.core.model;

import org.eclipse.cdt.debug.core.model.ICDebugTarget;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IThread;

public interface IPDebugTarget extends ICDebugTarget {
	/**
	 * The IPDebugTarget always has one process, this method checks
	 * whether the debug target is a multi-processes target or not.
	 * 
	 * @return
	 * @throws DebugException
	 */
	public boolean hasProcesses() throws DebugException;
	
	/**
	 * The method returns all processes in the debug target including
	 * process 0. In a single-process target, it behaves the same like
	 * IProcess getProcess() (except for the return type).
	 * 
	 * @return
	 */
	public IProcess[] getProcesses();
	
	public IThread[] getProcessThreads(IProcess process) throws DebugException;
}
