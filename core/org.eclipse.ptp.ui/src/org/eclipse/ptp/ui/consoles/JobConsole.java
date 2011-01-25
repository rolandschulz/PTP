/**
 * Copyright (c) 2007 ORNL and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the term of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 
 * @author - Feiyi Wang
 * initial API and implementation
 * 
 */

package org.eclipse.ptp.ui.consoles;

import java.util.BitSet;

import org.eclipse.ptp.core.attributes.StringAttribute;
import org.eclipse.ptp.core.attributes.StringAttributeDefinition;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.attributes.ProcessAttributes;
import org.eclipse.ptp.core.elements.events.IChangedProcessEvent;
import org.eclipse.ptp.core.elements.events.INewProcessEvent;
import org.eclipse.ptp.core.elements.events.IRemoveProcessEvent;
import org.eclipse.ptp.core.elements.listeners.IJobChildListener;
import org.eclipse.ptp.utils.core.BitSetIterable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

public class JobConsole implements IJobChildListener {
	/**
	 * search for console name first, if non-existed, create a new one
	 * 
	 * @param name
	 * @return
	 */
	public static MessageConsole findAndCreateConsole(String name) {
		MessageConsole myConsole = findConsole(name);
		if (myConsole == null) {
			// no console found, so create a new one
			ConsolePlugin plugin = ConsolePlugin.getDefault();
			myConsole = new MessageConsole(name, null);
			IConsoleManager conMan = plugin.getConsoleManager();
			conMan.addConsoles(new IConsole[] { myConsole });
		}
		return myConsole;
	}

	/**
	 * search and return a message console with given name
	 * 
	 * @param name
	 * @return null if not found
	 */
	public static MessageConsole findConsole(String name) {
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMan = plugin.getConsoleManager();
		IConsole[] existing = conMan.getConsoles();
		for (int i = 0; i < existing.length; i++) {
			if (name.equals(existing[i].getName()))
				return (MessageConsole) existing[i];
		}
		return null;
	}

	/**
	 * utility method that allow direct output to a particular console
	 * 
	 * @param name
	 *            - console name
	 * @param msg
	 *            - method for the output
	 */
	public static void jout(String name, String msg) {
		MessageConsole myConsole = findAndCreateConsole(name);
		MessageConsoleStream out = myConsole.newMessageStream();
		out.println(msg);
	}

	private Color red;

	private MessageConsole myConsole; // MessageConsole associated with this job
	private MessageConsoleStream outputStream; // Output stream for the console
	private MessageConsoleStream errorStream; // Error stream for the console
	// TODO get this flag from preferences
	private final boolean prefix = false; // Flag indicating if output should be
											// prefixed with process index

	public JobConsole(IPJob job) {
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMan = plugin.getConsoleManager();
		boolean haveConsole = false;
		ConsolePlugin.getStandardDisplay().syncExec(new Runnable() {
			public void run() {
				red = ConsolePlugin.getStandardDisplay().getSystemColor(SWT.COLOR_RED);
			}
		});
		// check if this id is already associated with a console
		// if it is, no new console will be created
		IConsole[] existing = conMan.getConsoles();
		String id = getUniqueName(job);
		for (int i = 0; i < existing.length; i++) {
			if (id.equals(existing[i].getName())) {
				myConsole = (MessageConsole) existing[i];
				haveConsole = true;
				break;
			}
		}
		if (!haveConsole) {
			myConsole = new MessageConsole(id, null);
			outputStream = myConsole.newMessageStream();
			errorStream = myConsole.newMessageStream();
			errorStream.setColor(red);
			conMan.addConsoles(new IConsole[] { myConsole });
		}
	}

	/**
	 * Send output from a process to the console. If the prefix flag is true,
	 * each line of the output will be prefixed by the process index.
	 * 
	 * @param index
	 *            prefix added to each line of output
	 * @param msg
	 *            output from process
	 */
	synchronized public void cout(String index, String msg, MessageConsoleStream stream) {
		String output = ""; //$NON-NLS-1$

		if (prefix) {
			String[] lines = msg.split("\n"); //$NON-NLS-1$
			for (int i = 0; i < lines.length; i++) {
				output += "[" + index + "] " + lines[i] + "\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
		} else {
			output = msg;
		}

		stream.print(output);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.elements.listeners.IJobChildListener#handleEvent
	 * (org.eclipse.ptp.core.elements.events.INewProcessEvent)
	 */
	public void handleEvent(INewProcessEvent e) {
		// no-op
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.elements.listeners.IJobChildListener#handleEvent
	 * (org.eclipse.ptp.core.elements.events.IChangedProcessEvent)
	 */
	public void handleEvent(IChangedProcessEvent e) {
		final IPJob job = e.getJob();

		final StringAttributeDefinition stdoutAttributeDefinition = ProcessAttributes.getStdoutAttributeDefinition();
		final boolean hasStdOut = e.getAttributes().getAttribute(stdoutAttributeDefinition) != null;

		final StringAttributeDefinition stderrAttributeDefinition = ProcessAttributes.getStderrAttributeDefinition();
		final boolean hasStdErr = e.getAttributes().getAttribute(stderrAttributeDefinition) != null;

		if (!hasStdErr && !hasStdOut) {
			return;
		}

		final BitSet indices = e.getProcesses();
		for (Integer index : new BitSetIterable(indices)) {
			if (hasStdOut) {
				StringAttribute stdout = job.getProcessAttribute(stdoutAttributeDefinition, index);
				if (stdout != null) {
					cout(index.toString(), stdout.getValueAsString(), outputStream);
				}
			}
			if (hasStdErr) {
				StringAttribute stderr = job.getProcessAttribute(stderrAttributeDefinition, index);
				if (stderr != null) {
					cout(index.toString(), stderr.getValueAsString(), errorStream);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.elements.listeners.IJobChildListener#handleEvent
	 * (org.eclipse.ptp.core.elements.events.IRemoveProcessEvent)
	 */
	public void handleEvent(IRemoveProcessEvent e) {
		// no-op
	}

	/**
	 * @param name
	 * @return
	 */
	public boolean removeConsole() {
		if (myConsole != null) {
			ConsolePlugin plugin = ConsolePlugin.getDefault();
			IConsoleManager conMan = plugin.getConsoleManager();
			conMan.removeConsoles(new IConsole[] { myConsole });
			return true;
		}
		return false;
	}

	/**
	 * Generate a unique name for the job
	 * 
	 * @param job
	 * @return unique name for the job
	 */
	private String getUniqueName(IPJob job) {
		String rmName = job.getResourceManager().getName();
		String jobName = job.getName();
		return rmName + ":" + jobName; //$NON-NLS-1$
	}
}
