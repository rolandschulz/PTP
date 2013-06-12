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
package org.eclipse.ptp.debug.core.pdi;

import java.math.BigInteger;

/**
 * Represents the information of file, function, line, address
 * 
 * @author clement
 * 
 */
public interface IPDILocator extends IPDIFileLocation, IPDILineLocation, IPDIFunctionLocation, IPDIAddressLocation {
	/**
	 * Test if this locator matches the supplied address
	 * 
	 * @param oAddress
	 * @return
	 */
	public boolean equalAddress(BigInteger oAddress);

	/**
	 * Check if this locator matches the supplied file
	 * 
	 * @param oFile
	 * @return
	 */
	public boolean equalFile(String oFile);

	/**
	 * Check if the locator matches the supplied function
	 * 
	 * @param oFunction
	 * @return
	 */
	public boolean equalFunction(String oFunction);

	/**
	 * Check if this locator matches the supplied line
	 * 
	 * @param oLine
	 * @return
	 */
	public boolean equalLine(int oLine);

	/**
	 * Check if this locator matches the supplied locator
	 * 
	 * @param locator
	 * @return
	 */
	public boolean equals(IPDILocator locator);
}
