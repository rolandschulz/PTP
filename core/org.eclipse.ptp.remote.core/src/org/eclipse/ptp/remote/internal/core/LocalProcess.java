/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.remote.internal.core;

import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.ptp.remote.core.AbstractRemoteProcess;

public class LocalProcess extends AbstractRemoteProcess {
	private Process localProcess;
	
	public LocalProcess(Process proc) {
		localProcess = proc;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Process#destroy()
	 */
	@Override
	public void destroy() {
		localProcess.destroy();
	}

	/* (non-Javadoc)
	 * @see java.lang.Process#exitValue()
	 */
	@Override
	public int exitValue() {
		return localProcess.exitValue();
	}

	/* (non-Javadoc)
	 * @see java.lang.Process#getErrorStream()
	 */
	@Override
	public InputStream getErrorStream() {
		return localProcess.getErrorStream();
	}

	/* (non-Javadoc)
	 * @see java.lang.Process#getInputStream()
	 */
	@Override
	public InputStream getInputStream() {
		return localProcess.getInputStream();
	}

	/* (non-Javadoc)
	 * @see java.lang.Process#getOutputStream()
	 */
	@Override
	public OutputStream getOutputStream() {
		return localProcess.getOutputStream();
	}

	/* (non-Javadoc)
	 * @see java.lang.Process#waitFor()
	 */
	@Override
	public int waitFor() throws InterruptedException {
		return localProcess.waitFor();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.AbstractRemoteProcess#isCompleted()
	 */
	public boolean isCompleted() {
		try {
			localProcess.exitValue();
			return true;
		} catch (IllegalThreadStateException e) {
			return false;
		}
	}
}
