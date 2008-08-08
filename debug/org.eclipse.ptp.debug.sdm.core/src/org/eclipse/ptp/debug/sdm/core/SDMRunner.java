package org.eclipse.ptp.debug.sdm.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.core.elementcontrols.IResourceManagerControl;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.debug.sdm.core.utils.DebugUtil;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionManager;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.remote.core.IRemoteProcess;
import org.eclipse.ptp.remote.core.IRemoteProcessBuilder;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
import org.eclipse.ptp.rm.remote.core.AbstractRemoteResourceManagerConfiguration;

public class SDMRunner extends Job {
	public enum SDMMasterState {UNKNOWN, STARTING, RUNNING, FINISHED, ERROR};

	private List<String> command;
	private String workDir;
	private SDMMasterState sdmState = SDMMasterState.STARTING;

	private IPJob ipJob = null;
	private IResourceManagerControl rmControl = null;
	private IRemoteProcess sdmProcess;

	public SDMRunner(IResourceManagerControl rmControl) {
		super("Master SDM control");
		this.setPriority(Job.LONG);
		this.setSystem(true);
		this.setSystem(false);
		this.rmControl = rmControl;
		DebugUtil.trace(DebugUtil.SDM_MASTER_TRACING_MORE, "Created sdm master runner."); //$NON-NLS-1$
	}

	public void setCommand(List<String> command) {
		DebugUtil.trace(DebugUtil.SDM_MASTER_TRACING_MORE, "sdm master: command: {0}", command.toString()); //$NON-NLS-1$
		this.command = command;
	}

	public void setWorkDir(String workDir) {
		DebugUtil.trace(DebugUtil.SDM_MASTER_TRACING_MORE, "sdm master: workdir: {0}", workDir); //$NON-NLS-1$
		this.workDir = workDir;
	}

	public synchronized SDMMasterState getSdmState() {
		return sdmState;
	}

	protected synchronized void setSdmState(SDMMasterState sdmState) {
		DebugUtil.trace(DebugUtil.SDM_MASTER_TRACING_MORE, "sdm master: changed state to {0}", sdmState.toString()); //$NON-NLS-1$
		this.sdmState = sdmState;
		this.notifyAll();
	}

	public void setJob(IPJob ipJob) {
		DebugUtil.trace(DebugUtil.SDM_MASTER_TRACING_MORE, "sdm master: associated to job #{0}", ipJob.getID()); //$NON-NLS-1$
		this.ipJob = ipJob;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		assert workDir != null;
		assert command != null;
		assert sdmProcess == null;

		DebugUtil.trace(DebugUtil.SDM_MASTER_TRACING, "sdm master: thread started."); //$NON-NLS-1$
		/*
		 * Catch all try...catch
		 */
		try {
			if (monitor.isCanceled()) throw new InterruptedException();
			/*
			 * Prepare remote connection.
			 */
			AbstractRemoteResourceManagerConfiguration configuration = (AbstractRemoteResourceManagerConfiguration) rmControl.getConfiguration();
			IRemoteServices remoteServices = PTPRemoteCorePlugin.getDefault().getRemoteServices(configuration.getRemoteServicesId());
			IRemoteConnectionManager connectionManager = remoteServices.getConnectionManager();
			IRemoteConnection connection = connectionManager.getConnection(configuration.getConnectionName());
			IRemoteFileManager fileManager = remoteServices.getFileManager(connection);

			IFileStore directory = null;
			try {
				directory = fileManager.getResource(new Path(workDir), monitor);
			} catch (IOException e) {
				throw new CoreException(new Status(IStatus.ERROR, PTPDebugCorePlugin.getUniqueIdentifier(), "Failed to determine working directory for master SDM.", e));
			}
			IRemoteProcessBuilder sdmProcessBuilder = remoteServices.getProcessBuilder(connection, command);
			sdmProcessBuilder.directory(directory);

			/*
			 * Wait some time to assure that SDM servers and front end have started.
			 */
			DebugUtil.trace(DebugUtil.SDM_MASTER_TRACING, "sdm master: waiting for SDM servers and front end."); //$NON-NLS-1$
			if (monitor.isCanceled()) throw new InterruptedException();
			synchronized (this) {
				wait(3000);
			}

			/*
			 * Create process.
			 */
			DebugUtil.trace(DebugUtil.SDM_MASTER_TRACING, "sdm master: create process"); //$NON-NLS-1$
			if (monitor.isCanceled()) throw new InterruptedException();
			synchronized (this) {
				sdmProcess = sdmProcessBuilder.start();
			}
			final BufferedReader err_reader = new BufferedReader(new InputStreamReader(sdmProcess.getErrorStream()));
			final BufferedReader out_reader = new BufferedReader(new InputStreamReader(sdmProcess.getInputStream()));

			if (DebugUtil.SDM_MASTER_OUTPUT_TRACING) {
				new Thread(new Runnable() {
					public void run() {
						try {
							String output;
							while ((output = out_reader.readLine()) != null) {
								System.out.println("sdm master: " + output); //$NON-NLS-1$
							}
						} catch (IOException e) {
							// Ignore
						}
					}
				}, "sdm master standard output thread").start(); //$NON-NLS-1$
			}

			if (DebugUtil.SDM_MASTER_OUTPUT_TRACING) {
				new Thread(new Runnable() {
					public void run() {
						try {
							String line;
							while ((line = err_reader.readLine()) != null) {
								System.err.println("sdm master: " + line); //$NON-NLS-1$
							}
						} catch (IOException e) {
							// Ignore
						}
					}
				}, "sdm master error output thread").start(); //$NON-NLS-1$
			}


			/*
			 * Wait while running but not canceled.
			 */
			setSdmState(SDMMasterState.RUNNING);
			DebugUtil.trace(DebugUtil.SDM_MASTER_TRACING_MORE, "sdm master: waiting to finish."); //$NON-NLS-1$
			while (! sdmProcess.isCompleted()) {
				synchronized (this) {
					wait(500);
				}
				if (monitor.isCanceled()) throw new InterruptedException();
			}

			/*
			 * Check if process terminated successfully (if not canceled).
			 */
			DebugUtil.trace(DebugUtil.SDM_MASTER_TRACING, "sdm master: exit value {0}.", sdmProcess.exitValue()); //$NON-NLS-1$
			if (sdmProcess.exitValue() != 0) {
				if (! monitor.isCanceled()) {
					throw new CoreException(new Status(IStatus.ERROR, SDMDebugCorePlugin.getUniqueIdentifier(), NLS.bind("sdm master process finished with exit code {0}.", sdmProcess.exitValue())));
				} else {
					DebugUtil.trace(DebugUtil.SDM_MASTER_TRACING, "sdm master: ignoring exit code since requested to terminate."); //$NON-NLS-1$
				}
			}
			setSdmState(SDMMasterState.FINISHED);
			return Status.OK_STATUS;
		} catch (Exception e) {
			/*
			 * Terminate the job, handling the error.
			 * Also terminates the ipjob since it does not make sense to the ipjob running without debugger.
			 */
			DebugUtil.error(DebugUtil.SDM_MASTER_TRACING, "sdm master: exception: {0}", e); //$NON-NLS-1$
			setSdmState(SDMMasterState.ERROR);
			synchronized (this) {
				DebugUtil.error(DebugUtil.SDM_MASTER_TRACING, "sdm master: destroy process", e); //$NON-NLS-1$
				sdmProcess.destroy();
			}
			try {
				DebugUtil.trace(DebugUtil.SDM_MASTER_TRACING, "sdm master: request job {0} to terminate.", ipJob.getID()); //$NON-NLS-1$
				rmControl.terminateJob(ipJob);
			} catch (CoreException e1) {
				PTPDebugCorePlugin.log(e1);
			}
			if (e instanceof InterruptedException) {
				return Status.CANCEL_STATUS;
			} else if (e instanceof CoreException) {
				return ((CoreException) e).getStatus();
			} else {
				return new Status(IStatus.ERROR, SDMDebugCorePlugin.getUniqueIdentifier(), "Failed to launch sdm master process.", e);
			}
		} finally {
			DebugUtil.trace(DebugUtil.SDM_MASTER_TRACING, "sdm master: thread finished."); //$NON-NLS-1$
			synchronized (this) {
				sdmProcess = null;
			}
		}
	}

	@Override
	protected void canceling() {
		synchronized (this) {
			if (sdmProcess != null) {
				sdmProcess.destroy();
			}
		}
	}
}
