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
package org.eclipse.ptp.debug.core.model;

/**
 * Represents the status of a debug element
 * 
 * @author Clement chu
 * 
 */
public interface IPDebugElementStatus {
	public static final int ERROR = 2;
	public static final int OK = 0;
	public static final int WARNING = 1;

	/**
	 * Get the status message
	 * 
	 * @return
	 */
	public String getMessage();

	/**
	 * Get the severity
	 * 
	 * @return
	 */
	public int getSeverity();

	/**
	 * Check if the status is OK
	 * 
	 * @return
	 */
	public boolean isOK();
}
