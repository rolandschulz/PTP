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
package org.eclipse.ptp.debug.core.pdi;

import org.eclipse.ptp.debug.core.TaskSet;

/**
 * Represents stackframe management
 * 
 * @author clement
 * 
 */
public interface IPDIStackframeManagement {
	/**
	 * Lists stack frames with given range of frames
	 * 
	 * @param tasks
	 *            target process
	 * @param low
	 *            lower of frame
	 * @param depth
	 *            depth of frame
	 * @throws PDIException
	 *             on failure
	 * @since 4.0
	 */
	void listStackFrames(TaskSet tasks, int low, int depth) throws PDIException;

	/**
	 * Sets current stack frame with given level
	 * 
	 * @param tasks
	 *            target process
	 * @param level
	 *            level of stack frame
	 * @throws PDIException
	 *             on failure
	 * @since 4.0
	 */
	void setCurrentStackFrame(TaskSet tasks, int level) throws PDIException;
}
