/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.core.runnable;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

	private class Redirect extends Thread {

		private final InputStream in;
		private final OutputStream out;

		private Redirect(InputStream in, OutputStream out) {
			this.in = in;
			this.out = out;
		}

		@Override
		public void run() {
			BufferedInputStream bin = new BufferedInputStream(in);
			BufferedOutputStream bout = new BufferedOutputStream(out);

			while (true) {
				try {
					int i = bin.read();
					if (i == EOF) {
						break;
					}
					bout.write(i);
					bout.flush();
				} catch (EOFException eof) {
					break;
				} catch (IOException t) {
					t.printStackTrace();
				}
				// we do not close the streams here
			}
		}
	}

	private final String uuid;
	private final Command command;
	private final JAXBResourceManager rm;
	private IStreamParserTokenizer stdoutTokenizer;
	private IStreamParserTokenizer stderrTokenizer;
	private Thread stdoutT;
	private Thread stderrT;
	private boolean success;

	public CommandJob(String jobUUID, Command command, JAXBResourceManager rm) {
		super(ZEROSTR);
		this.command = command;
		this.rm = rm;
		this.uuid = jobUUID;
	}

	public boolean getSuccess() {
		return success;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		try {
			success = false;

			IRemoteProcessBuilder builder = prepareCommand();

			prepareEnv(builder);

			IRemoteProcess process = null;

			try {
				process = builder.start();
			} catch (IOException t) {
				throw CoreExceptionUtils.newException(Messages.CouldNotLaunch + builder.command(), t);
			}

			runTokenizers(process);

			int exit = 0;

			try {
				exit = process.waitFor();
			} catch (InterruptedException ignored) {
			}

			if (exit != 0) {
				throw CoreExceptionUtils.newException(Messages.ProcessExitValueError + (ZEROSTR + exit), null);
			}

			if (stdoutT != null) {
				try {
					stdoutT.join();
				} catch (InterruptedException ignored) {
				}
				Throwable t = stdoutTokenizer.getInternalError();
				if (t != null) {
					throw CoreExceptionUtils.newException(Messages.ParserInternalError, t);
				}
			}

			if (stderrT != null) {
				try {
					stderrT.join();
				} catch (InterruptedException ignored) {
				}
				Throwable t = stderrTokenizer.getInternalError();
				if (t != null) {
					throw CoreExceptionUtils.newException(Messages.ParserInternalError, t);
				}
			}
		} catch (CoreException ce) {
			return ce.getStatus();
		}
		success = true;
		return Status.OK_STATUS;
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
		boolean append = rm.getAppendSysEnv();
		if (!append) {
			builder.environment().clear();
			Map<String, String> live = rm.getDynSystemEnv();
			for (String var : live.keySet()) {
				builder.environment().put(var, live.get(var));
			}
		} else {
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
	}

	private void runTokenizers(IRemoteProcess process) throws CoreException {
		Tokenizer t = command.getStdoutParser();
		if (t != null) {
			try {
				String type = t.getType();

				if (type != null) {
					stdoutTokenizer = getTokenizer(type);
				} else {
					stdoutTokenizer = new ConfigurableRegexTokenizer(uuid, t.getRead());
				}

				stdoutTokenizer.setInputStream(process.getInputStream());

				if (command.isDisplayStdout()) {
					stdoutTokenizer.setRedirectStream(System.out);
				}

				stdoutT = new Thread(stdoutTokenizer);
				stdoutT.start();
			} catch (Throwable e) {
				throw CoreExceptionUtils.newException(Messages.StdoutParserError, e);
			}
		} else if (command.isDisplayStdout()) {
			stdoutT = new Redirect(process.getInputStream(), System.out);
			stdoutT.start();
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

				stderrTokenizer.setInputStream(process.getErrorStream());

				if (command.isDisplayStderr()) {
					stderrTokenizer.setRedirectStream(System.err);
				}

				stderrT = new Thread(stderrTokenizer);
				stderrT.start();
			} catch (Throwable e) {
				throw CoreExceptionUtils.newException(Messages.StderrParserError, e);
			}
		} else if (command.isDisplayStderr()) {
			stderrT = new Redirect(process.getErrorStream(), System.err);
			stderrT.start();
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
