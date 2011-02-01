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

package org.eclipse.ptp.launch.internal.ui.console;

import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.ptp.rmsystem.IJobStatus;
import org.eclipse.ptp.rmsystem.IResourceManagerControl;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

public class JobConsole {
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

	public JobConsole(IResourceManagerControl rm, String jobId) {
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
		String id = getUniqueName(rm, jobId);
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
		IJobStatus status = rm.getJobStatus(jobId);
		IStreamsProxy proxy = status.getStreamsProxy();
		outputStream.print(proxy.getOutputStreamMonitor().getContents());
		errorStream.print(proxy.getErrorStreamMonitor().getContents());
		proxy.getOutputStreamMonitor().addListener(new IStreamListener() {
			public void streamAppended(String text, IStreamMonitor monitor) {
				outputStream.print(text);
			}
		});
		proxy.getErrorStreamMonitor().addListener(new IStreamListener() {
			public void streamAppended(String text, IStreamMonitor monitor) {
				errorStream.print(text);
			}
		});
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
	private String getUniqueName(IResourceManagerControl rm, String jobId) {
		String rmName = rm.getName();
		return rmName + ":" + jobId; //$NON-NLS-1$
	}
}
