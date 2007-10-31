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

import org.eclipse.ptp.debug.core.pdi.IPDISessionObject;
import org.eclipse.ptp.debug.core.pdi.PDIException;

/**
 * Represents a group of registers that are assigned to a target.
 * @author clement
 *
 */
public interface IPDIRegisterGroup extends IPDISessionObject {
	/**
	 * The name of the group
	 * @return String name
	 */
	String getName();

	/**
	 * Returns the register descriptors in this register group.
	 * @return IPDIRegisterDescriptor[] in this register group
	 * @throws PDIException on failure
	 */
	IPDIRegisterDescriptor[] getRegisterDescriptors() throws PDIException;

	/**
	 * Returns whether this register group currently contains any registers.
	 * @return whether this register group currently contains any registers
	 * @throws PDIException on failure
	 */
	public boolean hasRegisters() throws PDIException;	
}
