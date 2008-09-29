/******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 *****************************************************************************/
package org.eclipse.ptp.cell.sputiming.ui.parse;

public class TimeInfo {

	private int position;
	private int nopesCount;
	private int instructionSize;
	
	public TimeInfo(int position, int nopesCount, int instructionSize) {
		super();
		this.position = position;
		this.nopesCount = nopesCount;
		this.instructionSize = instructionSize;
	}
	
	public String toString() {
		return "( " + this.position + " \t " + nopesCount + " " + instructionSize + " )";   //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$//$NON-NLS-4$
	}

	public int getInstructionSize() {
		return instructionSize;
	}

	public int getNopesCount() {
		return nopesCount;
	}

	public int getPosition() {
		return position;
	}
	
}//class
