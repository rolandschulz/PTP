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
package org.eclipse.cldt.debug.core.model;

/**
 * A source line in disassembly.
 */
public interface IAsmSourceLine {

	/**
	 * Returns the array of the disassembly instructions associated with this source line.
	 *  
	 * @return the array of the disassembly instructions associated with this source line
	 */
	IAsmInstruction[] getInstructions(); 
}
