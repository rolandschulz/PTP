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
package org.eclipse.ptp.debug.external.core.commands;

import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.IAbstractDebugger;
import org.eclipse.ptp.debug.core.cdi.PCDIException;
import org.eclipse.ptp.debug.external.core.cdi.model.DataReadMemoryInfo;

/**
 * @author Clement chu
 * 
 */
public class DataReadMemoryCommand extends AbstractDebugCommand {
	long offset;
	String address;
	int wordFormat;
	int wordSize;
	int rows;
	int cols;
	Character asChar;

	public DataReadMemoryCommand(BitList tasks, long offset, String address, int wordFormat, int wordSize, int rows, int cols, Character asChar) {
		super(tasks, false, true);
		this.offset = offset;
		this.address = address;
		this.wordFormat = wordFormat;
		this.wordSize = wordSize;
		this.rows = rows;
		this.cols = cols;
		this.asChar = asChar;
	}
	public void execCommand(IAbstractDebugger debugger, int timeout) throws PCDIException {
		setTimeout(timeout);
		debugger.setDataReadMemoryCommand(tasks, offset, address, wordFormat, wordSize, rows, cols, asChar);
	}
	
	public DataReadMemoryInfo getDataReadMemoryInfo() throws PCDIException {
		if (waitForReturn()) {
			if (result instanceof DataReadMemoryInfo) {
				return (DataReadMemoryInfo)result;
			}
		}
		throw new PCDIException("Wrong type return on command: " + getName());
	}
	public String getName() {
		return "Data Read Memory"; 
	}	
}

