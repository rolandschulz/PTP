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
import org.eclipse.ptp.rm.jaxb.core.IVariableMap;
import org.eclipse.ptp.rm.jaxb.core.data.Arg;
import org.eclipse.ptp.rm.jaxb.core.data.Line;
import org.eclipse.ptp.rm.jaxb.core.data.Property;
import org.eclipse.ptp.rm.jaxb.core.data.Script;
import org.eclipse.ptp.rm.jaxb.core.data.impl.LineImpl;
import org.eclipse.ptp.rm.jaxb.core.messages.Messages;
import org.eclipse.ptp.rm.jaxb.core.utils.EnvironmentVariableUtils;
import org.eclipse.ptp.rm.jaxb.core.variables.LTVariableMap;
import org.eclipse.ptp.rm.jaxb.core.variables.RMVariableMap;

public class ScriptHandler extends Job implements IJAXBNonNLSConstants {

	private final String uuid;
	private final IVariableMap map;
	private final Map<String, String> live;
	private final boolean appendEnv;
	private Script script;
	private String scriptValue;

	public ScriptHandler(String uuid, Script script, IVariableMap map, Map<String, String> live, boolean appendEnv) {
		super(Messages.ScriptHandlerJob);
		this.uuid = uuid;
		this.script = script;
		this.live = live;
		this.appendEnv = appendEnv;
		this.map = map;
		if (map instanceof LTVariableMap) {
			convertScript();
		}
	}

	public String getScriptValue() {
		return scriptValue;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		SubMonitor progress = SubMonitor.convert(monitor, 10);
		scriptValue = composeScript(monitor);
		if (map instanceof RMVariableMap) {
			RMVariableMap rmMap = (RMVariableMap) map;
			Property p = new Property();
			p.setName(SCRIPT);
			p.setValue(scriptValue);
			rmMap.getVariables().put(SCRIPT, p);
		}
		progress.done();
		return Status.OK_STATUS;
	}

	private String composeScript(IProgressMonitor monitor) {
		List<Line> line = script.getLine();
		int len = line.size();
		if (len == 0) {
			return ZEROSTR;
		}
		int envBegin = script.getEnvBegin();
		int envEnd = script.getEnvEnd();
		if (envBegin == UNDEFINED) {
			envBegin = 1;
		}
		if (envEnd == UNDEFINED) {
			envEnd = 1;
		}
		SubMonitor progress = SubMonitor.convert(monitor, len);
		StringBuffer buffer = new StringBuffer();
		String s = null;
		int i = 0;
		String firstLine = new LineImpl(uuid, line.get(0), map).getResolved();
		for (; i < envBegin; i++) {
			s = new LineImpl(uuid, line.get(i), map).getResolved();
			if (!ZEROSTR.equals(s)) {
				buffer.append(s).append(REMOTE_LINE_SEP);
			}
			progress.worked(1);
		}

		if (live != null && !appendEnv) {
			for (String var : live.keySet()) {
				EnvironmentVariableUtils.addVariable(var, live.get(var), firstLine, buffer);
			}
		}
		for (; i < envEnd; i++) {
			s = new LineImpl(uuid, line.get(i), map).getResolved();
			if (!ZEROSTR.equals(s)) {
				buffer.append(s).append(REMOTE_LINE_SEP);
			}
			progress.worked(1);
		}
		if (live != null && appendEnv) {
			for (String var : live.keySet()) {
				EnvironmentVariableUtils.addVariable(var, live.get(var), firstLine, buffer);
			}
		}
		for (; i < len; i++) {
			s = new LineImpl(uuid, line.get(i), map).getResolved();
			if (!ZEROSTR.equals(s)) {
				buffer.append(s).append(REMOTE_LINE_SEP);
			}
			progress.worked(1);
		}
		progress.done();
		return buffer.toString();
	}

	private void convertScript() {
		Script ltScript = new Script();
		ltScript.setEnvBegin(script.getEnvBegin());
		ltScript.setEnvEnd(script.getEnvEnd());
		List<Line> lines = ltScript.getLine();
		List<Arg> args = null;
		for (Line line : script.getLine()) {
			Line newLine = new Line();
			args = newLine.getArg();
			for (Arg a : line.getArg()) {
				Arg newA = new Arg();
				newA.setIsUndefinedIfMatches(a.getIsUndefinedIfMatches());
				newA.setContent(a.getContent().replaceAll(VRM, VLT));
				newA.setResolve(a.isResolve());
				args.add(newA);
			}
			lines.add(newLine);
		}
		script = ltScript;
	}
}
