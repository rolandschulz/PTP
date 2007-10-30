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
package org.eclipse.ptp.remotetools.environment.launcher.internal.process;

import java.io.IOException;

import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.debug.core.model.IStreamsProxy2;

class ExecutionStreamsProxy implements IStreamsProxy, IStreamsProxy2 {
	final IStreamMonitor errorStreamMonitor;
	final IStreamMonitor outputStreamMonitor;
	
	public ExecutionStreamsProxy(final IStreamMonitor errorStreamMonitor, final IStreamMonitor outputStreamMonitor) {
		super();
		this.errorStreamMonitor = errorStreamMonitor;
		this.outputStreamMonitor = outputStreamMonitor;
	}

	public IStreamMonitor getErrorStreamMonitor() {
		return errorStreamMonitor;
	}

	public IStreamMonitor getOutputStreamMonitor() {
		return outputStreamMonitor;
	}

	public void write(String input) throws IOException {
		// Ignore, it is not possible to write to TargetProcess
	}

	public void closeInputStream() throws IOException {
		// Ignore until deciding how to handle close
	}
}
