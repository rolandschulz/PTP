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
package org.eclipse.ptp.debug.core.pdi.request;

import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.debug.core.TaskSet;
import org.eclipse.ptp.debug.core.pdi.PDIException;

/**
 * @author clement
 * 
 */
public interface IPDIInternalEventRequest extends IPDIEventRequest {
	/**
	 * Get result
	 * 
	 * @param qTasks
	 * @return
	 * @throws PDIException
	 * @since 4.0
	 */
	public Object getResult(TaskSet qTasks) throws PDIException;

	/**
	 * Get result map
	 * 
	 * @param qTasks
	 * @return
	 * @throws PDIException
	 * @since 4.0
	 */
	public Map<TaskSet, Object> getResultMap(TaskSet qTasks) throws PDIException;

	/**
	 * Wait until completed
	 * 
	 * @param qTasks
	 * @throws PDIException
	 * @since 4.0
	 */
	public void waitUntilCompleted(TaskSet qTasks) throws PDIException;

	/**
	 * Wait until completed. Can be cancelled using progress monitor.
	 * 
	 * @param qTasks
	 * @param monitor
	 * @throws PDIException
	 * @since 4.0
	 */
	public void waitUntilCompleted(TaskSet qTasks, IProgressMonitor monitor) throws PDIException;
}
