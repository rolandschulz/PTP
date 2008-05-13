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

import org.eclipse.ptp.core.util.BitList;

/**
 * Represents signal management
 * @author clement
 *
 */
public interface IPDISignalManagement {
	/**
	 * Requests to retrieve a list signals information with given signal name of specify process
	 * @param tasks target process 
	 * @param name name of signal
	 * @throws PDIException on failure
	 */
	void listSignals(BitList tasks, String name) throws PDIException;
	
	/**
	 * Requests to retrieve signal information with given argument of specify process
	 * @param tasks target process
	 * @param arg argument 
	 * @throws PDIException on failure
	 * @deprecated
	 */
	void retrieveSignalInfo(BitList tasks, String arg) throws PDIException;	
}
