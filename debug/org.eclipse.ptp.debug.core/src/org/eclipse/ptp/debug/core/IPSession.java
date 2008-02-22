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
package org.eclipse.ptp.debug.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.attributes.ProcessAttributes;
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.event.IPDebugInfo;
import org.eclipse.ptp.debug.core.launch.IPLaunch;
import org.eclipse.ptp.debug.core.model.IPDebugTarget;
import org.eclipse.ptp.debug.core.pdi.IPDISession;

public interface IPSession extends IAdaptable {
	/**
	 * @param monitor
	 * @param app
	 * @param path
	 * @param cwd
	 * @param args
	 * @throws CoreException
	 */
	public void connectToDebugger(IProgressMonitor monitor, String app, String path, String cwd, String[] args) throws CoreException;

	/**
	 * @param tasks
	 * @param refresh
	 * @param register
	 */
	public void createDebugTarget(BitList tasks, boolean refresh, boolean register);
	
	/**
	 * @param tasks
	 * @param refresh
	 * @param register
	 */
	public void deleteDebugTarget(BitList tasks, boolean refresh, boolean register);

	/**
	 * @param register
	 */
	public void deleteDebugTargets(boolean register);

	/**
	 * 
	 */
	public void dispose();

	/**
	 * @param tasks
	 * @return
	 */
	public IPDebugTarget findDebugTarget(BitList tasks);

	/**
	 * @param change
	 * @param breakpoint
	 * @param info
	 */
	public void fireDebugEvent(int change, int breakpoint, IPDebugInfo info);

	/**
	 * @param state
	 */
	public void forceStoppedDebugger(ProcessAttributes.State state);

	/**
	 * @return
	 */
	public IPBreakpointManager getBreakpointManager();

	/**
	 * @param tasks
	 * @return
	 */
	public IPDebugInfo getDebugInfo(BitList tasks);
	
	/**
	 * @return
	 */
	public IPJob getJob();
	
	/**
	 * @return
	 */
	public IPLaunch getLaunch();
	
	/**
	 * @return
	 */
	public IPMemoryManager getMemoryManager();

	/**
	 * @return
	 */
	public IPDISession getPDISession();

	/**
	 * @return
	 */
	public IProject getProject();

	/**
	 * @return
	 */
	public IPRegisterManager getRegisterManager();

	/**
	 * @return
	 */
	public IPSetManager getSetManager();

	/**
	 * @return
	 */
	public IPSignalManager getSignalManager();

	/**
	 * @return
	 */
	public BitList getTasks();

	/**
	 * @param id
	 * @return
	 */
	public BitList getTasks(int id);
	
	/**
	 * @return
	 */
	public boolean isReady();

	/**
	 * @param tasks
	 * @param refresh
	 * @param register
	 */
	public void reloadDebugTargets(BitList tasks, boolean refresh, boolean register);
}
