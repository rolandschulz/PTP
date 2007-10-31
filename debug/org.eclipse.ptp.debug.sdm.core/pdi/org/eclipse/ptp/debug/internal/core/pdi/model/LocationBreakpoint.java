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
package org.eclipse.ptp.debug.internal.core.pdi.model;

import java.math.BigInteger;

import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.pdi.IPDIAddressLocation;
import org.eclipse.ptp.debug.core.pdi.IPDICondition;
import org.eclipse.ptp.debug.core.pdi.IPDIFunctionLocation;
import org.eclipse.ptp.debug.core.pdi.IPDILineLocation;
import org.eclipse.ptp.debug.core.pdi.IPDILocation;
import org.eclipse.ptp.debug.core.pdi.IPDILocator;
import org.eclipse.ptp.debug.core.pdi.model.IPDILocationBreakpoint;
import org.eclipse.ptp.debug.internal.core.pdi.Locator;
import org.eclipse.ptp.debug.internal.core.pdi.Session;

/**
 * @author clement
 *
 */
public abstract class LocationBreakpoint extends Breakpoint implements IPDILocationBreakpoint {
	IPDILocation location;
	
	public LocationBreakpoint(Session session, BitList tasks, int type, IPDILocation location, IPDICondition condition, boolean enabled) {
		super(session, tasks, type, condition, enabled);
		this.location = location;
	}
	public int getLineNumber() {
		if (location instanceof IPDILineLocation) {
			return ((IPDILineLocation)location).getLineNumber();
		}
		return 0;
	}
	public String getFile() {
		if (location instanceof IPDILineLocation) {
			return ((IPDILineLocation)location).getFile();
		}
		else if (location instanceof IPDIFunctionLocation) {
			return ((IPDIFunctionLocation)location).getFile();
		}
		return null;
	}
	public BigInteger getAddress() {
		if (location instanceof IPDIAddressLocation) {
			return ((IPDIAddressLocation)location).getAddress();
		}
		return null;
	}
	public String getFunction() {
		if (location instanceof IPDIFunctionLocation) {
			return ((IPDIFunctionLocation)location).getFunction();
		}
		return null;
	}
	public IPDILocator getLocator() {
		return new Locator(getFile(), getFunction(), getLineNumber(), getAddress());
	}
}
