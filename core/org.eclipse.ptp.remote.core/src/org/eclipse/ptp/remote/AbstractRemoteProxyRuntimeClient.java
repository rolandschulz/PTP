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

package org.eclipse.ptp.remote;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.rtsystem.proxy.AbstractProxyRuntimeClient;

public class AbstractRemoteProxyRuntimeClient extends AbstractProxyRuntimeClient {
	
	private boolean			proxyDebugOutput = true;
	private final String	proxyName;
	private final String	proxyPath;
	private final int	 	proxyOptions;
	private final String	remoteServicesId;
	private final String	connectionName;

	public AbstractRemoteProxyRuntimeClient(AbstractRemoteResourceManagerConfiguration config, 
			int baseModelId) {
		super(config, baseModelId);
		this.remoteServicesId = config.getRemoteServicesId();
		this.connectionName = config.getConnectionName();
		this.proxyName = config.getName();
		this.proxyPath = config.getProxyServerPath();
		this.proxyOptions = config.getOptions();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.proxy.AbstractProxyRuntimeClient#shutdownProxyServer()
	 */
	@Override
	protected void shutdownProxyServer() {
		try {
			sessionFinish();
		} catch (IOException e) {
			e.printStackTrace();
			PTPCorePlugin.log(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.proxy.AbstractProxyRuntimeClient#startupProxyServer()
	 */
	@Override
	protected boolean startupProxyServer() {
		if (getEventLogging()) {
			System.out.println(toString() + " - firing up proxy, waiting for connection.  Please wait!  This can take a minute . . .");
			System.out.println("PROXY_SERVER path = '" + proxyPath + "'");
		}
		
		boolean stdio = (proxyOptions & IRemoteProxyOptions.STDIO) == IRemoteProxyOptions.STDIO;
		boolean portForwarding = (proxyOptions & IRemoteProxyOptions.PORT_FORWARDING) == IRemoteProxyOptions.PORT_FORWARDING;
		boolean manualLaunch = (proxyOptions & IRemoteProxyOptions.MANUAL_LAUNCH) == IRemoteProxyOptions.MANUAL_LAUNCH;
		
		try {
			/*
			 * This can fail if we are restarting the RM from saved information and the saved remote
			 * services provider is no longer available...
			 */
			IRemoteServices remoteServices = PTPRemotePlugin.getDefault().getRemoteServices(remoteServicesId);
			if (remoteServices == null) {
				PTPCorePlugin.log(toString() + " startup: Could not find remote services ID " + remoteServicesId);
				return false;
			}
			
			if (manualLaunch) {
				sessionCreate();
				final String msg = "Waiting for manual launch of proxy on port " + getSessionPort() + "...";
				System.out.println(msg);
				Status info = new Status(IStatus.INFO, PTPCorePlugin.getUniqueIdentifier(), IStatus.INFO, msg, null);
				PTPCorePlugin.log(info);
			} else {
				IRemoteConnectionManager connMgr = remoteServices.getConnectionManager();
				IRemoteConnection conn = connMgr.getConnection(connectionName);

				if (!stdio) {
					sessionCreate();
					
					String args = "--port="+getSessionPort();
					if (portForwarding) {
						args = "--host=localhost " + args;
					} else {
						args = "--host=" + getSessionHost() + " " + args;
					}
					
					IRemoteProcessBuilder processBuilder = remoteServices.getProcessBuilder(conn, proxyPath, args);
					IRemoteProcess process = processBuilder.asyncStart();
					
					final BufferedReader err_reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
					final BufferedReader out_reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

					new Thread(new Runnable() {
						public void run() {
							try {
								String output;
								while ((output = out_reader.readLine()) != null) {
									if (proxyDebugOutput) System.out.println(proxyName + ": " + output);
								}
							} catch (IOException e) {
								// Ignore
							}
						}
					}, "Program output Thread").start();
					
					new Thread(new Runnable() {
						public void run() {
							try {
								String line;
								while ((line = err_reader.readLine()) != null) {
									if (proxyDebugOutput) System.err.println(proxyName + ": " + line);
								}
							} catch (IOException e) {
								// Ignore
							}
						}
					}, "Error output Thread").start();
					
					if (getEventLogging()) {
						System.out.println(toString() + ": Waiting on accept.");
					}
				} else {
					IRemoteProcessBuilder processBuilder = remoteServices.getProcessBuilder(conn, proxyPath, "--proxy=stdio");
					IRemoteProcess process = processBuilder.asyncStart();
					
					sessionCreate(process.getOutputStream(), process.getInputStream());
					
					final BufferedReader err_reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
					
					new Thread(new Runnable() {
						public void run() {
							try {
								String line;
								while ((line = err_reader.readLine()) != null) {
									if (proxyDebugOutput) System.err.println(proxyName + ": " + line);
								}
							} catch (IOException e) {
								// Ignore
							}
						}
					}, "Error output Thread").start();
					
					if (getEventLogging()) {
						System.out.println(toString() + ": Waiting on accept.");
					}
				}
			}
		} catch (IOException e) {
			System.err.println("Exception starting up proxy. :(");
			try {
				sessionFinish();
			} catch (IOException e1) {
				PTPCorePlugin.log(e1);
			}
			return false;
		}
		return true;
	}
}
