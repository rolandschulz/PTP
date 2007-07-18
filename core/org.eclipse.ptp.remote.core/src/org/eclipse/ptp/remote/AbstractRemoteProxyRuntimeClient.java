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
	private final boolean 	launchManually;
	private final String	remoteServicesId;
	private final String	connectionName;


	public AbstractRemoteProxyRuntimeClient(AbstractRemoteResourceManagerConfiguration config, 
			int baseModelId) {
		super(config, baseModelId);
		this.remoteServicesId = config.getRemoteServicesId();
		this.connectionName = config.getConnectionName();
		this.proxyName = config.getName();
		this.proxyPath = config.getProxyServerPath();
		this.launchManually = config.isLaunchManually();
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
		
		try {
			// This will attempt to load and initialized the remote services plugin. If
			// initialization failse, we abandon attempt to start RM
			
			IRemoteServices remoteServices = PTPRemotePlugin.getDefault().getRemoteServices(remoteServicesId);
			if (remoteServices == null) {
				return false;
			}
			
			sessionCreate();

			if (launchManually) {
				final String msg = "Waiting for manual launch of proxy on port " + getSessionPort() + "...";
				System.out.println(msg);
				Status info = new Status(IStatus.INFO, PTPCorePlugin.getUniqueIdentifier(), IStatus.INFO, msg, null);
				PTPCorePlugin.log(info);
			} else {
				String args = "--port="+getSessionPort();
				IRemoteConnectionManager connMgr = remoteServices.getConnectionManager();
				IRemoteConnection conn = connMgr.getConnection();
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
			}
			if (getEventLogging()) System.out.println(toString() + ": Waiting on accept.");

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
