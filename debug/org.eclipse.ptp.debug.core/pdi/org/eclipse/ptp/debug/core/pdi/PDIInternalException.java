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
 * Thrown to indicate some unexpected internal error has occurred 
 * @author clement
 * 
 */
public class PDIInternalException extends RuntimeException {
	/**
	 * All serializable objects should have a stable serialVersionUID
	 */
	private static final long serialVersionUID = 1L;
	
	protected BitList tasks;
	/**
     * Constructs a new exception.
	 * @param tasks specify tasks throw exception 
	 */
	public PDIInternalException(BitList tasks) {
		super();
		this.tasks = tasks;
	}
	/**
     * Constructs a new exception.
	 * @param tasks specify tasks throw exception 
     * @param message the detail message. The detail message is saved for later retrieval by the {@link #getMessage()} method.
	 */
	public PDIInternalException(BitList tasks, String message) {
		super(message);
		this.tasks = tasks;
	}

}

