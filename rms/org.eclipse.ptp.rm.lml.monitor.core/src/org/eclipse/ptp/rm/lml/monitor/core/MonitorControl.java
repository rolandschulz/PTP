/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rm.lml.monitor.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.core.jobs.IJobListener;
import org.eclipse.ptp.core.jobs.IJobStatus;
import org.eclipse.ptp.core.jobs.JobManager;
import org.eclipse.ptp.core.util.CoreExceptionUtils;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionManager;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;
import org.eclipse.ptp.remote.core.server.RemoteServerManager;
import org.eclipse.ptp.rm.jaxb.control.core.ILaunchController;
import org.eclipse.ptp.rm.jaxb.control.core.LaunchControllerManager;
import org.eclipse.ptp.rm.jaxb.core.data.MonitorDriverType;
import org.eclipse.ptp.rm.jaxb.core.data.MonitorType;
import org.eclipse.ptp.rm.jaxb.core.data.SimpleCommandType;
import org.eclipse.ptp.rm.lml.core.JobStatusData;
import org.eclipse.ptp.rm.lml.core.LMLManager;
import org.eclipse.ptp.rm.lml.da.server.core.LMLDAServer;
import org.eclipse.ptp.rm.lml.internal.core.elements.CommandType;
import org.eclipse.ptp.rm.lml.internal.core.elements.DriverType;
import org.eclipse.ptp.rm.lml.internal.core.elements.RequestType;
import org.eclipse.ptp.rm.lml.monitor.LMLMonitorCorePlugin;
import org.eclipse.ptp.rm.lml.monitor.core.messages.Messages;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.XMLMemento;

/**
 * LML JAXB resource manager monitor
 */
@SuppressWarnings("restriction")
public class MonitorControl implements IMonitorControl {
	private class JobListener implements IJobListener {
		@Override
		public void jobAdded(IJobStatus status) {
			addJob(status);
		}

		@Override
		public void jobChanged(IJobStatus status) {
			updateJob(status);
		}
	}

	/**
	 * Job for running the LML DA server. This job gets run periodically based
	 * on the JOB_SCHEDULE_FREQUENCY.
	 */
	private class MonitorJob extends Job {
		private final LMLDAServer fServer;

		public MonitorJob(IRemoteConnection conn) {
			super(Messages.LMLResourceManagerMonitor_LMLMonitorJob);
			setSystem(true);
			fServer = (LMLDAServer) RemoteServerManager.getServer(LMLDAServer.SERVER_ID, conn);
			fServer.setWorkDir(new Path(conn.getWorkingDirectory()).append(".eclipsesettings").toString()); //$NON-NLS-1$
		}

		/**
		 * Schedule an immediate refresh
		 */
		public void refresh() {
			wakeUp();
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
							LMLManager.getInstance().update(getControlId(), fServer.getInputStream(), fServer.getOutputStream());
						}
					}
				} catch (final Exception e) {
					fActive = false;
					MonitorControlManager.getInstance().fireMonitorUpdated(new IMonitorControl[] { MonitorControl.this });
					return new Status(IStatus.ERROR, LMLMonitorCorePlugin.PLUGIN_ID, e.getLocalizedMessage());
				}
				IStatus status = fServer.waitForServerFinish(subMon.newChild(40));
				if (status == Status.OK_STATUS) {
					schedule(JOB_SCHEDULE_FREQUENCY);
				} else {
					fActive = false;
					MonitorControlManager.getInstance().fireMonitorUpdated(new IMonitorControl[] { MonitorControl.this });
				}
				return status;
			} finally {
				if (monitor != null) {
					monitor.done();
				}
			}
		}
	}

	/*
	 * needs to be parameter
	 */
	private static final int JOB_SCHEDULE_FREQUENCY = 60000;

	private MonitorJob fMonitorJob;

	private final String fControlId;
	private final LMLManager fLMLManager = LMLManager.getInstance();
	private final JobListener fJobListener = new JobListener();
	private final StringBuffer fSavedLayout = new StringBuffer();
	private final List<JobStatusData> fSavedJobs = new ArrayList<JobStatusData>();
	private String fConfigurationName;
	private boolean fActive;
	private String fRemoteServicesId;
	private String fConnectionName;

	private static final String XML = "xml";//$NON-NLS-1$ 
	private static final String JOBS_ATTR = "jobs";//$NON-NLS-1$ 
	private static final String JOB_ATTR = "job";//$NON-NLS-1$ 
	private static final String LAYOUT_ATTR = "layout";//$NON-NLS-1$
	private static final String LAYOUT_STRING_ATTR = "layoutString";//$NON-NLS-1$
	private static final String MONITOR_STATE = "monitorState";//$NON-NLS-1$;
	private static final String MONITOR_ATTR = "monitor";//$NON-NLS-1$

	public MonitorControl(String controlId) {
		fControlId = controlId;
	}

	private void addJob(IJobStatus status) {
		ILaunchController controller = LaunchControllerManager.getInstance().getLaunchController(status.getControlId());
		if (controller != null) {
			String[][] attrs = { { JobStatusData.JOB_ID_ATTR, status.getJobId() },
					{ JobStatusData.REMOTE_SERVICES_ID_ATTR, getRemoteServicesId() },
					{ JobStatusData.CONNECTION_NAME_ATTR, getConnectionName() },
					{ JobStatusData.CONFIGURATION_NAME_ATTR, controller.getConfiguration().getName() },
					{ JobStatusData.QUEUE_NAME_ATTR, status.getQueueName() }, { JobStatusData.OWNER_ATTR, status.getOwner() },
					{ JobStatusData.STDOUT_REMOTE_FILE_ATTR, status.getOutputPath() },
					{ JobStatusData.STDERR_REMOTE_FILE_ATTR, status.getErrorPath() },
					{ JobStatusData.INTERACTIVE_ATTR, Boolean.toString(status.isInteractive()) } };
			final JobStatusData data = new JobStatusData(attrs);
			data.setState(status.getState());
			data.setStateDetail(status.getStateDetail());
			fLMLManager.addUserJob(getControlId(), status.getJobId(), data);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.lml.monitor.core.IMonitorControl#dispose()
	 */
	@Override
	public void dispose() {
		try {
			getSaveLocation().delete();
		} catch (Exception e) {
			LMLMonitorCorePlugin.log(e.getLocalizedMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.lml.monitor.core.IMonitorControl#getConnectionName()
	 */
	@Override
	public String getConnectionName() {
		return fConnectionName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.lml.monitor.core.IMonitorControl#getControlId()
	 */
	@Override
	public String getControlId() {
		return fControlId;
	}

	private RequestType getMonitorConfigurationRequestType(ILaunchController controller) {
		RequestType request = new RequestType();
		final DriverType driver = new DriverType();
		driver.setName(getSystemType());
		MonitorType monitor = controller.getConfiguration().getMonitorData();
		if (monitor != null) {
			List<CommandType> driverCommands = driver.getCommand();
			for (MonitorDriverType monitorDriver : monitor.getDriver()) {
				for (SimpleCommandType monitorCmd : monitorDriver.getCmd()) {
					CommandType cmd = new CommandType();
					cmd.setName(monitorCmd.getName());
					cmd.setExec(monitorCmd.getExec());
					driverCommands.add(cmd);
				}
			}
		}
		request.getDriver().add(driver);
		return request;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.lml.monitor.core.IMonitorControl#getConfigurationName()
	 */
	@Override
	public String getConfigurationName() {
		return fConfigurationName;
	}

	/**
	 * Get the remote connection specified by the monitor configuration.
	 * 
	 * @param monitor
	 *            progress monitor
	 * @return connection for the monitor
	 */
	private IRemoteConnection getRemoteConnection(IProgressMonitor monitor) throws CoreException {
		final IRemoteServices services = PTPRemoteCorePlugin.getDefault().getRemoteServices(getRemoteServicesId(), monitor);
		if (services != null) {
			final IRemoteConnectionManager connMgr = services.getConnectionManager();
			return connMgr.getConnection(getConnectionName());
		}
		throw CoreExceptionUtils.newException(Messages.MonitorControl_unableToOpenRemoteConnection, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.lml.monitor.core.IMonitorControl#getRemoteServicesId()
	 */
	@Override
	public String getRemoteServicesId() {
		return fRemoteServicesId;
	}

	private File getSaveLocation() {
		return LMLMonitorCorePlugin.getDefault().getStateLocation().append(getControlId()).addFileExtension(XML).toFile();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.lml.monitor.core.IMonitorControl#getSystemType()
	 */
	@Override
	public String getSystemType() {
		return MonitorControlManager.getSystemType(fConfigurationName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.lml.monitor.core.IMonitorControl#isActive()
	 */
	@Override
	public boolean isActive() {
		return fActive;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.lml.monitor.core.IMonitorControl#load()
	 */
	@Override
	public boolean load() throws CoreException {
		fSavedLayout.setLength(0);
		fSavedJobs.clear();

		FileReader reader;
		try {
			reader = new FileReader(getSaveLocation());
		} catch (FileNotFoundException e) {
			throw CoreExceptionUtils.newException(e.getMessage(), e);
		}
		IMemento memento = XMLMemento.createReadRoot(reader);

		boolean active = loadState(memento);

		IMemento childLayout = memento.getChild(LAYOUT_ATTR);
		if (childLayout != null) {
			fSavedLayout.append(childLayout.getString(LAYOUT_STRING_ATTR));
		}

		childLayout = memento.getChild(JOBS_ATTR);
		loadJobs(childLayout, fSavedJobs);

		return active;
	}

	private void loadJobs(IMemento memento, List<JobStatusData> jobs) {
		if (memento != null) {
			final IMemento[] children = memento.getChildren(JOB_ATTR);
			for (final IMemento child : children) {
				String[][] attrs = { { JobStatusData.JOB_ID_ATTR, child.getID() },
						{ JobStatusData.REMOTE_SERVICES_ID_ATTR, getRemoteServicesId() },
						{ JobStatusData.CONNECTION_NAME_ATTR, getConnectionName() },
						{ JobStatusData.CONFIGURATION_NAME_ATTR, child.getString(JobStatusData.CONFIGURATION_NAME_ATTR) },
						{ JobStatusData.STATE_ATTR, child.getString(JobStatusData.STATE_ATTR) },
						{ JobStatusData.STATE_DETAIL_ATTR, child.getString(JobStatusData.STATE_DETAIL_ATTR) },
						{ JobStatusData.STDOUT_REMOTE_FILE_ATTR, child.getString(JobStatusData.STDOUT_REMOTE_FILE_ATTR) },
						{ JobStatusData.STDERR_REMOTE_FILE_ATTR, child.getString(JobStatusData.STDERR_REMOTE_FILE_ATTR) },
						{ JobStatusData.INTERACTIVE_ATTR, Boolean.toString(child.getBoolean(JobStatusData.INTERACTIVE_ATTR)) },
						{ JobStatusData.QUEUE_NAME_ATTR, child.getString(JobStatusData.QUEUE_NAME_ATTR) },
						{ JobStatusData.OWNER_ATTR, child.getString(JobStatusData.OWNER_ATTR) },
						{ JobStatusData.OID_ATTR, child.getString(JobStatusData.OID_ATTR) } };
				final JobStatusData jobData = new JobStatusData(attrs);

				for (final String attKey : child.getAttributeKeys()) {
					jobData.addInfo(attKey, child.getString(attKey));
				}

				jobs.add(jobData);
			}
		}
	}

	private boolean loadState(IMemento memento) {
		setRemoteServicesId(memento.getString(JobStatusData.REMOTE_SERVICES_ID_ATTR));
		setConnectionName(memento.getString(JobStatusData.CONNECTION_NAME_ATTR));
		setConfigurationName(memento.getString(JobStatusData.CONFIGURATION_NAME_ATTR));
		return memento.getBoolean(MONITOR_STATE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.lml.monitor.core.IMonitorControl#refresh()
	 */
	@Override
	public void refresh() {
		fMonitorJob.refresh();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.lml.monitor.core.IMonitorControl#save()
	 */
	@Override
	public void save() {
		final XMLMemento memento = XMLMemento.createWriteRoot(MONITOR_ATTR);

		final String layout = fLMLManager.getCurrentLayout(getControlId());
		final JobStatusData[] jobs = fLMLManager.getUserJobs(getControlId());

		saveState(memento);

		if (layout != null) {
			final IMemento layoutMemento = memento.createChild(LAYOUT_ATTR);
			layoutMemento.putString(LAYOUT_STRING_ATTR, layout);
		}

		if (jobs != null && jobs.length > 0) {
			final IMemento jobsMemento = memento.createChild(JOBS_ATTR);
			for (final JobStatusData status : jobs) {
				if (!status.isRemoved()) {
					saveJob(status, jobsMemento);
				}
			}
		}

		try {
			FileWriter writer = new FileWriter(getSaveLocation());
			memento.save(writer);
		} catch (final IOException e) {
			LMLMonitorCorePlugin.log(e.getLocalizedMessage());
		}

	}

	private void saveJob(JobStatusData job, IMemento memento) {
		final IMemento jobMemento = memento.createChild(JOB_ATTR, job.getJobId());
		jobMemento.putString(JobStatusData.CONFIGURATION_NAME_ATTR, job.getConfigurationName());
		jobMemento.putString(JobStatusData.STATE_ATTR, job.getState());
		jobMemento.putString(JobStatusData.STATE_DETAIL_ATTR, job.getStateDetail());
		jobMemento.putString(JobStatusData.STDOUT_REMOTE_FILE_ATTR, job.getOutputPath());
		jobMemento.putString(JobStatusData.STDERR_REMOTE_FILE_ATTR, job.getErrorPath());
		jobMemento.putBoolean(JobStatusData.INTERACTIVE_ATTR, job.isInteractive());
		jobMemento.putString(JobStatusData.QUEUE_NAME_ATTR, job.getQueueName());
		jobMemento.putString(JobStatusData.OWNER_ATTR, job.getOwner());
		jobMemento.putString(JobStatusData.OID_ATTR, job.getOid());
		
		for (final String key : job.getAdditionalKeys()) {
			// Save only the data, which was not saved by the above statements
			if (jobMemento.getString(key) == null) {
				jobMemento.putString(key, job.getInfo(key));
			}
		}
	}

	private void saveState(IMemento memento) {
		memento.putString(JobStatusData.REMOTE_SERVICES_ID_ATTR, getRemoteServicesId());
		memento.putString(JobStatusData.CONNECTION_NAME_ATTR, getConnectionName());
		memento.putString(JobStatusData.CONFIGURATION_NAME_ATTR, getConfigurationName());
		memento.putBoolean(MONITOR_STATE, isActive());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.lml.monitor.core.IMonitorControl#setConnectionName
	 * (java.lang.String)
	 */
	@Override
	public void setConnectionName(String connName) {
		fConnectionName = connName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.lml.monitor.core.IMonitorControl#setRemoteServicesId
	 * (java.lang.String)
	 */
	@Override
	public void setRemoteServicesId(String id) {
		fRemoteServicesId = id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.lml.monitor.core.IMonitorControl#setConfigurationName(java.lang.String)
	 */
	@Override
	public void setConfigurationName(String name) {
		fConfigurationName = name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.monitors.IMonitorControl#start(org.eclipse.core.
	 * runtime.IProgressMonitor)
	 */
	@Override
	public void start(IProgressMonitor monitor) throws CoreException {
		if (!isActive()) {
			SubMonitor progress = SubMonitor.convert(monitor, 30);
			try {
				ILaunchController controller = LaunchControllerManager.getInstance().getLaunchController(getRemoteServicesId(),
						getConnectionName(), getConfigurationName());

				if (controller == null) {
					throw new CoreException(new Status(IStatus.ERROR, LMLMonitorCorePlugin.getUniqueIdentifier(),
							"Unable to locate launch controller"));
				}

				try {
					load();
				} catch (CoreException e) {
					/*
					 * Can't find monitor data for some reason, so just log a
					 * message but allow the monitor to be started anyway
					 */
					LMLMonitorCorePlugin.log(e.getLocalizedMessage());
				}

				final IRemoteConnection conn = getRemoteConnection(progress.newChild(10));

				if (conn == null) {
					throw new CoreException(new Status(IStatus.ERROR, LMLMonitorCorePlugin.getUniqueIdentifier(), NLS.bind(
							Messages.MonitorControl_UnableToLocateConnection, getConnectionName())));

				}

				if (conn == null || progress.isCanceled()) {
					return;
				}

				if (!conn.isOpen()) {
					try {
						conn.open(progress.newChild(10));
					} catch (final RemoteConnectionException e) {
						throw new CoreException(new Status(IStatus.ERROR, LMLMonitorCorePlugin.getUniqueIdentifier(),
								e.getMessage()));
					}
					if (!conn.isOpen()) {
						throw new CoreException(new Status(IStatus.ERROR, LMLMonitorCorePlugin.getUniqueIdentifier(),
								Messages.LMLResourceManagerMonitor_unableToOpenConnection));
					}
				}

				/*
				 * Initialize LML classes
				 */
				fLMLManager.openLgui(getControlId(), conn.getUsername(), getMonitorConfigurationRequestType(controller),
						fSavedLayout.toString(), fSavedJobs.toArray(new JobStatusData[0]));

				fActive = true;

				MonitorControlManager.getInstance().fireMonitorUpdated(new IMonitorControl[] { this });

				/*
				 * Start monitoring job. Note that the monitoring job can fail,
				 * in which case the monitor is considered to be stopped and the
				 * active flag set appropriately.
				 */
				synchronized (this) {
					if (fMonitorJob == null) {
						fMonitorJob = new MonitorJob(conn);
					}
					fMonitorJob.schedule();
				}

				JobManager.getInstance().addListener(fJobListener);
			} finally {
				if (monitor != null) {
					monitor.done();
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.monitors.IMonitorControl#stop()
	 */
	@Override
	public void stop() throws CoreException {
		if (isActive()) {
			JobManager.getInstance().removeListener(fJobListener);

			save();

			fLMLManager.closeLgui(getControlId());

			synchronized (this) {
				if (fMonitorJob != null) {
					fMonitorJob.cancel();
					fMonitorJob = null;
				}
			}

			fActive = false;

			MonitorControlManager.getInstance().fireMonitorUpdated(new IMonitorControl[] { this });
		}
	}

	private void updateJob(IJobStatus status) {
		if (status.getControlId().equals(getControlId())) {
			fLMLManager.updateUserJob(getControlId(), status.getJobId(), status.getState(), status.getStateDetail());
		}
	}
}