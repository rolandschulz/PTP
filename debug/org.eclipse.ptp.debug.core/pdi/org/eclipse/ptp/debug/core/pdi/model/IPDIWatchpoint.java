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
package org.eclipse.ptp.debug.core.pdi.model;

import org.eclipse.ptp.debug.core.pdi.PDIException;

/**
 * Represents a watchpoint
 * 
 * @author clement
 * 
 */
public interface IPDIWatchpoint extends IPDIBreakpoint {
	/**
	 * Write value
	 */
	public final static int WRITE = 0x1;

	/**
	 * Read value
	 */
	public final static int READ = 0x2;

	/**
	 * Determines whether this watchpoint is a write watchpoint
	 * 
	 * @return true if this wwatchpoint is a write watchpoint
	 */
	public boolean isWriteType();

	/**
	 * Determines whether this watchpoint is a read watchpoint
	 * 
	 * @return true if this watchpoint is a read watchpiont
	 */
	public boolean isReadType();

	/**
	 * Returns the expression of this watchpoint
	 * 
	 * @return the expression of this watchpoint
	 * @throws PDIException
	 *             on failure
	 */
	public String getWatchExpression() throws PDIException;
}
