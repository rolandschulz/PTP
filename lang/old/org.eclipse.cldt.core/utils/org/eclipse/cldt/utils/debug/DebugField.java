/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cldt.utils.debug;


/**
 * DebugEnumField
 *  
 */
public class DebugField {

	String name;
	DebugType type;
	int offset;
	int bits;

	/**
	 *  
	 */
	public DebugField(String name, DebugType type, int offset, int bits) {
		this.name = name;
		this.type = type;
		this.offset = offset;
		this.bits = bits;
	}

	public String getName() {
		return name;
	}

	public DebugType getDebugType() {
		return type;
	}

}
