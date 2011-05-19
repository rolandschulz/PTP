/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.lml.monitor.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionManager;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;
import org.eclipse.ptp.remote.core.server.RemoteServerManager;
import org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManagerConfiguration;
import org.eclipse.ptp.rm.lml.core.LMLManager;
import org.eclipse.ptp.rm.lml.da.server.core.LMLDAServer;
import org.eclipse.ptp.rm.lml.monitor.LMLMonitorCorePlugin;
import org.eclipse.ptp.rmsystem.AbstractResourceManagerConfiguration;
import org.eclipse.ptp.rmsystem.AbstractResourceManagerMonitor;
import org.eclipse.ptp.rmsystem.IJobStatus;

/**
 * LML JAXB resource manager monitor
 */
public class LMLResourceManagerMonitor extends AbstractResourceManagerMonitor {
	private class MonitorJob extends Job {
		private final LMLDAServer fServer;
		
		public MonitorJob(String name, IRemoteConnection conn) {
			super(name);
			setSystem(true);
			fServer = (LMLDAServer) RemoteServerManager.getServer(LMLDAServer.SERVER_ID, conn);
			fServer.setWorkDir(new Path(conn.getWorkingDirectory()).append(".eclipsesettings").toString()); //$NON-NLS-1$
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			try {
				fServer.startServer(monitor);
			} catch (IOException e) {
			}
			fServer.waitForServerFinish(monitor);
			if (!monitor.isCanceled()) {
				schedule(JOB_SCHEDULE_FREQUENCY);
			}
			return Status.OK_STATUS;
		}
	}

	private static final int JOB_SCHEDULE_FREQUENCY = 60000; // needs to be
	// parameter
	private final IJAXBResourceManagerConfiguration fConfig;

	private MonitorJob fMonitorJob = null;
	
	private final LMLManager lmlManager;
	
	private InputStream input;
	
	private OutputStream output;


	public LMLResourceManagerMonitor(AbstractResourceManagerConfiguration config) {
		super(config);
		fConfig = (IJAXBResourceManagerConfiguration) config;

		lmlManager = LMLManager.getInstance();
		lmlManager.addLgui(getResourceManager().getUniqueName());
	}

	@Override
	protected void doAddJob(String jobId, IJobStatus status) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void doDispose() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void doRemoveJob(String jobId) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void doShutdown() throws CoreException {
		synchronized (this) {
			if (fMonitorJob != null) {
				fMonitorJob.cancel();
				fMonitorJob = null;
			}
		}
	}

	@Override
	protected void doStartup(IProgressMonitor monitor) throws CoreException {
		/*
		 * Initialize LML classes
		 */
		lmlManager.register(getResourceManager().getUniqueName(), input, output);

		/*
		 * Open connection and launch periodic job
		 */
		String id = getMonitorConfiguration().getRemoteServicesId();
		String name = getMonitorConfiguration().getConnectionName();
		IRemoteServices services = PTPRemoteCorePlugin.getDefault().getRemoteServices(id, monitor);
		if (services != null) {
			IRemoteConnectionManager connMgr = services.getConnectionManager();
			IRemoteConnection conn = connMgr.getConnection(name);
			if (conn != null) {
				if (!conn.isOpen()) {
					try {
						conn.open(monitor);
					} catch (RemoteConnectionException e) {
						throw new CoreException(new Status(IStatus.ERROR, LMLMonitorCorePlugin.getUniqueIdentifier(),
								e.getMessage()));
					}
				}
				if (!conn.isOpen()) {
					throw new CoreException(new Status(IStatus.ERROR, LMLMonitorCorePlugin.getUniqueIdentifier(),
							"Unable to open connection"));
				}
				synchronized (this) {
					if (fMonitorJob == null) {
						fMonitorJob = new MonitorJob("LML Monitor Job", conn);
						fMonitorJob.schedule();
					}
				}
			}
		}
	}

	@Override
	protected void doUpdateJob(String jobId, IJobStatus status) {
		// TODO Auto-generated method stub

	}
}