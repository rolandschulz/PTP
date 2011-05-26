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
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.ptp.core.elements.IPResourceManager;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionManager;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;
import org.eclipse.ptp.remote.core.server.RemoteServerManager;
import org.eclipse.ptp.rm.core.rmsystem.AbstractRemoteResourceManagerConfiguration;
import org.eclipse.ptp.rm.lml.core.LMLManager;
import org.eclipse.ptp.rm.lml.da.server.core.LMLDAServer;
import org.eclipse.ptp.rm.lml.monitor.LMLMonitorCorePlugin;
import org.eclipse.ptp.rm.lml.monitor.core.messages.Messages;
import org.eclipse.ptp.rmsystem.AbstractResourceManagerConfiguration;
import org.eclipse.ptp.rmsystem.AbstractResourceManagerMonitor;
import org.eclipse.ptp.rmsystem.IJobStatus;
import org.eclipse.ptp.ui.IRMSelectionListener;
import org.eclipse.ptp.ui.PTPUIPlugin;
import org.eclipse.ptp.ui.managers.RMManager;

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
			SubMonitor subMon = SubMonitor.convert(monitor, 100);
			try {
				try {
					fServer.startServer(subMon.newChild(20));
					if (!subMon.isCanceled()) {
						fServer.waitForServerStart(subMon.newChild(20));
						if (!subMon.isCanceled()) {
							LMLManager.getInstance().register(getResourceManager().getUniqueName(), fServer.getInputStream(),
									fServer.getOutputStream());
						}
					}
				} catch (IOException e) {
					fireResourceManagerError(e.getLocalizedMessage());
					return new Status(IStatus.ERROR, LMLMonitorCorePlugin.PLUGIN_ID, e.getLocalizedMessage());
				}
				fServer.waitForServerFinish(subMon.newChild(40));
				if (!subMon.isCanceled()) {
					schedule(JOB_SCHEDULE_FREQUENCY);
				}
				return Status.OK_STATUS;
			} finally {
				if (monitor != null) {
					monitor.done();
				}
			}
		}
	}

	public class RMListener implements IRMSelectionListener {
		public void setDefault(Object rm) {
			// TODO Auto-generated method stub

		}

		public void selectionChanged(ISelection selection) {
			String name = null;
			if (!selection.isEmpty()) {
				TreePath path = ((ITreeSelection) selection).getPaths()[0];
				Object segment = path.getFirstSegment();
				if (segment instanceof IPResourceManager) {
					name = ((IPResourceManager) segment).getResourceManager().getUniqueName();
				}
			}
			LMLManager.getInstance().selectLgui(name);
		}
	}

	/*
	 * needs to be parameter
	 */
	private static final int JOB_SCHEDULE_FREQUENCY = 60000;

	private MonitorJob fMonitorJob = null;
	private final RMListener fListener = new RMListener();
	private final RMManager fRMManager = PTPUIPlugin.getDefault().getRMManager();

	public LMLResourceManagerMonitor(AbstractResourceManagerConfiguration config) {
		super(config);
	}

	/**
	 * Get the remote connection specified by the monitor configuration. This
	 * may be the same as the control connection (if "use same" is selected) or
	 * an independent connection.
	 * 
	 * @param monitor
	 *            progress monitor
	 * @return connection for the monitor
	 */
	private IRemoteConnection getRemoteConnection(IProgressMonitor monitor) {
		AbstractRemoteResourceManagerConfiguration conf = (AbstractRemoteResourceManagerConfiguration) getMonitorConfiguration();
		String id;
		String name;
		if (conf.getUseDefault()) {
			id = getResourceManager().getControlConfiguration().getRemoteServicesId();
			name = getResourceManager().getControlConfiguration().getConnectionName();
		} else {
			id = getMonitorConfiguration().getRemoteServicesId();
			name = getMonitorConfiguration().getConnectionName();
		}
		IRemoteServices services = PTPRemoteCorePlugin.getDefault().getRemoteServices(id, monitor);
		if (services != null) {
			IRemoteConnectionManager connMgr = services.getConnectionManager();
			return connMgr.getConnection(name);
		}
		return null;
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
		fRMManager.removeRMSelectionListener(fListener);

		LMLManager.getInstance().closeLgui(getResourceManager().getUniqueName());

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
		LMLManager.getInstance().openLgui(getResourceManager().getUniqueName());

		/*
		 * Open connection and launch periodic job
		 */
		IRemoteConnection conn = getRemoteConnection(monitor);
		if (conn != null) {
			if (!conn.isOpen()) {
				try {
					conn.open(monitor);
				} catch (RemoteConnectionException e) {
					throw new CoreException(new Status(IStatus.ERROR, LMLMonitorCorePlugin.getUniqueIdentifier(), e.getMessage()));
				}
			}
			if (!conn.isOpen()) {
				throw new CoreException(new Status(IStatus.ERROR, LMLMonitorCorePlugin.getUniqueIdentifier(),
						Messages.LMLResourceManagerMonitor_unableToOpenConnection));
			}
			synchronized (this) {
				if (fMonitorJob == null) {
					fMonitorJob = new MonitorJob(Messages.LMLResourceManagerMonitor_LMLMonitorJob, conn);
					fMonitorJob.schedule();
				}
			}
		}

		/*
		 * Register for notifications from RM view
		 */
		fRMManager.addRMSelectionListener(fListener);
	}

	@Override
	protected void doUpdateJob(String jobId, IJobStatus status) {
		// TODO Auto-generated method stub

	}
}