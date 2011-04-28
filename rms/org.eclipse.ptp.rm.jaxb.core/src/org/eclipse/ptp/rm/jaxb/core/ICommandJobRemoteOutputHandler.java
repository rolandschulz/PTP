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
 * CommandJob-specific handler for capturing job output redirected to file.
 * 
 * @author arossi
 * 
 */
public interface ICommandJobRemoteOutputHandler {

	/**
	 * @param listener
	 *            to be notified when file is ready
	 */
	void addFileReadyListener(IFileReadyListener listener);

	/**
	 * See if the file exists and is ready to read.
	 * 
	 * @param block
	 *            until the file is ready
	 * @return thread running the check
	 */
	Thread checkForReady(boolean block);

	/**
	 * @return contents of file
	 */
	String getFileContents();

	/**
	 * @return path to remote file
	 */
	String getRemoteFilePath();

	/**
	 * Initialize file path from current env.
	 * 
	 * @param jobId
	 *            for the associated job
	 */
	void initialize(String jobId);

	/**
	 * @param listener
	 *            to be notified when file is ready
	 */
	void removeFileReadyListener(IFileReadyListener listener);
}
