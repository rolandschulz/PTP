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


import java.util.Vector;

/**
 * @author richardm
 *
 */
public class ParsedTimingFile 
{
	private String filename;
	private Vector lineVector;
	private int maxTime;
	
	ParsedTimingFile(String filename)
	{
		this.filename = filename;
	}

	public Vector getLineVector()
	{
		return lineVector;
	}

	public void setLineVector(Vector lineVector)
	{
		this.lineVector = lineVector;
	}

	public String getFilename()
	{
		return filename;
	}

	public void setMaxTime(int maxTime) {
		this.maxTime = maxTime;
	}
	
	public int getMaxTime() {
		return maxTime;
	}
}
