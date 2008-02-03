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
package org.eclipse.ptp.debug.core.pdi.manager;

import org.eclipse.ptp.core.util.BitList;

/**
 * Represent expression manager to manage processes
 * 
 * @author clement
 * 
 */
public interface IPDITaskManager extends IPDIManager {
	/**
	 * @param tasks
	 * @return
	 */
	public boolean canAllStepReturn(BitList tasks);

	/**
	 * @param tasks
	 * @return
	 */
	public BitList getCannotStepReturnTasks(BitList tasks);

	/**
	 * @return
	 */
	public BitList getCanStepReturnTasks();

	/**
	 * @param tasks
	 * @return
	 */
	public BitList getCanStepReturnTasks(BitList tasks);

	/**
	 * @param tasks
	 * @return
	 */
	public BitList getNonPendingTasks(BitList tasks);

	/**
	 * Find terminated or suspended tasks
	 * 
	 * @param tasks
	 * @return
	 */
	public BitList getNonRunningTasks(BitList tasks);

	/**
	 * Find terminated or running tasks
	 * 
	 * @param tasks
	 * @return
	 */
	public BitList getNonSuspendedTasks(BitList tasks);

	/**
	 * Find running or suspended tasks
	 * 
	 * @param tasks
	 * @return
	 */
	public BitList getNonTerminatedTasks(BitList tasks);

	/**
	 * @return
	 */
	public BitList getPendingTasks();

	/**
	 * @param tasks
	 * @return
	 */
	public BitList getPendingTasks(BitList tasks);

	/**
	 * Get all registered tasks
	 * 
	 * @return
	 */
	public BitList getRegisteredTasks();

	/**
	 * Find registered tasks
	 * 
	 * @param tasks
	 * @return
	 */
	public BitList getRegisteredTasks(BitList tasks);

	/**
	 * Find running tasks
	 * 
	 * @param tasks
	 * @return
	 */
	public BitList getRunningTasks(BitList tasks);

	/**
	 * Get all suspended tasks
	 * 
	 * @return
	 */
	public BitList getSuspendedTasks();

	/**
	 * Find suspended tasks
	 * 
	 * @param tasks
	 * @return
	 */
	public BitList getSuspendedTasks(BitList tasks);

	/**
	 * Get all terminated tasks
	 * 
	 * @return
	 */
	public BitList getTerminatedTasks();

	/**
	 * Find terminated tasks
	 * 
	 * @param tasks
	 * @return
	 */
	public BitList getTerminatedTasks(BitList tasks);

	/**
	 * Find unregistered tasks
	 * 
	 * @param tasks
	 * @return
	 */
	public BitList getUnregisteredTasks(BitList tasks);

	/**
	 * @param tasks
	 * @return
	 */
	public boolean isAllPending(BitList tasks);

	/**
	 * @param tasks
	 * @return
	 */
	public boolean isAllRegistered(BitList tasks);

	/**
	 * @param tasks
	 * @return
	 */
	public boolean isAllRunning(BitList tasks);

	/**
	 * @param tasks
	 * @return
	 */
	public boolean isAllSuspended(BitList tasks);

	/**
	 * @param tasks
	 * @return
	 */
	public boolean isAllTerminated(BitList tasks);

	/**
	 * @param isAdd
	 * @param tasks
	 */
	public void setCanStepReturnTasks(boolean isAdd, BitList tasks);

	/**
	 * @param isAdd
	 * @param tasks
	 */
	public void setPendingTasks(boolean isAdd, BitList tasks);

	/**
	 * @param isAdd
	 * @param tasks
	 */
	public void setRegisterTasks(boolean isAdd, BitList tasks);

	/**
	 * @param isAdd
	 * @param tasks
	 */
	public void setSuspendTasks(boolean isAdd, BitList tasks);

	/**
	 * @param isAdd
	 * @param tasks
	 */
	public void setTerminateTasks(boolean isAdd, BitList tasks);
}
