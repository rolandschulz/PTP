/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.debug.core.pdi;

import java.math.BigInteger;

import org.eclipse.ptp.debug.internal.core.pdi.AddressLocation;
import org.eclipse.ptp.debug.internal.core.pdi.FileLocation;
import org.eclipse.ptp.debug.internal.core.pdi.FunctionLocation;
import org.eclipse.ptp.debug.internal.core.pdi.LineLocation;
import org.eclipse.ptp.debug.internal.core.pdi.Locator;

/**
 * @author greg
 *
 */
public class PDILocationFactory {
	/**
	 * @param addr
	 * @return
	 */
	public static IPDIAddressLocation newAddressLocation(BigInteger addr) {
		return new AddressLocation(addr);
	}
	
	/**
	 * @param file
	 * @return
	 */
	public static IPDIFileLocation newFileLocationLocation(String file) {
		return new FileLocation(file);
	}

	/**
	 * @param file
	 * @param function
	 * @return
	 */
	public static IPDIFunctionLocation newFunctionLocation(String file, String function) {
		return new FunctionLocation(file, function);
	}

	/**
	 * @param file
	 * @param line
	 * @return
	 */
	public static IPDILineLocation newLineLocation(String file, int line) {
		return new LineLocation(file, line);
	}

	/**
	 * @param file
	 * @param function
	 * @param line
	 * @param address
	 * @return
	 */
	public static IPDILocator newLocator(String file, String function, int line, BigInteger address) {
		return new Locator(file, function, line, address);
	}

}

