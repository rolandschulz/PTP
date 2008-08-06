/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.core.rtsystem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ptp.core.util.ArgumentParser;
import org.eclipse.ptp.remote.core.IRemoteProcess;
import org.eclipse.ptp.remote.core.IRemoteProcessBuilder;
import org.eclipse.ptp.rm.core.Activator;
import org.eclipse.ptp.rm.core.utils.DebugUtil;

/**
 * Abstract implementation of a job that executes a command on the remote host and parses its output.
 * The job might be automatically rescheduled if created with the proper constructor.
 *
 * @author dfferber
 *
 */
abstract public class AbstractRemoteCommandJob extends Job {
	static final String EMPTY_STRING = ""; //$NON-NLS-1$
	String command;
	String interruptedErrorMessage;
	String processErrorMessage;
	String parsingErrorMessage;
	int reschedule = 0;
	IRemoteProcess cmd;
	AbstractToolRuntimeSystem rtSystem;

	/**
	 * A job for a remote command that is run only once.
	 * @param name Name of the job
	 * @param command Command executed remotely
	 * @param interruptedErrorMessage Error message if the job is interrupted or <code>null</code>.
	 * @param processErrorMessage Error message remote command fails or <code>null</code>.
	 * @param parsingErrorMessage Error message if the output of the remote command cannot be parsed or <code>null</code>.
	 */
	public AbstractRemoteCommandJob(AbstractToolRuntimeSystem rtSystem, String name, String command, String interruptedErrorMessage, String processErrorMessage,
			String parsingErrorMessage) {
		super(name);
		this.rtSystem = rtSystem;
		this.command = command;
		this.interruptedErrorMessage = interruptedErrorMessage;
		this.processErrorMessage = processErrorMessage;
		this.parsingErrorMessage = parsingErrorMessage;
	}

	/**
	 * A job for a remote command that is run periodically.
	 * @param name Name of the job
	 * @param command Command executed remotely
	 * @param interruptedErrorMessage Error message if the job is interrupted or <code>null</code>.
	 * @param processErrorMessage Error message remote command fails or <code>null</code>.
	 * @param parsingErrorMessage Error message if the output of the remote command cannot be parsed or <code>null</code>.
	 * @param reschedule Time in milliseconds between executions of the command.
	 */
	public AbstractRemoteCommandJob(AbstractToolRuntimeSystem rtSystem, String name, String command, String interruptedErrorMessage, String processErrorMessage,
			String parsingErrorMessage, int reschedule) {
		super(name);
		this.rtSystem = rtSystem;
		this.command = command;
		this.interruptedErrorMessage = interruptedErrorMessage;
		this.processErrorMessage = processErrorMessage;
		this.parsingErrorMessage = parsingErrorMessage;
		this.reschedule = reschedule;
	}

	/**
	 * Parses output of the command.
	 * @param output Reader with output from the command.
	 * @throws CoreException Parsing failed
	 */
	protected abstract void parse(BufferedReader output) throws CoreException;

	/**
	 * Default implementation of the job.
	 */
	@Override
	protected IStatus run(IProgressMonitor monitor) {
		try {
			Assert.isNotNull(rtSystem);
			Assert.isNotNull(command);
			Assert.isTrue(! command.trim().equals(EMPTY_STRING));

			/*
			 * Proposed enhancements
			 * TODO: Substitution of variables in the command string
			 * TODO: Substitution of attributes in the command strng
			 * TODO: Append remote installation path to launch command.
			 * TODO: Extend class to provide XML SAX parser.
			 * TODO: Use better argument parser.
			 */
			ArgumentParser argumentParser = new ArgumentParser(command);
			List<String> arguments = argumentParser.getArguments();

			checkCancel(monitor);

			try {
				IRemoteProcessBuilder cmdBuilder = rtSystem.createProcessBuilder(arguments);
				synchronized (this) {
					DebugUtil.trace(DebugUtil.COMMAND_TRACING, "Run command: {0}", command); //$NON-NLS-1$
					cmd = cmdBuilder.start();
				}
			} catch (IOException e) {
				throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, processErrorMessage, e));
			}

			checkCancel(monitor);

			BufferedReader stdout = new BufferedReader(new InputStreamReader(cmd.getInputStream()));
			IStatus parseStatus = Status.OK_STATUS;
			try {
				parse(stdout);
			} catch (CoreException e) {
				DebugUtil.error(DebugUtil.COMMAND_TRACING_MORE, "Command parsing failed: {0}", e); //$NON-NLS-1$
				parseStatus = e.getStatus();
				if (parseStatus.getSeverity() == IStatus.ERROR) {
					throw e;
				}
			}

			checkCancel(monitor);

			try {
				DebugUtil.trace(DebugUtil.COMMAND_TRACING_MORE, "Command: waiting to finish."); //$NON-NLS-1$
				cmd.waitFor();
			} catch (InterruptedException e) {
				throw new CoreException(new Status(IStatus.INFO, Activator.PLUGIN_ID, interruptedErrorMessage, e));
			}

			DebugUtil.trace(DebugUtil.COMMAND_TRACING_MORE, "Command: exit value {0}.", cmd.exitValue()); //$NON-NLS-1$

			if (reschedule > 0) {
				DebugUtil.trace(DebugUtil.COMMAND_TRACING_MORE, "Command: reschedule in {0} miliseconds.", reschedule); //$NON-NLS-1$
				schedule(reschedule);
			}

			return parseStatus;
		} catch (CoreException e) {
			DebugUtil.error(DebugUtil.COMMAND_TRACING_MORE, "Command failed: {0}", e); //$NON-NLS-1$
			return new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), "Command failed.", e);
		} catch (Exception e) {
			DebugUtil.error(DebugUtil.COMMAND_TRACING_MORE, "Command failed: {0}", e); //$NON-NLS-1$
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Internal error", e);
		} finally {
			synchronized (this) {
				cmd.destroy();
				cmd = null;
			}
		}
	}

	private void checkCancel(IProgressMonitor monitor) throws CoreException {
		if (monitor.isCanceled()) {
			throw new CoreException(new Status(IStatus.INFO, Activator.PLUGIN_ID, interruptedErrorMessage, null));
		}
	}

	@Override
	protected void canceling() {
		synchronized (this) {
			if (cmd != null) {
				cmd.destroy();
				cmd = null;
			}
		}
	}
}

