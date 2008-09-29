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
 * Represents a line from the generated timing file. 
 */
public class TimingFileLine 
{
	private int groupNumber, lineNumber;
	
	/**
	 * @param linenum
	 * @param grpnum
	 * 
	 * Sets attributes
	 */
	TimingFileLine(int linenum, int grpnum)
	{
		groupNumber = grpnum;
		lineNumber = linenum;
	}

	public int getGroupNumber()
	{
		return groupNumber;
	}

	public int getLineNumber()
	{
		return lineNumber;
	}
}
