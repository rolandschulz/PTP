/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.core.runnable.command;

import java.io.OutputStream;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ptp.remote.core.IRemoteProcess;
import org.eclipse.ptp.rm.jaxb.core.ICommandJob;
import org.eclipse.ptp.rm.jaxb.core.ICommandJobStreamsProxy;
import org.eclipse.ptp.rm.jaxb.core.IJAXBNonNLSConstants;
import org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManagerControl;
import org.eclipse.ptp.rm.jaxb.core.data.ArgType;
import org.eclipse.ptp.rm.jaxb.core.data.CommandType;
import org.eclipse.ptp.rm.jaxb.core.data.impl.ArgImpl;
import org.eclipse.ptp.rm.jaxb.core.messages.Messages;
import org.eclipse.ptp.rm.jaxb.core.utils.CoreExceptionUtils;

/**
 * A command which is input to an open process. Used in the batch-interactive
 * resource managers (e.g., PBS -I).
 * 
 * @author arossi
 * 
 */
public class CommandJobInput extends Job implements ICommandJob, IJAXBNonNLSConstants {

	private final String uuid;
	private final CommandType command;
	private final IJAXBResourceManagerControl rm;

	/**
	 * @param jobUUID
	 * @param command
	 * @param rm
	 */
	public CommandJobInput(String jobUUID, CommandType command, IJAXBResourceManagerControl rm) {
		super(command.getName());
		this.uuid = jobUUID;
		this.command = command;
		this.rm = rm;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.core.ICommandJob#getProcess()
	 */
	public IRemoteProcess getProcess() {
		return rm.getProcessTable().get(command.getAsInputToProcess());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.core.ICommandJob#getProxy()
	 */
	public ICommandJobStreamsProxy getProxy() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.core.ICommandJob#isActive()
	 */
	public boolean isActive() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.core.ICommandJob#isBatch()
	 */
	public boolean isBatch() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.core.ICommandJob#waitForId()
	 */
	public boolean waitForId() {
		return false;
	}

	/*
	 * Looks up the parent process and feeds the input commands to it.
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.
	 * IProgressMonitor)
	 */
	@Override
	protected IStatus run(IProgressMonitor monitor) {
		IRemoteProcess process = getProcess();
		if (process != null && !process.isCompleted()) {
			OutputStream stream = process.getOutputStream();
			try {
				stream.write(prepareCommand().getBytes());
				stream.flush();
			} catch (Throwable t) {
				return CoreExceptionUtils.getErrorStatus(Messages.ProcessRunError, t);
			}
		}
		return Status.OK_STATUS;
	}

	/**
	 * Resolves the command arguments against the current environment.
	 * 
	 * @return the arguments as a single string
	 * @throws CoreException
	 */
	private String prepareCommand() throws CoreException {
		List<ArgType> args = command.getArg();
		if (args == null) {
			throw CoreExceptionUtils.newException(Messages.MissingArglistFromCommandError, null);
		}
		return ArgImpl.toString(uuid, args, rm.getEnvironment());
	}
}
