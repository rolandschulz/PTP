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
package org.eclipse.ptp.debug.mi.core.gdb.output;

/**
 * GDB/MI var-update.
 */

public class MIVarChange {
	String name;
	boolean inScope;
	boolean changed;

	public MIVarChange(String n) {
		name = n;
	}

	public String getVarName() {
		return name;
	}

	public boolean isInScope() {
		return inScope;
	}

	public boolean isChanged() {
		return changed;
	}

	public void setInScope(boolean b) {
		inScope = b;
	}

	public void setChanged(boolean c) {
		changed = c;
	}
}
