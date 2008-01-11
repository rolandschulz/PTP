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

package org.eclipse.ptp.rm.remote.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.util.DebugUtil;
import org.eclipse.ptp.proxy.runtime.client.AbstractProxyRuntimeClient;
import org.eclipse.ptp.proxy.runtime.command.IProxyRuntimeCommandFactory;
import org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeEventFactory;
import org.eclipse.ptp.proxy.util.DebugOptions;
import org.eclipse.ptp.remote.IRemoteConnection;
import org.eclipse.ptp.remote.IRemoteConnectionManager;
import org.eclipse.ptp.remote.IRemoteFileManager;
import org.eclipse.ptp.remote.IRemoteProcess;
import org.eclipse.ptp.remote.IRemoteProcessBuilder;
import org.eclipse.ptp.remote.IRemoteProxyOptions;
import org.eclipse.ptp.remote.IRemoteServices;
import org.eclipse.ptp.remote.PTPRemotePlugin;
import org.eclipse.ptp.remote.exception.RemoteConnectionException;

public class AbstractRemoteProxyRuntimeClient extends AbstractProxyRuntimeClient {
	
	private IRemoteConnection connection;
	private AbstractRemoteResourceManagerConfiguration config;

	public AbstractRemoteProxyRuntimeClient(AbstractRemoteResourceManagerConfiguration config, 
			int baseModelId) {
		super(config.getName(), baseModelId);
		this.config = config;
		initDebugOptions();
	}

	public AbstractRemoteProxyRuntimeClient(AbstractRemoteResourceManagerConfiguration config, 
			int baseModelId,
			IProxyRuntimeCommandFactory cmdFactory,
			IProxyRuntimeEventFactory eventFactory) {
		super(config.getName(), baseModelId, cmdFactory, eventFactory);
		this.config = config;
		initDebugOptions();
	}

	/**
	 * Shut down remote proxy. 
	 * 
	 * Calls shutdown() to stop the state machine, then sessionFinish()
	 * to close down the connection.
	 * 
	 * @param monitor
	 * @throws IOException
	 */
	public void shutdown(IProgressMonitor monitor) throws IOException {
		shutdown();
		try {
			sessionFinish();
		} catch (IOException e) {
			PTPCorePlugin.log(e);
		}
		if (connection.isOpen()) {
			connection.close(monitor);
		}
	}

	/**
	 * Start the remote connection.
	 * 
	 * @param monitor
	 * @throws IOException
	 */
	public void startup(IProgressMonitor monitor) throws IOException {
		if (DebugOptions.CLIENT_TRACING) {
			System.out.println(toString() + " - firing up proxy, waiting for connection.  Please wait!  This can take a minute . . ."); //$NON-NLS-1$
			System.out.println("PROXY_SERVER path = '" + config.getName() + "'");  //$NON-NLS-1$  //$NON-NLS-2$
		}
		
		try {
			/*
			 * This can fail if we are restarting the RM from saved information and the saved remote
			 * services provider is no longer available...
			 */
			IRemoteServices remoteServices = PTPRemotePlugin.getDefault().getRemoteServices(config.getRemoteServicesId());
			if (remoteServices == null) {
				throw new IOException("Could not find remote services ID " + config.getRemoteServicesId());  //$NON-NLS-1$
			}

			if (config.testOption(IRemoteProxyOptions.MANUAL_LAUNCH)) {
				sessionCreate();
				
				List<String> args = new ArrayList<String>();
				args.add(config.getProxyServerPath());
				args.add("--proxy=tcp"); //$NON-NLS-1$
				if (config.testOption(IRemoteProxyOptions.PORT_FORWARDING)) {
					args.add("--host=localhost"); //$NON-NLS-1$
				} else {
					args.add("--host=" + config.getLocalAddress()); //$NON-NLS-1$
				}
				args.add("--port="+getSessionPort()); //$NON-NLS-1$
				if (DebugOptions.SERVER_DEBUG_LEVEL > 0) {
					args.add("--debug=" + DebugOptions.SERVER_DEBUG_LEVEL); //$NON-NLS-1$
				}
				args.addAll(config.getInvocationOptions());
				
				if (DebugOptions.CLIENT_TRACING) {
					System.out.println("Launch command: " + args.toString()); //$NON-NLS-1$
				}
				
				final String msg = "Waiting for manual launch of proxy: " + args.toString(); //$NON-NLS-1$
				System.out.println(msg);
				Status info = new Status(IStatus.INFO, PTPCorePlugin.getUniqueIdentifier(), IStatus.INFO, msg, null);
				PTPCorePlugin.log(info);
			} else {
				IRemoteConnectionManager connMgr = remoteServices.getConnectionManager();
				connection = connMgr.getConnection(config.getConnectionName());
				if (connection == null) {
					throw new IOException("No such connection: " + config.getConnectionName()); //$NON-NLS-1$
				}
				if (!connection.isOpen()) {
					connection.open(monitor);
				}
				if (monitor.isCanceled()) {
					return;
				}
				
				/*
				 * Check the remote proxy exists
				 */
				IRemoteFileManager fileManager = remoteServices.getFileManager(connection);
				IFileStore res = fileManager.getResource(new Path(config.getProxyServerPath()), monitor);
				if (!res.fetchInfo().exists()){
					throw new IOException("Could not find proxy executable \"" + config.getProxyServerPath() + "\""); //$NON-NLS-1$  //$NON-NLS-2$
				}

				sessionCreate();

				ArrayList<String> args = new ArrayList<String>();
				args.add(config.getProxyServerPath());
				args.add("--proxy=tcp"); //$NON-NLS-1$
	
				if (config.testOption(IRemoteProxyOptions.PORT_FORWARDING)) {
					int remotePort;
					try {
						remotePort = connection.forwardRemotePort("localhost", getSessionPort(), monitor); //$NON-NLS-1$
					} catch (RemoteConnectionException e) {
						throw new IOException(e.getMessage());
					}
					if (monitor.isCanceled()) {
						return;
					}
					args.add("--host=localhost"); //$NON-NLS-1$
					args.add("--port="+ remotePort); //$NON-NLS-1$
				} else {
					args.add("--host=" + config.getLocalAddress()); //$NON-NLS-1$
					args.add("--port="+ getSessionPort()); //$NON-NLS-1$
				}

				if (DebugOptions.SERVER_DEBUG_LEVEL > 0) {
					args.add("--debug=" + DebugOptions.SERVER_DEBUG_LEVEL); //$NON-NLS-1$
				}
				args.addAll(config.getInvocationOptions());
				
				if (DebugOptions.CLIENT_TRACING) {
					System.out.println("Launch command: " + args.toString()); //$NON-NLS-1$
				}

				IRemoteProcessBuilder processBuilder = remoteServices.getProcessBuilder(connection, args);
				IRemoteProcess process = processBuilder.asyncStart();
				
				final BufferedReader err_reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
				final BufferedReader out_reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

				new Thread(new Runnable() {
					public void run() {
						try {
							String output;
							while ((output = out_reader.readLine()) != null) {
								System.out.println(config.getName() + ": " + output); //$NON-NLS-1$
							}
						} catch (IOException e) {
							// Ignore
						}
					}
				}, "Program output Thread").start(); //$NON-NLS-1$
				
				new Thread(new Runnable() {
					public void run() {
						try {
							String line;
							while ((line = err_reader.readLine()) != null) {
								System.err.println(config.getName() + ": " + line); //$NON-NLS-1$
							}
						} catch (IOException e) {
							// Ignore
						}
					}
				}, "Error output Thread").start(); //$NON-NLS-1$
				
				if (DebugOptions.CLIENT_TRACING) {
					System.out.println(toString() + ": Waiting on accept."); //$NON-NLS-1$
				}
			}
		} catch (IOException e) {
			try {
				sessionFinish();
			} catch (IOException e1) {
				PTPCorePlugin.log(e1);
			}
			throw new IOException("Failed to start proxy: " + e.getMessage()); //$NON-NLS-1$
		} catch (RemoteConnectionException e) {
			throw new IOException("Failed to start proxy: " + e.getMessage()); //$NON-NLS-1$
		}
		startup();
	}

	/**
	 * Initialize debugging options
	 */
	private void initDebugOptions() {
		/*
		 * Set up debug options
		 */
		DebugOptions.PROTOCOL_TRACING = DebugUtil.PROTOCOL_TRACING;
		DebugOptions.CLIENT_TRACING = DebugUtil.PROXY_CLIENT_TRACING;
		DebugOptions.SERVER_DEBUG_LEVEL = DebugUtil.PROXY_SERVER_DEBUG_LEVEL;

	}
}
