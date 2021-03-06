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

import org.eclipse.ptp.debug.core.TaskSet;
import org.eclipse.ptp.debug.core.pdi.model.IPDITarget;

/**
 * Represent expression manager to manage processes
 * 
 * @author clement
 * 
 */
public interface IPDITargetManager extends IPDIManager {
	/**
	 * Returns target of specify tasks
	 * 
	 * @param qTasks
	 * @return target of specify tasks
	 * @since 4.0
	 */
	IPDITarget getTarget(TaskSet qTasks);

	/**
	 * Returns all targets
	 * 
	 * @return all targets
	 */
	IPDITarget[] getTargets();

	/**
	 * Adds task to be a target
	 * 
	 * @param qTasks
	 * @return created target
	 * @since 4.0
	 */
	IPDITarget addTarget(TaskSet qTasks);

	/**
	 * Returns whether given tasks are deleted its target
	 * 
	 * @param qTasks
	 * @return true if given tasks are deleted its target
	 * @since 4.0
	 */
	boolean removeTarget(TaskSet qTasks);

	/**
	 * Cleans up storage
	 */
	void cleanup();
}
