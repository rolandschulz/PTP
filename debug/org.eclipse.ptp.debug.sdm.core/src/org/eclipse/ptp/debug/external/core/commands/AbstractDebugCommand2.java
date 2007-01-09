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
package org.eclipse.ptp.debug.external.core.commands;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.cdi.ICommandResult;
import org.eclipse.ptp.debug.core.cdi.PCDIException;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIErrorEvent;


/** This is an abstract class of debug command.  It contains all common actions and functions of all debug commands.
 * @author Clement chu
 * 
 */
public abstract class AbstractDebugCommand2 extends AbstractDebugCommand {
	protected List completedTasks = new ArrayList();
	protected List completedReturns = new ArrayList();

	/** constructor
	 * @param tasks
	 */
	public AbstractDebugCommand2(BitList tasks) {
		super(tasks);
	}
	protected boolean checkReturn() throws PCDIException {
		synchronized (lock) {
			Object result = getReturn();
			if (result != null) {
				if (result.equals(RETURN_NOTHING)) {
					throw new PCDIException("Unknown error - Command " + getCommandName());
				}
				if (result instanceof PCDIException) {
					//if (((PCDIException)result).getErrorCode() == IPCDIErrorEvent.DBG_NORMAL) {
						//return false;
					//}
					throw (PCDIException)getReturn();
				}
				if (result.equals(RETURN_ERROR)) {
					throw new PCDIException("Tasks do not match with <" + getCommandName() + "> command.");
				}
				if (result.equals(RETURN_CANCEL)) {
					throw new PCDIException("Cancelled - command " + getCommandName());
				}
				if (result.equals(RETURN_FLUSH)) {
					return false;
				}
			}
			if (isBlocked()) {
				throw new PCDIException("Time out - Command " + getCommandName(), IPCDIErrorEvent.DBG_NORMAL);
			}
			if (check_tasks != null && !check_tasks.isEmpty()) {
				throw new PCDIException("Incomplete - Command " + getCommandName());
			}
			return true;
		}
	}
	public void setReturn(BitList return_tasks, Object result) {
		synchronized (lock) {
			setCheckTasks();
			if (return_tasks != null) {
				if (result instanceof PCDIException) {
					setReturn(result);
				}
				else {
					completedTasks.add(return_tasks.copy());
					completedReturns.add(result);
					
					//check whether return tasks is same as command tasks
					check_tasks.andNot(return_tasks);
					if (check_tasks.isEmpty()) {
						releaseLock();
					}
					else {
						//if return tasks is not equal to command tasks, wait again
						lockAgain();
					}
				}
			}
			else {
				setReturn(RETURN_INCOMPLETE);
			}
		}
	}
	public ICommandResult getCommandResult() throws PCDIException {
		waitForReturn();
		if (getReturn() instanceof PCDIException) {
			throw (PCDIException)getReturn();
		}
		return new ICommandResult() {
			public BitList[] getTasksArray() {
				return (BitList[])completedTasks.toArray(new BitList[0]);
			}
			public Object[] getResultsArray() {
				return completedReturns.toArray(new Object[0]);				
			}
		};		
	} 
}
