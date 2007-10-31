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
package org.eclipse.ptp.debug.core.model;

import org.eclipse.core.resources.IFile;
import org.eclipse.debug.core.DebugException;

/**
 * Provides the ability to run a debug target to the given line.
 * @author clement
 *
 */
public interface IRunToLine {

	/**
	 * Returns whether this operation is currently available for this file and line number.
	 * 
	 * @return whether this operation is currently available
	 */
	public boolean canRunToLine(IFile file, int lineNumber);

	/**
	 * Causes this element to run to specified location.
	 * 
	 * @exception DebugException on failure. Reasons include:
	 */
	public void runToLine(IFile file, int lineNumber, boolean skipBreakpoints) throws DebugException;

	/**
	 * Returns whether this operation is currently available for this file and line number.
	 * 
	 * @return whether this operation is currently available
	 */
	public boolean canRunToLine(String fileName, int lineNumber);

	/**
	 * Causes this element to run to specified location.
	 * 
	 * @exception DebugException on failure. Reasons include:
	 */
	public void runToLine(String fileName, int lineNumber, boolean skipBreakpoints) throws DebugException;
}
