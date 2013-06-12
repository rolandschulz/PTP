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

import org.eclipse.debug.core.DebugException;

/**
 * Provides the ability to resume execution without giving a signal. This is useful when the program stopped on account of a signal
 * and would ordinary see the signal when resumed.
 * 
 * @author Clement
 * 
 */
public interface IResumeWithoutSignal {
	/**
	 * Causes this element to resume its execution ignoring a signal. Has no
	 * effect on an element that is not suspended because of a signal.
	 * 
	 * @exception DebugException
	 *                on failure. Reasons include:
	 */
	public void resumeWithoutSignal() throws DebugException;

	/**
	 * Returns whether this element can currently be resumed without signal.
	 * 
	 * @return whether this element can currently be resumed without signal
	 */
	public boolean canResumeWithoutSignal();
}
