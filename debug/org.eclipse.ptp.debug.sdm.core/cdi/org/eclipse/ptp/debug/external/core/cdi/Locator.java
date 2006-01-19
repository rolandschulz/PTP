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
import org.eclipse.ptp.debug.core.cdi.model.IPCDILocator;

/**
 * @author Clement chu
 * 
 */
public class Locator extends Location implements IPCDILocator {
	public Locator(String file, String function, int line, BigInteger address) {
		super(file, function, line, address);
	}
	boolean equalFile(String oFile) {
		return equalString(oFile, getFile());
	}
	boolean equalFunction(String oFunction) {
		return equalString(oFunction, getFunction());
	}
	boolean equalLine(int oLine) {
		return oLine == getLineNumber();
	}
	boolean equalAddress(BigInteger oAddress) {
		if (oAddress == null && getAddress() == null) {
			return true;
		}
		if (oAddress != null && oAddress.equals(getAddress())) {
			return true;
		}
		return false;
	}
	public static boolean equalString(String f1, String f2) {
		if (f1 != null && f1.length() > 0 && f2 != null && f2.length() > 0) {
			return f1.equals(f2);
		} else if ((f1 == null || f1.length() == 0) && (f2 == null || f2.length() == 0)) {
			return true;
		}
		return false;
	}
	public boolean equals(IPCDILocator locator) {
		if (locator == this) {
			return true;
		}
		String oFile = locator.getFile();
		String oFunction = locator.getFunction();
		int oLine = locator.getLineNumber();
		BigInteger oAddress = locator.getAddress();
		if (equalFile(oFile) && equalFunction(oFunction) && equalLine(oLine) && equalAddress(oAddress)) {
			return true;
		}
		return false;
	}
}
