/******************************************************************************
 * Copyright (c) 2011 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 *****************************************************************************/
package org.eclipse.ptp.internal.rm.jaxb.control.core.runnable.command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ptp.core.util.ArgumentParser;
import org.eclipse.ptp.core.util.CoreExceptionUtils;
import org.eclipse.ptp.internal.rm.jaxb.control.core.JAXBControlConstants;
import org.eclipse.ptp.internal.rm.jaxb.control.core.LaunchController;
import org.eclipse.ptp.internal.rm.jaxb.control.core.RemoteServicesDelegate;
import org.eclipse.ptp.internal.rm.jaxb.control.core.messages.Messages;
import org.eclipse.ptp.internal.rm.jaxb.control.core.utils.DebuggingLogger;
import org.eclipse.ptp.rm.jaxb.control.core.ILaunchController;
import org.eclipse.ptp.rm.jaxb.core.IVariableMap;
import org.eclipse.ptp.rm.jaxb.core.data.SimpleCommandType;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteProcess;
import org.eclipse.remote.core.IRemoteProcessBuilder;

/**
 * Implementation of runnable Job for the simple execution of external processes. Uses the IRemoteProcessBuilder with the
 * IRemoteConnection for the resource manager's target.
 * 
 * @author gwatson
 * 
 */
public class SimpleCommandJob extends Job {

	private final SimpleCommandType fCommand;
	private final ILaunchController fControl;
	private final IVariableMap fVarMap;
	private final CommandJob fCommandJob;
	private final String fUuid;
	private final String fDirectory;

	private IRemoteProcess fProcess;
	private IStatus fStatus;
	private boolean fActive;

	/**
	 * @param uuid
	 *            either internal or resource specific identifier
	 * @param command
	 *            JAXB data element
	 * @param directory
	 *            directory of parent command, or null if none
	 * @param rm
	 *            the calling resource manager
	 */
	public SimpleCommandJob(String uuid, SimpleCommandType command, String directory, ILaunchController control, IVariableMap map,
			CommandJob commandJob) {
		super(command.getName() != null ? command.getName() : "Simple Command"); //$NON-NLS-1$
		fUuid = uuid;
		fCommand = command;
		fDirectory = command.getDirectory() != null ? command.getDirectory() : directory;
		fControl = control;
		fVarMap = map;
		fCommandJob = commandJob;
	}

	/**
	 * @return the process wrapper
	 */
	public IRemoteProcess getProcess() {
		return fProcess;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.core.ICommandJob#getRunStatus()
	 */
	public IStatus getRunStatus() {
		return fStatus;
	}

	/**
	 * @return if job is active
	 */
	public boolean isActive() {
		boolean b = false;
		synchronized (this) {
			b = fActive;
		}
		return b;
	}

	/*
	 * First unblock any wait; this will allow the run method to return. Destroy the process and close streams, interrupt the thread
	 * and cancel with manager. (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.core.ICommandJob#terminate()
	 */
	public synchronized void terminate() {
		if (fActive) {
			fActive = false;
			if (fProcess != null && !fProcess.isCompleted()) {
				fProcess.destroy();
			}
			cancel();
		}
	}

	/**
	 * If this process has no input, execute it normally. Otherwise, if the process is to be kept open, check for the pseudoTerminal
	 * job; if it is there and still alive, send the input to it; if not, start the process, and then send the input.
	 */
	@Override
	protected IStatus run(IProgressMonitor monitor) {
		SubMonitor progress = SubMonitor.convert(monitor, 100);
		try {
			fStatus = execute(progress.newChild(50));
		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}
		return fStatus;
	}

	/**
	 * Uses the IRemoteProcessBuilder to set up the command and environment. After start, the tokenizers (if any) are handled, and
	 * stream redirection managed. Returns immediately if <code>keepOpen</code> is true; else waits for the process, then joins on
	 * the consumers.x
	 */
	private IStatus execute(IProgressMonitor monitor) {
		SubMonitor progress = SubMonitor.convert(monitor, 100);
		try {
			synchronized (this) {
				fStatus = null;
				fActive = false;
			}
			IRemoteProcessBuilder builder = prepareCommand(progress.newChild(10));
			if (progress.isCanceled()) {
				return Status.CANCEL_STATUS;
			}
			fCommandJob.prepareEnv(builder);
			progress.worked(10);

			fProcess = null;
			try {
				fProcess = builder.start(fCommandJob.getFlags(fCommand.getFlags()));
			} catch (IOException t) {
				throw CoreExceptionUtils.newException(Messages.CouldNotLaunch + builder.command(), t);
			}
			progress.worked(30);

			synchronized (this) {
				fActive = true;
			}
			progress.worked(20);

			if (DebuggingLogger.getLogger().getCommandOutput()) {
				final BufferedReader stdout = new BufferedReader(new InputStreamReader(fProcess.getInputStream()));
				new Thread(new Runnable() {
					public void run() {
						try {
							String output;
							while ((output = stdout.readLine()) != null) {
								DebuggingLogger.getLogger().logCommandOutput(getName() + ": " + output); //$NON-NLS-1$
							}
							stdout.close();
						} catch (IOException e) {
							// Ignore
						}
					}
				}, getName() + " stdout").start(); //$NON-NLS-1$

				final BufferedReader stderr = new BufferedReader(new InputStreamReader(fProcess.getErrorStream()));
				new Thread(new Runnable() {
					public void run() {
						try {
							String output;
							while ((output = stderr.readLine()) != null) {
								DebuggingLogger.getLogger().logCommandOutput(getName() + ": " + output); //$NON-NLS-1$
							}
							stderr.close();
						} catch (IOException e) {
							// Ignore
						}
					}
				}, getName() + " stderr").start(); //$NON-NLS-1$
			}

			int exit = 0;

			while (!fProcess.isCompleted() && !progress.isCanceled()) {
				synchronized (this) {
					try {
						wait(500);
					} catch (InterruptedException e) {
						// Ignore
					}
				}
			}

			if (progress.isCanceled()) {
				if (!fProcess.isCompleted()) {
					fProcess.destroy();
				}
				return Status.CANCEL_STATUS;
			}

			progress.worked(20);

			if (!fCommand.isIgnoreExitStatus() && exit > 0) {
				throw CoreExceptionUtils.newException(builder.command().get(0) + JAXBControlConstants.SP
						+ Messages.ProcessExitValueError + Integer.toString(exit), null);
			}

		} catch (CoreException ce) {
			return ce.getStatus();
		} catch (Throwable t) {
			return CoreExceptionUtils.getErrorStatus(Messages.ProcessRunError, t);
		}

		synchronized (this) {
			fActive = false;
		}
		return Status.OK_STATUS;
	}

	/**
	 * Resolves the command arguments against the current environment, then gets the process builder from the remote connection.
	 * Also sets the directory if it is defined (otherwise it defaults to the connection dir).
	 * 
	 * @param monitor
	 * @return the process builder
	 * @throws CoreException
	 */
	private IRemoteProcessBuilder prepareCommand(IProgressMonitor monitor) throws CoreException {
		SubMonitor progress = SubMonitor.convert(monitor, 10);
		ArgumentParser args = new ArgumentParser(fVarMap.getString(fUuid, fCommand.getExec()));
		RemoteServicesDelegate delegate = RemoteServicesDelegate.getDelegate(fControl.getRemoteServicesId(),
				fControl.getConnectionName(), progress.newChild(5));
		if (delegate.getRemoteConnection() == null) {
			throw CoreExceptionUtils.newException(Messages.MissingArglistFromCommandError, new Throwable(
					Messages.UninitializedRemoteServices));
		}
		if (progress.isCanceled()) {
			return null;
		}
		IRemoteConnection conn = delegate.getRemoteConnection();
		LaunchController.checkConnection(conn, progress.newChild(5));
		if (progress.isCanceled()) {
			return null;
		}
		if (DebuggingLogger.getLogger().getCommand()) {
			System.out.println(getName() + ": " + args.getCommandLine(false)); //$NON-NLS-1$
		}
		IRemoteProcessBuilder builder = conn.getProcessBuilder(args.getTokenList());
		if (fDirectory != null && !JAXBControlConstants.ZEROSTR.equals(fDirectory)) {
			String directory = fVarMap.getString(fUuid, fDirectory);
			IFileStore dir = delegate.getRemoteFileManager().getResource(directory);
			builder.directory(dir);
		}
		return builder;
	}
}
