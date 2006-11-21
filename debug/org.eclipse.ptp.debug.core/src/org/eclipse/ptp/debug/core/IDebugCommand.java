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

import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.cdi.PCDIException;
/**
 * @author Clement chu
 */
public interface IDebugCommand extends Comparable {
	public static final int PRIORITY_L = 1;
	public static final int PRIORITY_M = 2;
	public static final int PRIORITY_H = 3;
	public static final String RETURN_NOTHING = "Nothing";
	public static final String RETURN_OK = "OK";
	public static final String RETURN_FLUSH = "Flush";
	public static final String RETURN_CANCEL = "Cancel";
	public static final String RETURN_ERROR = "Error"; // tasks do not match
	/**
	 * Get the tasks of the command
	 * 
	 * @return tasks
	 */
	public BitList getTasks();
	/** 
	 * Get the priority of the command
	 * @return priority
	 */
	public int getPriority();
	/**
	 * execuate command
	 * 
	 * @param debugger
	 *            debugger to execute the command
	 * @throws PCDIException
	 */
	public void execCommand(IAbstractDebugger debugger) throws PCDIException;
	/**
	 * Whether this command can be interrupted
	 * 
	 * @return true can be interrupted, otherwise not
	 */
	public boolean canInterrupt();
	/**
	 * Whether this command need to wait for return back
	 * 
	 * @param timeout
	 *            special the time for finish
	 * @return
	 * @throws PCDIException
	 */
	public boolean waitForReturn(long timeout) throws PCDIException;
	/**
	 * Whether this command need to wait for return back
	 * 
	 * @return true is wait for return back, otherwise not
	 */
	public boolean isWaitForReturn();
	/**
	 * Whether this command need to wait in command queue
	 * 
	 * @return true need to wait in command queue, otehrwise this command can jump the queue
	 */
	public boolean isWaitInQueue();
	/**
	 * Set return back
	 * 
	 * @param tasks
	 *            update the tasks for this command
	 * @param result
	 *            return value
	 */
	public void setReturn(BitList tasks, Object result);
	/**
	 * Cancel the waiting if the command is waiting in the command queue
	 */
	public void doCancelWaiting();
	/**
	 * Wait the command for return back
	 * 
	 * @return true OK, otherwise not
	 * @throws PCDIException
	 */
	public boolean waitForReturn() throws PCDIException;
	/**
	 * Set time out for command
	 * 
	 * @param timeout
	 */
	public void setTimeout(long timeout);
	/**
	 * Flush command
	 */
	public void doFlush();
	/**
	 * Get command name
	 * 
	 * @return name of command
	 */
	public String getCommandName();
	/**
	 * Compare the command
	 * 
	 * @param obj
	 *            compared object
	 * @return 0 means equals, otherwise not equals
	 */
	public int compareTo(Object obj);
}
