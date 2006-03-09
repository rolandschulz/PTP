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
package org.eclipse.ptp.debug.external.core.cdi.breakpoints;

import java.math.BigInteger;
import org.eclipse.cdt.debug.core.cdi.ICDIFunctionLocation;
import org.eclipse.ptp.debug.core.cdi.IPCDIAddressLocation;
import org.eclipse.ptp.debug.core.cdi.IPCDICondition;
import org.eclipse.ptp.debug.core.cdi.IPCDIFunctionLocation;
import org.eclipse.ptp.debug.core.cdi.IPCDILineLocation;
import org.eclipse.ptp.debug.core.cdi.model.IPCDILocation;
import org.eclipse.ptp.debug.core.cdi.model.IPCDILocator;
import org.eclipse.ptp.debug.external.core.cdi.Locator;

/**
 * @author Clement chu
 * 
 */
public abstract class LocationBreakpoint extends Breakpoint {
	IPCDILocation fLocation;

	public LocationBreakpoint(int kind, IPCDILocation loc, IPCDICondition cond) {
		super(kind, cond);
		fLocation = loc;
	}
	public int getLineNumber() {
		if (fLocation instanceof IPCDILineLocation) {
			return ((IPCDILineLocation) fLocation).getLineNumber();
		}
		return 0;
	}
	public String getFile() {
		if (fLocation instanceof IPCDILineLocation) {
			return ((IPCDILineLocation) fLocation).getFile();
		} else if (fLocation instanceof ICDIFunctionLocation) {
			return ((IPCDIFunctionLocation) fLocation).getFile();
		}
		return null;
	}
	public BigInteger getAddress() {
		if (fLocation instanceof IPCDIAddressLocation) {
			return ((IPCDIAddressLocation) fLocation).getAddress();
		}
		return null;
	}
	public String getFunction() {
		if (fLocation instanceof IPCDIFunctionLocation) {
			return ((IPCDIFunctionLocation) fLocation).getFunction();
		}
		return null;
	}
	public IPCDILocator getLocator() {
		return new Locator(getFile(), getFunction(), getLineNumber(), getAddress());
	}
}