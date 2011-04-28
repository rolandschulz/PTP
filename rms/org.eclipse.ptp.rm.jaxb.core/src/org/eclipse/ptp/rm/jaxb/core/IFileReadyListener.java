/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.core;

/**
 * CommandJob-specific handler accepting notification that a remote file is
 * ready to be read.
 * 
 * @author arossi
 * 
 */
public interface IFileReadyListener {

	/**
	 * @param jobId
	 * @param remoteFile
	 * @param ready
	 *            if file is ready to read
	 */
	void handleReadyFile(String jobId, String remoteFile, boolean ready);
}
