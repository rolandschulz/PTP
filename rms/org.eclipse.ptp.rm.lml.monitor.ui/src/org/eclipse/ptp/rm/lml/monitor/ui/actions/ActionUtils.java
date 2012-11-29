/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.lml.monitor.ui.actions;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.List;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ptp.core.jobs.IJobStatus;
import org.eclipse.ptp.core.util.CoreExceptionUtils;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionManager;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
import org.eclipse.ptp.remote.core.RemoteServicesDelegate;
import org.eclipse.ptp.rm.jaxb.control.ILaunchController;
import org.eclipse.ptp.rm.jaxb.control.LaunchControllerManager;
import org.eclipse.ptp.rm.lml.core.JobStatusData;
import org.eclipse.ptp.rm.lml.core.LMLManager;
import org.eclipse.ptp.rm.lml.monitor.core.MonitorControlManager;
import org.eclipse.ptp.rm.lml.monitor.ui.messages.Messages;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleOutputStream;

/**
 * Executes various control and status calls on the resource manager initiated by actions.
 * 
 * @author arossi
 * 
 */
public class ActionUtils {

	/**
	 * Does reads from an EFS file store to an IOConsole. Used to fetch the stdout and stderr from batch jobs.
	 * 
	 * @author arossi
	 * 
	 */
	private static class FileReadConsoleAppender extends Job {
		private IOConsole console;
		private IOConsoleOutputStream stream;
		private int read;
		private final String fRemoteServicesId;
		private final String fConnName;

		/**
		 * @param name
		 *            path of file to read
		 */
		public FileReadConsoleAppender(String remoteServices, String connName, String path) {
			super(path);
			fRemoteServicesId = remoteServices;
			fConnName = connName;
			read = 0;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime. IProgressMonitor)
		 */
		@Override
		protected IStatus run(IProgressMonitor monitor) {
			try {
				console = new IOConsole(getName(), null);
				ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[] { console });
				console.activate();
				stream = console.newOutputStream();
				SubMonitor progress = SubMonitor.convert(monitor, 1000);
				IFileStore lres = getRemoteFile(fRemoteServicesId, fConnName, getName(), progress);
				if (lres != null) {
					BufferedInputStream is = new BufferedInputStream(lres.openInputStream(EFS.NONE, progress.newChild(25)));
					byte[] buffer = new byte[COPY_BUFFER_SIZE];
					try {
						while (!progress.isCanceled()) {
							try {
								read = is.read(buffer, 0, COPY_BUFFER_SIZE);
							} catch (final EOFException t) {
								break;
							}
							if (read == UNDEFINED) {
								break;
							}
							if (progress.isCanceled()) {
								read = UNDEFINED;
								break;
							}
							progress.worked(5);
							stream.write(buffer, 0, read);
						}
					} finally {
						try {
							if (stream != null) {
								stream.flush();
								stream.close();
							}
							if (is != null) {
								is.close();
							}
						} catch (IOException t) {
							t.printStackTrace();
						}
						monitor.done();
					}
				}
			} catch (Throwable t) {
				if (!monitor.isCanceled()) {
					return CoreExceptionUtils.getErrorStatus(t.getMessage(), t);
				}
			}
			return Status.OK_STATUS;
		}
	}

	private static final int UNDEFINED = -1;
	private static final int COPY_BUFFER_SIZE = 64 * 1024;

	/**
	 * Exercises a control operation on the remote job.
	 * 
	 * @param job
	 * @param autoStart
	 * @param operation
	 * @throws CoreException
	 */
	public static void callDoControl(JobStatusData status, String operation, IProgressMonitor monitor) throws CoreException {
		SubMonitor progress = SubMonitor.convert(monitor, 20);
		try {
			ILaunchController controller = LaunchControllerManager.getInstance().getLaunchController(status.getRemoteId(),
					status.getConnectionName(), status.getConfigurationName());
			if (controller != null) {
				controller.start(progress.newChild(10));
				controller.control(status.getJobId(), operation, progress.newChild(10));
				maybeUpdateJobState(controller, status, progress.newChild(10));
			}
		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}
	}

	/**
	 * @param status
	 * @return
	 */
	public static boolean isAuthorised(JobStatusData status) {
		if (status.getRemoteId() == null || status.getConnectionName() == null) {
			return false;
		}
		IRemoteServices services = PTPRemoteCorePlugin.getDefault().getRemoteServices(status.getRemoteId());
		if (!services.isInitialized()) {
			return false;
		}
		IRemoteConnection connection = services.getConnectionManager().getConnection(status.getConnectionName());
		if (connection == null || !connection.getUsername().equals(status.getOwner())) {
			return false;
		}
		return true;
	}

	/**
	 * 
	 * @param job
	 * @param autoStart
	 * @param monitor
	 * @throws CoreException
	 */
	public static void maybeUpdateJobState(JobStatusData status, IProgressMonitor monitor) throws CoreException {
		SubMonitor progress = SubMonitor.convert(monitor, 20);
		try {
			ILaunchController controller = LaunchControllerManager.getInstance().getLaunchController(status.getRemoteId(),
					status.getConnectionName(), status.getConfigurationName());
			if (controller != null) {
				controller.start(progress.newChild(10));
				maybeUpdateJobState(controller, status, progress.newChild(10));
			}
		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}
	}

	private static void maybeUpdateJobState(ILaunchController controller, JobStatusData status, IProgressMonitor monitor)
			throws CoreException {
		SubMonitor progress = SubMonitor.convert(monitor, 20);
		try {
			IJobStatus refreshed = controller.getJobStatus(status.getJobId(), true, progress.newChild(10));
			status.updateState(refreshed.getState(), refreshed.getStateDetail());
			maybeCheckFiles(status);
			String monitorId = MonitorControlManager.generateMonitorId(status.getRemoteId(), status.getConnectionName(),
					status.getConfigurationName());
			LMLManager.getInstance().updateUserJob(monitorId, status.getJobId(), status.getState(), status.getStateDetail());
		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}

	}

	/**
	 * Streamed read from EFS stream to Console. Uses {@link FileReadConsoleAppender}
	 * 
	 * @param resourceManagerId
	 * @param path
	 */
	public static void readRemoteFile(String remoteServicesId, String connName, String path) {
		FileReadConsoleAppender reader = new FileReadConsoleAppender(remoteServicesId, connName, path);
		reader.setUser(true);
		reader.schedule();
	}

	/**
	 * Delete stdout or stderr files on remote host.
	 * 
	 * @param selected
	 *            list of job data objects
	 */
	public static void removeFiles(final List<JobStatusData> selected) {
		Job j = new Job(Messages.ActionUtils_Remove_Files) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				SubMonitor progress = SubMonitor.convert(monitor, 50 * selected.size());
				for (JobStatusData status : selected) {
					if (status.getRemoteId() != null && status.getConnectionName() != null) {
						String remotePath = status.getOutputPath();
						if (remotePath != null) {
							try {
								IFileStore lres = getRemoteFile(status.getRemoteId(), status.getConnectionName(), remotePath,
										progress);
								if (lres != null) {
									if (lres.fetchInfo(EFS.NONE, progress.newChild(25)).exists()) {
										lres.delete(EFS.NONE, progress.newChild(25));
									}
								}
							} catch (Throwable t) {
								// continue to remove if possible
							}
						}
						remotePath = status.getErrorPath();
						if (remotePath != null) {
							try {
								IFileStore lres = getRemoteFile(status.getRemoteId(), status.getConnectionName(), remotePath,
										progress);
								if (lres != null) {
									if (lres.fetchInfo(EFS.NONE, progress.newChild(25)).exists()) {
										lres.delete(EFS.NONE, progress.newChild(25));
									}
								}
							} catch (Throwable t) {
								// continue to remove if possible
							}
						}
					}
				}
				return Status.OK_STATUS;
			}
		};
		j.setUser(true);
		j.schedule();
	}

	/**
	 * Similar to {@link RemoteServicesDelegate#initialize(IProgressMonitor)}
	 * 
	 * @param path
	 * @param control
	 * @param progress
	 * @return file, if retrieval was successful
	 */
	private static IFileStore getRemoteFile(String remoteServicesId, String remoteConnectionName, String path, SubMonitor progress) {
		if (PTPRemoteCorePlugin.getDefault() == null) {
			return null;
		}
		IRemoteServices remoteServices = PTPRemoteCorePlugin.getDefault()
				.getRemoteServices(remoteServicesId, progress.newChild(25));
		if (remoteServices != null) {
			IRemoteConnectionManager remoteConnectionManager = remoteServices.getConnectionManager();
			if (remoteConnectionManager != null) {
				IRemoteConnection remoteConnection = remoteConnectionManager.getConnection(remoteConnectionName);
				if (remoteConnection != null) {
					IRemoteFileManager remoteFileManager = remoteServices.getFileManager(remoteConnection);
					if (remoteFileManager != null) {
						return remoteFileManager.getResource(path);
					}
				}
			}
		}
		return null;
	}

	/**
	 * Set the flags if this update carries ready info for the output files.
	 * 
	 * @param job
	 */
	private static void maybeCheckFiles(JobStatusData job) {
		if (IJobStatus.JOB_OUTERR_READY.equals(job.getStateDetail())) {
			if (job.getOutputPath() != null) {
				job.setOutReady(true);
			}
			if (job.getErrorPath() != null) {
				job.setErrReady(true);
			}
		}
	}

	/*
	 * static only
	 */
	private ActionUtils() {
	}
}
