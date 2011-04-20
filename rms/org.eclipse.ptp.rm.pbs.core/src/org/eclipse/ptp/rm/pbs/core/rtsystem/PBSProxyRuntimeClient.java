/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ptp.rm.pbs.core.rtsystem;

import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionManager;
import org.eclipse.ptp.remote.core.IRemoteProxyOptions;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;
import org.eclipse.ptp.remote.core.server.RemoteServerManager;
import org.eclipse.ptp.rm.core.rtsystem.AbstractRemoteProxyRuntimeClient;
import org.eclipse.ptp.rm.pbs.core.messages.Messages;
import org.eclipse.ptp.rm.pbs.core.rmsystem.PBSResourceManagerConfiguration;
import org.eclipse.ptp.rm.pbs.core.server.PBSProxyServerRunner;

/**
 * @since 4.0
 */
public class PBSProxyRuntimeClient extends AbstractRemoteProxyRuntimeClient {
	private PBSProxyServerRunner fServerRunner = null;

	public PBSProxyRuntimeClient(PBSResourceManagerConfiguration config, int baseModelId) {
		super(config, baseModelId);
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
		super.shutdown();
		synchronized (this) {
			if (fServerRunner != null) {
				fServerRunner.cancel();
			}
		}
	}

	/**
	 * Start the remote connection.
	 * 
	 * @param monitor
	 *            progress monitor
	 * @throws IOException
	 */
	@Override
	public void startup(IProgressMonitor monitor) throws IOException {
		SubMonitor subMon = SubMonitor.convert(monitor, 15);

		if (getDebugOptions().CLIENT_TRACING) {
			System.out.println(toString()
					+ " - firing up proxy, waiting for connection.  Please wait!  This can take a minute . . ."); //$NON-NLS-1$
			System.out.println("PROXY_SERVER path = '" + getConfiguration().getName() + "'"); //$NON-NLS-1$  //$NON-NLS-2$
		}

		try {
			subMon.subTask(Messages.PBSProxyRuntimeClient_0);

			/*
			 * This can fail if we are restarting the RM from saved information
			 * and the saved remote services provider is no longer available...
			 */
			IRemoteServices remoteServices = PTPRemoteCorePlugin.getDefault().getRemoteServices(
					getConfiguration().getRemoteServicesId(), subMon.newChild(3));
			if (remoteServices == null) {
				throw new IOException(NLS.bind(Messages.PBSProxyRuntimeClient_1, getConfiguration().getRemoteServicesId()));
			}

			subMon.worked(5);

			if (getConfiguration().testOption(IRemoteProxyOptions.MANUAL_LAUNCH)) {
				// TODO: implement manual launch
			} else {
				IRemoteConnectionManager connMgr = remoteServices.getConnectionManager();
				IRemoteConnection conn = connMgr.getConnection(getConfiguration().getConnectionName());
				if (conn == null) {
					throw new IOException(NLS.bind(Messages.PBSProxyRuntimeClient_2, getConfiguration().getConnectionName()));
				}

				subMon.subTask(Messages.PBSProxyRuntimeClient_3);

				if (!conn.isOpen()) {
					conn.open(subMon.newChild(4));
				}
				if (monitor.isCanceled()) {
					return;
				}

				subMon.subTask(Messages.PBSProxyRuntimeClient_4);

				sessionCreate();

				subMon.worked(1);

				String args = ""; //$NON-NLS-1$

				if (getConfiguration().testOption(IRemoteProxyOptions.PORT_FORWARDING)) {
					int remotePort;
					try {
						remotePort = conn.forwardRemotePort("localhost", getSessionPort(), subMon.newChild(1)); //$NON-NLS-1$
					} catch (RemoteConnectionException e) {
						throw new IOException(e.getMessage());
					}
					if (subMon.isCanceled()) {
						sessionFinish();
						return;
					}
					args = "--host=localhost --port=" + remotePort; //$NON-NLS-1$
				} else {
					args = "--host=" + getConfiguration().getLocalAddress(); //$NON-NLS-1$
					args += " --port=" + getSessionPort(); //$NON-NLS-1$
				}

				if (getDebugOptions().SERVER_DEBUG_LEVEL > 0) {
					// args += " --debug=" + getDebugOptions().SERVER_DEBUG_LEVEL; //$NON-NLS-1$
				}

				if (getDebugOptions().CLIENT_TRACING) {
					System.out.println("Server args: " + args.toString()); //$NON-NLS-1$
				}

				subMon.subTask(Messages.PBSProxyRuntimeClient_5);

				PBSProxyServerRunner runner = (PBSProxyServerRunner) RemoteServerManager.getServer(PBSProxyServerRunner.SERVER_ID,
						conn);
				runner.setVariable("payload_args", args); //$NON-NLS-1$
				runner.setVariable("javaargs", getConfiguration().getInvocationOptionsStr()); //$NON-NLS-1$
				runner.setWorkDir(new Path(conn.getWorkingDirectory()).append(".eclipsesettings").toString()); //$NON-NLS-1$
				runner.startServer(subMon);

				synchronized (this) {
					fServerRunner = runner;
				}

				subMon.worked(2);
			}

			subMon.subTask(Messages.PBSProxyRuntimeClient_6);
			super.startup();
			subMon.worked(2);

		} catch (IOException e) {
			try {
				sessionFinish();
			} catch (IOException e1) {
				PTPCorePlugin.log(e1);
			}
			throw new IOException(NLS.bind(Messages.PBSProxyRuntimeClient_7, e.getMessage()));
		} catch (RemoteConnectionException e) {
			try {
				sessionFinish();
			} catch (IOException e1) {
				PTPCorePlugin.log(e1);
			}
			throw new IOException(NLS.bind(Messages.PBSProxyRuntimeClient_7, e.getMessage()));
		} finally {
			if (monitor != null) {
				monitor.done();
			}
			synchronized (this) {
				fServerRunner = null;
			}
		}
	}
}
