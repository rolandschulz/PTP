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
package org.eclipse.ptp.internal.remote;

import java.io.IOException;
import java.util.List;

import org.eclipse.ptp.remote.AbstractRemoteProcessBuilder;
import org.eclipse.ptp.remote.IRemoteConnection;
import org.eclipse.ptp.remote.IRemoteProcess;

public class LocalProcessBuilder extends AbstractRemoteProcessBuilder {
	private ProcessBuilder localProcessBuilder;

	public LocalProcessBuilder(IRemoteConnection conn, List<String> command) {
		super(conn, command);
		localProcessBuilder = new ProcessBuilder(command);
	}
	
	public LocalProcessBuilder(IRemoteConnection conn, String... command) {
		super(conn, command);
		localProcessBuilder = new ProcessBuilder(command);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteProcessBuilder#start()
	 */
	public IRemoteProcess start() throws IOException {
		return new LocalProcess(localProcessBuilder.start());
	}
}
