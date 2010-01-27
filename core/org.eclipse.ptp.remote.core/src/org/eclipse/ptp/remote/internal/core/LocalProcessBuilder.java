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

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.cdt.utils.spawner.ProcessFactory;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.ptp.remote.core.AbstractRemoteProcessBuilder;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteProcess;

public class LocalProcessBuilder extends AbstractRemoteProcessBuilder {
	private ProcessFactory localProcessBuilder;
	private Map<String, String> remoteEnv = new HashMap<String, String>();
	
	public LocalProcessBuilder(IRemoteConnection conn, List<String> command) {
		super(conn, command);
		remoteEnv.putAll(System.getenv());
		localProcessBuilder = ProcessFactory.getFactory();
		String cwd = System.getProperty("user.dir"); //$NON-NLS-1$
		if (cwd != null) {
			IPath path = new Path(cwd);
			if (path.isAbsolute()) {
				directory(EFS.getLocalFileSystem().getStore(path));
			}
		}
	}

	public LocalProcessBuilder(IRemoteConnection conn, String... command) {
		this(conn, Arrays.asList(command));
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.AbstractRemoteProcessBuilder#environment()
	 */
	@Override
	public Map<String, String> environment() {
		return remoteEnv;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteProcessBuilder#start()
	 */
	public IRemoteProcess start() throws IOException {
		String commandArray[] = command().toArray(new String[0]);
		String environmentArray[] = new String[environment().size()];
		int index = 0;
		for (Entry<String,String>  entry : environment().entrySet()) {
			environmentArray[index++] = entry.getKey() + "=" + entry.getValue(); //$NON-NLS-1$
		}
		Process localProc;
		if (directory() != null) {
			try {
				localProc = localProcessBuilder.exec(commandArray, environmentArray, 
						directory().toLocalFile(EFS.NONE, new NullProgressMonitor()));
			} catch (CoreException e) {
				throw new IOException(e.getMessage());
			}
		} else {
			localProc = localProcessBuilder.exec(commandArray, environmentArray);
		}
		return new LocalProcess(localProc, redirectErrorStream());
	}
}
