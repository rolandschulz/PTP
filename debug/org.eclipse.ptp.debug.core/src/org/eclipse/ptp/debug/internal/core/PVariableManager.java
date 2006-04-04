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
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
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
	private final String VALUE_UNKNOWN = "Unkwnon";
	private List listeners = new ArrayList();

	private Map jobList = new HashMap(); 
	private PCDIDebugModel debugModel = null;
	
	public PVariableManager(PCDIDebugModel debugModel) {
		this.debugModel = debugModel;
	}	
	public void shutdown() {
		for(Iterator i=jobList.values().iterator(); i.hasNext();) {
			JobVariable jobVar = (JobVariable)i.next();
			jobVar.clear();
		}
		jobList.clear();
		listeners.clear();
	}
	public void fireListener(IPJob job) {
		for (Iterator i=listeners.iterator(); i.hasNext();) {
			((IPVariableListener)i.next()).update(job);
		}
	}
	public void addListener(IPVariableListener listener) {
		if (!listeners.contains(listener))
			listeners.add(listener);
	}
	public void removeListener(IPVariableListener listener) {
		if (listeners.contains(listener))
			listeners.remove(listener);
	}
	private JobVariable getJobVariable(IPJob job) {
		if (job == null)
			return null;
		
		JobVariable jobVar = (JobVariable)jobList.get(job.getIDString());
		if (jobVar == null) {
			jobVar = new JobVariable(job);
			jobList.put(job.getIDString(), jobVar);
		}
		return jobVar;
	}
	public void addVariable(IPJob job, String set_id, String variable, IProgressMonitor monitor) throws CoreException {
		if (job != null) {
			JobVariable jobVar = getJobVariable(job);
			if (!jobVar.containVariable(variable)) {
				jobVar.addName(variable);
				updateVariableResults(job, set_id, monitor);
			}
			fireListener(job);
		}
		monitor.done();
	}
	public void removeVariable(IPJob job, String variable, IProgressMonitor monitor) throws CoreException {
		if (job != null) { 
			JobVariable jobVar = getJobVariable(job);
			int index = jobVar.getVarIndex(variable);
			jobVar.removeName(variable);
			if (index > -1) {
				jobVar.removeResults(index, monitor);
			}
			fireListener(job);
		}
		monitor.done();
	}
	public void removeAllVariables(IPJob job) {
		if (job != null) {
			JobVariable jobVar = (JobVariable)jobList.remove(job.getIDString());
			if (jobVar != null) {
				jobVar.clear();
			}
			fireListener(job);
		}
	}
	public boolean hasVariable(IPJob job) {
		if (job == null) {
			return false;
		}
		return getJobVariable(job).hasMore();
	}
	public String[] getVariables(IPJob job) {
		if (job != null) {
			return getJobVariable(job).getVariables();
		}
		return new String[0];
	}

	public void cleanVariableResults(IPJob job) {
		JobVariable jobVar = getJobVariable(job);
		if (jobVar != null) {
			jobVar.cleanupResults();
		}
	}
	public void updateVariableResults(IPJob job, String set_id, IProgressMonitor monitor) throws CoreException {
		JobVariable jobVar = getJobVariable(job);
		if (jobVar == null || !jobVar.hasMore()) {
			monitor.done();
			return;
		}
		IPCDISession session = debugModel.getPCDISession(job);
		if (session != null) {
			BitList taskList = debugModel.getTasks(job.getIDString(), set_id);
			int[] tasks = taskList.toArray();
			String[] vars = jobVar.getVariables();
			monitor.beginTask("Updating variables info...", (tasks.length * vars.length) + 1);
			for (int i=0; i<tasks.length; i++) {
				if (!monitor.isCanceled()) {
					try {
						jobVar.setResult(getValue(session, tasks[i], monitor, vars), tasks[i]);
						monitor.worked(1);
					} catch (PCDIException e) {
						throw new CoreException(new Status(IStatus.ERROR, PTPDebugCorePlugin.getUniqueIdentifier(), IStatus.ERROR, e.getMessage(), null));
					}
				}
			}
		}
		monitor.done();
	}
	
	public String getResultDisplay(IPJob job, int taskID) {
		JobVariable jobVar = getJobVariable(job);
		if (jobVar == null || !jobVar.hasMore()) {
			return "";
		}
		
		String[] values = jobVar.getResult(taskID);
		String display = "";
		if (values.length > 0) {
			String[] vars = jobVar.getVariables();
			for (int i=0; i<values.length; i++) {
				display += "<i>" + vars[i] + ":</i> ";
				display += values[i] + "<br>"; 
			}
		}
		return display;
	}
	
	private String[] getValue(IPCDISession session, int taskID, IProgressMonitor monitor, String[] vars) throws PCDIException {
		String[] values = new String[vars.length];
		for (int i=0; i<vars.length; i++) {			
			try {
				IAIF aif = session.getExpressionValue(taskID, vars[i]);
				if (aif == null) {
					values[i] = VALUE_UNKNOWN;
				}
				else {
					values[i] = aif.getValue().getValueString();
				}
			} catch (AIFException e) {
				values[i] = VALUE_UNKNOWN;
			} finally {
				monitor.worked(1);
			}
		}
		return values;
	}
	
	private class JobVariable {
		private List variableList = new ArrayList();
		private Object[] results = new Object[0];
		
		JobVariable(IPJob job) {
			results = new Object[job.totalProcesses()];
		}
		boolean containVariable(String name) {
			return variableList.contains(name);
		}
		void addName(String name) {
			variableList.add(name);
		}
		int getVarIndex(String name) {
			return variableList.indexOf(name);
		}
		void removeName(String name) {
			variableList.remove(name);
		}
		boolean hasMore() {
			return !variableList.isEmpty();
		}
		String[] getVariables() {
			return (String[])variableList.toArray(new String[0]);
		}
		Object[] getResults() {
			return results;
		}
		String[] getResult(int taskID) {
			if (isValidTask(taskID)) {
				Object result = results[taskID];
				if (result instanceof String[]) {
					return (String[])result;
				}
			}
			return new String[0];
		}
		void clear() {
			variableList.clear();
			for (int i=0; i<results.length; i++) {
				results[i] = null;
			}
			results = null;
		}
		void cleanupResults() {
			results = new Object[results.length];
		}
		boolean isValidTask(int taskID) {
			return (taskID < results.length);
		}
		void setResult(String[] values, int taskID) {
			if (isValidTask(taskID)) {
				results[taskID] = values;
			}
		}
		void removeResult(int taskID) {
			if (isValidTask(taskID)) {
				results[taskID] = null; 
			}
		}
		void removeResults(int index, IProgressMonitor monitor) {
			monitor.beginTask("Refreshing variables info...", results.length);
			for (int i=0; i<results.length; i++) {
				String[] values = getResult(i);
				if (values.length > 0) {
					String[] new_values = new String[values.length-1];
					for (int j=0, h=0; j<new_values.length; j++, h++) {
						if (j == index) {
							h++;
						}
						new_values[j] = values[h];
					}
					setResult(new_values, i);
				}
				monitor.worked(1);
			}
			monitor.done();
		}
	}
}
