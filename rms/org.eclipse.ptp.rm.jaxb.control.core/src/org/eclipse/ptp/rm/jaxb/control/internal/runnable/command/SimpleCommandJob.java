/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.control.internal.runnable.command;

import java.io.IOException;
import java.util.Map;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ptp.core.util.CoreExceptionUtils;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteProcess;
import org.eclipse.ptp.remote.core.IRemoteProcessBuilder;
import org.eclipse.ptp.remote.core.RemoteServicesDelegate;
import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;
import org.eclipse.ptp.rm.jaxb.control.JAXBControlConstants;
import org.eclipse.ptp.rm.jaxb.control.JAXBResourceManagerControl;
import org.eclipse.ptp.rm.jaxb.control.internal.messages.Messages;
import org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManager;
import org.eclipse.ptp.rm.jaxb.core.IVariableMap;
import org.eclipse.ptp.rm.jaxb.core.data.SimpleCommandType;

/**
 * Implementation of runnable Job for the simple execution of external processes. Uses the IRemoteProcessBuilder with the
 * IRemoteConnection for the resource manager's target.
 * 
 * @author gwatson
 * 
 */
public class SimpleCommandJob extends Job {

	private final SimpleCommandType fCommand;
	private final JAXBResourceManagerControl fControl;
	private final IVariableMap fRmVarMap;
	private final int fFlags;

	private IRemoteProcess process;
	private IStatus status;
	private boolean active;

	/**
	 * @param jobUUID
	 *            either internal or resource specific identifier
	 * @param command
	 *            JAXB data element
	 * @param mode
	 *            whether submission is batch, interactive or status
	 * @param rm
	 *            the calling resource manager
	 */
	public SimpleCommandJob(SimpleCommandType command, IJAXBResourceManager rm) {
		super(command.getName());
		fCommand = command;
		fControl = (JAXBResourceManagerControl) rm.getControl();
		fRmVarMap = fControl.getEnvironment();
		fFlags = getFlags(command.getFlags());
	}

	/**
	 * @return the process wrapper
	 */
	public IRemoteProcess getProcess() {
		return process;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.core.ICommandJob#getRunStatus()
	 */
	public IStatus getRunStatus() {
		return status;
	}

	/**
	 * @return if job is active
	 */
	public boolean isActive() {
		boolean b = false;
		synchronized (this) {
			b = active;
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
		if (active) {
			active = false;
			if (process != null && !process.isCompleted()) {
				process.destroy();
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
			status = execute(progress.newChild(50));
		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}
		return status;
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
				status = null;
				active = false;
			}
			IRemoteProcessBuilder builder = prepareCommand(progress.newChild(10));
			prepareEnv(builder);
			progress.worked(10);

			process = null;
			try {
				process = builder.start(fFlags);
			} catch (IOException t) {
				throw CoreExceptionUtils.newException(Messages.CouldNotLaunch + builder.command(), t);
			}
			progress.worked(30);

			synchronized (this) {
				active = true;
			}
			progress.worked(20);

			int exit = 0;

			try {
				exit = process.waitFor();
			} catch (InterruptedException ignored) {
			}

			progress.worked(20);

			if (!fCommand.isIgnoreExitStatus() && exit > 0) {
				throw CoreExceptionUtils.newException(builder.command().get(0) + JAXBControlConstants.SP
						+ Messages.ProcessExitValueError + (JAXBControlConstants.ZEROSTR + exit), null);
			}

		} catch (CoreException ce) {
			return ce.getStatus();
		} catch (Throwable t) {
			return CoreExceptionUtils.getErrorStatus(Messages.ProcessRunError, t);
		}

		synchronized (this) {
			active = false;
		}
		return Status.OK_STATUS;
	}

	/**
	 * Converts or'd string into bit-wise or of available flags for remote process builder.
	 * 
	 * @param flags
	 * @return bit-wise or
	 */
	private int getFlags(String flags) {
		if (flags == null) {
			return IRemoteProcessBuilder.NONE;
		}

		String[] split = flags.split(JAXBControlConstants.REGPIP);
		int f = IRemoteProcessBuilder.NONE;
		for (String s : split) {
			s = s.trim();
			if (JAXBControlConstants.TAG_ALLOCATE_PTY.equals(s)) {
				f |= IRemoteProcessBuilder.ALLOCATE_PTY;
			} else if (JAXBControlConstants.TAG_FORWARD_X11.equals(s)) {
				f |= IRemoteProcessBuilder.FORWARD_X11;
			}
		}
		return f;
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
		String commandToExec = fRmVarMap.getString(fCommand.getExec());
		RemoteServicesDelegate delegate = fControl.getRemoteServicesDelegate(monitor);
		if (delegate.getRemoteConnection() == null) {
			throw CoreExceptionUtils.newException(Messages.MissingArglistFromCommandError, new Throwable(
					Messages.UninitializedRemoteServices));
		}
		IRemoteConnection conn = delegate.getRemoteConnection();
		SubMonitor progress = SubMonitor.convert(monitor, 10);
		try {
			JAXBResourceManagerControl.checkConnection(conn, progress);
		} catch (RemoteConnectionException rce) {
			throw CoreExceptionUtils.newException(rce.getLocalizedMessage(), rce);
		}
		IRemoteProcessBuilder builder = delegate.getRemoteServices().getProcessBuilder(conn, commandToExec);
		String directory = fCommand.getDirectory();
		if (directory != null && !JAXBControlConstants.ZEROSTR.equals(directory)) {
			directory = fRmVarMap.getString(directory);
			IFileStore dir = delegate.getRemoteFileManager().getResource(directory);
			builder.directory(dir);
		}
		return builder;
	}

	/**
	 * Either appends to or replaces the process builder's environment with the Launch Configuration environment variables.
	 * 
	 * @param builder
	 * @throws CoreException
	 */
	private void prepareEnv(IRemoteProcessBuilder builder) throws CoreException {
		if (!fControl.getAppendEnv()) {
			builder.environment().clear();
		}

		Map<String, String> live = fControl.getLaunchEnv();
		for (String var : live.keySet()) {
			builder.environment().put(var, live.get(var));
		}

		builder.redirectErrorStream(fCommand.isRedirectStderr());
	}
}
