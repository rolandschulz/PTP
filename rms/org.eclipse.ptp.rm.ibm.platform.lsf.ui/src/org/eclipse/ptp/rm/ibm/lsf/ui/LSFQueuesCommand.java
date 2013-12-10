// Copyright (c) 2013 IBM Corporation and others. All rights reserved. 
// This program and the accompanying materials are made available under the 
// terms of the Eclipse Public License v1.0s which accompanies this distribution, 
// and is available at http://www.eclipse.org/legal/epl-v10.html

package org.eclipse.ptp.rm.ibm.lsf.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ptp.rm.ibm.lsf.ui.widgets.Messages;
import org.eclipse.remote.core.IRemoteConnection;

/**
 * This class implements invocation of LSF commands as a job running on a non-UI
 * thread with the ability for the user to cancel the job.
 */
public class LSFQueuesCommand extends LSFCommand {

	private final boolean isInteractiveSession;

	/**
	 * Constructor for creating a job to run a LSF command
	 * 
	 * @param name
	 *            - Name of the job
	 * @param connection
	 *            The remote connection where the job will be run
	 * @param cmd
	 *            Array containing command name and command parameters
	 */
	public LSFQueuesCommand(String name, IRemoteConnection connection, String cmd[], boolean isInteractive) {
		super(name, connection, cmd);
		isInteractiveSession = isInteractive;
	}

	@Override
	protected IStatus getCommandOutput(IProgressMonitor monitor) {
		BufferedReader reader;
		String columnData[];
		String data;
		String queueData;
		String queueName;
		boolean headerProcessed;
		boolean headerNext;
		boolean queueDataNext;
		boolean batchQueueAllowed;
		boolean interactiveQueueAllowed;
		try {
			/*
			 * Read stdout and tokenize each line of data to build an array of
			 * queue data. Output for each queue consists of several lines of
			 * text where this method extracts the queue name, queue statistics
			 * and scheduling restrictions from the text to build an entry for
			 * each queue. Queue data is also filtered to include queues that
			 * can be used by this target system configuration, for example
			 * excluding batch only queues when the target system configuration
			 * is an interactive one
			 */
			reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			commandResponse.clear();
			headerProcessed = false;
			headerNext = false;
			queueDataNext = false;
			batchQueueAllowed = true;
			interactiveQueueAllowed = true;
			queueName = "";
			queueData = null;
			data = reader.readLine();
			while (data != null) {
				if (data.startsWith("QUEUE:")) {
					// Queue name is in a line like "QUEUE: normal"
					if ((queueData != null)
							&& ((isInteractiveSession && interactiveQueueAllowed) || ((!isInteractiveSession) && batchQueueAllowed))) {
						queueData = queueData.replaceAll(" +/ +", "/"); //$NON-NLS-1$ //$NON-NLS-2$
						columnData = queueData.split(" +"); //$NON-NLS-1$
						commandResponse.add(columnData);
					}
					queueName = data.substring(7).trim();
					headerNext = false;
					queueDataNext = false;
					batchQueueAllowed = true;
					interactiveQueueAllowed = true;
				} else if (data.equals("PARAMETERS/STATISTICS")) {
					headerNext = true;
				} else if (headerNext) {
					if (!headerProcessed) {
						data = "QUEUE_NAME " + data;
						columnLabels = data.split(" +");
					}
					headerNext = false;
					headerProcessed = true;
					queueDataNext = true;
				} else if (queueDataNext) {
					queueData = queueName + " " + data;
					queueDataNext = false;
				} else if (data.startsWith("SCHEDULING POLICIES:")) {
					if (data.contains("NO_INTERACTIVE")) {
						interactiveQueueAllowed = false;
					} else if (data.contains("ONLY_INTERACTIVE")) {
						batchQueueAllowed = false;
					}
				}
				data = reader.readLine();
				if (monitor.isCanceled()) {
					reader.close();
					return new Status(IStatus.CANCEL, Activator.PLUGIN_ID, CANCELED, Messages.CommandCancelMessage, null);
				}
			}
			if ((queueData != null)
					&& ((isInteractiveSession && interactiveQueueAllowed) || ((!isInteractiveSession) && batchQueueAllowed))) {
				queueData = queueData.replaceAll(" +/ +", "/"); //$NON-NLS-1$ //$NON-NLS-2$
				columnData = queueData.split(" +"); //$NON-NLS-1$
				commandResponse.add(columnData);
			}
			reader.close();
		} catch (IOException e) {
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, COMMAND_ERROR, "Error reading command output", e);
		}
		return new Status(IStatus.OK, Activator.PLUGIN_ID, OK, Messages.OkMessage, null);
	}
}
