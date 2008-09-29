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


/**
 * @author richardm
 *
 */
public class NonInstructionLine extends TimingFileLine 
{
	// Text from the line.
	private String line;
	
	/**
	 * @param linenum
	 * @param grpnum
	 * @param line
	 * 
	 * Sets attributes
	 */
	public NonInstructionLine(int linenum, int grpnum, String line)
	{
		super(linenum, grpnum);
		this.line = line.trim();
	}
	
	public String getLine() {
		return line;
	}
	
	public String toString() {
		return "- " + this.line + " - \n"; //$NON-NLS-1$ //$NON-NLS-2$
	}
}
