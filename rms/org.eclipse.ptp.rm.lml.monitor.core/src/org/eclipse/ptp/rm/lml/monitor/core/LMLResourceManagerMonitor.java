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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionManager;
import org.eclipse.ptp.remote.core.IRemoteProcess;
import org.eclipse.ptp.remote.core.IRemoteProcessBuilder;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;
import org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManagerConfiguration;
import org.eclipse.ptp.rm.lml.monitor.LMLMonitorCorePlugin;
import org.eclipse.ptp.rmsystem.AbstractResourceManagerConfiguration;
import org.eclipse.ptp.rmsystem.AbstractResourceManagerMonitor;
import org.eclipse.ptp.rmsystem.IJobStatus;

/**
 * LML JAXB resource manager monitor
 */
public class LMLResourceManagerMonitor extends AbstractResourceManagerMonitor {
	private final IJAXBResourceManagerConfiguration fConfig;
	private MonitorJob fMonitorJob = null;

	private class MonitorJob extends Job {

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.runtime.jobs.Job#canceling()
		 */
		@Override
		protected void canceling() {
			if (fProcess != null && !fProcess.isCompleted()) {
				fProcess.destroy();
			}
			super.canceling();
		}

		private final IRemoteConnection fConnection;
		private final IRemoteProcessBuilder fProcessBuilder;
		private IRemoteProcess fProcess = null;

		public MonitorJob(String name, IRemoteConnection conn) {
			super(name);
			fConnection = conn;
			fProcessBuilder = fConnection.getRemoteServices().getProcessBuilder(conn, "");
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			try {
				fProcess = fProcessBuilder.start();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				fProcess.waitFor();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return Status.OK_STATUS;
		}
	}

	public LMLResourceManagerMonitor(AbstractResourceManagerConfiguration config) {
		super(config);
		fConfig = (IJAXBResourceManagerConfiguration) config;
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