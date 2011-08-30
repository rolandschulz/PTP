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
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

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
import org.eclipse.ptp.core.util.CoreExceptionUtils;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionManager;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;
import org.eclipse.ptp.remote.core.server.RemoteServerManager;
import org.eclipse.ptp.rm.core.rmsystem.AbstractRemoteResourceManagerConfiguration;
import org.eclipse.ptp.rm.jaxb.control.JAXBResourceManagerConfiguration;
import org.eclipse.ptp.rm.jaxb.core.data.MonitorCommandType;
import org.eclipse.ptp.rm.jaxb.core.data.MonitorDriverType;
import org.eclipse.ptp.rm.jaxb.core.data.MonitorType;
import org.eclipse.ptp.rm.jaxb.core.data.ResourceManagerData;
import org.eclipse.ptp.rm.lml.core.JobStatusData;
import org.eclipse.ptp.rm.lml.core.LMLManager;
import org.eclipse.ptp.rm.lml.da.server.core.LMLDAServer;
import org.eclipse.ptp.rm.lml.internal.core.elements.CommandType;
import org.eclipse.ptp.rm.lml.internal.core.elements.DriverType;
import org.eclipse.ptp.rm.lml.internal.core.elements.RequestType;
import org.eclipse.ptp.rm.lml.monitor.LMLMonitorCorePlugin;
import org.eclipse.ptp.rm.lml.monitor.core.messages.Messages;
import org.eclipse.ptp.rmsystem.AbstractResourceManagerConfiguration;
import org.eclipse.ptp.rmsystem.AbstractResourceManagerMonitor;
import org.eclipse.ptp.rmsystem.IJobStatus;
import org.eclipse.ptp.ui.IRMSelectionListener;
import org.eclipse.ptp.ui.PTPUIPlugin;
import org.eclipse.ptp.ui.managers.RMManager;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.XMLMemento;

/**
 * LML JAXB resource manager monitor
 */
@SuppressWarnings("restriction")
public class LMLResourceManagerMonitor extends AbstractResourceManagerMonitor {
	/**
	 * Job for running the LML DA server. This job gets run periodically based
	 * on the JOB_SCHEDULE_FREQUENCY.
	 */
	private class MonitorJob extends Job {
		private final LMLDAServer fServer;

		public MonitorJob(String name, IRemoteConnection conn) {
			super(name);
			setSystem(true);
			fServer = (LMLDAServer) RemoteServerManager.getServer(
					LMLDAServer.SERVER_ID, conn);
			fServer.setWorkDir(new Path(conn.getWorkingDirectory()).append(
					".eclipsesettings").toString()); //$NON-NLS-1$
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			final SubMonitor subMon = SubMonitor.convert(monitor, 100);
			try {
				try {
					fServer.startServer(subMon.newChild(20));
					if (!subMon.isCanceled()) {
						fServer.waitForServerStart(subMon.newChild(20));
						if (!subMon.isCanceled()) {
							LMLManager.getInstance().update(
									getResourceManager().getUniqueName(),
									fServer.getInputStream(),
									fServer.getOutputStream());
						}
					}
				} catch (final Exception e) {
					fRMManager.removeRMSelectionListener(fListener);
					fireResourceManagerError(e.getLocalizedMessage());
					return new Status(IStatus.ERROR,
							LMLMonitorCorePlugin.PLUGIN_ID,
							e.getLocalizedMessage());
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

	/**
	 * Listener for resource manager selection events. These are generated when
	 * the user selects a resource manager in the RM view. This will cause the
	 * UI to switch to displaying the jobs/nodes for that RM.
	 */
	private class RMListener implements IRMSelectionListener {
		public void selectionChanged(ISelection selection) {
			String name = null;
			if (!selection.isEmpty()) {
				final TreePath path = ((ITreeSelection) selection).getPaths()[0];
				final Object segment = path.getFirstSegment();
				if (segment instanceof IPResourceManager) {
					name = ((IPResourceManager) segment).getResourceManager()
							.getUniqueName();
				}
			}
			final RMSelectionJob job = new RMSelectionJob(
					Messages.LMLResourceManagerMonitor_RMSelectionJob, name);
			job.schedule();
		}

		public void setDefault(Object rm) {
			// TODO Auto-generated method stub
		}
	}

	/**
	 * Job for updating the UI when a resource manager is selected. This is done
	 * in a job since the update is a long running operation.
	 */
	private class RMSelectionJob extends Job {
		private final String fRMName;

		public RMSelectionJob(String jobName, String rmName) {
			super(jobName);
			setSystem(true);
			fRMName = rmName;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			LMLManager.getInstance().selectLgui(fRMName);
			return Status.OK_STATUS;
		}
	}

	private static final String USER_JOBS = "user-jobs";//$NON-NLS-1$ 

	/*
	 * needs to be parameter
	 */
	private static final int JOB_SCHEDULE_FREQUENCY = 60000;

	private MonitorJob fMonitorJob = null;
	private final RMListener fListener = new RMListener();
	private final RMManager fRMManager = PTPUIPlugin.getDefault()
			.getRMManager();
	private final LMLManager fLMLManager = LMLManager.getInstance();

	private static final String LAYOUT = "layout";//$NON-NLS-1$
	private static final String LAYOUT_STRING = "layoutString";//$NON-NLS-1$
	private static final String JOB_ID_ATTR = "job_id";//$NON-NLS-1$
	private static final String RM_ID_ATTR = "rm_id";//$NON-NLS-1$
	private static final String STDOUT_REMOTE_FILE_ATTR = "stdout_remote_path";//$NON-NLS-1$
	private static final String STDERR_REMOTE_FILE_ATTR = "stderr_remote_path";//$NON-NLS-1$
	private static final String INTERACTIVE_ATTR = "interactive";//$NON-NLS-1$;
	private static final String STATE_ATTR = "state";//$NON-NLS-1$;
	private static final String STATE_DETAIL_ATTR = "state_detail";//$NON-NLS-1$;
	private static final String OID_ATTR = "oid";//$NON-NLS-1$;
	private static final String QUEUE_NAME_ATTR = "queue_name";//$NON-NLS-1$;
	private static final String OWNER_ATTR = "owner";//$NON-NLS-1$;

	public LMLResourceManagerMonitor(AbstractResourceManagerConfiguration config) {
		super(config);
	}

	private RequestType getMonitorConfigurationRequestType() {
		final JAXBResourceManagerConfiguration config = (JAXBResourceManagerConfiguration) getResourceManager()
				.getConfiguration();
		ResourceManagerData data;
		try {
			data = config.getResourceManagerData();
		} catch (final Throwable e) {
			return null;
		}

		final MonitorType monitorType = data.getMonitorData();
		RequestType request = null;
		if (monitorType != null) {
			request = new RequestType();
			for (final MonitorDriverType monitorDriver : monitorType
					.getDriver()) {
				final DriverType driver = new DriverType();
				driver.setName(monitorType.getSchedulerType());

				for (final MonitorCommandType cmd : monitorDriver.getCmd()) {
					final CommandType command = new CommandType();
					command.setName(cmd.getName());
					command.setExec(cmd.getExec());
					driver.getCommand().add(command);
				}
				request.getDriver().add(driver);
			}

			if (monitorType.getDriver().size() == 0) {
				final DriverType driver = new DriverType();
				driver.setName(monitorType.getSchedulerType());
				request.getDriver().add(driver);
			}
		}
		return request;
	}

	// private String getMonitorConfigurationString() {
	// final JAXBResourceManagerConfiguration config =
	// (JAXBResourceManagerConfiguration) getResourceManager()
	// .getConfiguration();
	// ResourceManagerData data;
	// try {
	// data = config.getResourceManagerData();
	// } catch (final Throwable e) {
	//			return ""; //$NON-NLS-1$
	// }
	// final MonitorType monitorType = data.getMonitorData();
	// return JAXBInitializationUtils.marshalData(monitorType,
	//				MonitorType.class, "monitor-data"); //$NON-NLS-1$
	// }

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
		final AbstractRemoteResourceManagerConfiguration conf = (AbstractRemoteResourceManagerConfiguration) getMonitorConfiguration();
		String id;
		String name;
		if (conf.getUseDefault()) {
			id = getResourceManager().getControlConfiguration()
					.getRemoteServicesId();
			name = getResourceManager().getControlConfiguration()
					.getConnectionName();
		} else {
			id = getMonitorConfiguration().getRemoteServicesId();
			name = getMonitorConfiguration().getConnectionName();
		}
		final IRemoteServices services = PTPRemoteCorePlugin.getDefault()
				.getRemoteServices(id, monitor);
		if (services != null) {
			final IRemoteConnectionManager connMgr = services
					.getConnectionManager();
			return connMgr.getConnection(name);
		}
		return null;
	}

	private JobStatusData[] reload(IMemento memento) {
		final List<JobStatusData> jobs = new ArrayList<JobStatusData>();
		if (memento != null) {
			final IMemento[] children = memento.getChildren(JOB_ID_ATTR);
			for (final IMemento child : children) {
				jobs.add(new JobStatusData(child.getID(), child
						.getString(RM_ID_ATTR), child.getString(STATE_ATTR),
						child.getString(STATE_DETAIL_ATTR), child
								.getString(STDOUT_REMOTE_FILE_ATTR), child
								.getString(STDERR_REMOTE_FILE_ATTR), child
								.getBoolean(INTERACTIVE_ATTR), child
								.getString(QUEUE_NAME_ATTR), child
								.getString(OWNER_ATTR), child
								.getString(OID_ATTR)));
			}
		}
		return jobs.toArray(new JobStatusData[jobs.size()]);
	}

	private void save(JobStatusData job, XMLMemento memento) {
		final IMemento jobMemento = memento.createChild(JOB_ID_ATTR,
				job.getJobId());
		jobMemento.putString(RM_ID_ATTR, job.getRmId());
		jobMemento.putString(STATE_ATTR, job.getState());
		jobMemento.putString(STATE_DETAIL_ATTR, job.getStateDetail());
		jobMemento.putString(STDOUT_REMOTE_FILE_ATTR, job.getOutputPath());
		jobMemento.putString(STDERR_REMOTE_FILE_ATTR, job.getErrorPath());
		jobMemento.putBoolean(INTERACTIVE_ATTR, job.isInteractive());
		jobMemento.putString(QUEUE_NAME_ATTR, job.getQueueName());
		jobMemento.putString(OWNER_ATTR, job.getOwner());
		jobMemento.putString(OID_ATTR, job.getOid());

	}

	@Override
	protected void doAddJob(String jobId, IJobStatus status) {
		final JobStatusData data = new JobStatusData(status.getRmUniqueName(),
				status.getJobId(), status.getQueueName(), status.getOwner(),
				status.getOutputPath(), status.getErrorPath(),
				status.isInteractive());
		data.setState(status.getState());
		data.setStateDetail(status.getStateDetail());
		fLMLManager.addUserJob(getResourceManager().getUniqueName(), jobId,
				data);
	}

	@Override
	protected void doDispose() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void doRemoveJob(String jobId) {
		fLMLManager.removeUserJob(getResourceManager().getUniqueName(), jobId);
	}

	@Override
	protected void doShutdown() throws CoreException {
		fRMManager.removeRMSelectionListener(fListener);

		/*
		 * Give LML manager fresh memento to save
		 */
		final XMLMemento memento = XMLMemento.createWriteRoot(USER_JOBS);

		if (memento != null) {
			final String layout = fLMLManager
					.getCurrentLayout(getResourceManager().getUniqueName());
			final JobStatusData[] jobs = fLMLManager
					.getUserJobs(getResourceManager().getUniqueName());

			if (layout != null) {
				final IMemento layoutMemento = memento.createChild(LAYOUT);
				layoutMemento.putString(LAYOUT_STRING, layout);
			}

			if (jobs != null && jobs.length > 0) {
				for (final JobStatusData status : jobs) {
					if (!status.isRemoved()) {
						save(status, memento);
					}
				}
			}
		}

		// fLMLManager.closeLgui(getResourceManager().getUniqueName(), memento);
		fLMLManager.closeLgui(getResourceManager().getUniqueName());

		final StringWriter writer = new StringWriter();
		if (memento != null) {
			try {
				memento.save(writer);
			} catch (final IOException t) {
				throw CoreExceptionUtils.newException(t.getMessage(), t);
			}
		}

		/*
		 * Too late for API change to IResourceManagerComponentConfiguration
		 * (05/27/2011 - alr) FIXME
		 */
		((AbstractResourceManagerConfiguration) getMonitorConfiguration())
				.putString(USER_JOBS, writer.toString());

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
		 * Too late for API change to IResourceManagerComponentConfiguration
		 * (05/27/2011 - alr) FIXME
		 */
		final String userJobs = ((AbstractResourceManagerConfiguration) getMonitorConfiguration())
				.getString(USER_JOBS, null);

		IMemento memento = null;
		if (userJobs != null) {
			memento = XMLMemento.createReadRoot(new StringReader(userJobs));
		}

		final StringBuilder layout = new StringBuilder();
		JobStatusData[] jobs = null;

		if (memento != null) {
			final IMemento child = memento.getChild(LAYOUT);
			if (child != null) {
				final String childString = child.getString(LAYOUT_STRING);
				layout.append(childString);
			}

			jobs = reload(memento);
		}
		/*
		 * Initialize LML classes
		 */
		// fLMLManager.openLgui(getResourceManager().getUniqueName(), memento);
		fLMLManager.openLgui(getResourceManager().getUniqueName(),
				getMonitorConfigurationRequestType(), layout, jobs);
		/*
		 * Open connection and launch periodic job
		 */
		final IRemoteConnection conn = getRemoteConnection(monitor);
		if (conn != null) {
			if (!conn.isOpen()) {
				try {
					conn.open(monitor);
				} catch (final RemoteConnectionException e) {
					throw new CoreException(new Status(IStatus.ERROR,
							LMLMonitorCorePlugin.getUniqueIdentifier(),
							e.getMessage()));
				}
			}
			if (!conn.isOpen()) {
				throw new CoreException(
						new Status(
								IStatus.ERROR,
								LMLMonitorCorePlugin.getUniqueIdentifier(),
								Messages.LMLResourceManagerMonitor_unableToOpenConnection));
			}
			synchronized (this) {
				if (fMonitorJob == null) {
					fMonitorJob = new MonitorJob(
							Messages.LMLResourceManagerMonitor_LMLMonitorJob,
							conn);
				}
				fMonitorJob.schedule();
			}
		}

		/*
		 * Register for notifications from RM view
		 */
		fRMManager.addRMSelectionListener(fListener);
	}

	@Override
	protected void doUpdateJob(String jobId, IJobStatus status) {
		fLMLManager.updateUserJob(getResourceManager().getUniqueName(), jobId,
				status.getState(), status.getStateDetail());
	}
}