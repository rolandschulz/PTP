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
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ptp.ems.core.EnvManagerRegistry;
import org.eclipse.ptp.ems.core.IEnvManager;
import org.eclipse.ptp.ems.core.IEnvManager2;
import org.eclipse.ptp.ems.core.IEnvManagerConfig;
import org.eclipse.ptp.internal.ems.ui.EMSUIPlugin;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.exception.RemoteConnectionException;
import org.eclipse.ui.PlatformUI;

/**
 * An implementation of {@link IEnvManager} which avoids connecting to the remote machine until it is actually
 * necessary, then displays a progress dialog while the connection is established and subsequently forwards all
 * method calls to the {@link IEnvManager} returned by {@link EnvManagerRegistry#getEnvManager(IProgressMonitor, IRemoteConnection)}
 * .
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
public class LazyEnvManagerDetector implements IEnvManager2 {

	private class GetEnvManagerRunnable implements IRunnableWithProgress {
		private IEnvManager envManager = EnvManagerRegistry.getNullEnvManager();

		public IEnvManager getResult() {
			return this.envManager;
		}

		@Override
		public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
			this.envManager = EnvManagerRegistry.getEnvManager(monitor, remoteConnection);
		}
	}

	private final IRemoteConnection remoteConnection;
	private IProgressMonitor fMonitor;

	private IEnvManager envManager = null;

	/**
	 * Constructor.
	 * 
	 * @param remoteConnection
	 *            {@link IRemoteConnection} used to access files and execute shell commands on the remote machine (non-
	 *            <code>null</code>)
	 * @since 3.0
	 */
	public LazyEnvManagerDetector(final IRemoteConnection remoteConnection) {
		this.remoteConnection = remoteConnection;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ems.core.IEnvManager#checkForCompatibleInstallation(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public boolean checkForCompatibleInstallation(IProgressMonitor pm) throws RemoteConnectionException, IOException {
		return ensureEnvManagerDetected().checkForCompatibleInstallation(pm);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ems.core.IEnvManager#configure(org.eclipse.remote.core.IRemoteConnection)
	 */
	/**
	 * @since 3.0
	 */
	@Override
	public void configure(IRemoteConnection remoteConnection) {
		ensureEnvManagerDetected().configure(remoteConnection);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ems.core.IEnvManager#createBashScript(org.eclipse.core.runtime.IProgressMonitor, boolean,
	 * org.eclipse.ptp.ems.core.IEnvManagerConfig, java.lang.String)
	 */
	@Override
	public String createBashScript(IProgressMonitor pm, boolean echo, IEnvManagerConfig config, String commandToExecuteAfterward)
			throws RemoteConnectionException, IOException {
		return ensureEnvManagerDetected().createBashScript(pm, echo, config, commandToExecuteAfterward);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ems.core.IEnvManager#determineAvailableElements(org.eclipse.core.runtime.IProgressMonitor)
	 */
	/**
	 * @since 2.0
	 */
	@Override
	public List<String> determineAvailableElements(IProgressMonitor pm) throws RemoteConnectionException, IOException {
		return ensureEnvManagerDetected().determineAvailableElements(pm);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ems.core.IEnvManager#determineAvailableElements(org.eclipse.core.runtime.IProgressMonitor, java.util.List)
	 */
	/**
	 * @since 3.0
	 */
	@Override
	public List<String> determineAvailableElements(IProgressMonitor pm, List<String> selectedElements) throws RemoteConnectionException, IOException {
		IEnvManager mgr = ensureEnvManagerDetected();
		if (mgr instanceof IEnvManager2) {
			return ((IEnvManager2)mgr).determineAvailableElements(pm, selectedElements);
		} else {
			return mgr.determineAvailableElements(pm);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ems.core.IEnvManager#determineDefaultElements(org.eclipse.core.runtime.IProgressMonitor)
	 */
	/**
	 * @since 2.0
	 */
	@Override
	public List<String> determineDefaultElements(IProgressMonitor pm) throws RemoteConnectionException, IOException {
		return ensureEnvManagerDetected().determineDefaultElements(pm);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ems.core.IEnvManager#getBashConcatenation(java.lang.String, boolean,
	 * org.eclipse.ptp.ems.core.IEnvManagerConfig, java.lang.String)
	 */
	@Override
	public String getBashConcatenation(String separator, boolean echo, IEnvManagerConfig config, String commandToExecuteAfterward) {
		return ensureEnvManagerDetected().getBashConcatenation(separator, echo, config, commandToExecuteAfterward);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ems.core.IEnvManager#getComparator()
	 */
	@Override
	public Comparator<String> getComparator() {
		return ensureEnvManagerDetected().getComparator();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ems.core.IEnvManager#getDescription(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public String getDescription(IProgressMonitor pm) throws RemoteConnectionException, IOException {
		return ensureEnvManagerDetected().getDescription(pm);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ems.core.IEnvManager#getInstructions()
	 */
	@Override
	public String getInstructions() {
		return ensureEnvManagerDetected().getInstructions();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ems.core.IEnvManager#getName()
	 */
	@Override
	public String getName() {
		return ensureEnvManagerDetected().getName();
	}

	/**
	 * Set a progress monitor to use for long running operations. If no progress monitor is set, the workbench progress service will
	 * be used
	 * 
	 * @param monitor
	 *            progress monitor
	 */
	public void setProgressMonitor(IProgressMonitor monitor) {
		fMonitor = monitor;
	}

	private IEnvManager ensureEnvManagerDetected() {
		if (envManager == null) {
			GetEnvManagerRunnable runnable = new GetEnvManagerRunnable();
			try {
				if (fMonitor != null) {
					runnable.run(SubMonitor.convert(fMonitor));
				} else {
					PlatformUI.getWorkbench().getProgressService().busyCursorWhile(runnable);
				}
			} catch (InvocationTargetException e) {
				EMSUIPlugin.log(e);
			} catch (InterruptedException e) {
			}
			envManager = runnable.getResult();
		}
		return envManager;
	}
}
