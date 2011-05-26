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

package org.eclipse.ptp.rm.core.rtsystem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.core.ModelManager;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.util.DebugUtil;
import org.eclipse.ptp.proxy.runtime.client.AbstractProxyRuntimeClient;
import org.eclipse.ptp.proxy.runtime.command.IProxyRuntimeCommandFactory;
import org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeEventFactory;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionChangeEvent;
import org.eclipse.ptp.remote.core.IRemoteConnectionChangeListener;
import org.eclipse.ptp.remote.core.IRemoteConnectionManager;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.remote.core.IRemoteProcess;
import org.eclipse.ptp.remote.core.IRemoteProcessBuilder;
import org.eclipse.ptp.remote.core.IRemoteProxyOptions;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;
import org.eclipse.ptp.rm.core.RMCorePlugin;
import org.eclipse.ptp.rm.core.messages.Messages;
import org.eclipse.ptp.rm.core.rmsystem.IRemoteResourceManagerConfiguration;
import org.eclipse.ptp.rmsystem.AbstractResourceManagerConfiguration;
import org.eclipse.ptp.rmsystem.IResourceManager;

public abstract class AbstractRemoteProxyRuntimeClient extends AbstractProxyRuntimeClient {
	private class ConnectionChangeHandler implements IRemoteConnectionChangeListener {
		public ConnectionChangeHandler() {
			// Nothing
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ptp.remote.core.IRemoteConnectionChangeListener#
		 * connectionChanged
		 * (org.eclipse.ptp.remote.core.IRemoteConnectionChangeEvent)
		 */
		public void connectionChanged(IRemoteConnectionChangeEvent event) {
			if (event.getType() == IRemoteConnectionChangeEvent.CONNECTION_ABORTED
					|| event.getType() == IRemoteConnectionChangeEvent.CONNECTION_CLOSED) {
				IRemoteResourceManagerConfiguration config = getConfiguration();
				if (config instanceof AbstractResourceManagerConfiguration) {
					IResourceManager rm = ModelManager.getInstance().getResourceManagerFromUniqueName(
							((AbstractResourceManagerConfiguration) config).getUniqueName());
					try {
						rm.stop();
					} catch (CoreException e) {
						RMCorePlugin.log(e);
					}
				}
			}
		}

	}

	private IProgressMonitor fStartupMonitor = null;
	private IRemoteConnection fRemoteConnection = null;
	private final IRemoteResourceManagerConfiguration fConfig;
	private final ConnectionChangeHandler fConnectionChangeHandler = new ConnectionChangeHandler();

	public AbstractRemoteProxyRuntimeClient(IRemoteResourceManagerConfiguration config, int baseModelId) {
		super(config.getName(), baseModelId);
		fConfig = config;
		initDebugOptions();
	}

	public AbstractRemoteProxyRuntimeClient(IRemoteResourceManagerConfiguration config, int baseModelId,
			IProxyRuntimeCommandFactory cmdFactory, IProxyRuntimeEventFactory eventFactory) {
		super(config.getName(), baseModelId, cmdFactory, eventFactory);
		fConfig = config;
		initDebugOptions();
	}

	/**
	 * Shut down remote proxy.
	 * 
	 * Calls shutdown() to stop the state machine, then sessionFinish() to close
	 * down the connection.
	 * 
	 * @param monitor
	 * @throws IOException
	 */
	@Override
	public void shutdown() throws IOException {
		/*
		 * Remove listener to avoid re-entry
		 */
		if (fRemoteConnection != null) {
			fRemoteConnection.removeConnectionChangeListener(fConnectionChangeHandler);
		}

		super.shutdown();
		try {
			sessionFinish();
		} catch (IOException e) {
			PTPCorePlugin.log(e);
		}

		synchronized (this) {
			if (fStartupMonitor != null) {
				fStartupMonitor.setCanceled(true);
			}
		}
	}

	/**
	 * Start the remote connection.
	 * 
	 * @param monitor
	 * @throws IOException
	 */
	public void startup(IProgressMonitor monitor) throws IOException {
		SubMonitor subMon = SubMonitor.convert(monitor, 16);

		if (getDebugOptions().CLIENT_TRACING) {
			System.out.println(toString()
					+ " - firing up proxy, waiting for connection.  Please wait!  This can take a minute . . ."); //$NON-NLS-1$
			System.out.println("PROXY_SERVER path = '" + getConfiguration().getName() + "'"); //$NON-NLS-1$  //$NON-NLS-2$
		}

		synchronized (this) {
			fStartupMonitor = subMon;
		}

		try {
			subMon.subTask(Messages.AbstractRemoteProxyRuntimeClient_1);

			/*
			 * This can fail if we are restarting the RM from saved information
			 * and the saved remote services provider is no longer available...
			 */
			IRemoteServices remoteServices = PTPRemoteCorePlugin.getDefault().getRemoteServices(
					getConfiguration().getRemoteServicesId(), subMon.newChild(4));
			if (remoteServices == null) {
				throw new IOException(NLS.bind(Messages.AbstractRemoteProxyRuntimeClient_10, getConfiguration()
						.getRemoteServicesId()));
			}

			IRemoteConnectionManager connMgr = remoteServices.getConnectionManager();
			fRemoteConnection = connMgr.getConnection(getConfiguration().getConnectionName());
			if (fRemoteConnection == null) {
				throw new IOException(
						NLS.bind(Messages.AbstractRemoteProxyRuntimeClient_11, getConfiguration().getConnectionName()));
			}

			subMon.worked(5);

			if (getConfiguration().testOption(IRemoteProxyOptions.MANUAL_LAUNCH)) {
				subMon.subTask(Messages.AbstractRemoteProxyRuntimeClient_2);

				sessionCreate();

				subMon.worked(5);

				List<String> args = new ArrayList<String>();
				args.add(getConfiguration().getProxyServerPath());
				args.add("--proxy=tcp"); //$NON-NLS-1$
				if (getConfiguration().testOption(IRemoteProxyOptions.PORT_FORWARDING)) {
					subMon.subTask(Messages.AbstractRemoteProxyRuntimeClient_4);

					if (!fRemoteConnection.isOpen()) {
						fRemoteConnection.open(subMon.newChild(4));
					}
					if (subMon.isCanceled()) {
						fRemoteConnection.close();
						return;
					}

					subMon.subTask(Messages.AbstractRemoteProxyRuntimeClient_5);
					int remotePort;
					try {
						remotePort = fRemoteConnection.forwardRemotePort("localhost", getSessionPort(), subMon.newChild(1)); //$NON-NLS-1$
					} catch (RemoteConnectionException e) {
						throw new IOException(e.getMessage());
					}
					if (subMon.isCanceled()) {
						sessionFinish();
						return;
					}
					args.add("--host=localhost"); //$NON-NLS-1$
					args.add("--port=" + remotePort); //$NON-NLS-1$
				} else {
					args.add("--host=" + getConfiguration().getLocalAddress()); //$NON-NLS-1$
					args.add("--port=" + getSessionPort()); //$NON-NLS-1$
				}
				if (getDebugOptions().SERVER_DEBUG_LEVEL > 0) {
					args.add("--debug=" + getDebugOptions().SERVER_DEBUG_LEVEL); //$NON-NLS-1$
				}
				args.addAll(getConfiguration().getInvocationOptions());

				if (getDebugOptions().CLIENT_TRACING) {
					System.out.println("Launch command: " + args.toString()); //$NON-NLS-1$
				}

				final String msg = NLS.bind(Messages.AbstractRemoteProxyRuntimeClient_3, args.toString());

				subMon.subTask(msg);

				Status info = new Status(IStatus.INFO, PTPCorePlugin.getUniqueIdentifier(), IStatus.INFO, msg, null);
				PTPCorePlugin.log(info);
			} else {
				subMon.subTask(Messages.AbstractRemoteProxyRuntimeClient_4);

				if (!fRemoteConnection.isOpen()) {
					fRemoteConnection.open(subMon.newChild(4));
				}
				if (subMon.isCanceled()) {
					fRemoteConnection.close();
					return;
				}

				subMon.subTask(Messages.AbstractRemoteProxyRuntimeClient_5);

				/*
				 * Check the remote proxy exists
				 */
				IRemoteFileManager fileManager = remoteServices.getFileManager(fRemoteConnection);
				if (fileManager == null) {
					throw new IOException(Messages.AbstractRemoteProxyRuntimeClient_9);
				}

				IFileStore res = fileManager.getResource(getConfiguration().getProxyServerPath());
				try {
					if (!res.fetchInfo(EFS.NONE, subMon.newChild(2)).exists()) {
						throw new IOException(NLS.bind(Messages.AbstractRemoteProxyRuntimeClient_12, getConfiguration()
								.getProxyServerPath()));
					}
				} catch (CoreException e1) {
					throw new IOException(NLS.bind(Messages.AbstractRemoteProxyRuntimeClient_12, getConfiguration()
							.getProxyServerPath()));
				}

				if (subMon.isCanceled()) {
					fRemoteConnection.close();
					return;
				}

				subMon.subTask(Messages.AbstractRemoteProxyRuntimeClient_6);

				sessionCreate();

				subMon.worked(1);

				ArrayList<String> args = new ArrayList<String>();
				args.add(getConfiguration().getProxyServerPath());
				args.add("--proxy=tcp"); //$NON-NLS-1$

				if (getConfiguration().testOption(IRemoteProxyOptions.PORT_FORWARDING)) {
					int remotePort;
					try {
						remotePort = fRemoteConnection.forwardRemotePort("localhost", getSessionPort(), subMon.newChild(1)); //$NON-NLS-1$
					} catch (RemoteConnectionException e) {
						throw new IOException(e.getMessage());
					}
					if (subMon.isCanceled()) {
						sessionFinish();
						return;
					}
					args.add("--host=localhost"); //$NON-NLS-1$
					args.add("--port=" + remotePort); //$NON-NLS-1$
				} else {
					args.add("--host=" + getConfiguration().getLocalAddress()); //$NON-NLS-1$
					args.add("--port=" + getSessionPort()); //$NON-NLS-1$
				}

				if (getDebugOptions().SERVER_DEBUG_LEVEL > 0) {
					args.add("--debug=" + getDebugOptions().SERVER_DEBUG_LEVEL); //$NON-NLS-1$
				}
				args.addAll(getConfiguration().getInvocationOptions());

				if (getDebugOptions().CLIENT_TRACING) {
					System.out.println("Launch command: " + args.toString()); //$NON-NLS-1$
				}

				subMon.subTask(Messages.AbstractRemoteProxyRuntimeClient_7);

				IRemoteProcessBuilder processBuilder = remoteServices.getProcessBuilder(fRemoteConnection, args);
				IRemoteProcess process = processBuilder.start();

				subMon.worked(2);

				final BufferedReader err_reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
				final BufferedReader out_reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

				new Thread(new Runnable() {
					public void run() {
						try {
							String output;
							while ((output = out_reader.readLine()) != null) {
								System.out.println(getConfiguration().getName() + ": " + output); //$NON-NLS-1$
							}
							out_reader.close();
						} catch (IOException e) {
							// Ignore
						}
					}
				}, Messages.AbstractRemoteProxyRuntimeClient_15).start();

				new Thread(new Runnable() {
					public void run() {
						try {
							String line;
							while ((line = err_reader.readLine()) != null) {
								System.err.println(getConfiguration().getName() + ": " + line); //$NON-NLS-1$
							}
							err_reader.close();
						} catch (IOException e) {
							// Ignore
						}
					}
				}, Messages.AbstractRemoteProxyRuntimeClient_14).start();

				if (getDebugOptions().CLIENT_TRACING) {
					System.out.println(toString() + ": Waiting on accept."); //$NON-NLS-1$
				}
			}

			subMon.subTask(Messages.AbstractRemoteProxyRuntimeClient_8);
			super.startup();
			subMon.worked(2);

			fRemoteConnection.addConnectionChangeListener(fConnectionChangeHandler);
		} catch (IOException e) {
			try {
				sessionFinish();
			} catch (IOException e1) {
				PTPCorePlugin.log(e1);
			}
			throw new IOException(NLS.bind(Messages.AbstractRemoteProxyRuntimeClient_13, e.getMessage()));
		} catch (RemoteConnectionException e) {
			throw new IOException(NLS.bind(Messages.AbstractRemoteProxyRuntimeClient_13, e.getMessage()));
		} finally {
			synchronized (this) {
				fStartupMonitor = null;
			}
			if (monitor != null) {
				monitor.done();
			}
		}
	}

	/**
	 * Initialize debugging options
	 */
	private void initDebugOptions() {
		/*
		 * Set up debug options
		 */
		getDebugOptions().PROTOCOL_TRACING = DebugUtil.PROTOCOL_TRACING;
		getDebugOptions().CLIENT_TRACING = DebugUtil.PROXY_CLIENT_TRACING;
		getDebugOptions().SERVER_DEBUG_LEVEL = DebugUtil.PROXY_SERVER_DEBUG_LEVEL;

	}

	/**
	 * @since 2.0
	 */
	protected IRemoteResourceManagerConfiguration getConfiguration() {
		return fConfig;
	}
}
