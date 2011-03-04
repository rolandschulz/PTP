package org.eclipse.ptp.rm.jaxb.core.runnable;

import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ptp.rm.jaxb.core.IJAXBNonNLSConstants;
import org.eclipse.ptp.rm.jaxb.core.data.Arglist;
import org.eclipse.ptp.rm.jaxb.core.data.DirectiveDefinition;
import org.eclipse.ptp.rm.jaxb.core.data.DirectiveDefinitions;
import org.eclipse.ptp.rm.jaxb.core.data.EnvironmentVariable;
import org.eclipse.ptp.rm.jaxb.core.data.EnvironmentVariables;
import org.eclipse.ptp.rm.jaxb.core.data.ExecuteCommand;
import org.eclipse.ptp.rm.jaxb.core.data.PostExecuteCommands;
import org.eclipse.ptp.rm.jaxb.core.data.PreExecuteCommands;
import org.eclipse.ptp.rm.jaxb.core.data.Script;
import org.eclipse.ptp.rm.jaxb.core.data.impl.ArglistImpl;
import org.eclipse.ptp.rm.jaxb.core.messages.Messages;
import org.eclipse.ptp.rm.jaxb.core.utils.EnvironmentVariableUtils;
import org.eclipse.ptp.rm.jaxb.core.variables.RMVariableMap;

public class ScriptHandler extends Job implements IJAXBNonNLSConstants {

	private final RMVariableMap map;
	private final Map<String, String> live;
	private final boolean appendEnv;
	private final Script script;

	public ScriptHandler(Script script, Map<String, String> live, boolean appendEnv) {
		super(Messages.ScriptHandlerJob);
		this.script = script;
		this.live = live;
		this.appendEnv = appendEnv;
		map = RMVariableMap.getActiveInstance();
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		SubMonitor progress = SubMonitor.convert(monitor, 10);
		String script = composeScript(monitor);
		map.getVariables().put(SCRIPT, script);
		progress.done();
		return Status.OK_STATUS;
	}

	private void addDirectives(DirectiveDefinitions defs, StringBuffer buffer) {
		if (defs == null) {
			return;
		}
		for (DirectiveDefinition def : defs.getDirectiveDefinition()) {
			String key = def.getValueFrom();
			String value = EnvironmentVariableUtils.getValue(key, map);
			if (value != null && !ZEROSTR.equals(value)) {
				buffer.append(def.getContent()).append(value.trim()).append(REMOTE_LINE_SEP);
			}
		}
	}

	private void addEnvironment(EnvironmentVariables vars, StringBuffer buffer) {
		String syntax = getSyntax(script.getShell());
		if (!appendEnv) {
			for (String var : live.keySet()) {
				EnvironmentVariableUtils.addVariable(var, live.get(var), syntax, buffer);
			}
		} else {
			if (vars != null) {
				for (EnvironmentVariable var : vars.getEnvironmentVariable()) {
					EnvironmentVariableUtils.addVariable(var, syntax, buffer, map);
				}
			}

			for (String var : live.keySet()) {
				EnvironmentVariableUtils.addVariable(var, live.get(var), syntax, buffer);
			}
		}
	}

	private void addExecute(ExecuteCommand commands, StringBuffer buffer) {
		new ArglistImpl(commands.getArglist()).toString(buffer);
		buffer.append(REMOTE_LINE_SEP);
	}

	private void addPostExecute(PostExecuteCommands commands, StringBuffer buffer) {
		if (commands == null) {
			return;
		}
		for (Arglist args : commands.getArglist()) {
			new ArglistImpl(args).toString(buffer);
			buffer.append(REMOTE_LINE_SEP);
		}
	}

	private void addPreExecute(PreExecuteCommands commands, StringBuffer buffer) {
		if (commands == null) {
			return;
		}
		for (Arglist args : commands.getArglist()) {
			new ArglistImpl(args).toString(buffer);
			buffer.append(REMOTE_LINE_SEP);
		}
	}

	private void addShell(String shell, StringBuffer buffer) {
		buffer.append(map.getString(shell)).append(REMOTE_LINE_SEP);
	}

	private String composeScript(IProgressMonitor monitor) {
		SubMonitor progress = SubMonitor.convert(monitor, 30);
		StringBuffer buffer = new StringBuffer();
		addShell(script.getShell(), buffer);
		progress.worked(5);
		addDirectives(script.getDirectiveDefinitions(), buffer);
		progress.worked(5);
		addEnvironment(script.getEnvironmentVariables(), buffer);
		progress.worked(5);
		addPreExecute(script.getPreExecuteCommands(), buffer);
		progress.worked(5);
		addExecute(script.getExecuteCommand(), buffer);
		progress.worked(5);
		addPostExecute(script.getPostExecuteCommands(), buffer);
		progress.done();
		return buffer.toString();
	}

	private String getSyntax(String shell) {
		if (shell.indexOf(CSH) >= 0) {
			return SETENV;
		}
		return EXPORT;
	}
}
