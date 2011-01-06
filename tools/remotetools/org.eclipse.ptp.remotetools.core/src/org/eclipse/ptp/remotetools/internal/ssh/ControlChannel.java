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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.remotetools.core.messages.Messages;
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
	private final static String markerPID = "PID="; //$NON-NLS-1$
	private final static String markerPIID = "PIID="; //$NON-NLS-1$
	private final static String markerSSH = "SSH_TTY="; //$NON-NLS-1$
	private final Pattern pidPattern = Pattern.compile(markerPID + "(\\p{Digit}+) " + markerPIID + "(\\p{Digit}+)"); //$NON-NLS-1$ //$NON-NLS-2$
	private final Pattern terminalPathPattern = Pattern.compile(markerSSH + "(/.+)"); //$NON-NLS-1$

	// OutputStream that sends input to remote process input stream.
	private OutputStream outputToControlTerminalInput;
	private InputStream inputFromControlTerminalOutput;

	// Control channel
	private ChannelExec shell;

	// Control terminal path.
	private String controlTerminalPath;

	// Control terminal observer thread.
	private TextStreamObserver controlTerminalObserver;

	/**
	 * Parent execution manager who will be notified about events from the
	 * control channel.
	 */
	private final Connection connection;

	public ControlChannel(Connection connection) {
		this.connection = connection;
	}

	public void open(IProgressMonitor monitor) throws RemoteConnectionException {
		try {
			// Open exec channel and alloc terminal
			shell = connection.createExecChannel(false);
			shell.setPty(true);
			shell.setCommand("/bin/sh"); //$NON-NLS-1$
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
			outputToControlTerminalInput.write("PS1=\n".getBytes()); //$NON-NLS-1$
			outputToControlTerminalInput.write("export PS1\n".getBytes()); //$NON-NLS-1$

			// Write terminal path on control channel, to be read by the
			// observer
			String command = "/bin/sh -c 'echo \"" + markerSSH + "$SSH_TTY\"'\n"; //$NON-NLS-1$ //$NON-NLS-2$
			outputToControlTerminalInput.write(command.getBytes());
			outputToControlTerminalInput.flush();
		} catch (IOException e) {
			throw new RemoteConnectionException(Messages.ControlChannel_Open_FailedSendInitCommands, e);
		}

		// Wait until the channel answers the terminal path
		Debug.println2(Messages.ControlChannel_Debug_StartedWaitingControlTerminalPath);
		synchronized (this) {
			while (!monitor.isCanceled() && controlTerminalPath == null) {
				try {
					this.wait(200);
				} catch (InterruptedException e) {
					throw new RemoteConnectionException(Messages.ControlChannel_Open_WaitControlTerminalPathInterrupted, e);
				}
			}
		}
		if (monitor.isCanceled()) {
			close();
			throw new RemoteConnectionException(Messages.ControlChannel_Open_WaitControlTerminalPathInterrupted);
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
			String pid = pidmatch.group(1);
			String piid = pidmatch.group(2);
			connection.setPID(Integer.parseInt(piid), Integer.parseInt(pid));
			return;
		}

		Matcher terminalPathMatcher = terminalPathPattern.matcher(line);
		if (terminalPathMatcher.find()) {
			synchronized (this) {
				controlTerminalPath = terminalPathMatcher.group(1);
				Debug.println2(Messages.ControlChannel_0 + controlTerminalPath);
				this.notifyAll();
			}
		}

	}

	public void streamClosed() {
	}

	public void streamError(Exception e) {
		Debug.println2(Messages.ControlChannel_Debug_ControlConnectionReceived + e);
	}

	public synchronized String getControlTerminalPath() {
		return controlTerminalPath;
	}

	public void killRemoteProcess(int pid) {
		if (pid > 0) {
			try {
				outputToControlTerminalInput.write(new String("kill -9 " + pid + "\n").getBytes()); //$NON-NLS-1$ //$NON-NLS-2$
				outputToControlTerminalInput.flush();
			} catch (IOException e) {
			}
		}
	}

	public synchronized String getKillablePrefix(int internaID) {
		return "echo \"" + markerPID + "$$ " //$NON-NLS-1$ //$NON-NLS-2$
				+ markerPIID + Integer.toString(internaID) + "\" > " + controlTerminalPath; //$NON-NLS-1$
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
	}

	public boolean isConnected() {
		return shell != null && shell.isConnected();
	}
}
