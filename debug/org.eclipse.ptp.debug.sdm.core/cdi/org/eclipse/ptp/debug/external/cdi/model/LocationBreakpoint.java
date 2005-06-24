/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.debug.external.cdi.model;

import java.math.BigInteger;

import org.eclipse.cdt.debug.core.cdi.ICDIAddressLocation;
import org.eclipse.cdt.debug.core.cdi.ICDICondition;
import org.eclipse.cdt.debug.core.cdi.ICDIFunctionLocation;
import org.eclipse.cdt.debug.core.cdi.ICDILineLocation;
import org.eclipse.cdt.debug.core.cdi.ICDILocation;
import org.eclipse.cdt.debug.core.cdi.ICDILocator;
import org.eclipse.ptp.debug.external.cdi.Locator;

public abstract class LocationBreakpoint extends Breakpoint {

	ICDILocation fLocation;

	public LocationBreakpoint(Target target, int kind, ICDILocation loc, ICDICondition cond) {
		super(target, kind, cond);
		fLocation = loc;
	}

	public int getLineNumber() {
		if (fLocation instanceof ICDILineLocation) {
			return ((ICDILineLocation)fLocation).getLineNumber();
		}
		return 0;
	}

	public String getFile() {
		if (fLocation instanceof ICDILineLocation) {
			return ((ICDILineLocation)fLocation).getFile();
		} else if (fLocation instanceof ICDIFunctionLocation) {
			return ((ICDIFunctionLocation)fLocation).getFile();
		}
		return null;
	}

	public BigInteger getAddress() {
		if (fLocation instanceof ICDIAddressLocation) {
			return ((ICDIAddressLocation)fLocation).getAddress();
		}
		return null;
	}

	public String getFunction() {
		if (fLocation instanceof ICDIFunctionLocation) {
			return ((ICDIFunctionLocation)fLocation).getFunction();
		}
		return null;
	}
	
	public ICDILocator getLocator() {
		return new Locator(getFile(), getFunction(), getLineNumber(), getAddress());
	}

}
