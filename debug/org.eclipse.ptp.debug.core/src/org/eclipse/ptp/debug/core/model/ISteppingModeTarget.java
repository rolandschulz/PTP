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

/**
 * Support for the instruction stepping mode for a debug target.
 * @author clement
 *
 */
public interface ISteppingModeTarget {
	/**
	 * Returns whether this debug target supports instruction stepping.
	 * @return whether this debug target supports instruction stepping
	 */
	boolean supportsInstructionStepping();

	/**
	 * Sets whether the instruction stepping are enabled in this debug target.
	 * @param enabled whether the instruction stepping are enabled in this debug target
	 */
	void enableInstructionStepping(boolean enabled);
	
	/**
	 * Returns whether the instruction stepping are currently enabled in this debug target.
	 * @return whether the instruction stepping are currently enabled in this debug target
	 */
	boolean isInstructionSteppingEnabled();
}
