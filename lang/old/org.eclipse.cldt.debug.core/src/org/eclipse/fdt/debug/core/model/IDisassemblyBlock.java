/**********************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.fdt.debug.core.model;

/**
 * Represents a contiguous segment of disassembly in an execution context.
 */
public interface IDisassemblyBlock {
	
	/**
	 * Returns the parent disassembly object.
	 * 
	 * @return the parent disassembly object
	 */
	IDisassembly getDisassembly();

	/**
	 * Returns the platform-dependent path of the executable associated 
	 * with this segment.
	 * 
	 * @return the platform-dependent path of the executable
	 */
	String getModuleFile();

	/**
	 * Returns whether this block contains given stack frame.
	 *  
	 * @param frame the stack frame
	 * @return whether this block contains given stack frame
	 */
	boolean contains( ICStackFrame frame );

	/**
	 * Return the array of source lines associated with this block.
	 *  
	 * @return the array of source lines associated with this block
	 */
	IAsmSourceLine[] getSourceLines();

	/**
	 * Returns whether this block contains mixed source/disassembly information.
	 *  
	 * @return whether this block contains mixed source/disassembly information
	 */
	boolean isMixedMode();
}
