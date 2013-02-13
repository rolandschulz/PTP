/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.control.core.runnable;

import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ptp.internal.rm.jaxb.control.core.JAXBControlConstants;
import org.eclipse.ptp.internal.rm.jaxb.control.core.data.LineImpl;
import org.eclipse.ptp.internal.rm.jaxb.control.core.messages.Messages;
import org.eclipse.ptp.internal.rm.jaxb.control.core.utils.EnvironmentVariableUtils;
import org.eclipse.ptp.internal.rm.jaxb.control.core.variables.RMVariableMap;
import org.eclipse.ptp.rm.jaxb.core.IVariableMap;
import org.eclipse.ptp.rm.jaxb.core.data.ArgType;
import org.eclipse.ptp.rm.jaxb.core.data.AttributeType;
import org.eclipse.ptp.rm.jaxb.core.data.LineType;
import org.eclipse.ptp.rm.jaxb.core.data.ScriptType;

/**
 * Extension of the Job runnable to handle the generation of the script by resolving the Script object contents against the active
 * environment.
 * 
 * @author arossi
 * 
 */
public class ScriptHandler extends Job {

	private final String uuid;
	private final IVariableMap map;
	private final Map<String, String> launchEnv;
	private ScriptType script;
	private String scriptValue;

	/**
	 * @param uuid
	 *            internal job identifier (the job has not yet been submitted)
	 * @param script
	 *            JAXB data element
	 * @param map
	 *            the active resource manager or launch tab environment map
	 * @param launchEnv
	 *            any special application environment variables set by the user in the Launch Tab
	 * @param convert
	 *            to "${ptp_lc:}" tags.
	 */
	public ScriptHandler(String uuid, ScriptType script, IVariableMap map, Map<String, String> launchEnv, boolean convert) {
		super(Messages.ScriptHandlerJob);
		this.uuid = uuid;
		this.script = script;
		this.launchEnv = launchEnv;
		this.map = map;
		if (convert) {
			convertScript();
		}
	}

	/**
	 * @return the generated script string
	 */
	public String getScriptValue() {
		return scriptValue;
	}

	/**
	 * Composes script. If the variable map is the resource manager's environment, then the SCRIPT attribute is added to it, with it
	 * value set to the script string that has been generated.
	 */
	@Override
	protected IStatus run(IProgressMonitor monitor) {
		scriptValue = composeScript(monitor);
		if (map instanceof RMVariableMap) {
			RMVariableMap rmMap = (RMVariableMap) map;
			AttributeType a = new AttributeType();
			a.setName(JAXBControlConstants.SCRIPT);
			a.setValue(scriptValue);
			a.setVisible(false);
			rmMap.put(JAXBControlConstants.SCRIPT, a);
		}
		return Status.OK_STATUS;
	}

	/**
	 * Reads from the data object line by line, resolving its arguments and eliminating lines whose resolved arguments qualify as
	 * undefined.<br>
	 * <br>
	 * The <code>envBegin</code> and <code>envEnd</code> tell the script generator between which lines to insert the special
	 * environment variables passed in as the <code>launchEnv</code> if there are any.
	 * 
	 * @param monitor
	 * @return the generated script string
	 */
	private String composeScript(IProgressMonitor monitor) {
		List<LineType> line = script.getLine();
		int len = line.size();
		if (len == 0) {
			return JAXBControlConstants.ZEROSTR;
		}
		int envAfter = script.getInsertEnvironmentAfter();
		if (envAfter == JAXBControlConstants.UNDEFINED || envAfter < 0 || envAfter >= len) {
			envAfter = 1;
		}
		SubMonitor progress = SubMonitor.convert(monitor, len);
		StringBuffer buffer = new StringBuffer();
		String s = null;
		int i = 0;
		String firstLine = new LineImpl(uuid, line.get(0), map).getResolved();
		for (; i <= envAfter; i++) {
			s = new LineImpl(uuid, line.get(i), map).getResolved();
			if (!JAXBControlConstants.ZEROSTR.equals(s)) {
				buffer.append(s).append(JAXBControlConstants.REMOTE_LINE_SEP);
			}
			progress.worked(1);
		}

		if (launchEnv != null) {
			for (String var : launchEnv.keySet()) {
				EnvironmentVariableUtils.addVariable(var, launchEnv.get(var), firstLine, buffer);
			}
		}
		for (; i < len; i++) {
			s = new LineImpl(uuid, line.get(i), map).getResolved();
			if (!JAXBControlConstants.ZEROSTR.equals(s)) {
				buffer.append(s).append(JAXBControlConstants.REMOTE_LINE_SEP);
			}
			progress.worked(1);
		}
		return buffer.toString();
	}

	/**
	 * An auxiliary used in the case of the LaunchTab calling this handler. A temporary replacement Script is created, with the
	 * resolver tags changed from ${ptp_rm:...} to ${ptp_lc:...} (the user never sees the latter).
	 */
	private void convertScript() {
		ScriptType ltScript = new ScriptType();
		ltScript.setInsertEnvironmentAfter(script.getInsertEnvironmentAfter());
		List<LineType> lines = ltScript.getLine();
		List<ArgType> args = null;
		for (LineType line : script.getLine()) {
			LineType newLine = new LineType();
			args = newLine.getArg();
			for (ArgType a : line.getArg()) {
				ArgType newA = new ArgType();
				newA.setIsUndefinedIfMatches(a.getIsUndefinedIfMatches());
				newA.setContent(a.getContent().replaceAll(JAXBControlConstants.VRM, JAXBControlConstants.VLC));
				newA.setResolve(a.isResolve());
				args.add(newA);
			}
			lines.add(newLine);
		}
		script = ltScript;
	}
}
