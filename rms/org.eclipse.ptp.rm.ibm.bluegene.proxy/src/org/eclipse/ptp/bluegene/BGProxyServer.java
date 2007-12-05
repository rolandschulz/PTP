/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.bluegene;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.eclipse.ptp.bluegene.db.BGCommands;
import org.eclipse.ptp.bluegene.db.IBGEventListener;
import org.eclipse.ptp.bluegene.db.JobInfo;
import org.eclipse.ptp.proxy.event.IProxyEvent;
import org.eclipse.ptp.proxy.event.ProxyEventFactory;
import org.eclipse.ptp.proxy.runtime.command.IProxyRuntimeInitCommand;
import org.eclipse.ptp.proxy.runtime.command.IProxyRuntimeModelDefCommand;
import org.eclipse.ptp.proxy.runtime.command.IProxyRuntimeStartEventsCommand;
import org.eclipse.ptp.proxy.runtime.command.IProxyRuntimeStopEventsCommand;
import org.eclipse.ptp.proxy.runtime.command.IProxyRuntimeSubmitJobCommand;
import org.eclipse.ptp.proxy.runtime.command.IProxyRuntimeTerminateJobCommand;
import org.eclipse.ptp.proxy.runtime.server.AbstractProxyRuntimeServer;
import org.eclipse.ptp.proxy.runtime.server.IProxyRuntimeCommandListener;

public class BGProxyServer extends AbstractProxyRuntimeServer implements
		IProxyRuntimeCommandListener, IBGEventListener {

	private final BGCommands	commands = new BGCommands();
	
	private String protocolVersion;
	private String baseElementId;
	private int startEventsId = -1;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String host = "localhost";
		int port = 0;
		
		for (String arg : args) {
			if (arg.startsWith("--host=")) {
				host = arg.substring(7);
			} else if (arg.startsWith("--port=")) {
				port = Integer.parseInt(arg.substring(7));
			}
		}
		
		BGProxyServer server = new BGProxyServer(host, port);
		
		try {
			server.connect();
		} catch (IOException e) {
			System.err.println("Could not connect to client \"" + host + "\"");
			return;
		}
		
		server.start();
	}

	public BGProxyServer(String host, int port) {
		super(host, port);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.proxy.runtime.server.IProxyRuntimeCommandListener#handleCommand(org.eclipse.ptp.proxy.runtime.command.IProxyRuntimeInitCommand)
	 */
	public void handleCommand(IProxyRuntimeInitCommand c) {
		IProxyEvent event;
		
		String[] args = c.getArguments();
		if (args.length < 2) {
			sendEvent(ProxyEventFactory.newErrorEvent(c.getTransactionID(), 0, "Not enough arguments"));
			return;			
		}
		
		protocolVersion = args[0];
		baseElementId = args[1];
		
		String[] newArgs = Arrays.asList(args).subList(2, args.length).toArray(args);
		
		if (commands.init(newArgs)) {
			event = ProxyEventFactory.newOKEvent(c.getTransactionID());
		} else {
			event = ProxyEventFactory.newErrorEvent(c.getTransactionID(), commands.getErrorCode(), commands.getErrorMessage());
		}
		
		sendEvent(event);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.proxy.runtime.server.IProxyRuntimeCommandListener#handleCommand(org.eclipse.ptp.proxy.runtime.command.IProxyRuntimeModelDefCommand)
	 */
	public void handleCommand(IProxyRuntimeModelDefCommand c) {
		sendEvent(ProxyEventFactory.newOKEvent(c.getTransactionID()));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.proxy.runtime.server.IProxyRuntimeCommandListener#handleCommand(org.eclipse.ptp.proxy.runtime.command.IProxyRuntimeStartEventsCommand)
	 */
	public void handleCommand(IProxyRuntimeStartEventsCommand c) {
		if (startEventsId < 0) {
			startEventsId = c.getTransactionID();
			commands.addListener(this);
			commands.startEvents();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.proxy.runtime.server.IProxyRuntimeCommandListener#handleCommand(org.eclipse.ptp.proxy.runtime.command.IProxyRuntimeStopEventsCommand)
	 */
	public void handleCommand(IProxyRuntimeStopEventsCommand c) {
		if (startEventsId >= 0) {
			commands.stopEvents();
			commands.removeListener(this);
			sendEvent(ProxyEventFactory.newOKEvent(c.getTransactionID()));
			sendEvent(ProxyEventFactory.newOKEvent(startEventsId));
			startEventsId = -1;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.proxy.runtime.server.IProxyRuntimeCommandListener#handleCommand(org.eclipse.ptp.proxy.runtime.command.IProxyRuntimeSubmitJobCommand)
	 */
	public void handleCommand(IProxyRuntimeSubmitJobCommand c) {
		commands.submitJob(c.getArguments());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.proxy.runtime.server.IProxyRuntimeCommandListener#handleCommand(org.eclipse.ptp.proxy.runtime.command.IProxyRuntimeTerminateJobCommand)
	 */
	public void handleCommand(IProxyRuntimeTerminateJobCommand c) {
		commands.terminateJob(c.getArguments());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.bluegene.db.IBGEventListener#handleJobChangedEvent(java.util.List)
	 */
	public void handleJobChangedEvent(List<JobInfo> jobs) {
		
	}
}
