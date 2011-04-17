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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ptp.remote.core.IRemoteProcess;
import org.eclipse.ptp.remote.core.IRemoteProcessBuilder;
import org.eclipse.ptp.rm.jaxb.core.ICommandJobStreamsProxy;
import org.eclipse.ptp.rm.jaxb.core.IJAXBNonNLSConstants;
import org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManagerControl;
import org.eclipse.ptp.rm.jaxb.core.IStreamParserTokenizer;
import org.eclipse.ptp.rm.jaxb.core.JAXBCorePlugin;
import org.eclipse.ptp.rm.jaxb.core.data.Arg;
import org.eclipse.ptp.rm.jaxb.core.data.Command;
import org.eclipse.ptp.rm.jaxb.core.data.NameValuePair;
import org.eclipse.ptp.rm.jaxb.core.data.Tokenizer;
import org.eclipse.ptp.rm.jaxb.core.data.impl.ArgImpl;
import org.eclipse.ptp.rm.jaxb.core.messages.Messages;
import org.eclipse.ptp.rm.jaxb.core.utils.CoreExceptionUtils;
import org.eclipse.ptp.rm.jaxb.core.utils.EnvironmentVariableUtils;
import org.eclipse.ptp.rm.jaxb.core.utils.RemoteServicesDelegate;
import org.eclipse.ptp.rm.jaxb.core.variables.RMVariableMap;

/**
 * Implementation of runnable Job for executing external processes. Uses the
 * IRemoteProcessBuilder with the IRemoteConnection for the resource manager's
 * target.
 * 
 * @author arossi
 * 
 */
public class CommandJob extends Job implements IJAXBNonNLSConstants {

	/**
	 * Internal class used for multiplexing output streams between two different
	 * endpoints, usually a tokenizer on the one hand and the stream proxy
	 * passed back to the caller on the other.
	 * 
	 * @author arossi
	 */
	private class StreamSplitter extends Thread {
		private final InputStream in;
		private final PipedOutputStream[] pout;
		private final BufferedOutputStream[] bout;

		/**
		 * @param in
		 *            the stream to be multiplexed
		 * @param pipe1
		 *            sink's stream from which it will read
		 * @param pipe2
		 *            sink's stream from which it will read
		 * @throws IOException
		 */
		private StreamSplitter(InputStream in, PipedInputStream pipe1, PipedInputStream pipe2) throws IOException {
			this.in = in;
			assert (pipe1 != null && pipe2 != null);
			pout = new PipedOutputStream[] { new PipedOutputStream(pipe1), new PipedOutputStream(pipe2) };
			bout = new BufferedOutputStream[] { new BufferedOutputStream(pout[0], STREAM_BUFFER_SIZE),
					new BufferedOutputStream(pout[1], STREAM_BUFFER_SIZE) };
		}

		/**
		 * Reads from input and writes to the piped streams.
		 */
		@Override
		public void run() {
			BufferedInputStream bin = new BufferedInputStream(in);
			while (true) {
				try {
					int i = bin.read();
					if (i == -1) {
						break;
					}
					for (BufferedOutputStream b : bout) {
						b.write(i);
						b.flush();
					}
				} catch (EOFException eof) {
					break;
				} catch (IOException t) {
					JAXBCorePlugin.log(t);
					break;
				}
			}
			for (BufferedOutputStream b : bout) {
				try {
					b.close();
				} catch (IOException t) {
					JAXBCorePlugin.log(t);
				}
			}
			// input stream closed elsewhere
		}
	}

	private final String uuid;
	private final Command command;
	private final IJAXBResourceManagerControl rm;
	private final ICommandJobStreamsProxy proxy;
	private final boolean waitForId;
	private final boolean ignoreExitStatus;
	private final boolean batch;

	private IRemoteProcess process;
	private IStreamParserTokenizer stdoutTokenizer;
	private IStreamParserTokenizer stderrTokenizer;
	private Thread stdoutT;
	private Thread stderrT;
	private InputStream tokenizerOut;
	private InputStream tokenizerErr;
	private StreamSplitter outSplitter;
	private StreamSplitter errSplitter;
	private String remoteOutPath;
	private String remoteErrPath;
	private final StringBuffer error;
	private boolean active;

	/**
	 * @param jobUUID
	 *            either internal or resource specific identifier
	 * @param command
	 *            JAXB data element
	 * @param batch
	 *            whether submission is batch or interactive
	 * @param rm
	 *            the calling resource manager
	 */
	public CommandJob(String jobUUID, Command command, boolean batch, IJAXBResourceManagerControl rm) {
		super(command.getName());
		this.command = command;
		this.batch = batch;
		this.rm = rm;
		this.uuid = jobUUID;
		this.proxy = new CommandJobStreamsProxy();
		this.waitForId = command.isWaitForId();
		this.ignoreExitStatus = command.isIgnoreExitStatus();
		this.error = new StringBuffer();
	}

	/**
	 * @return the process wrapper
	 */
	public IRemoteProcess getProcess() {
		return process;
	}

	/**
	 * @return object wrapping stream monitors.
	 */
	public ICommandJobStreamsProxy getProxy() {
		return proxy;
	}

	/**
	 * @return if job is active
	 */
	public boolean isActive() {
		boolean b = false;
		synchronized (this) {
			b = active;
		}
		return b;
	}

	/**
	 * @return if job is batch
	 */
	public boolean isBatch() {
		return batch;
	}

	/**
	 * Used by stream proxy to read stderr from file if submission is batch.
	 * 
	 * @param remoteErrPath
	 *            for stream redirection (batch submissions)
	 */
	public void setRemoteErrPath(String remoteErrPath) {
		this.remoteErrPath = remoteErrPath;
	}

	/**
	 * Used by stream proxy to read stdout from file if submission is batch.
	 * 
	 * @param remoteOutPath
	 */
	public void setRemoteOutPath(String remoteOutPath) {
		this.remoteOutPath = remoteOutPath;
	}

	/**
	 * The resource manager should wait for the job id on the stream (parsed by
	 * an apposite tokenizer) before returning the status object to the caller.
	 * 
	 * @return whether to wait
	 */
	public boolean waitForId() {
		return waitForId;
	}

	/**
	 * Uses the IRemoteProcessBuilder to set up the command and environment.
	 * After start, the tokenizers (if any) are handled, and stream redirection
	 * managed. Waits for the process, then joins on the consumers.<br>
	 * <br>
	 * Note: the resource manager does not join on this thread, but retrieves
	 * the status object from the job, potentially while it is still running, in
	 * order to hand it off to the caller for stream processing.
	 */
	@Override
	protected IStatus run(IProgressMonitor monitor) {
		try {
			synchronized (this) {
				active = false;
			}
			IRemoteProcessBuilder builder = prepareCommand();
			prepareEnv(builder);

			process = null;
			try {
				process = builder.start();
			} catch (IOException t) {
				throw CoreExceptionUtils.newException(Messages.CouldNotLaunch + builder.command(), t);
			}

			maybeInitializeTokenizers(builder);
			setOutStreamRedirection(process);
			setErrStreamRedirection(process);
			startConsumers(process);

			synchronized (this) {
				active = true;
			}

			int exit = 0;
			try {
				exit = process.waitFor();
			} catch (InterruptedException ignored) {
			}

			if (exit != 0 && !ignoreExitStatus) {
				String t = error.toString();
				error.setLength(0);
				throw CoreExceptionUtils.newException(Messages.ProcessExitValueError + (ZEROSTR + exit) + SP + CO + t, null);
			}

			joinConsumers();
		} catch (CoreException ce) {
			return ce.getStatus();
		} catch (Throwable t) {
			return CoreExceptionUtils.getErrorStatus(Messages.ProcessRunError, t);
		}
		synchronized (this) {
			active = false;
		}
		return Status.OK_STATUS;
	}

	private void errorStreamReader(final InputStream err) {
		new Thread() {
			@Override
			public void run() {
				BufferedReader br = new BufferedReader(new InputStreamReader(err));
				while (true) {
					try {
						String line = br.readLine();
						if (line == null) {
							break;
						}
						error.append(line).append(LINE_SEP);
					} catch (EOFException eof) {
						break;
					} catch (IOException io) {
						JAXBCorePlugin.log(io);
						break;
					}
				}
			}
		}.start();
	}

	/**
	 * Wait for any stream consumer threads to exit.
	 * 
	 * @throws CoreException
	 */
	private void joinConsumers() throws CoreException {
		Throwable t = null;

		if (outSplitter != null) {
			try {
				outSplitter.join();
			} catch (InterruptedException ignored) {
			}
		}

		if (errSplitter != null) {
			try {
				errSplitter.join();
			} catch (InterruptedException ignored) {
			}
		}

		if (stdoutT != null) {
			try {
				stdoutT.join();
			} catch (InterruptedException ignored) {
			}
			t = stdoutTokenizer.getInternalError();
		}

		if (stderrT != null) {
			try {
				stderrT.join();
			} catch (InterruptedException ignored) {
			}
			t = stderrTokenizer.getInternalError();
		}

		if (t != null) {
			throw CoreExceptionUtils.newException(Messages.ParserInternalError, t);
		}
	}

	/**
	 * Checks to see what tokenizers are configured for this resource manager.
	 * If the two streams have been joined, it will prefer the redirect parser
	 * if it exists; otherwise the joined streams will be parsed by the stdout
	 * parser.<br>
	 * <br>
	 * If there is a custom extension tokenizer, it will be instantiated here.
	 * 
	 * @param builder
	 * @throws CoreException
	 */
	private void maybeInitializeTokenizers(IRemoteProcessBuilder builder) throws CoreException {
		Tokenizer t = null;

		if (builder.redirectErrorStream()) {
			t = command.getRedirectParser();
		}

		if (t == null) {
			t = command.getStdoutParser();
		}

		if (t != null) {
			try {
				String type = t.getType();
				if (type != null) {
					stdoutTokenizer = getTokenizer(type);
				} else {
					stdoutTokenizer = new ConfigurableRegexTokenizer(uuid, t);
				}
			} catch (Throwable e) {
				throw CoreExceptionUtils.newException(Messages.StdoutParserError, e);
			}
		}

		t = command.getStderrParser();
		if (t != null) {
			try {
				String type = t.getType();
				if (type != null) {
					stderrTokenizer = getTokenizer(type);
				} else {
					stderrTokenizer = new ConfigurableRegexTokenizer(uuid, t);
				}
			} catch (Throwable e) {
				throw CoreExceptionUtils.newException(Messages.StdoutParserError, e);
			}
		}
	}

	/**
	 * Resolves the command arguments against the current environment, then gets
	 * the process builder from the remote connection.
	 * 
	 * @return the process builder
	 * @throws CoreException
	 */
	private IRemoteProcessBuilder prepareCommand() throws CoreException {
		List<Arg> args = command.getArg();
		if (args == null) {
			throw CoreExceptionUtils.newException(Messages.MissingArglistFromCommandError, null);
		}
		RMVariableMap map = RMVariableMap.getActiveInstance();
		String[] cmdArgs = ArgImpl.getArgs(uuid, args, map);
		RemoteServicesDelegate delegate = rm.getRemoteServicesDelegate();
		return delegate.getRemoteServices().getProcessBuilder(delegate.getRemoteConnection(), cmdArgs);
	}

	/**
	 * Either appends to or replaces the process builder's environment with the
	 * Launch Configuration environment variables. Also sets redirectErrorStream
	 * on the builder.
	 * 
	 * @param builder
	 * @throws CoreException
	 */
	private void prepareEnv(IRemoteProcessBuilder builder) throws CoreException {
		if (!rm.getAppendEnv()) {
			builder.environment().clear();
			Map<String, String> live = rm.getLaunchEnv();
			for (String var : live.keySet()) {
				builder.environment().put(var, live.get(var));
			}
		} else {
			if (command.isReplaceEnvironment()) {
				builder.environment().clear();
			}
			/*
			 * first static env, then dynamic
			 */
			List<NameValuePair> vars = command.getEnvironment();
			RMVariableMap map = RMVariableMap.getActiveInstance();
			for (NameValuePair var : vars) {
				EnvironmentVariableUtils.addVariable(uuid, var, builder.environment(), map);
			}

			Map<String, String> live = rm.getLaunchEnv();
			for (String var : live.keySet()) {
				builder.environment().put(var, live.get(var));
			}
		}

		builder.redirectErrorStream(command.isRedirectStderr());
	}

	/**
	 * Configures handling of the error stream. If there is a tokenizer, it
	 * first checks to see if there will be redirection from a remote file, and
	 * if not, splits the stream between the proxy and the tokenizer in the case
	 * of an interactive job; if there is a remote file, that stream is given to
	 * the proxy and the tokenizer gets the stderr of the submission process. If
	 * there is no tokenizer, then the proxy gets either stderr of the
	 * submission process if interactive, or redirection from the remote file,
	 * accordingly.
	 * 
	 * @param process
	 * @throws IOException
	 */
	private void setErrStreamRedirection(IRemoteProcess process) throws IOException {
		if (stderrTokenizer != null) {
			if (remoteErrPath != null) {
				tokenizerErr = process.getErrorStream();
				proxy.setErrMonitor(new CommandJobStreamTailF2Monitor(rm, remoteErrPath));
			} else if (!batch) {
				PipedInputStream tokenizerErr = new PipedInputStream();
				this.tokenizerErr = tokenizerErr;
				PipedInputStream monitorErr = new PipedInputStream();
				errSplitter = new StreamSplitter(process.getErrorStream(), tokenizerErr, monitorErr);
				proxy.setErrMonitor(new CommandJobStreamMonitor(monitorErr));
			} else {
				tokenizerErr = process.getErrorStream();
			}
		} else if (remoteErrPath != null) {
			proxy.setErrMonitor(new CommandJobStreamTailF2Monitor(rm, remoteErrPath));
			/*
			 * grab error stream for error reporting
			 */
			errorStreamReader(process.getErrorStream());
		} else if (!batch) {
			proxy.setErrMonitor(new CommandJobStreamMonitor(process.getErrorStream()));
		}
	}

	/**
	 * Configures handling of the stdout stream. If there is a tokenizer, it
	 * first checks to see if there will be redirection from a remote file, and
	 * if not, splits the stream between the proxy and the tokenizer in the case
	 * of an interactive job; if there is a remote file, that stream is given to
	 * the proxy and the tokenizer gets the stdout of the submission process. If
	 * there is no tokenizer, then the proxy gets either stdout of the
	 * submission process if interactive, or redirection from the remote file,
	 * accordingly.
	 * 
	 * @param process
	 * @throws IOException
	 */
	private void setOutStreamRedirection(IRemoteProcess process) throws IOException {
		if (stdoutTokenizer != null) {
			if (remoteOutPath != null) {
				tokenizerOut = process.getInputStream();
				proxy.setOutMonitor(new CommandJobStreamTailF2Monitor(rm, remoteOutPath));
			} else if (!batch) {
				PipedInputStream tokenizerOut = new PipedInputStream();
				this.tokenizerOut = tokenizerOut;
				PipedInputStream monitorOut = new PipedInputStream();
				outSplitter = new StreamSplitter(process.getInputStream(), tokenizerOut, monitorOut);
				proxy.setOutMonitor(new CommandJobStreamMonitor(monitorOut));
			} else {
				tokenizerOut = process.getInputStream();
			}
		} else if (remoteOutPath != null) {
			proxy.setOutMonitor(new CommandJobStreamTailF2Monitor(rm, remoteOutPath));
			errorStreamReader(process.getInputStream());
		} else if (!batch) {
			proxy.setOutMonitor(new CommandJobStreamMonitor(process.getInputStream()));
		}
	}

	/**
	 * Initiates stream reading on all consumers.
	 * 
	 * @param process
	 * @throws CoreException
	 */
	private void startConsumers(IRemoteProcess process) throws CoreException {
		if (outSplitter != null) {
			outSplitter.start();
		}

		if (errSplitter != null) {
			errSplitter.start();
		}

		if (remoteOutPath == null) {
			proxy.startMonitors();
		}

		if (stdoutTokenizer != null) {
			try {
				stdoutTokenizer.setInputStream(tokenizerOut);
				stdoutT = new Thread(stdoutTokenizer);
				stdoutT.start();
			} catch (Throwable e) {
				throw CoreExceptionUtils.newException(Messages.StdoutParserError, e);
			}
		}
		if (stderrTokenizer != null) {
			try {
				stderrTokenizer.setInputStream(tokenizerErr);
				stderrT = new Thread(stderrTokenizer);
				stderrT.start();
			} catch (Throwable e) {
				throw CoreExceptionUtils.newException(Messages.StderrParserError, e);
			}
		}
	}

	/**
	 * Extension-based instantiation for custom tokenizer.
	 * 
	 * @param type
	 *            extension name
	 * @return the tokenizer instance
	 * @throws CoreException
	 */
	public static IStreamParserTokenizer getTokenizer(String type) throws CoreException {
		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(JAXBCorePlugin.PLUGIN_ID,
				TOKENIZER_EXT_PT);
		IConfigurationElement[] elements = extensionPoint.getConfigurationElements();
		for (int i = 0; i < elements.length; i++) {
			IConfigurationElement element = elements[i];
			try {
				if (element.getAttribute(ID).equals(type)) {
					return (IStreamParserTokenizer) element.createExecutableExtension(CLASS);
				}
			} catch (CoreException ce) {
				throw ce;
			} catch (Throwable t) {
				throw CoreExceptionUtils.newException(Messages.StreamTokenizerInstantiationError + type, t);
			}
		}
		return null;
	}
}
