package org.eclipse.ptp.debug.core.launch;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.model.IPDebugTarget;

/**
 * Extend ILaunch to support parallel debug jobs
 *
 */
public interface IPLaunch extends ILaunch {
	/**
	 * Get the IPDebugTarget responsible for the processes in procs
	 * 
	 * @param tasks BitList containing processes we're interested in
	 * @return IPDebugTarget responsible for processes
	 */
	public IPDebugTarget getDebugTarget(BitList procs);
	
	/**
	 * Get the IPDebugTarget responsible for the procId.
	 * 
	 * @param procId process we're interested in
	 * @return IPDebugTarget responsible for procId
	 */
	public IPDebugTarget getDebugTarget(int procId);
	
	/**
	 * Get the job associated with this launch
	 * 
	 * @return IPJob
	 */
	public IPJob getPJob();
	
	/**
	 * Set the job associated with this launch
	 * 
	 * @param job
	 */
	public void setPJob(IPJob job);
}
