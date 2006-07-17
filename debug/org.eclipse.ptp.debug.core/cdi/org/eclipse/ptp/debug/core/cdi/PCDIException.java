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
package org.eclipse.ptp.debug.core.cdi;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIErrorEvent;

/**
 * @author Clement chu
 *
 */
public class PCDIException extends CDIException {
	static final long serialVersionUID = -2387516993124229949L;
	public static final String NOT_IMPLEMENTED = "<Not implemented>";
	
	private int error_code;
	/** Constructor
	 * @param msg
	 * @param error_code
	 */
	public PCDIException(String msg, int error_code) {
		super(msg);
		this.error_code = error_code;
	}
	/** Constructor
	 * @param msg
	 */
	public PCDIException(String msg) {
		this(msg, IPCDIErrorEvent.DBG_WARNING);
	}
	/** Constructor
	 * @param msg
	 * @param details
	 * @param error_code
	 */
	public PCDIException(String msg, String details, int error_code) {
		super(msg, details);
		this.error_code = error_code;
	}
	/** Constructor
	 * @param msg
	 * @param details
	 */
	public PCDIException(String msg, String details) {
		this(msg, details, IPCDIErrorEvent.DBG_WARNING);
	}
	/** Constructor
	 * @param e
	 * @param error_code
	 */
	public PCDIException(Throwable e, int error_code) {
		this(e.getMessage(), error_code);
	}
	/** Constructor
	 * @param e
	 */
	public PCDIException(Throwable e) {
		this(e.getMessage());
	}
	
	/** Get error code
	 * @return
	 */
	public int getErrorCode() {
		return error_code;
	}
}
