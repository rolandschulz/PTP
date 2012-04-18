/*******************************************************************************
 * Copyright (c) 2012 University of Illinois and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Jeff Overbey (Illinois) - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.ems.ui;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Comparator;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ptp.ems.core.EnvManagerRegistry;
import org.eclipse.ptp.ems.core.IEnvManager;
import org.eclipse.ptp.ems.core.IEnvManagerConfig;
import org.eclipse.ptp.ems.internal.ui.EMSUIPlugin;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;
import org.eclipse.swt.widgets.Shell;

/**
 * An implementation of {@link IEnvManager} which avoids connecting to the remote machine until it is actually
 * necessary, then displays a progress dialog while the connection is established and subsequently forwards all
 * method calls to the {@link IEnvManager} returned by {@link EnvManagerRegistry#getEnvManager(IProgressMonitor, IRemoteConnection)}.
 * <p>
 * When it becomes necessary to connect to the remote machine, a modal progress dialog is displayed while the connection is
 * established. Then, {@link EnvManagerRegistry#getEnvManager(IProgressMonitor, IRemoteConnection)} is invoked to detects the
 * environment management system on the remote machine, if any, and acquire an {@link IEnvManager} capable of interfacing with that
 * system.
 * <p>
 * All of the {@link IEnvManager} methods delegate to the {@link IEnvManager} returned by
 * {@link EnvManagerRegistry#getEnvManager(IProgressMonitor, IRemoteConnection)}.
 * 
 * @author Jeff Overbey
 * 
 * @since 6.0
 */
public class LazyEnvManagerDetector implements IEnvManager {

	private final Shell shell;
	private final IRemoteConnection remoteConnection;
	
	private IEnvManager envManager = null;

	/**
	 * Constructor.
	 * 
	 * @param shell
	 *            the parent shell, or <code>null</code> to create the progress dialog as a top-level shell
	 * @param remoteConnection
	 *            {@link IRemoteConnection} used to access files and execute shell commands on the remote machine (non-
	 *            <code>null</code>)
	 */
	public LazyEnvManagerDetector(final Shell shell, final IRemoteConnection remoteConnection) {
		this.shell = shell;
		this.remoteConnection = remoteConnection;
	}

	private IEnvManager ensureEnvManagerDetected() {
		if (envManager == null) {
			GetEnvManagerRunnable runnable = new GetEnvManagerRunnable();
			try {
				new ProgressMonitorDialog(shell).run(true, true, runnable);
			} catch (InvocationTargetException e) {
				EMSUIPlugin.log(e);
			} catch (InterruptedException e) {
			}
			envManager = runnable.getResult();
		}
		return envManager;
	}

	private class GetEnvManagerRunnable implements IRunnableWithProgress {
		private IEnvManager envManager = EnvManagerRegistry.getNullEnvManager();

		@Override
		public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
			this.envManager = EnvManagerRegistry.getEnvManager(monitor, remoteConnection);
		}

		public IEnvManager getResult() {
			return this.envManager;
		}
	}

	@Override
	public String getName() {
		return ensureEnvManagerDetected().getName();
	}
	
	@Override
	public String getInstructions() {
		return ensureEnvManagerDetected().getInstructions();
	}
	
	@Override
	public String getDescription(IProgressMonitor pm) throws RemoteConnectionException, IOException {
		return ensureEnvManagerDetected().getDescription(pm);
	}
	
	@Override
	public Comparator<String> getComparator() {
		return ensureEnvManagerDetected().getComparator();
	}
	
	@Override
	public String getBashConcatenation(String separator, boolean echo, IEnvManagerConfig config, String commandToExecuteAfterward) {
		return ensureEnvManagerDetected().getBashConcatenation(separator, echo, config, commandToExecuteAfterward);
	}
	
	@Override
	public Set<String> determineDefaultElements(IProgressMonitor pm) throws RemoteConnectionException, IOException {
		return ensureEnvManagerDetected().determineDefaultElements(pm);
	}
	
	@Override
	public Set<String> determineAvailableElements(IProgressMonitor pm) throws RemoteConnectionException, IOException {
		return ensureEnvManagerDetected().determineAvailableElements(pm);
	}
	
	@Override
	public String createBashScript(IProgressMonitor pm, boolean echo, IEnvManagerConfig config, String commandToExecuteAfterward)
			throws RemoteConnectionException, IOException {
		return ensureEnvManagerDetected().createBashScript(pm, echo, config, commandToExecuteAfterward);
	}
	
	@Override
	public void configure(IRemoteConnection remoteConnection) {
		ensureEnvManagerDetected().configure(remoteConnection);
	}
	
	@Override
	public boolean checkForCompatibleInstallation(IProgressMonitor pm) throws RemoteConnectionException, IOException {
		return ensureEnvManagerDetected().checkForCompatibleInstallation(pm);
	}
}
