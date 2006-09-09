/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.debug.core.cdi;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.IAbstractDebugger;
import org.eclipse.ptp.debug.core.cdi.model.IPCDITarget;
import org.eclipse.ptp.debug.core.launch.IPLaunch;

public interface IPCDISession extends ICommonActions {
	/** Start debugger
	 * @param monitor
	 * @throws CoreException
	 */
	public void start(IProgressMonitor monitor) throws CoreException;
	
	/** Get debug targets
	 * @return
	 */
	IPCDITarget[] getTargets();
	/** Set attribute to session
	 * @param key
	 * @param value
	 */
	void setAttribute(String key, String value);
	/** Get value in attribute
	 * @param key
	 * @return
	 */
	String getAttribute(String key);
	/** Get pcid event manager
	 * @return
	 */
	IPCDIEventManager getEventManager();
	/** Get pcdi breakpoint manager
	 * @return
	 */
	IPCDIBreakpointManager getBreakpointManager();
	/** Get pcdi session configuration
	 * @return
	 */
	IPCDISessionConfiguration getConfiguration();
	/** Terminate debugger
	 * @throws PCDIException
	 */
	void terminate() throws PCDIException;
	/** Get session process
	 * @return
	 * @throws PCDIException
	 */
	Process getSessionProcess() throws PCDIException;
	
	/** Register process to Debug View
	 * @param tasks process ids
	 * @param sendEvent true to send event to UI
	 * @param resumeTarget true to resume this process
	 */
	public void registerTargets(BitList tasks, boolean sendEvent, boolean resumeTarget);
	/** Register processes to Debug View
	 * @param tasks process ids
	 * @param sendEvent true to send event to UI
	 */
	public void registerTargets(BitList tasks, boolean sendEvent);
	/** Unregister process to Debug View
	 * @param tasks process ids
	 * @param sendEvent true to send event to UI
	 */
	public void unregisterTargets(BitList tasks, boolean sendEvent);
	/** Get Job
	 * @return
	 */
	public IPJob getJob();
	/** Get total processes in this session
	 * @return total processes
	 */
	public int getTotalProcesses();
	
	/** Create an empty BitList and set range 0 to total processes
	 * @return
	 */
	public BitList createBitList();
	/** Create an empty BitList
	 * @return
	 */
	public BitList createEmptyBitList();
	/** Create an empty BitList and set range to given index
	 * @param index set range to 
	 * @return
	 */
	public BitList createBitList(int index);
	
	/** Get registered debug processes
	 * @return
	 */
	public BitList getRegisteredTargets();
	/** Get pcdi target by given task ID
	 * @param target_id
	 * @return
	 */
	public IPCDITarget getTarget(int target_id);
	/** Shutdown this session
	 * 
	 */
	public void shutdown();

	/** Get launch
	 * @return
	 */
	public IPLaunch getLaunch();
	
	/** Get external debugger
	 * @return
	 */
	public IAbstractDebugger getDebugger();
}
