/******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 *****************************************************************************/

package org.eclipse.ptp.cell.ui.console;

import java.io.IOException;

import org.eclipse.ptp.cell.utils.stream.IStreamListener;
import org.eclipse.ptp.cell.utils.stream.StreamObserver;
import org.eclipse.ptp.cell.utils.terminal.AbstractTerminalProvider;
import org.eclipse.ptp.cell.utils.terminal.AbstractTerminalReceiver;
import org.eclipse.ptp.cell.utils.terminal.ITerminalProvider;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleInputStream;
import org.eclipse.ui.console.IOConsoleOutputStream;


/**
 * Connects an Eclipse IOConsole to a AbstractTerminalProvider.
 * 
 * @author Daniel Felix Ferber
 * @since 1.0
 */
public class TerminalToConsoleBridge extends AbstractTerminalReceiver {
	/*
	 * TODO: Open issues
	 * - Show errors in highlight color in console.
	 * - Nice formating in console for meta messages
	 * - Filter xterm escape codes
	 * - Interpret xterm escape codes
	 */
	
	/**
	 * Listener for data received from console.
	 * Simple forward data to terminal provider.
	 */
	class ConsoleListener implements IStreamListener {
		TerminalToConsoleBridge bridge;
		
		public ConsoleListener(TerminalToConsoleBridge bridge) {
			this.bridge = bridge;
		}
		public void newBytes(byte[] bytes, int length) {
			bridge.writeData(bytes, length);
		}
		public void streamClosed() {
			// Nothing to do yet
		}
		public void streamError(Exception e) {
			// Nothing to do yet
		}
		
	}

	/**
	 * Console associated with the bridge.
	 */
	IOConsole console;
	IOConsoleInputStream fromConsoleStream;
	IOConsoleOutputStream toConsoleStream;
	
	/**
	 * Stream observer associated with the inputstream from the console.
	 */
	StreamObserver consoleInputObserver;
	ConsoleListener consoleListener;
	ITerminalProvider terminalProvider;
	
	public TerminalToConsoleBridge(ITerminalProvider provider, IOConsole console, String name) {
		super(provider);
		if (name == null) {
			name = this.getClass().getName();
		}
		
		// Fetch input stream
		this.console = console;
		this.fromConsoleStream = console.getInputStream();
		
		// Create console observer and listener
		this.toConsoleStream = console.newOutputStream();
		this.consoleListener = new ConsoleListener(this);
		this.consoleInputObserver = new StreamObserver(this.fromConsoleStream, consoleListener, name);
		this.consoleInputObserver.start();
		
		// Connect to provider
		this.terminalProvider = provider;
		this.terminalProvider.addListener(this);
	}
	
	public void disconnect() {
		terminalProvider.removeListener(this);
		consoleInputObserver.interrupt();
		terminalProvider = null;
		consoleInputObserver = null;
		consoleListener = null;
		fromConsoleStream = null;
		toConsoleStream = null;
	}
	
	public TerminalToConsoleBridge(AbstractTerminalProvider terminalProvider, IOConsole console) {
		this(terminalProvider, console, null);
	}

	public void receiveData(byte[] bytes, int length) {
		try {
			toConsoleStream.write(bytes, 0, length);
		} catch (IOException e) {
			// This Exception should not happen for terminals.
		}		
	}

	public void receiveError(byte[] bytes, int length) {
		try {
			// Forward error message to console
			toConsoleStream.write(bytes, 0, length);
		} catch (IOException e) {
			// This Exception should not happen for terminals.
		}
	}

	public void receiveMetaMessage(String message) {
		try {
			// Write a nice formated message inside the console.
			toConsoleStream.write("( INFO: "); //$NON-NLS-1$
			toConsoleStream.write(message);
			toConsoleStream.write(")\n"); //$NON-NLS-1$
		} catch (IOException e) {
			// This Exception should not happen for terminals.
		}
	}

}
