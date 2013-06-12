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
package org.eclipse.ptp.debug.core.pdi.model;

import java.util.Properties;

import org.eclipse.ptp.debug.core.pdi.IPDISessionObject;
import org.eclipse.ptp.debug.core.pdi.PDIException;

/**
 * Describes the configuration of debug session.
 * 
 * @author clement
 * 
 */
public interface IPDIRuntimeOptions extends IPDISessionObject {
	/**
	 * Program/Inferior arguments
	 * 
	 * @param args
	 *            the string representing the arguments
	 * @throws PDIException
	 *             on failure
	 */
	public void setArguments(String[] args) throws PDIException;

	/**
	 * Program/Inferior environment settings
	 * 
	 * @param props
	 *            the new environment variable to add
	 * @throws PDIException
	 *             on failure
	 */
	public void setEnvironment(Properties props) throws PDIException;

	/**
	 * Program/Inferior working directory
	 * 
	 * @param wd
	 *            the working directory to start the program
	 * @throws PDIException
	 *             on failure
	 */
	public void setWorkingDirectory(String wd) throws PDIException;
}
