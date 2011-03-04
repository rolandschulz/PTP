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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ptp.remote.core.IRemoteProcess;
import org.eclipse.ptp.remote.core.IRemoteProcessBuilder;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.rm.jaxb.core.IJAXBNonNLSConstants;
import org.eclipse.ptp.rm.jaxb.core.IStreamParserTokenizer;
import org.eclipse.ptp.rm.jaxb.core.data.Arglist;
import org.eclipse.ptp.rm.jaxb.core.data.Command;
import org.eclipse.ptp.rm.jaxb.core.data.EnvironmentVariable;
import org.eclipse.ptp.rm.jaxb.core.data.EnvironmentVariables;
import org.eclipse.ptp.rm.jaxb.core.data.StreamParser;
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

	private final Command command;
	private final JAXBResourceManager rm;
	private StreamParser stdoutParser;
	private StreamParser stderrParser;
	private IStreamParserTokenizer stdoutTokenizer;
	private IStreamParserTokenizer stderrTokenizer;
	private Thread stdoutT;
	private Thread stderrT;
	private boolean success;

	public CommandJob(Command command, JAXBResourceManager rm) {
		super(command.getName());
		this.command = command;
		this.rm = rm;
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
			getParsers();

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

	private void getParsers() throws CoreException {
		List<String> refs = command.getParserRef();
		if (refs != null) {
			for (String ref : refs) {
				ref = RMVariableMap.getActiveInstance().getString(ref);
				StreamParser p = (StreamParser) RMVariableMap.getActiveInstance().getVariables().get(ref);
				if (p == null) {
					throw CoreExceptionUtils.newException(Messages.RMNoSuchParserError + ref, null);
				}
				if (p.isStderr()) {
					stderrParser = p;
				} else {
					stdoutParser = p;
				}
			}
		}
	}

	private IRemoteProcessBuilder prepareCommand() throws CoreException {
		Arglist args = command.getArglist();
		if (args == null) {
			throw CoreExceptionUtils.newException(Messages.MissingArglistFromCommandError + command.getName(), null);
		}
		ArglistImpl arglist = new ArglistImpl(args);
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
			EnvironmentVariables vars = command.getEnvironmentVariables();
			RMVariableMap map = RMVariableMap.getActiveInstance();
			if (vars != null) {
				for (EnvironmentVariable var : vars.getEnvironmentVariable()) {
					EnvironmentVariableUtils.addVariable(var, builder.environment(), map);
				}
			}

			Map<String, String> live = rm.getDynSystemEnv();
			for (String var : live.keySet()) {
				builder.environment().put(var, live.get(var));
			}
		}
	}

	private void runTokenizers(IRemoteProcess process) throws CoreException {
		if (stdoutParser != null) {
			try {
				Tokenizer t = stdoutParser.getTokenizer();
				String type = t.getType();

				if (type != null) {
					stdoutTokenizer = null; // get extension TODO
				} else {
					stdoutTokenizer = new ConfigurableRegexTokenizer(t.getApply());
				}

				stdoutTokenizer.setInputStream(process.getInputStream());

				if (command.isDisplayStdout()) {
					stdoutTokenizer.setRedirectStream(System.out);
				}

				stdoutT = new Thread(stdoutTokenizer);
				stdoutT.start();
			} catch (Throwable t) {
				throw CoreExceptionUtils.newException(Messages.StdoutParserError, t);
			}
		} else if (command.isDisplayStdout()) {
			stdoutT = new Redirect(process.getInputStream(), System.out);
			stdoutT.start();
		}

		if (stderrParser != null) {
			try {
				Tokenizer t = stderrParser.getTokenizer();
				String type = t.getType();

				if (type != null) {
					stderrTokenizer = null; // get extension TODO
				} else {
					stderrTokenizer = new ConfigurableRegexTokenizer(t.getApply());
				}

				stderrTokenizer.setInputStream(process.getErrorStream());

				if (command.isDisplayStderr()) {
					stderrTokenizer.setRedirectStream(System.err);
				}

				stderrT = new Thread(stderrTokenizer);
				stderrT.start();
			} catch (Throwable t) {
				throw CoreExceptionUtils.newException(Messages.StderrParserError, t);
			}
		} else if (command.isDisplayStderr()) {
			stderrT = new Redirect(process.getErrorStream(), System.err);
			stderrT.start();
		}
	}
}
