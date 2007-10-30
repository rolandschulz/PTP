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
package org.eclipse.ptp.remotetools.internal.ssh;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.ptp.remotetools.exception.RemoteConnectionException;
import org.eclipse.ptp.remotetools.internal.common.Debug;
import org.eclipse.ptp.remotetools.utils.stream.ILineStreamListener;
import org.eclipse.ptp.remotetools.utils.stream.TextStreamObserver;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;

public class ControlChannel implements ILineStreamListener {
	/*
	 * Patterns recognized by the observer.
	 */
	final static String marker = "//"; //$NON-NLS-1$
	final static String markerPID = "PID:"; //$NON-NLS-1$
	final static String markerPIID = "PIID:"; //$NON-NLS-1$
	final static String markerSSH = "SSH_TTY:"; //$NON-NLS-1$
	final Pattern pidPattern = Pattern.compile("^" + marker + markerPID + "\\p{Digit}+" + marker + markerPIID //$NON-NLS-1$ //$NON-NLS-2$
			+ "\\p{Digit}+" + marker + "$"); //$NON-NLS-1$ //$NON-NLS-2$
	final Pattern pidFilterPattern = Pattern.compile("\\p{Digit}+"); //$NON-NLS-1$
	final Pattern terminalPathPattern = Pattern.compile("^" + marker + markerSSH + ".+" + marker + "$"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	// OutputStream that sends input to remote process input stream.
	OutputStream outputToControlTerminalInput;
	InputStream inputFromControlTerminalOutput;

	// Control channel
	ChannelExec shell;

	// Control terminal path.
	String controlTerminalPath;

	// Control terminal observer thread.
	TextStreamObserver controlTerminalObserver;

	/**
	 * Parent execution manager who will be notified about events from the control channel.
	 */
	private Connection connection;

	public ControlChannel(Connection connection) {
		this.connection = connection;
	}
	
	public void open() throws RemoteConnectionException {
		try{
			// Open exec channel and alloc terminal
			shell = connection.createExecChannel(false);
			shell.setPty(true);
			shell.setCommand("/bin/bash"); //$NON-NLS-1$
			inputFromControlTerminalOutput = shell.getInputStream();
			outputToControlTerminalInput = shell.getOutputStream();
			shell.connect();
		} catch (JSchException e) {
			close();
			throw new RemoteConnectionException(Messages.ControlChannel_Open_FailedCreateAuxiliaryShell, e);
		} catch (IOException e) {
			close();
			throw new RemoteConnectionException(Messages.ControlChannel_Open_FailedCreateIOStream, e);
		}
		
		// Create stream observer and start it
		controlTerminalObserver = new TextStreamObserver(inputFromControlTerminalOutput, this);
		controlTerminalObserver.start();
		
		try {
			// Clean shell prompt.
			outputToControlTerminalInput.write("export PS1=\n".getBytes()); //$NON-NLS-1$
			
			// Write terminal path on control channel, to be read by the observer
			// "//SSH_TTY: $SSH_TTY//\"\n"
			outputToControlTerminalInput.write(new String("echo \"" + marker + markerSSH + "$SSH_TTY" + marker + "\"\n").getBytes()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			//outputToControlTerminalInput.write(new String("echo helloworld > /tmp/helloworld\n").getBytes());
			outputToControlTerminalInput.flush();
		} catch (IOException e) {
			throw new RemoteConnectionException(Messages.ControlChannel_Open_FailedSendInitCommands, e);
		}
		
		// Wait until the channel answers the terminal path
		Debug.println2(Messages.ControlChannel_Debug_StartedWaitingControlTerminalPath);
		synchronized (this) {
			while (controlTerminalPath == null) {
				try {
					this.wait(200);
				} catch (InterruptedException e) {
					throw new RemoteConnectionException(Messages.ControlChannel_Open_WaitControlTerminalPathInterrupted, e);
				}
			}
		}
		Debug.println2(Messages.ControlChannel_Debug_ReceivedControlTerminalPath + controlTerminalPath);
	}

	public void newLine(String line) {
		Debug.println2(Messages.ControlChannel_Debug_ControlConnectionReceived + line);
		/*
		 * Test if the line contains a PID.
		 */
		Matcher pidmatch = pidPattern.matcher(line);
		if (pidmatch.find()) {
			Matcher pidmatch2 = pidFilterPattern.matcher(line);
			pidmatch2.find();
			String temppid = pidmatch2.group();
			pidmatch2.find();
			String temppiid = pidmatch2.group();
			connection.setPID(Integer.parseInt(temppiid), Integer.parseInt(temppid));
			return;
		}

		Matcher terminalPathMatcher = terminalPathPattern.matcher(line);
		if (terminalPathMatcher.find()) {
			int pre = new String(marker + markerSSH).length();
			int pos = marker.length();

			synchronized (this) {
				controlTerminalPath = line.substring(pre, line.length() - pos);
				this.notifyAll();
			}
		}

	}

	public void streamClosed() {
	}

	public void streamError(Exception e) {
		Debug.println2(Messages.ControlChannel_Debug_ControlConnectionReceived + e); //$NON-NLS-1$
	}

	public synchronized String getControlTerminalPath() {
		return controlTerminalPath;
	}

	public void killRemoteProcess(int pid) {
		try {
			outputToControlTerminalInput.write(new String("kill -9 " + pid + "\n").getBytes()); //$NON-NLS-1$ //$NON-NLS-2$
			outputToControlTerminalInput.flush();
		} catch (IOException e) {
		}
	}

	public synchronized String getKillablePrefix(int internaID) {
		return "echo " + marker + markerPID + "$$" + marker + markerPIID + Integer.toString(internaID) + marker + " > " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				+ controlTerminalPath;
	}

	public void close() {
		if (controlTerminalObserver != null) { 
			controlTerminalObserver.kill();
		}
		controlTerminalObserver = null;
		outputToControlTerminalInput = null;
		inputFromControlTerminalOutput = null;
		if (shell != null) {
			shell.disconnect();
		}
		shell = null;
		connection = null;
	}
}
