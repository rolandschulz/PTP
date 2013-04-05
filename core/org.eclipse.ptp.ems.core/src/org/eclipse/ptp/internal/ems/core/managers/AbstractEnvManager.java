/*******************************************************************************
 * Copyright (c) 2012 University of Illinois and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Jeff Overbey (Illinois) - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.ems.core.managers;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.ptp.ems.core.IEnvManager;
import org.eclipse.ptp.ems.core.IEnvManagerConfig;
import org.eclipse.ptp.internal.ems.core.EMSCorePlugin;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.remote.core.IRemoteProcess;
import org.eclipse.ptp.remote.core.IRemoteProcessBuilder;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;

/**
 * Base class for implementations of {@link IEnvManager}.
 * <p>
 * Provides default implementations of {@link #configure(IRemoteConnection)},
 * {@link #createBashScript(IProgressMonitor, boolean, IEnvManagerConfig, String)},
 * {@link #getBashConcatenation(String, boolean, IEnvManagerConfig, String)}, and {@link #getComparator()}, as well as several
 * protected-visibility utility methods for use by subclasses.
 * 
 * @author Jeff Overbey
 */
public abstract class AbstractEnvManager implements IEnvManager {

	/** Timeout in milliseconds before {@link #runCommand(boolean, String...)} will forcibly terminate the process */
	private static final long TIMEOUT = 10000L;

	/** Case-insensitive string comparator. Assumes non-<code>null</code> arguments. */
	// TODO: Handle embedded numbers correctly (so module/1.0 < module/2.0 < module/10.0)
	protected static final Comparator<String> MODULE_NAME_COMPARATOR = new Comparator<String>() {
		@Override
		public int compare(String s, String t) {
			assert s != null && t != null;
			return s.compareToIgnoreCase(t);
		}
	};

	/** {@link IRemoteConnection} used to access files and execute shell commands on the remote machine. */
	private IRemoteConnection remoteConnection = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ems.core.IEnvManager#getComparator()
	 */
	@Override
	public Comparator<String> getComparator() {
		return MODULE_NAME_COMPARATOR;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ems.core.IEnvManager#configure(org.eclipse.ptp.remote.core.IRemoteConnection)
	 */
	@Override
	public final void configure(IRemoteConnection remoteConnection) {
		if (remoteConnection == null) {
			throw new IllegalArgumentException("remoteConnection must be non-null"); //$NON-NLS-1$
		}

		this.remoteConnection = remoteConnection;
	}

	/**
	 * @return the {@link IRemoteServices} corresponding to the remote connection previously set using
	 *         {@link #configure(IRemoteConnection)} (may be <code>null</code>)
	 */
	protected final IRemoteServices getRemoteServices() {
		if (remoteConnection == null) {
			throw new IllegalStateException("remoteConnection cannot be null"); //$NON-NLS-1$
		}

		return this.remoteConnection.getRemoteServices();
	}

	/**
	 * @return the {@link IRemoteConnection} object previously set using {@link #configure(IRemoteConnection)} (may be
	 *         <code>null</code>)
	 */
	protected final IRemoteConnection getRemoteConnection() {
		if (remoteConnection == null) {
			throw new IllegalStateException("remoteConnection cannot be null"); //$NON-NLS-1$
		}

		return this.remoteConnection;
	}

	/**
	 * Runs the given command on the remote machine that has been configured by {@link #configure(IRemoteConnection)}.
	 * 
	 * @param requireCleanExit
	 *            if <code>true</code>, then an empty list will be returned if the process completes with a non-zero exit code or is
	 *            terminated after {@value #TIMEOUT} ms
	 * @param command
	 *            command (including arguments) to be executed on the remote machine
	 * 
	 * @return combined output and error (non-<code>null</code>). Each list element corresponds to one line from the process's
	 *         standard output or standard error.
	 * 
	 * @throws NullPointerException
	 *             if {@link #configure(IRemoteConnection)} has not been called
	 * @throws RemoteConnectionException
	 * @throws IOException
	 */
	protected final List<String> runCommand(IProgressMonitor pm, boolean requireCleanExit, String... command)
			throws RemoteConnectionException,
			IOException {
		log(IStatus.INFO, "Running remote command; clean exit%s required:", requireCleanExit ? "" : " not"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		for (int i = 0; i < command.length; i++) {
			log(IStatus.INFO, "[%d] %s", i, command[i]); //$NON-NLS-1$
		}

		final IRemoteProcessBuilder processBuilder = createRemoteProcessBuilder(pm, command);
		if (processBuilder == null) {
			log(IStatus.ERROR, "- Could not create remote process builder"); //$NON-NLS-1$
			return null;
		}

		final IRemoteProcess p = processBuilder.start();
		if (p == null) {
			log(IStatus.ERROR, "- Could not start remote process builder"); //$NON-NLS-1$
			return null;
		}

		final long start = System.currentTimeMillis();
		while (!hasTerminated(p) && System.currentTimeMillis() - start < TIMEOUT) {
			try {
				Thread.sleep(500);
			} catch (final InterruptedException e) {
				// Keep waiting
			}
		}

		if (hasTerminated(p)) {
			final int severity = (p.exitValue() == 0 ? IStatus.INFO : IStatus.WARNING);
			log(severity, "- Remote process terminated with exit code %d", p.exitValue()); //$NON-NLS-1$
		} else {
			log(IStatus.WARNING, "- Remote process timed out"); //$NON-NLS-1$
		}

		final List<String> result = new ArrayList<String>(256);
		readLines(p.getInputStream(), result);
		readLines(p.getErrorStream(), result);
		log(IStatus.INFO, "- Lines read: %d", result.size()); //$NON-NLS-1$

		p.destroy();
		log(IStatus.INFO, "- Remote process destroyed"); //$NON-NLS-1$
		if (requireCleanExit && (!hasTerminated(p) || p.exitValue() != 0)) {
			return Collections.emptyList();
		} else {
			return result;
		}
	}

	// See http://wiki.eclipse.org/FAQ_How_do_I_use_the_platform_debug_tracing_facility%3F
	protected final void log(int severity, String format, Object... args) {
		final String message = String.format(format, args);

		if (EMSCorePlugin.getDefault().isDebugging()) {
			System.out.println(message);
		}

		if (severity == IStatus.ERROR || severity == IStatus.WARNING) {
			EMSCorePlugin.log(new Status(severity, EMSCorePlugin.PLUGIN_ID, message));
		}
	}

	private static boolean hasTerminated(IRemoteProcess p) {
		return p.isCompleted();
	}

	private IRemoteProcessBuilder createRemoteProcessBuilder(IProgressMonitor pm, String... command)
			throws RemoteConnectionException {
		SubMonitor monitor = SubMonitor.convert(pm, 100);
		final IRemoteConnection connection = getRemoteConnection();
		if (connection == null) {
			return null;
		}

		if (!connection.isOpen()) {
			connection.open(monitor.newChild(80));
		}

		return connection.getRemoteServices().getProcessBuilder(connection, command);
	}

	private void readLines(InputStream input, List<String> result) throws IOException {
		final BufferedReader in = new BufferedReader(new InputStreamReader(input));
		for (String line = in.readLine(); line != null; line = in.readLine()) {
			result.add(line);
		}
		in.close();
	}

	/**
	 * Runs the given command on the remote machine that has been configured by {@link #configure(IRemoteConnection)} using
	 * <tt>bash --login -c </tt><i>command</i>.
	 * <p>
	 * This is a convenience method equivalent to <tt>runCommand(pm, true, "bash", "--login", "-c", command)</tt>.
	 * 
	 * @param command
	 *            command (including arguments) to be executed on the remote machine
	 * 
	 * @return combined output and error (non-<code>null</code>). Each list element corresponds to one line from the process's
	 *         standard output or standard error.
	 * 
	 * @throws NullPointerException
	 *             if {@link #configure(IRemoteConnection)} has not been called
	 * @throws RemoteConnectionException
	 * @throws IOException
	 */
	protected final List<String> runCommandInBashLoginShell(IProgressMonitor pm, String command) throws RemoteConnectionException,
			IOException {
		return runCommand(pm, false, "bash", "--login", "-c", command); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ems.core.IEnvManager#getBashConcatenation(java.lang.String, boolean,
	 * org.eclipse.ptp.ems.core.IEnvManagerConfig, java.lang.String)
	 */
	@Override
	public String getBashConcatenation(String separator, boolean echo, IEnvManagerConfig config, String commandToExecuteAfterward) {
		final StringBuilder sb = new StringBuilder();

		if (config.isEnvMgmtEnabled()) {
			if (config.isManualConfigEnabled()) {
				concatenateManualConfigText(separator, config.getManualConfigText(), sb);
			} else {
				concatenate(separator, getInitialBashCommands(echo), sb);
				for (final String moduleName : config.getConfigElements()) {
					concatenate(separator, getBashCommand(echo, moduleName), sb);
				}
			}
		}

		if (commandToExecuteAfterward != null) {
			if (echo) {
				concatenate(separator, "echo '" + commandToExecuteAfterward + "'", sb); //$NON-NLS-1$ //$NON-NLS-2$
			}
			concatenate(separator, commandToExecuteAfterward, sb);
		}

		return sb.toString();
	}

	private void concatenateManualConfigText(String separator, String manualConfigText, StringBuilder sb) {
		if (separator.contains(";")) { //$NON-NLS-1$
			// Collapse the manual configuration text into a single-line Bash command (attempt to, anyway)
			final String[] lines = manualConfigText.split("\n"); //$NON-NLS-1$
			boolean lastLineWasBashControlLine = false;
			for (final String line : lines) {
				if (isBashComment(line) || isBlankLine(line)) {
					// Skip this line
					lastLineWasBashControlLine = false;
				} else if (isBashControlLine(line)) {
					concatenate(separator, line, sb);
					lastLineWasBashControlLine = true;
				} else {
					if (lastLineWasBashControlLine) {
						concatenate("", line, sb); //$NON-NLS-1$ // No separator after control line
					} else {
						concatenate(separator, line, sb);
					}
					lastLineWasBashControlLine = false;
				}
			}
		} else {
			concatenate(separator, manualConfigText, sb);
		}
	}

	private boolean isBlankLine(String line) {
		return line.trim().equals(""); //$NON-NLS-1$
	}

	private boolean isBashComment(String line) {
		return line.trim().startsWith("#"); //$NON-NLS-1$
	}

	/*
	 * This isn't completely correct -- e.g., it doesn't handle trailing comments -- but, given how infrequently this should be
	 * used anyway, it should be close enough to get by.
	 */
	private boolean isBashControlLine(String line) {
		line = line.trim();
		return line.startsWith("for") && line.endsWith("do") //$NON-NLS-1$ //$NON-NLS-2$
				|| line.startsWith("select") && line.endsWith("do") //$NON-NLS-1$ //$NON-NLS-2$
				|| line.startsWith("if") && line.endsWith("then") //$NON-NLS-1$ //$NON-NLS-2$
				|| line.startsWith("elif") && line.endsWith("then") //$NON-NLS-1$ //$NON-NLS-2$
				|| line.equals("else") //$NON-NLS-1$
				|| line.startsWith("while") && line.endsWith("do") //$NON-NLS-1$ //$NON-NLS-2$
				|| line.startsWith("until") && line.endsWith("do"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private void concatenate(String separator, List<String> commands, final StringBuilder sb) {
		for (final String command : commands) {
			concatenate(separator, command, sb);
		}
	}

	private void concatenate(String separator, String command, final StringBuilder sb) {
		if (sb.length() > 0) {
			sb.append(separator);
		}
		sb.append(command);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ems.core.IEnvManager#createBashScript(boolean, org.eclipse.ptp.ems.core.IEnvManagerConfig,
	 * java.lang.String)
	 */
	@Override
	public String createBashScript(IProgressMonitor pm, boolean echo, IEnvManagerConfig config, String commandToExecuteAfterward)
			throws RemoteConnectionException, IOException {
		final String pathToTempFile = createTempFile(pm);
		checkTempFile(pathToTempFile);
		writeBashScript(echo, pathToTempFile, config, commandToExecuteAfterward);
		return pathToTempFile;
	}

	private String createTempFile(IProgressMonitor pm) throws RemoteConnectionException, IOException {
		final List<String> output = runCommand(pm, true, "mktemp", "-qt", "ptpscript_XXXXXX"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		if (output.size() != 1) {
			throw new IOException("Unexpected output from mktemp -t ptpscript"); //$NON-NLS-1$
		}
		return output.get(0);
	}

	private void checkTempFile(String pathToTempFile) throws IOException {
		final IRemoteFileManager fileManager = getRemoteServices().getFileManager(getRemoteConnection());
		final IFileStore script = fileManager.getResource(pathToTempFile);
		final IFileInfo info = script.fetchInfo();
		if (info.isDirectory() || info.getLength() > 0) {
			throw new IOException("Temp file from mktemp -t ptpscript is invalid"); //$NON-NLS-1$
		}
	}

	private void writeBashScript(boolean echo, String pathToTempFile, IEnvManagerConfig config, String commandToExecuteAfterward) {
		final IRemoteFileManager fileManager = getRemoteServices().getFileManager(getRemoteConnection());
		final IFileStore script = fileManager.getResource(pathToTempFile);
		PrintStream out = null;
		try {
			// Bash requires line endings to be "\n", so don't use println (will use "\r\n" on Windows)
			out = new PrintStream(new BufferedOutputStream(script.openOutputStream(EFS.NONE, null)));
			out.print("#!/bin/bash --login\n"); //$NON-NLS-1$
			out.print("echo ''\n"); //$NON-NLS-1$
			out.print("echo '**** Environment configuration script temporarily stored in " + pathToTempFile + " ****'\n"); //$NON-NLS-1$ //$NON-NLS-2$
			if (config.isEnvMgmtEnabled()) {
				if (config.isManualConfigEnabled()) {
					out.print(config.getManualConfigText().replace("\r\n", "\n") + "\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				} else {
					for (final String command : getInitialBashCommands(echo)) {
						out.print(command + "\n"); //$NON-NLS-1$
					}
					for (final String moduleName : config.getConfigElements()) {
						for (final String command : getBashCommand(echo, moduleName)) {
							out.print(command + "\n"); //$NON-NLS-1$
						}
					}
				}
			}
			if (commandToExecuteAfterward != null && commandToExecuteAfterward.length() > 0) {
				if (echo) {
					out.print("echo '" + commandToExecuteAfterward + "'\n"); //$NON-NLS-1$ //$NON-NLS-2$
				}
				out.print(commandToExecuteAfterward + "\n"); //$NON-NLS-1$
			}
			out.print("rm -f '" + pathToTempFile + "'\n"); //$NON-NLS-1$ //$NON-NLS-2$
			out.flush();
			out.close();
		} catch (final Exception e) {
			EMSCorePlugin.log(e);
			if (out != null) {
				out.close();
			}
			attemptToDelete(script);
		}
	}

	private boolean attemptToDelete(IFileStore script) {
		try {
			script.delete(EFS.NONE, null);
			return true;
		} catch (final CoreException e) {
			EMSCorePlugin.log(e);
			return false;
		}
	}

	/**
	 * @return a list of Bash commands which will be executed before any environment configuration elements are handled. Usually,
	 *         this will purge or reset the environment (e.g., &quot;module purge&quot;).
	 */
	protected abstract List<String> getInitialBashCommands(boolean echo);

	/**
	 * @return a list of Bash commands which will configure the given element in the environment (e.g., &quot;module load
	 *         element&quot;).
	 */
	protected abstract List<String> getBashCommand(boolean echo, String element);
}
