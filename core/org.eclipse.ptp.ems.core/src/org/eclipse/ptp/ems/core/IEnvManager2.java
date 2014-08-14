/*******************************************************************************
 * Copyright (c) 2014 Auburn University and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Jeff Overbey (Auburn) - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.ems.core;

import java.io.IOException;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.exception.RemoteConnectionException;

/**
 * Extension to {@link IEnvManager} providing a second implementation of {@link #determineAvailableElements(IProgressMonitor, List)}
 * that allows the list of selected modules to be passed to the method to determine available modules.
 * <p>
 * This allows EMS to support nested modules (Bug 405413) by allowing the selected modules to be loaded before running
 * 
 * <pre>
 * module avail
 * </pre>.
 * 
 * @author Jeff Overbey
 * 
 * @since 3.1
 */
public interface IEnvManager2 extends IEnvManager {
	/**
	 * Returns the set of all environment configuration elements available on the remote machine (e.g., the result of
	 * <tt>module -t avail</tt>).
	 * 
	 * @param pm
	 *            progress monitor used to report the status of potentially long-running operations to the user (non-
	 *            <code>null</code>)
	 * @param selectedElements
	 *            elements selected for inclusion (non-<code>null</code>)
	 * 
	 * @return unmodifiable Set (non-<code>null</code>)
	 * 
	 * @throws NullPointerException
	 *             if {@link #configure(IRemoteConnection)} has not been called
	 * @throws RemoteConnectionException
	 *             if an remote connection error occurs
	 * @throws IOException
	 *             if an input/output error occurs
	 * @since 2.0
	 */
	public List<String> determineAvailableElements(IProgressMonitor pm, List<String> selectedElements)
			throws RemoteConnectionException, IOException;
}
