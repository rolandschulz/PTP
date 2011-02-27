package org.eclipse.ptp.rm.jaxb.core.runnable;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ptp.remote.core.IRemoteProcess;
import org.eclipse.ptp.remote.core.IRemoteProcessBuilder;
import org.eclipse.ptp.rm.jaxb.core.IJAXBNonNLSConstants;
import org.eclipse.ptp.rm.jaxb.core.data.Arglist;
import org.eclipse.ptp.rm.jaxb.core.data.ArglistImpl;
import org.eclipse.ptp.rm.jaxb.core.data.Command;
import org.eclipse.ptp.rm.jaxb.core.data.StreamParser;
import org.eclipse.ptp.rm.jaxb.core.messages.Messages;
import org.eclipse.ptp.rm.jaxb.core.utils.CoreExceptionUtils;
import org.eclipse.ptp.rm.jaxb.core.variables.RMVariableMap;

public class CommandJob extends Job implements IJAXBNonNLSConstants {

	private final Command command;
	private final IRemoteProcessBuilder builder;
	private StreamParser stdoutParser;
	private StreamParser stderrParser;
	private StreamParserImpl stdoutParserImpl;
	private StreamParserImpl stderrParserImpl;

	public CommandJob(Command command, IRemoteProcessBuilder builder) {
		super(command.getName());
		this.command = command;
		this.builder = builder;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		try {
			prepareCommand();
			prepareDir();
			prepareEnv();
			prepareParsers();

			IRemoteProcess process = null;

			try {
				process = builder.start();
			} catch (IOException t) {
				throw CoreExceptionUtils.newException(Messages.CouldNotLaunch + builder.command(), t);
			}

			runParsers(process);

			int exit = 0;

			try {
				exit = process.waitFor();
			} catch (InterruptedException ignored) {
			}

			if (exit != 0) {
				throw CoreExceptionUtils.newException(Messages.ProcessExitValueError + (ZEROSTR + exit), null);
			}

			if (stdoutParserImpl != null) {
				try {
					stdoutParserImpl.join();
				} catch (InterruptedException ignored) {
				}
				Throwable t = stdoutParserImpl.getInternalError();
				if (t != null) {
					throw CoreExceptionUtils.newException(Messages.ParserInternalError, t);
				}
			}

			if (stderrParserImpl != null) {
				try {
					stderrParserImpl.join();
				} catch (InterruptedException ignored) {
				}
				Throwable t = stderrParserImpl.getInternalError();
				if (t != null) {
					throw CoreExceptionUtils.newException(Messages.ParserInternalError, t);
				}
			}
		} catch (CoreException ce) {
			return ce.getStatus();
		}

		return Status.OK_STATUS;
	}

	private void prepareCommand() throws CoreException {
		Arglist args = command.getArglist();
		if (args == null) {
			throw CoreExceptionUtils.newException(Messages.MissingArglistFromCommandError + command.getName(), null);
		}
		ArglistImpl arglist = new ArglistImpl(args);
		StringBuffer buffer = new StringBuffer();
		arglist.toString(buffer);
		builder.command(buffer.toString());
	}

	private void prepareDir() throws CoreException {

	}

	private void prepareEnv() throws CoreException {

	}

	private void prepareParsers() throws CoreException {
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
		if (stdoutParser != null) {
			stdoutParser.setRedirect(command.isDisplayStdout());
		}
		if (stderrParser != null) {
			stderrParser.setRedirect(command.isDisplayStderr());
		}
	}

	private void runParsers(IRemoteProcess process) throws CoreException {
		if (stdoutParser != null) {
			try {
				stdoutParserImpl = new StreamParserImpl(stdoutParser, process.getInputStream());
				if (stdoutParser.isRedirect()) {
					stdoutParserImpl.setOut(new OutputStreamWriter(System.out));
				}
				stdoutParserImpl.start();
			} catch (Throwable t) {
				throw CoreExceptionUtils.newException(Messages.StdoutParserError, t);
			}
		}
		if (stderrParser != null) {
			try {
				stderrParserImpl = new StreamParserImpl(stderrParser, process.getErrorStream());
				if (stderrParser.isRedirect()) {
					stderrParserImpl.setOut(new OutputStreamWriter(System.err));
				}
				stderrParserImpl.start();
			} catch (Throwable t) {
				throw CoreExceptionUtils.newException(Messages.StderrParserError, t);
			}
		}
	}
}
