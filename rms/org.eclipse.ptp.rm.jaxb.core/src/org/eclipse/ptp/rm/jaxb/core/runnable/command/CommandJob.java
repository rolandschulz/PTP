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
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
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
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.rm.jaxb.core.ICommandJobStreamsProxy;
import org.eclipse.ptp.rm.jaxb.core.IJAXBNonNLSConstants;
import org.eclipse.ptp.rm.jaxb.core.IStreamParserTokenizer;
import org.eclipse.ptp.rm.jaxb.core.JAXBCorePlugin;
import org.eclipse.ptp.rm.jaxb.core.data.Arglist;
import org.eclipse.ptp.rm.jaxb.core.data.Command;
import org.eclipse.ptp.rm.jaxb.core.data.EnvironmentVariable;
import org.eclipse.ptp.rm.jaxb.core.data.Tokenizer;
import org.eclipse.ptp.rm.jaxb.core.data.impl.ArglistImpl;
import org.eclipse.ptp.rm.jaxb.core.messages.Messages;
import org.eclipse.ptp.rm.jaxb.core.rm.JAXBResourceManager;
import org.eclipse.ptp.rm.jaxb.core.utils.CoreExceptionUtils;
import org.eclipse.ptp.rm.jaxb.core.utils.EnvironmentVariableUtils;
import org.eclipse.ptp.rm.jaxb.core.variables.RMVariableMap;

public class CommandJob extends Job implements IJAXBNonNLSConstants {

	private class StreamSplitter extends Thread {
		private final InputStream in;
		private final PipedOutputStream[] pout;
		private final BufferedOutputStream[] bout;

		private StreamSplitter(InputStream in, PipedInputStream pipe1, PipedInputStream pipe2) throws IOException {
			this.in = in;
			assert (pipe1 != null && pipe2 != null);
			pout = new PipedOutputStream[] { new PipedOutputStream(pipe1), new PipedOutputStream(pipe2) };
			bout = new BufferedOutputStream[] { new BufferedOutputStream(pout[0], STREAM_BUFFER_SIZE),
					new BufferedOutputStream(pout[1], STREAM_BUFFER_SIZE) };
		}

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
					t.printStackTrace();
					break;
				}
			}
			for (BufferedOutputStream b : bout) {
				try {
					b.close();
				} catch (IOException t) {
					t.printStackTrace();
				}
			}
			// input stream closed elsewhere
		}
	}

	private final String uuid;
	private final Command command;
	private final JAXBResourceManager rm;
	private final ICommandJobStreamsProxy proxy;
	private final boolean waitForId;

	private IStreamParserTokenizer stdoutTokenizer;
	private IStreamParserTokenizer stderrTokenizer;
	private Thread stdoutT;
	private Thread stderrT;
	private InputStream tokenizerOut;
	private InputStream tokenizerErr;
	private StreamSplitter outSplitter;
	private StreamSplitter errSplitter;
	private boolean active;
	private String remoteOutPath;
	private String remoteErrPath;

	public CommandJob(String jobUUID, Command command, JAXBResourceManager rm) {
		super(command.getName());
		this.command = command;
		this.rm = rm;
		this.uuid = jobUUID;
		this.proxy = new CommandJobStreamsProxy();
		this.waitForId = command.isWaitForId();
	}

	public ICommandJobStreamsProxy getProxy() {
		return proxy;
	}

	public boolean isActive() {
		boolean b = false;
		synchronized (this) {
			b = active;
		}
		return b;
	}

	public boolean waitForId() {
		return waitForId;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		try {
			synchronized (this) {
				active = false;
			}
			IRemoteProcessBuilder builder = prepareCommand();
			prepareEnv(builder);
			maybeInitializeTokenizers();

			IRemoteProcess process = null;
			try {
				process = builder.start();
			} catch (IOException t) {
				throw CoreExceptionUtils.newException(Messages.CouldNotLaunch + builder.command(), t);
			}

			setOutStreamRedirection(process);
			setErrStreamRedirection(process);

			if (remoteOutPath == null) {
				proxy.startMonitors();
			}

			runTokenizers(process);

			synchronized (this) {
				active = true;
			}

			int exit = 0;
			try {
				exit = process.waitFor();
			} catch (InterruptedException ignored) {
			}

			if (exit != 0) {
				throw CoreExceptionUtils.newException(Messages.ProcessExitValueError + (ZEROSTR + exit), null);
			}

			joinAll();
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

	private void joinAll() throws CoreException {
		Throwable t = null;

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

		if (t != null) {
			throw CoreExceptionUtils.newException(Messages.ParserInternalError, t);
		}
	}

	private void maybeInitializeTokenizers() throws CoreException {
		Tokenizer t = command.getStdoutParser();
		if (t != null) {
			try {
				String type = t.getType();
				if (type != null) {
					stdoutTokenizer = getTokenizer(type);
				} else {
					stdoutTokenizer = new ConfigurableRegexTokenizer(uuid, t.getRead());
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
					stderrTokenizer = new ConfigurableRegexTokenizer(uuid, t.getRead());
				}
			} catch (Throwable e) {
				throw CoreExceptionUtils.newException(Messages.StdoutParserError, e);
			}
		}
	}

	private IRemoteProcessBuilder prepareCommand() throws CoreException {
		Arglist args = command.getArgs();
		if (args == null) {
			throw CoreExceptionUtils.newException(Messages.MissingArglistFromCommandError, null);
		}
		ArglistImpl arglist = new ArglistImpl(uuid, args);
		String[] cmdArgs = arglist.toArray();
		IRemoteServices service = rm.getRemoteServices();
		return service.getProcessBuilder(rm.getRemoteConnection(), cmdArgs);
	}

	private void prepareEnv(IRemoteProcessBuilder builder) throws CoreException {
		if (!rm.getAppendSysEnv()) {
			builder.environment().clear();
			Map<String, String> live = rm.getDynSystemEnv();
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
			List<EnvironmentVariable> vars = command.getEnvironment();
			RMVariableMap map = RMVariableMap.getActiveInstance();
			for (EnvironmentVariable var : vars) {
				EnvironmentVariableUtils.addVariable(uuid, var, builder.environment(), map);
			}

			Map<String, String> live = rm.getDynSystemEnv();
			for (String var : live.keySet()) {
				builder.environment().put(var, live.get(var));
			}
		}

		builder.redirectErrorStream(command.isRedirectStderr());
	}

	private void runTokenizers(IRemoteProcess process) throws CoreException {
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

	private void setErrStreamRedirection(IRemoteProcess process) throws IOException {
		if (stderrTokenizer != null) {
			if (remoteErrPath == null) {
				PipedInputStream tokenizerErr = new PipedInputStream();
				this.tokenizerErr = tokenizerErr;
				PipedInputStream monitorErr = new PipedInputStream();
				errSplitter = new StreamSplitter(process.getErrorStream(), tokenizerErr, monitorErr);
				proxy.setErrMonitor(new CommandJobStreamMonitor(monitorErr));
			} else {
				tokenizerErr = process.getErrorStream();
				proxy.setErrMonitor(new CommandJobStreamMonitor(rm, remoteErrPath));
			}
		} else if (remoteErrPath == null) {
			proxy.setErrMonitor(new CommandJobStreamMonitor(process.getErrorStream()));
		} else {
			proxy.setErrMonitor(new CommandJobStreamMonitor(rm, remoteErrPath));
		}
	}

	private void setOutStreamRedirection(IRemoteProcess process) throws IOException {
		if (stdoutTokenizer != null) {
			if (remoteOutPath == null) {
				PipedInputStream tokenizerOut = new PipedInputStream();
				this.tokenizerOut = tokenizerOut;
				PipedInputStream monitorOut = new PipedInputStream();
				outSplitter = new StreamSplitter(process.getInputStream(), tokenizerOut, monitorOut);
				proxy.setOutMonitor(new CommandJobStreamMonitor(monitorOut));
			} else {
				tokenizerOut = process.getInputStream();
				proxy.setOutMonitor(new CommandJobStreamMonitor(rm, remoteOutPath));
			}
		} else if (remoteOutPath == null) {
			proxy.setErrMonitor(new CommandJobStreamMonitor(process.getInputStream()));
		} else {
			proxy.setOutMonitor(new CommandJobStreamMonitor(rm, remoteOutPath));
		}
	}

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
