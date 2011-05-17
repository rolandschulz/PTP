/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.control.internal.runnable.command;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.ptp.core.util.CoreExceptionUtils;
import org.eclipse.ptp.remote.core.IRemoteProcess;
import org.eclipse.ptp.remote.core.IRemoteProcessBuilder;
import org.eclipse.ptp.remote.core.RemoteServicesDelegate;
import org.eclipse.ptp.rm.jaxb.control.JAXBControlConstants;
import org.eclipse.ptp.rm.jaxb.control.JAXBControlCorePlugin;
import org.eclipse.ptp.rm.jaxb.control.JAXBResourceManagerControl;
import org.eclipse.ptp.rm.jaxb.control.data.ArgImpl;
import org.eclipse.ptp.rm.jaxb.control.internal.ICommandJob;
import org.eclipse.ptp.rm.jaxb.control.internal.ICommandJobStatus;
import org.eclipse.ptp.rm.jaxb.control.internal.ICommandJobStreamsProxy;
import org.eclipse.ptp.rm.jaxb.control.internal.IStreamParserTokenizer;
import org.eclipse.ptp.rm.jaxb.control.internal.messages.Messages;
import org.eclipse.ptp.rm.jaxb.control.internal.utils.EnvironmentVariableUtils;
import org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManager;
import org.eclipse.ptp.rm.jaxb.core.IVariableMap;
import org.eclipse.ptp.rm.jaxb.core.JAXBCoreConstants;
import org.eclipse.ptp.rm.jaxb.core.data.ArgType;
import org.eclipse.ptp.rm.jaxb.core.data.CommandType;
import org.eclipse.ptp.rm.jaxb.core.data.NameValuePairType;
import org.eclipse.ptp.rm.jaxb.core.data.TokenizerType;
import org.eclipse.ptp.rmsystem.IJobStatus;

/**
 * Implementation of runnable Job for executing external processes. Uses the
 * IRemoteProcessBuilder with the IRemoteConnection for the resource manager's
 * target.
 * 
 * @author arossi
 * 
 */
public class CommandJob extends Job implements ICommandJob {

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
		private final List<BufferedOutputStream> boutList;

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
			boutList = new ArrayList<BufferedOutputStream>();
			boutList.add(new BufferedOutputStream(pout[0], JAXBControlConstants.STREAM_BUFFER_SIZE));
			boutList.add(new BufferedOutputStream(pout[1], JAXBControlConstants.STREAM_BUFFER_SIZE));
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
					BufferedOutputStream stream = null;
					for (Iterator<BufferedOutputStream> b = boutList.iterator(); b.hasNext();) {
						try {
							stream = b.next();
							stream.write(i);
							stream.flush();
						} catch (IOException dead) {
							/*
							 * we need to check for this here because the
							 * tokenizer can be set to exit early
							 */
							if (dead.getMessage().indexOf(JAXBControlConstants.DEAD) >= 0) {
								b.remove();
								try {
									stream.close();
								} catch (IOException t) {
								}
							} else {
								throw dead;
							}
						}
					}
				} catch (EOFException eof) {
					break;
				} catch (IOException t) {
					JAXBControlCorePlugin.log(t);
					break;
				}
			}
			for (BufferedOutputStream b : boutList) {
				try {
					b.close();
				} catch (IOException t) {
					JAXBControlCorePlugin.log(t);
				}
			}
			// input stream closed elsewhere
		}
	}

	private final String uuid;
	private final CommandType command;
	private final IJAXBResourceManager rm;
	private final JAXBResourceManagerControl control;
	private final ICommandJobStreamsProxy proxy;
	private final IVariableMap rmVarMap;
	private final int flags;
	private final boolean waitForId;
	private final boolean batch;
	private final boolean keepOpen;
	private final StringBuffer error;

	private Thread jobThread;
	private IRemoteProcess process;
	private IStreamParserTokenizer stdoutTokenizer;
	private IStreamParserTokenizer stderrTokenizer;
	private Thread stdoutT;
	private Thread stderrT;
	private InputStream tokenizerOut;
	private InputStream tokenizerErr;
	private StreamSplitter outSplitter;
	private StreamSplitter errSplitter;
	private ICommandJobStatus jobStatus;
	private IStatus status;
	private boolean active;
	private boolean ignoreExitStatus;

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
	public CommandJob(String jobUUID, CommandType command, boolean batch, IJAXBResourceManager rm) {
		super(command.getName() + JAXBControlConstants.CO + JAXBControlConstants.SP + (jobUUID == null ? rm.getName() : jobUUID));
		this.command = command;
		this.batch = batch;
		this.rm = rm;
		this.control = (JAXBResourceManagerControl) rm.getControl();
		this.rmVarMap = this.control.getEnvironment();
		this.uuid = jobUUID;
		this.proxy = new CommandJobStreamsProxy();
		this.waitForId = command.isWaitForId();
		this.ignoreExitStatus = command.isIgnoreExitStatus();
		this.error = new StringBuffer();
		this.keepOpen = command.isKeepOpen();
		String flags = command.getFlags();
		this.flags = getFlags(flags);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.core.ICommandJob#getExecuteStatus()
	 */
	public ICommandJobStatus getJobStatus() {
		return jobStatus;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.core.ICommandJob#getRunStatus()
	 */
	public IStatus getRunStatus() {
		return status;
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

	/*
	 * First unblock any wait; this will allow the run method to return. Destroy
	 * the process and close streams, interrupt the thread and cancel with
	 * manager. (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.core.ICommandJob#terminate()
	 */
	public synchronized void terminate() {
		if (active) {
			active = false;
			if (jobStatus != null) {
				jobStatus.cancelWait();
			}
			if (process != null && !process.isCompleted()) {
				process.destroy();
				if (proxy != null) {
					proxy.close();
				}
				try {
					joinConsumers();
				} catch (CoreException ce) {
					JAXBControlCorePlugin.log(ce);
				}
			}
			if (jobThread != null && jobThread != Thread.currentThread()) {
				if (jobThread.isAlive()) {
					jobThread.interrupt();
				}
			}
			cancel();
			control.getJobTable().remove(getName());
		}
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
	 * If this process has no input, execute it normally. Otherwise, if the
	 * process is to be kept open, check for the command job in the job table;
	 * if it is there and still alive, send the input to it; if not, start the
	 * process, and then send the input.
	 */
	@Override
	protected IStatus run(IProgressMonitor monitor) {
		SubMonitor progress = SubMonitor.convert(monitor, 100);
		try {
			jobThread = Thread.currentThread();
			boolean input = !command.getInput().isEmpty();
			if (input) {
				ICommandJob job = control.getJobTable().get(getName());
				if (job != null && job.isActive()) {
					IRemoteProcess process = job.getProcess();
					if (process != null && !process.isCompleted()) {
						progress.done();
						return writeInputToProcess(process);
					} else {
						job.terminate();
					}
				}
			}
			progress.worked(25);

			status = execute(progress.newChild(50));

			/*
			 * When there is a UUID defined for this command, set the status for
			 * it. If the submit job lacks a jobId on the standard streams, then
			 * we assign it the UUID (it is most probably interactive); else we
			 * wait for the id to be set by the tokenizer. NOTE that the caller
			 * should now join on all commands with this property (05.01.2011).
			 * Open connection jobs should have their jobId tokenizers set a
			 * RUNNING state.
			 */
			jobStatus = null;
			String waitUntil = keepOpen ? IJobStatus.RUNNING : IJobStatus.SUBMITTED;
			ICommandJob parent = keepOpen ? this : null;
			if (uuid != null) {
				if (waitForId) {
					jobStatus = new CommandJobStatus(rm.getUniqueName(), parent, control);
					try {
						jobStatus.waitForJobId(uuid, waitUntil, control.getStatusMap(), progress.newChild(20));
					} catch (CoreException failed) {
						status = CoreExceptionUtils.getErrorStatus(
								failed.getMessage() + JAXBCoreConstants.LINE_SEP + error.toString(), null);
						ignoreExitStatus = true;
						error.setLength(0);
						return status;
					}
				} else {
					String state = isActive() ? IJobStatus.RUNNING : IJobStatus.FAILED;
					jobStatus = new CommandJobStatus(rm.getUniqueName(), uuid, state, parent, control);
				}

				jobStatus.setProxy(getProxy());

				if (!jobStatus.getState().equals(IJobStatus.COMPLETED)) {
					if (input) {
						if (process != null && !process.isCompleted()) {
							status = writeInputToProcess(process);
						}
					}
				} else if (keepOpen && IJobStatus.CANCELED.equals(jobStatus.getStateDetail())) {
					terminate();
				}
			}
		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}
		return status;
	}

	/**
	 * Uses the IRemoteProcessBuilder to set up the command and environment.
	 * After start, the tokenizers (if any) are handled, and stream redirection
	 * managed. Returns immediately if <code>keepOpen</code> is true; else waits
	 * for the process, then joins on the consumers.x
	 */
	private IStatus execute(IProgressMonitor monitor) {
		SubMonitor progress = SubMonitor.convert(monitor, 100);
		try {
			synchronized (this) {
				status = null;
				active = false;
			}
			IRemoteProcessBuilder builder = prepareCommand(progress.newChild(10));
			prepareEnv(builder);
			progress.worked(10);

			process = null;
			try {
				process = builder.start(flags);
			} catch (IOException t) {
				throw CoreExceptionUtils.newException(Messages.CouldNotLaunch + builder.command(), t);
			}
			progress.worked(30);
			maybeInitializeTokenizers(builder);
			setOutStreamRedirection(process);
			setErrStreamRedirection(process);
			startConsumers(process);

			synchronized (this) {
				active = true;
			}
			progress.worked(20);

			if (keepOpen) {
				control.getJobTable().put(getName(), this);
				progress.done();
				return Status.OK_STATUS;
			}

			int exit = 0;
			try {
				exit = process.waitFor();
			} catch (InterruptedException ignored) {
			}

			progress.worked(20);

			if (exit != 0 && !ignoreExitStatus) {
				String message = error.toString();
				error.setLength(0);
				throw CoreExceptionUtils.newException(Messages.ProcessExitValueError + (JAXBControlConstants.ZEROSTR + exit)
						+ JAXBControlConstants.LINE_SEP + message, null);
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

	/**
	 * Converts or'd string into bit-wise or of available flags for remote
	 * process builder.
	 * 
	 * @param flags
	 * @return bit-wise or
	 */
	private int getFlags(String flags) {
		if (flags == null) {
			return IRemoteProcessBuilder.NONE;
		}

		String[] split = flags.split(JAXBControlConstants.REGPIP);
		int f = IRemoteProcessBuilder.NONE;
		for (String s : split) {
			s = s.trim();
			if (JAXBControlConstants.TAG_ALLOCATE_PTY.equals(s)) {
				f |= IRemoteProcessBuilder.ALLOCATE_PTY;
			} else if (JAXBControlConstants.TAG_FORWARD_X11.equals(s)) {
				f |= IRemoteProcessBuilder.FORWARD_X11;
			}
		}
		return f;
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
		TokenizerType t = null;

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
					stdoutTokenizer = new ConfigurableRegexTokenizer(uuid, t, rmVarMap);
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
					stderrTokenizer = new ConfigurableRegexTokenizer(uuid, t, rmVarMap);
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
	 * @param monitor
	 * @return the process builder
	 * @throws CoreException
	 */
	private IRemoteProcessBuilder prepareCommand(IProgressMonitor monitor) throws CoreException {
		List<ArgType> args = command.getArg();
		if (args == null) {
			throw CoreExceptionUtils.newException(Messages.MissingArglistFromCommandError, null);
		}
		String[] cmdArgs = ArgImpl.getArgs(uuid, args, rmVarMap);
		RemoteServicesDelegate delegate = control.getRemoteServicesDelegate(monitor);
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
		if (!control.getAppendEnv()) {
			builder.environment().clear();
			Map<String, String> live = control.getLaunchEnv();
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
			List<NameValuePairType> vars = command.getEnvironment();
			for (NameValuePairType var : vars) {
				EnvironmentVariableUtils.addVariable(uuid, var, builder.environment(), rmVarMap);
			}

			Map<String, String> live = control.getLaunchEnv();
			for (String var : live.keySet()) {
				builder.environment().put(var, live.get(var));
			}
		}

		builder.redirectErrorStream(command.isRedirectStderr());
	}

	/**
	 * Resolves the command input arguments against the current environment.
	 * 
	 * @return the arguments as a single string
	 * @throws CoreException
	 */
	private String prepareInput() throws CoreException {
		List<ArgType> args = command.getInput();
		return ArgImpl.toString(uuid, args, control.getEnvironment());
	}

	/**
	 * Configures handling of the error stream. If there is a tokenizer, the
	 * stream is split between it and the proxy monitor. Otherwise, the proxy
	 * just gets the stream.
	 * 
	 * @param process
	 * @throws IOException
	 */
	private void setErrStreamRedirection(IRemoteProcess process) throws IOException {
		if (stderrTokenizer == null) {
			proxy.setErrMonitor(new CommandJobStreamMonitor(process.getErrorStream()));
		} else {
			PipedInputStream tokenizerErr = new PipedInputStream();
			this.tokenizerErr = tokenizerErr;
			PipedInputStream monitorErr = new PipedInputStream();
			errSplitter = new StreamSplitter(process.getErrorStream(), tokenizerErr, monitorErr);
			proxy.setErrMonitor(new CommandJobStreamMonitor(monitorErr));
		}
		proxy.getErrorStreamMonitor().addListener(new IStreamListener() {
			public void streamAppended(String text, IStreamMonitor monitor) {
				error.append(text);
			}
		});
	}

	/**
	 * Configures handling of the stdout stream. If there is a tokenizer, the
	 * stream is split between it and the proxy monitor. Otherwise, the proxy
	 * just gets the stream.
	 * 
	 * @param process
	 * @throws IOException
	 */
	private void setOutStreamRedirection(IRemoteProcess process) throws IOException {
		if (stdoutTokenizer == null) {
			proxy.setOutMonitor(new CommandJobStreamMonitor(process.getInputStream()));
		} else {
			PipedInputStream tokenizerOut = new PipedInputStream();
			this.tokenizerOut = tokenizerOut;
			PipedInputStream monitorOut = new PipedInputStream();
			outSplitter = new StreamSplitter(process.getInputStream(), tokenizerOut, monitorOut);
			proxy.setOutMonitor(new CommandJobStreamMonitor(monitorOut));
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

		proxy.startMonitors();

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
	 * Calls {@link #prepareInput()}.
	 * 
	 * @param process
	 */
	private IStatus writeInputToProcess(IRemoteProcess process) {
		OutputStream stream = process.getOutputStream();
		try {
			stream.write(prepareInput().getBytes());
			stream.write(JAXBControlConstants.LINE_SEP.getBytes());
			stream.flush();
		} catch (Throwable t) {
			return CoreExceptionUtils.getErrorStatus(Messages.ProcessRunError, t);
		}
		return Status.OK_STATUS;
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
		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(JAXBControlCorePlugin.PLUGIN_ID,
				JAXBControlConstants.TOKENIZER_EXT_PT);
		IConfigurationElement[] elements = extensionPoint.getConfigurationElements();
		for (int i = 0; i < elements.length; i++) {
			IConfigurationElement element = elements[i];
			try {
				if (element.getAttribute(JAXBControlConstants.ID).equals(type)) {
					return (IStreamParserTokenizer) element.createExecutableExtension(JAXBControlConstants.CLASS);
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
