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
package org.eclipse.ptp.internal.debug.core.pdi;

import java.math.BigInteger;

import org.eclipse.ptp.debug.core.pdi.IPDIAddressLocation;
import org.eclipse.ptp.debug.core.pdi.IPDIFileLocation;
import org.eclipse.ptp.debug.core.pdi.IPDIFunctionLocation;
import org.eclipse.ptp.debug.core.pdi.IPDILineLocation;
import org.eclipse.ptp.debug.core.pdi.IPDILocation;

/**
 * @author clement
 *
 */
public class Location implements IPDILocation {
	private BigInteger addr = null;
	private String file = null;
	private String function = null;
	private int line = 0;
	
	public Location(String file) {
		this(file, null, 0, null);
	}
	public Location(String file, String function) {
		this(file, function, 0, null);
	}
	public Location(String file, int line) {
		this(file, null, line, null);
	}
	public Location(BigInteger addr) {
		this(null, null, 0, addr);
	}
	public Location(String file, String function, int line, BigInteger addr) {
		this.file = file;
		this.function = function;
		this.line = line;
		this.addr = addr;
	}
	public BigInteger getAddress() {
		return addr;
	}
	public String getFile() {
		return file;
	}
	public String getFunction() {
		return function;
	}
	public int getLineNumber() {
		return line;
	}
	public boolean equals(IPDILocation location) {
		if (location == this) {
			return true;
		}
		if (location instanceof IPDILineLocation) {
			IPDILineLocation lineLocation = (IPDILineLocation)location;
			String oFile = lineLocation.getFile();
			if (oFile != null && oFile.length() > 0 && file != null && file.length() > 0 && oFile.equals(file)) {
				if (lineLocation.getLineNumber() == line) {
					return true;
				}
			} else if ((file == null || file.length() == 0) && (oFile == null || oFile.length() == 0)) {
				if (lineLocation.getLineNumber() == line) {
					return true;
				}
			}
		} else if (location instanceof IPDIFunctionLocation) {
			IPDIFunctionLocation funcLocation = (IPDIFunctionLocation)location;
			String oFile = funcLocation.getFile();
			String oFunction = funcLocation.getFunction();
			if (oFile != null && oFile.length() > 0 && file != null && file.length() > 0 && oFile.equals(file)) {
				if (oFunction != null && oFunction.length() > 0 && function != null && function.length() > 0 && oFunction.equals(function)) {
					return true;
				} else if ((oFunction == null || oFunction.length() == 0) && (function == null || function.length() == 0)) {
					return true;
				}
			} else if ((file == null || file.length() == 0) && (oFile == null || oFile.length() == 0)) {
				if (oFunction != null && oFunction.length() > 0 && function != null && function.length() > 0 && oFunction.equals(function)) {
					return true;
				} else if ((oFunction == null || oFunction.length() == 0) && (function == null || function.length() == 0)) {
					return true;
				}
			}
		} else if (location instanceof IPDIAddressLocation) {
			IPDIAddressLocation addrLocation = (IPDIAddressLocation)location;
			BigInteger oAddr = addrLocation.getAddress();
			if (oAddr != null && oAddr.equals(addr)) {
				return true;
			} else if (oAddr == null && addr == null) {
				return true;
			}
		} else if (location instanceof IPDIFileLocation) {
			IPDIFileLocation fileLocation = (IPDIFileLocation)location;
			String oFile = fileLocation.getFile();
			if (oFile != null && oFile.length() > 0 && file != null && file.length() > 0 && oFile.equals(file)) {
				return true;
			} else if ((file == null || file.length() == 0) && (oFile == null || oFile.length() == 0)) {
				return true;
			}			
		}
		return false;
	}
}
