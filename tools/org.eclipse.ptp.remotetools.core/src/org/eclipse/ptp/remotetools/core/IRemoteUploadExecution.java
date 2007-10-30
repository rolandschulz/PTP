/******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 *****************************************************************************/
package org.eclipse.ptp.remotetools.core;

import java.io.IOException;
import java.io.OutputStream;

public interface IRemoteUploadExecution extends IRemoteOperation {
	/**
	 * Process return exit status.
	 */
	public int getReturnCode();
	
	/**
	 * Get the output stream that writes to the remote file.
	 */
	public OutputStream getOutputStreamToProcessRemoteFile() throws IOException;
	
	public String getErrorMessage();
}
