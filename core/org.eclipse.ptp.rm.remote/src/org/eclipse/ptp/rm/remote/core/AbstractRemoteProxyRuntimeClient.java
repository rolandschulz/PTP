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
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.util.DebugUtil;
import org.eclipse.ptp.proxy.runtime.client.AbstractProxyRuntimeClient;
import org.eclipse.ptp.proxy.runtime.command.IProxyRuntimeCommandFactory;
import org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeEventFactory;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionManager;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.remote.core.IRemoteProcess;
import org.eclipse.ptp.remote.core.IRemoteProcessBuilder;
import org.eclipse.ptp.remote.core.IRemoteProxyOptions;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;
import org.eclipse.ptp.rm.remote.messages.Messages;

public class AbstractRemoteProxyRuntimeClient extends AbstractProxyRuntimeClient {
	
	private IRemoteConnection connection = null;
	private IProgressMonitor startupMonitor = null;
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
	public void shutdown() throws IOException {
		super.shutdown();
		try {
			sessionFinish();
		} catch (IOException e) {
			PTPCorePlugin.log(e);
		}
		
		synchronized (this) {
			if (startupMonitor != null) {
				startupMonitor.setCanceled(true);
			}
		}
		
		if (connection != null && connection.isOpen()) {
			connection.close();
		}
	}

	/**
	 * Start the remote connection.
	 * 
	 * @param monitor
	 * @throws IOException
	 */
	public void startup(IProgressMonitor monitor) throws IOException {
		SubMonitor subMon = SubMonitor.convert(monitor, 12);

		if (getDebugOptions().CLIENT_TRACING) {
			System.out.println(toString() + " - firing up proxy, waiting for connection.  Please wait!  This can take a minute . . ."); //$NON-NLS-1$
			System.out.println("PROXY_SERVER path = '" + config.getName() + "'");  //$NON-NLS-1$  //$NON-NLS-2$
		}
		
		synchronized (this) {
			startupMonitor = monitor;
		}
		
		try {
			monitor.subTask(Messages.AbstractRemoteProxyRuntimeClient_1);
			
			/*
			 * This can fail if we are restarting the RM from saved information and the saved remote
			 * services provider is no longer available...
			 */
			IRemoteServices remoteServices = PTPRemoteCorePlugin.getDefault().getRemoteServices(config.getRemoteServicesId());
			if (remoteServices == null) {
				throw new IOException(NLS.bind(Messages.AbstractRemoteProxyRuntimeClient_10, config.getRemoteServicesId()));  
			}
			
			monitor.worked(5);

			if (config.testOption(IRemoteProxyOptions.MANUAL_LAUNCH)) {
				monitor.subTask(Messages.AbstractRemoteProxyRuntimeClient_2);
				
				sessionCreate();
				
				monitor.worked(5);
				
				List<String> args = new ArrayList<String>();
				args.add(config.getProxyServerPath());
				args.add("--proxy=tcp"); //$NON-NLS-1$
				if (config.testOption(IRemoteProxyOptions.PORT_FORWARDING)) {
					args.add("--host=localhost"); //$NON-NLS-1$
				} else {
					args.add("--host=" + config.getLocalAddress()); //$NON-NLS-1$
				}
				args.add("--port="+getSessionPort()); //$NON-NLS-1$
				if (getDebugOptions().SERVER_DEBUG_LEVEL > 0) {
					args.add("--debug=" + getDebugOptions().SERVER_DEBUG_LEVEL); //$NON-NLS-1$
				}
				args.addAll(config.getInvocationOptions());
				
				if (getDebugOptions().CLIENT_TRACING) {
					System.out.println("Launch command: " + args.toString()); //$NON-NLS-1$
				}
				
				final String msg = NLS.bind(Messages.AbstractRemoteProxyRuntimeClient_3, args.toString());
				
				monitor.subTask(msg);
				
				Status info = new Status(IStatus.INFO, PTPCorePlugin.getUniqueIdentifier(), IStatus.INFO, msg, null);
				PTPCorePlugin.log(info);
			} else {
				IRemoteConnectionManager connMgr = remoteServices.getConnectionManager();
				connection = connMgr.getConnection(config.getConnectionName());
				if (connection == null) {
					throw new IOException(NLS.bind(Messages.AbstractRemoteProxyRuntimeClient_11, config.getConnectionName())); 
				}
				
				monitor.subTask(Messages.AbstractRemoteProxyRuntimeClient_4);
				
				if (!connection.isOpen()) {
					connection.open(subMon.newChild(4));
				}
				if (monitor.isCanceled()) {
					connection.close();
					return;
				}
				
				monitor.subTask(Messages.AbstractRemoteProxyRuntimeClient_5);
				
				/*
				 * Check the remote proxy exists
				 */
				IRemoteFileManager fileManager = remoteServices.getFileManager(connection);
				if (fileManager == null) {
					throw new IOException(Messages.AbstractRemoteProxyRuntimeClient_9);
				}
				
				IFileStore res = fileManager.getResource(new Path(config.getProxyServerPath()), subMon.newChild(2));
				if (!res.fetchInfo().exists()){
					throw new IOException(NLS.bind(Messages.AbstractRemoteProxyRuntimeClient_12, config.getProxyServerPath()));
				}
				
				if (monitor.isCanceled()) {
					connection.close();
					return;
				}
				
				monitor.subTask(Messages.AbstractRemoteProxyRuntimeClient_6);

				sessionCreate();
				
				monitor.worked(1);

				ArrayList<String> args = new ArrayList<String>();
				args.add(config.getProxyServerPath());
				args.add("--proxy=tcp"); //$NON-NLS-1$
	
				if (config.testOption(IRemoteProxyOptions.PORT_FORWARDING)) {
					int remotePort;
					try {
						remotePort = connection.forwardRemotePort("localhost", getSessionPort(), subMon.newChild(1)); //$NON-NLS-1$
					} catch (RemoteConnectionException e) {
						throw new IOException(e.getMessage());
					}
					if (monitor.isCanceled()) {
						sessionFinish();
						return;
					}
					args.add("--host=localhost"); //$NON-NLS-1$
					args.add("--port="+ remotePort); //$NON-NLS-1$
				} else {
					args.add("--host=" + config.getLocalAddress()); //$NON-NLS-1$
					args.add("--port="+ getSessionPort()); //$NON-NLS-1$
				}

				if (getDebugOptions().SERVER_DEBUG_LEVEL > 0) {
					args.add("--debug=" + getDebugOptions().SERVER_DEBUG_LEVEL); //$NON-NLS-1$
				}
				args.addAll(config.getInvocationOptions());
				
				if (getDebugOptions().CLIENT_TRACING) {
					System.out.println("Launch command: " + args.toString()); //$NON-NLS-1$
				}

				monitor.subTask(Messages.AbstractRemoteProxyRuntimeClient_7);
				
				IRemoteProcessBuilder processBuilder = remoteServices.getProcessBuilder(connection, args);
				IRemoteProcess process = processBuilder.start();
				
				monitor.worked(2);
				
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
				}, Messages.AbstractRemoteProxyRuntimeClient_15).start(); 
				
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
				}, Messages.AbstractRemoteProxyRuntimeClient_14).start(); 
				
				if (getDebugOptions().CLIENT_TRACING) {
					System.out.println(toString() + ": Waiting on accept."); //$NON-NLS-1$
				}
			}
			
			monitor.subTask(Messages.AbstractRemoteProxyRuntimeClient_8);
			super.startup();
			monitor.worked(2);

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
			synchronized(this) {
				startupMonitor = null;
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
}
