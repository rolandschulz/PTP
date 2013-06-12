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
package org.eclipse.ptp.debug.core.pdi.event;

import org.eclipse.ptp.debug.core.pdi.IPDISessionObject;

/**
 * Represents an information provided by the session when the program exited
 * 
 * @author clement
 * 
 */
public interface IPDIErrorInfo extends IPDISessionObject {
	public static final int DBG_NORMAL = 0; // show in error log and pupup error dialog and no action
	public static final int DBG_WARNING = 1; // show in error log and pupup error dialog and terminate error proceses
	public static final int DBG_FATAL = 2; // show in error log and pupup error dialog and action - terminate all processes and stop
											// debugger
	public static final int DBG_IGNORE = 3; // ignore this error

	/**
	 * Returns the error message
	 * 
	 * @return the error message
	 */
	public String getMessage();

	/**
	 * Returns a more verbatim error message (if any)
	 * 
	 * @return a more verbatim error message
	 */
	public String getDetailMessage();

	/**
	 * Returns error code
	 * 
	 * @return error code
	 */
	public int getCode();
}
