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

import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ptp.rm.jaxb.core.IJAXBNonNLSConstants;
import org.eclipse.ptp.rm.jaxb.core.data.Arglist;
import org.eclipse.ptp.rm.jaxb.core.data.DirectiveDefinition;
import org.eclipse.ptp.rm.jaxb.core.data.EnvironmentVariable;
import org.eclipse.ptp.rm.jaxb.core.data.Property;
import org.eclipse.ptp.rm.jaxb.core.data.Script;
import org.eclipse.ptp.rm.jaxb.core.data.impl.ArglistImpl;
import org.eclipse.ptp.rm.jaxb.core.messages.Messages;
import org.eclipse.ptp.rm.jaxb.core.utils.EnvironmentVariableUtils;
import org.eclipse.ptp.rm.jaxb.core.variables.RMVariableMap;

public class ScriptHandler extends Job implements IJAXBNonNLSConstants {

	private final String uuid;
	private final RMVariableMap map;
	private final Map<String, String> live;
	private final boolean appendEnv;
	private final Script script;

	public ScriptHandler(String uuid, Script script, Map<String, String> live, boolean appendEnv) {
		super(Messages.ScriptHandlerJob);
		this.uuid = uuid;
		this.script = script;
		this.live = live;
		this.appendEnv = appendEnv;
		map = RMVariableMap.getActiveInstance();
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		SubMonitor progress = SubMonitor.convert(monitor, 10);
		String script = composeScript(monitor);
		Property p = new Property();
		p.setName(SCRIPT);
		p.setValue(script);
		map.getVariables().put(SCRIPT, p);
		progress.done();
		return Status.OK_STATUS;
	}

	private void addCommand(Arglist arglist, StringBuffer buffer) {
		new ArglistImpl(uuid, arglist).toString(buffer);
		buffer.append(REMOTE_LINE_SEP);
	}

	private void addCommands(List<Arglist> arglist, StringBuffer buffer) {
		for (Arglist args : arglist) {
			new ArglistImpl(uuid, args).toString(buffer);
			buffer.append(REMOTE_LINE_SEP);
		}
	}

	private void addDirectives(List<DirectiveDefinition> defs, StringBuffer buffer) {
		if (defs == null) {
			return;
		}
		for (DirectiveDefinition def : defs) {
			String key = def.getValueFrom();
			String value = EnvironmentVariableUtils.getValue(uuid, key, map);
			if (value != null && !ZEROSTR.equals(value)) {
				buffer.append(def.getContent()).append(value.trim()).append(REMOTE_LINE_SEP);
			}
		}
	}

	private void addEnvironment(String syntax, List<EnvironmentVariable> vars, StringBuffer buffer) {
		if (!appendEnv) {
			for (String var : live.keySet()) {
				EnvironmentVariableUtils.addVariable(var, live.get(var), syntax, buffer);
			}
		} else {
			if (vars != null) {
				for (EnvironmentVariable var : vars) {
					EnvironmentVariableUtils.addVariable(uuid, var, syntax, buffer, map);
				}
			}

			for (String var : live.keySet()) {
				EnvironmentVariableUtils.addVariable(var, live.get(var), syntax, buffer);
			}
		}
	}

	private void addShell(String shell, StringBuffer buffer) {
		buffer.append(map.getString(uuid, shell)).append(REMOTE_LINE_SEP);
	}

	private String composeScript(IProgressMonitor monitor) {
		SubMonitor progress = SubMonitor.convert(monitor, 30);
		StringBuffer buffer = new StringBuffer();
		String shell = script.getShellDirective();
		addShell(shell, buffer);
		progress.worked(5);
		addDirectives(script.getDirective(), buffer);
		progress.worked(5);
		addEnvironment(getSyntax(shell), script.getVariable(), buffer);
		progress.worked(5);
		addCommands(script.getPreExecuteCommand(), buffer);
		progress.worked(5);
		addCommand(script.getExecuteCommand(), buffer);
		progress.worked(5);
		addCommands(script.getPostExecuteCommand(), buffer);
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
