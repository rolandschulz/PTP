/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.debug.internal.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.PCDIDebugModel;
import org.eclipse.ptp.debug.core.aif.AIFException;
import org.eclipse.ptp.debug.core.aif.IAIF;
import org.eclipse.ptp.debug.core.cdi.IPCDISession;
import org.eclipse.ptp.debug.core.cdi.PCDIException;
import org.eclipse.ptp.debug.core.model.IPVariableManager;

/**
 * @author Clement chu
 * 
 */
public class PVariableManager implements IPVariableManager {
	private Map jobList = new HashMap(); 
	private List variables = new ArrayList();
	private PCDIDebugModel debugModel = null;
	
	public PVariableManager(PCDIDebugModel debugModel) {
		this.debugModel = debugModel;
	}	
	public void shutdown() {
		variables.clear();
		jobList.clear();
	}

	public void addVariable(String variable_name) {
		if (!variables.contains(variable_name))
			variables.add(variable_name);
	}
	public void removeVariable(String variable_name) {
		if (variables.contains(variable_name))
			variables.remove(variable_name);
	}
	public String[] getVariables() {
		return (String[])variables.toArray(new String[0]);
	}
	public void removeAllVariables() {
		variables.clear();
	}
	public boolean hasVariable() {
		return (variables.size()>0);
	}

	public String[] getVariables(IPJob job) {
		if (jobList.containsKey(job.getIDString())) {
			return (String[])jobList.get(job.getIDString());
		}
		return new String[job.totalProcesses()];
	}
	public void setVariable(String job_id, String[] variableTexts) {
		jobList.put(job_id, variableTexts);
	}
	public void updateVariables(IPJob job, String set_id, IProgressMonitor monitor) throws CoreException {
		if (variables.size() == 0) {
			monitor.done();
			return;
		}
		if (job == null) {
			throw new CoreException(new Status(IStatus.ERROR, PCDIDebugModel.getPluginIdentifier(), IStatus.ERROR, "No job", null));
		}
		IPCDISession session = debugModel.getPCDISession(job);
		if (session == null) {
			throw new CoreException(new Status(IStatus.ERROR, PCDIDebugModel.getPluginIdentifier(), IStatus.ERROR, "No session found", null));
		}

		String[] variableTexts = getVariables(job);
		BitList taskList = debugModel.getTasks(job.getIDString(), set_id);
		int[] tasks = taskList.toArray();
		monitor.beginTask("Updating " + tasks.length + " variables info...", tasks.length);
		for (int i=0; i<tasks.length; i++) {
			if (!monitor.isCanceled()) {
				variableTexts[tasks[i]] = getValue(session, tasks[i]);
				monitor.worked(1);
			}
		}
		setVariable(job.getIDString(), variableTexts);
		monitor.done();
	}
	
	public void cleanVariables(IPJob job) {
		if (jobList.containsKey(job.getIDString())) {
			jobList.remove(job.getIDString());
		}
	}
	
	public String getValueText(IPJob job, int taskID) {
		if (hasVariable()) {
			String[] variableTexts = getVariables(job);
			if (variableTexts.length > 0 && taskID < variableTexts.length) {
				if (variableTexts[taskID] != null) {
					return variableTexts[taskID];
				}
				return "No value found.";
			}
		}
		return "";
	}
	
	private String getValue(IPCDISession session, int taskID) {
		String content = "";
		for (Iterator i=variables.iterator(); i.hasNext();) {
			String variable = (String)i.next();
			content += "-<i>" + variable + ":</i> ";
			try {
				IAIF aif = session.getExpressionValue(taskID, variable);
				content += aif.getValue().getValueString(); 
			} catch (PCDIException e) {
				content += "Unknown";
			} catch (AIFException e) {
				content += "Unknown";
			}
			content += "<br>";
		}
		return content;
	}
}
