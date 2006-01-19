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
package org.eclipse.ptp.debug.external.core.cdi;

import java.math.BigInteger;
import org.eclipse.cdt.debug.core.cdi.ICDIAddressLocation;
import org.eclipse.cdt.debug.core.cdi.ICDIFileLocation;
import org.eclipse.cdt.debug.core.cdi.ICDIFunctionLocation;
import org.eclipse.cdt.debug.core.cdi.ICDILineLocation;
import org.eclipse.cdt.debug.core.cdi.ICDILocation;
import org.eclipse.ptp.debug.core.cdi.model.IPCDILocation;

/**
 * @author Clement chu
 * 
 */
public class Location implements IPCDILocation {
	BigInteger fAddress = null;
	String fFile = null;
	String fFunction = null;
	int fLine;

	public Location(String file) {
		this(file, null, 0, null);
	}
	public Location(String file, String function) {
		this(file, function, 0, null);
	}
	public Location(String file, int line) {
		this(file, null, line, null);
	}
	public Location(BigInteger address) {
		this(null, null, 0, address);
	}
	protected Location(String file, String function, int line, BigInteger address) {
		fFile = file;
		fFunction = function;
		fLine = line;
		fAddress = address;
	}
	public BigInteger getAddress() {
		return fAddress;
	}
	public String getFile() {
		return fFile;
	}
	public String getFunction() {
		return fFunction;
	}
	public int getLineNumber() {
		return fLine;
	}
	public boolean equals(ICDILocation location) {
		if (location == this) {
			return true;
		}
		if (location instanceof ICDILineLocation) {
			ICDILineLocation lineLocation = (ICDILineLocation) location;
			String oFile = lineLocation.getFile();
			if (oFile != null && oFile.length() > 0 && fFile != null && fFile.length() > 0 && oFile.equals(fFile)) {
				if (lineLocation.getLineNumber() == fLine) {
					return true;
				}
			} else if ((fFile == null || fFile.length() == 0) && (oFile == null || oFile.length() == 0)) {
				if (lineLocation.getLineNumber() == fLine) {
					return true;
				}
			}
		} else if (location instanceof ICDIFunctionLocation) {
			ICDIFunctionLocation funcLocation = (ICDIFunctionLocation) location;
			String oFile = funcLocation.getFile();
			String oFunction = funcLocation.getFunction();
			if (oFile != null && oFile.length() > 0 && fFile != null && fFile.length() > 0 && oFile.equals(fFile)) {
				if (oFunction != null && oFunction.length() > 0 && fFunction != null && fFunction.length() > 0 && oFunction.equals(fFunction)) {
					return true;
				} else if ((oFunction == null || oFunction.length() == 0) && (fFunction == null || fFunction.length() == 0)) {
					return true;
				}
			} else if ((fFile == null || fFile.length() == 0) && (oFile == null || oFile.length() == 0)) {
				if (oFunction != null && oFunction.length() > 0 && fFunction != null && fFunction.length() > 0 && oFunction.equals(fFunction)) {
					return true;
				} else if ((oFunction == null || oFunction.length() == 0) && (fFunction == null || fFunction.length() == 0)) {
					return true;
				}
			}
		} else if (location instanceof ICDIAddressLocation) {
			ICDIAddressLocation addrLocation = (ICDIAddressLocation) location;
			BigInteger oAddr = addrLocation.getAddress();
			if (oAddr != null && oAddr.equals(fAddress)) {
				return true;
			} else if (oAddr == null && fAddress == null) {
				return true;
			}
		} else if (location instanceof ICDIFileLocation) {
			ICDIFileLocation fileLocation = (ICDIFileLocation) location;
			String oFile = fileLocation.getFile();
			if (oFile != null && oFile.length() > 0 && fFile != null && fFile.length() > 0 && oFile.equals(fFile)) {
				return true;
			} else if ((fFile == null || fFile.length() == 0) && (oFile == null || oFile.length() == 0)) {
				return true;
			}
		}
		return false;
	}
}
