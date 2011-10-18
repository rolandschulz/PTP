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

import org.eclipse.ptp.remotetools.core.messages.Messages;
import org.eclipse.ptp.remotetools.exception.RemoteConnectionException;
import org.eclipse.ptp.remotetools.internal.common.AbstractRemoteExecution;
import org.eclipse.ptp.remotetools.internal.common.Debug;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;

/**
 * A remote execution that can be canceled by killing the running bash command.
 * 
 * @author Daniel Felix Ferber
 * 
 */
public abstract class KillableExecution extends AbstractRemoteExecution {

	private ChannelExec channel;
	/**
	 * Internal process identifier.
	 */
	private int PIID;
	/**
	 * Process id on the remote host.
	 */
	private int PID;

	public KillableExecution(ExecutionManager executionManager) throws RemoteConnectionException {
		super(executionManager);
	}

	public void startExecution() throws RemoteConnectionException {
		try {
			getExecutionManager().registerOperation(this);
			getExecutionManager().getConnection().registerObservedExecution(this);
			synchronized (getExecutionManager().getConnection()) {
				channel.connect();
			}
		} catch (JSchException e) {
			throw new RemoteConnectionException(e.getLocalizedMessage());
		}
	}

	@Override
	protected void notifyCancel() {
		/*
		 * Stop observing this channel.
		 */
		getExecutionManager().unregisterOperation(this);
		getExecutionManager().getConnection().unregisterObservedExecution(this);
		/*
		 * Force the end of the execution by closing the channel and killing (SIGHUP or something similar) on the remote host.
		 * Remove the channel from the connection pool.
		 */
		getExecutionManager().getConnection().killExecution(this);
		getExecutionManager().getConnection().releaseChannel(channel);
		super.notifyCancel();
	}

	@Override
	protected void notifyFinish() {
		/*
		 * Stop observing this channel. Remote the channel from the connection pool
		 */
		getExecutionManager().unregisterOperation(this);
		getExecutionManager().getConnection().unregisterObservedExecution(this);
		getExecutionManager().getConnection().releaseChannel(channel);
		super.notifyFinish();
	}

	@Override
	public void close() {
		/*
		 * Cancel execution, if still not finished. Then, make sure the channel is closed and released from the execution manager.
		 */
		if (isRunning()) {
			cancel();
		}
		super.close();
	}

	protected ChannelExec createChannel(boolean hasPTY) throws RemoteConnectionException {
		/*
		 * Get a channel from the connection pool. Channels with PTY must be managed by the pool.
		 */
		channel = getExecutionManager().getConnection().createExecChannel(hasPTY);
		channel.setPty(hasPTY);
		return channel;
	}

	/**
	 * Create a killable command line that will run on any system.
	 * 
	 * 1. The whole command is run using /bin/sh to ensure that it will work on any system. 2. The killable prefix echos the PID of
	 * the shell to the control terminal. This is read by the connection manager and can be used to send a kill signal in order to
	 * terminate the process.
	 * 
	 * NOTE: there is a maximum line length on most systems. If this is exceeded the command will fail.
	 * 
	 * @param commandLine
	 *            command line to run
	 */
	protected void setCommandLine(String commandLine) {
		PIID = getExecutionManager().getConnection().createNextPIID();

		String newCommandLine = "/bin/sh -c '" //$NON-NLS-1$
				+ getExecutionManager().getConnection().getKillablePrefix(this) + "; " //$NON-NLS-1$
				+ commandLine + "'"; //$NON-NLS-1$

		Debug.println2(Messages.KillableExecution_Debug_1 + newCommandLine);
		channel.setCommand(newCommandLine);
	}

	public int getReturnCode() {
		if (wasCanceled()) {
			return -1;
		} else if (!wasFinished()) {
			throw new IllegalStateException();
		}
		return channel.getExitStatus();
	}

	public int getPID() {
		return PID;
	}

	public void setPID(int pid) {
		PID = pid;
	}

	public int getInternalID() {
		return PIID;
	}

	public boolean isRunning() {
		return !channel.isClosed();
	}

	public int getFinishStatus() {
		int code = getReturnCode();

		if (code == 0) {
			return SUCCESS_OK;
		} else if (code <= 125) {
			return SUCCESS_ERROR;
		} else if (code == 126) {
			return ERROR_NOT_EXECUTABLE;
		} else if (code == 127) {
			return ERROR_NOT_FOUND;
		} else if (code == 128) {
			return UNKNOWN;
		} else if (code == 255) {
			return INVALID_EXIT_CODE;
		} else if (code == 128 + 1) {
			return SIGHUP;
		} else if (code == 128 + 2) {
			return SIGINT;
		} else if (code == 128 + 3) {
			return SIGQUIT;
		} else if (code == 128 + 4) {
			return SIGILL;
		} else if (code == 128 + 5) {
			return SIGTRAP;
		} else if (code == 128 + 6) {
			return SIGIOT;
		} else if (code == 128 + 7) {
			return SIGBUS;
		} else if (code == 128 + 8) {
			return SIGFPE;
		} else if (code == 128 + 9) {
			return SIGKILL;
		} else if (code == 128 + 10) {
			return SIGUSR1;
		} else if (code == 128 + 11) {
			return SIGSEGV;
		} else if (code == 128 + 12) {
			return SIGUSR2;
		} else if (code == 128 + 13) {
			return SIGPIPE;
		} else if (code == 128 + 14) {
			return SIGALRM;
		} else if (code == 128 + 15) {
			return SIGTERM;
		} else if (code == 128 + 16) {
			return SIGSTKFLT;
		} else if (code == 128 + 17) {
			return SIGCHLD;
		} else if (code == 128 + 18) {
			return SIGCONT;
		} else if (code == 128 + 19) {
			return SIGSTOP;
		} else if (code == 128 + 20) {
			return SIGTSTP;
		} else if (code == 128 + 21) {
			return SIGTTIN;
		} else if (code == 128 + 22) {
			return SIGTTOU;
		} else if (code == 128 + 23) {
			return SIGURG;
		} else if (code == 128 + 24) {
			return SIGXCPU;
		} else if (code == 128 + 25) {
			return SIGXFSZ;
		} else if (code == 128 + 26) {
			return SIGVTALRM;
		} else if (code == 128 + 27) {
			return SIGPROF;
		} else if (code == 128 + 28) {
			return SIGWINCH;
		} else if (code == 128 + 29) {
			return SIGIO;
		} else if (code == 128 + 30) {
			return SIGPWR;
		} else {
			return UNKNOWN;
		}
	}

	public String getFinishStatusText(int status) {
		switch (status) {
		case SUCCESS_OK:
			return Messages.KillableExecution_FinishStatus_Ok;
		case SUCCESS_ERROR:
			return Messages.KillableExecution_FinishStatus_Error;
		case ERROR_NOT_EXECUTABLE:
			return Messages.KillableExecution_FinishStatus_NotExecutable;
		case ERROR_NOT_FOUND:
			return Messages.KillableExecution_FinishStatus_CommandNotFound;
		case INVALID_EXIT_CODE:
			return Messages.KillableExecution_FinishStatus_InvalidExitCode;
		case SIGHUP:
			return Messages.KillableExecution_FinishStatus_Hangup;
		case SIGINT:
			return Messages.KillableExecution_FinishStatus_TerminalInterrupt;
		case SIGQUIT:
			return Messages.KillableExecution_FinishStatus_TerminalQuit;
		case SIGILL:
			return Messages.KillableExecution_FinishStatus_IllegalInstruction;
		case SIGTRAP:
			return Messages.KillableExecution_FinishStatus_TraceTrap;
		case SIGIOT:
			return Messages.KillableExecution_FinishStatus_IOTTrap;
		case SIGBUS:
			return Messages.KillableExecution_FinishStatus_BUSError;
		case SIGFPE:
			return Messages.KillableExecution_FinishStatus_FloatingPointException;
		case SIGKILL:
			return Messages.KillableExecution_FinishStatus_Kill;
		case SIGUSR1:
			return Messages.KillableExecution_FinishStatus_UserDefinedSignal1;
		case SIGSEGV:
			return Messages.KillableExecution_FinishStatus_InvalidMemorySegmentAccess;
		case SIGUSR2:
			return Messages.KillableExecution_FinishStatus_UserDefinedSignal2;
		case SIGPIPE:
			return Messages.KillableExecution_FinishStatus_BrokenPipe;
		case SIGALRM:
			return Messages.KillableExecution_FinishStatus_AlarmClock;
		case SIGTERM:
			return Messages.KillableExecution_FinishStatus_Termination;
		case SIGSTKFLT:
			return Messages.KillableExecution_FinishStatus_StackFault;
		case SIGCHLD:
			return Messages.KillableExecution_FinishStatus_ChildProcessStoppedOrExited;
		case SIGCONT:
			return Messages.KillableExecution_FinishStatus_ContinueExecuting;
		case SIGSTOP:
			return Messages.KillableExecution_FinishStatus_StopExecuting;
		case SIGTSTP:
			return Messages.KillableExecution_FinishStatus_TerminalStopSignal;
		case SIGTTIN:
			return Messages.KillableExecution_FinishStatus_BackgroundProcessReadTTY;
		case SIGTTOU:
			return Messages.KillableExecution_FinishStatus_BackgroundWriteTTY;
		case SIGURG:
			return Messages.KillableExecution_FinishStatus_UrgentConditionSocket;
		case SIGXCPU:
			return Messages.KillableExecution_FinishStatus_CPULimitExceeded;
		case SIGXFSZ:
			return Messages.KillableExecution_FinishStatus_FileSizeLimitExceeded;
		case SIGVTALRM:
			return Messages.KillableExecution_FinishStatus_VirtualAlarmClock;
		case SIGPROF:
			return Messages.KillableExecution_FinishStatus_ProfilingAlarmClock;
		case SIGWINCH:
			return Messages.KillableExecution_FinishStatus_WindowSizeChange;
		case SIGIO:
			return Messages.KillableExecution_FinishStatus_IOPossible;
		case SIGPWR:
			return Messages.KillableExecution_FinishStatus_PowerFailureRestart;
		default:
			return Messages.KillableExecution_FinishStatus_Unknown;
		}
	}

	public boolean isException(int status) {
		return (status >= 129) && (status <= 158);
	}

	public boolean isOK(int status) {
		return status <= 125;
	}

	public boolean isExecutableError(int status) {
		return (status >= 126) && (status <= 128);
	}

	public boolean wasException() {
		return isException(getReturnCode());
	}

	public boolean wasOK() {
		return isOK(getReturnCode());
	}

	public boolean wasCommandError() {
		return isExecutableError(getReturnCode());
	}

	public static final int UNKNOWN = 0;
	public static final int SUCCESS_OK = 1;
	public static final int SUCCESS_ERROR = 2;
	public static final int ERROR_NOT_EXECUTABLE = 126;
	public static final int ERROR_NOT_FOUND = 127;
	public static final int INVALID_EXIT_CODE = 128;
	public static final int SIGHUP = 129;
	public static final int SIGINT = 130;
	public static final int SIGQUIT = 131;
	public static final int SIGILL = 132;
	public static final int SIGTRAP = 133;
	public static final int SIGIOT = 134;
	public static final int SIGBUS = 135;
	public static final int SIGFPE = 136;
	public static final int SIGKILL = 137;
	public static final int SIGUSR1 = 138;
	public static final int SIGSEGV = 139;
	public static final int SIGUSR2 = 140;
	public static final int SIGPIPE = 141;
	public static final int SIGALRM = 142;
	public static final int SIGTERM = 143;
	public static final int SIGSTKFLT = 144;
	public static final int SIGCHLD = 145;
	public static final int SIGCONT = 146;
	public static final int SIGSTOP = 147;
	public static final int SIGTSTP = 148;
	public static final int SIGTTIN = 149;
	public static final int SIGTTOU = 150;
	public static final int SIGURG = 151;
	public static final int SIGXCPU = 152;
	public static final int SIGXFSZ = 153;
	public static final int SIGVTALRM = 154;
	public static final int SIGPROF = 155;
	public static final int SIGWINCH = 156;
	public static final int SIGIO = 157;
	public static final int SIGPWR = 158;

}