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
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.remote.core.IRemoteProcess;
import org.eclipse.ptp.remote.core.IRemoteProcessBuilder;
import org.eclipse.ptp.rm.core.RMCorePlugin;
import org.eclipse.ptp.rm.core.messages.Messages;
import org.eclipse.ptp.rm.core.utils.DebugUtil;
import org.eclipse.ptp.utils.core.ArgumentParser;

/**
 * Abstract implementation of a job that executes a command on the remote host and parses its output.
 * The job might be automatically rescheduled if created with the proper constructor.
 * Use this class a useful starting point to implement discover and monitor jobs.
 *
 * @author Daniel Felix Ferber
 */
abstract public class AbstractRemoteCommandJob extends Job {
	protected static final String EMPTY_STRING = ""; //$NON-NLS-1$
	protected String fCommand;
	protected String fInterruptedErrorMessage;
	protected String fProcessErrorMessage;
	protected String fParsingErrorMessage;
	protected int fReschedule = 0;
	protected IRemoteProcess fJobProcess;
	protected AbstractToolRuntimeSystem fRtSystem;
	protected IProgressMonitor fMonitor;

	/**
	 * A job for a remote command that is run only once.
	 * @param name Name of the job
	 * @param command Command executed remotely
	 * @param interruptedErrorMessage Error message if the job is interrupted or <code>null</code>.
	 * @param processErrorMessage Error message remote command fails or <code>null</code>.
	 * @param parsingErrorMessage Error message if the output of the remote command cannot be parsed or <code>null</code>.
	 * @param monitor progress monitor to use, or null to use system progress monitor
	 */
	public AbstractRemoteCommandJob(AbstractToolRuntimeSystem rtSystem, String name, String command, String interruptedErrorMessage, String processErrorMessage,
			String parsingErrorMessage, IProgressMonitor monitor) {
		super(name);
		fRtSystem = rtSystem;
		fCommand = command;
		fInterruptedErrorMessage = interruptedErrorMessage;
		fProcessErrorMessage = processErrorMessage;
		fParsingErrorMessage = parsingErrorMessage;
		fMonitor = monitor;
	}

	/**
	 * A job for a remote command that is run periodically.
	 * @param name Name of the job
	 * @param command Command executed remotely
	 * @param interruptedErrorMessage Error message if the job is interrupted or <code>null</code>.
	 * @param processErrorMessage Error message remote command fails or <code>null</code>.
	 * @param parsingErrorMessage Error message if the output of the remote command cannot be parsed or <code>null</code>.
	 * @param reschedule Time in milliseconds between executions of the command.
	 * @param monitor progress monitor to use, or null to use system progress monitor
	 */
	public AbstractRemoteCommandJob(AbstractToolRuntimeSystem rtSystem, String name, String command, String interruptedErrorMessage, String processErrorMessage,
			String parsingErrorMessage, int reschedule, IProgressMonitor monitor) {
		super(name);
		fRtSystem = rtSystem;
		fCommand = command;
		fInterruptedErrorMessage = interruptedErrorMessage;
		fProcessErrorMessage = processErrorMessage;
		fParsingErrorMessage = parsingErrorMessage;
		fReschedule = reschedule;
		fMonitor = monitor;
	}

	/**
	 * Parses output of the command.
	 * @param output Reader with output from the command.
	 * @throws CoreException Parsing failed
	 */
	protected abstract IStatus parse(BufferedReader output) throws CoreException;

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected IStatus run(IProgressMonitor monitor) {
		try {
			Assert.isNotNull(fRtSystem);
			Assert.isNotNull(fCommand);
			Assert.isTrue(!fCommand.trim().equals(EMPTY_STRING));

			if (fMonitor == null) {
				fMonitor = monitor;
			}
			
			/*
			 * Proposed enhancements
			 * TODO: Substitution of variables in the command string
			 * TODO: Substitution of attributes in the command string
			 * TODO: Append remote installation path to launch command.
			 * TODO: Extend class to provide XML SAX parser.
			 */
			ArgumentParser argumentParser = new ArgumentParser(fCommand);
			List<String> arguments = argumentParser.getTokenList();

			if (fMonitor.isCanceled()) {
				return new Status(IStatus.CANCEL, RMCorePlugin.PLUGIN_ID, fInterruptedErrorMessage, null);
			}

			try {
				IRemoteProcessBuilder cmdBuilder = fRtSystem.createProcessBuilder(arguments);
				synchronized (this) {
					DebugUtil.trace(DebugUtil.COMMAND_TRACING, "Run command: {0}", fCommand); //$NON-NLS-1$
					fJobProcess = cmdBuilder.start();
				}
			} catch (IOException e) {
				return new Status(IStatus.ERROR, RMCorePlugin.PLUGIN_ID, fProcessErrorMessage, e);
			}

			if (fMonitor.isCanceled()) {
				return new Status(IStatus.CANCEL, RMCorePlugin.PLUGIN_ID, fInterruptedErrorMessage, null);
			}

			BufferedReader stdout = new BufferedReader(new InputStreamReader(fJobProcess.getInputStream()));
			IStatus parseStatus = parse(stdout);
			if (parseStatus.getSeverity() == IStatus.ERROR) {
				DebugUtil.error(DebugUtil.COMMAND_TRACING_MORE, "Command parsing failed: {0}", parseStatus.getMessage()); //$NON-NLS-1$
				return parseStatus;
			}
			
			/*
			 * Wait for job to complete so that we can check for exit value of command.
			 */
			while (!fJobProcess.isCompleted() && !fMonitor.isCanceled()) {
				System.out.println("waiting for job"); //$NON-NLS-1$
				synchronized (this) {
					wait(500);
				}
			}
			
			if (fMonitor.isCanceled()) {
				return new Status(IStatus.CANCEL, RMCorePlugin.PLUGIN_ID, fInterruptedErrorMessage, null);
			}
			
			if (fJobProcess.exitValue() != 0) {
				return new Status(IStatus.ERROR, RMCorePlugin.getDefault().getBundle().getSymbolicName(), 
						NLS.bind("Command failed with exit status {0}", Integer.valueOf(fJobProcess.exitValue())), null); //$NON-NLS-1$
			}
			
			if (fMonitor.isCanceled()) {
				return new Status(IStatus.CANCEL, RMCorePlugin.PLUGIN_ID, fInterruptedErrorMessage, null);
			}

			try {
				DebugUtil.trace(DebugUtil.COMMAND_TRACING_MORE, "Command: waiting to finish."); //$NON-NLS-1$
				fJobProcess.waitFor();
			} catch (InterruptedException e) {
				return new Status(IStatus.INFO, RMCorePlugin.PLUGIN_ID, fInterruptedErrorMessage, e);
			}

			DebugUtil.trace(DebugUtil.COMMAND_TRACING_MORE, "Command: exit value {0}.", Integer.valueOf(fJobProcess.exitValue())); //$NON-NLS-1$

			if (fReschedule > 0) {
				DebugUtil.trace(DebugUtil.COMMAND_TRACING_MORE, "Command: reschedule in {0} miliseconds.", Integer.valueOf(fReschedule)); //$NON-NLS-1$
				schedule(fReschedule);
			}

			return parseStatus;
		} catch (Exception e) {
			DebugUtil.error(DebugUtil.COMMAND_TRACING_MORE, "Command failed: {0}", e); //$NON-NLS-1$
			return new Status(IStatus.ERROR, RMCorePlugin.PLUGIN_ID, Messages.AbstractRemoteCommandJob_Exception_InternalError, e);
		} finally {
			synchronized (this) {
				if (fJobProcess != null) {
					fJobProcess.destroy();
				}
				fJobProcess = null;
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.Job#canceling()
	 */
	@Override
	protected void canceling() {
		synchronized (this) {
			if (fJobProcess != null) {
				fJobProcess.destroy();
				fJobProcess = null;
			}
		}
	}
}

