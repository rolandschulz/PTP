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
package org.eclipse.ptp.debug.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.debug.core.PCDIDebugModel;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.debug.core.cdi.IPCDISession;

/**
 * @author Clement chu
 */
public final class PJobVariableManager {
	private Map jobMap = new HashMap();
	
	public void shutdown() {
		removeAllJobVariables();
	}
	public JobVariable[] getJobVariables() {
		return (JobVariable[])jobMap.values().toArray(new JobVariable[0]);
	}
	public String getResultDisplay(String jid, int taskID) {
		JobVariable jobVariable = getJobVariable(jid);
		if (jobVariable == null || !jobVariable.hasVariables()) {
			return "";
		}
		
		String[] values = jobVariable.getValues(new Integer(taskID));
		String display = "";
		if (values.length > 0) {
			VariableInfo[] vars = jobVariable.getVariables();
			for (int i=0; i<values.length; i++) {
				if (vars[i].isEnable())
				display += "<i>" + vars[i].getVar() + ":</i> ";
				display += values[i] + "<br>"; 
			}
		}
		return display;
	}
	public boolean addJobVariable(IPJob job, String[] sets, String var) {
		return addJobVariable(job, sets, var, true);
	}
	public VariableInfo getVariableInfo(IPJob job, String var) {
		JobVariable jobVariable = getJobVariable(job.getIDString());
		if (jobVariable != null) {
			return jobVariable.getVariableInfo(var);
		}
		return null;
	}
	public boolean isContainVariable(IPJob job, String var) {
		return (getVariableInfo(job, var) != null);
	}
	public boolean addJobVariable(IPJob job, String[] sets, String var, boolean enable) {
		JobVariable jobVariable = getJobVariable(job.getIDString());
		if (jobVariable == null) {
			jobVariable = new JobVariable();
			jobMap.put(job.getIDString(), jobVariable);
		}
		if (var == null || jobVariable.getVariableInfo(var) != null) {
			return false;
		}		

		VariableInfo varInfo = new VariableInfo(job, var);
		for (int i=0; i<sets.length; i++) {
			varInfo.addSet(sets[i]);
		}
		varInfo.setEnable(enable);
		jobVariable.addVariableInfo(varInfo);
		return true;
	}	
	public boolean changeJobVariable(IPJob job, IPJob newJob, String[] sets, String var, String newVar, boolean enable) {
		if (!removeJobVariable(job.getIDString(), var)) {
			return false;
		}
		return addJobVariable(newJob, sets, newVar, enable);
	}
	public boolean deleteSet(String jid, String sid) {
		JobVariable jobVariable = getJobVariable(jid);
		if (jobVariable == null) {
			return false;
		}
		VariableInfo[] varInfos = jobVariable.getVariables();
		for (int i=0; i<varInfos.length; i++) {
			varInfos[i].removeSet(sid);
		}
		return true;
	}
	public boolean removeJobVariable(String jid, String var) {
		JobVariable jobVariable = getJobVariable(jid);
		if (jobVariable == null) {
			return false;
		}
		VariableInfo varInfo = jobVariable.getVariableInfo(var);
		if (varInfo == null) {
			return false;
		}
		varInfo.clearSets();
		jobVariable.removeVariable(varInfo);
		if (!jobVariable.hasVariables()) {
			jobMap.remove(jid);
		}
		return true;
	}
	public boolean removeJobVariable(String jid) {
		JobVariable jobVariable = getJobVariable(jid);
		if (jobVariable == null) {
			return false;
		}
		jobVariable.clearAll();
		jobMap.remove(jid);
		return true;
	}
	public void removeAllJobVariables() {
		for(Iterator i=jobMap.values().iterator(); i.hasNext();) {
			JobVariable jobVar = (JobVariable)i.next();
			jobVar.clearAll();
		}
		jobMap.clear();
	}
	private JobVariable getJobVariable(String jid) {
		return (JobVariable)jobMap.get(jid);
	}
	public void cleanupJobVariableValues() {
		for(Iterator i=jobMap.values().iterator(); i.hasNext();) {
			JobVariable jobVar = (JobVariable)i.next();
			jobVar.clearValues();
		}		
	}
	public void updateJobVariableValues(String jid, String sid, IProgressMonitor monitor) throws CoreException {
		cleanupJobVariableValues();
		JobVariable jobVariable = getJobVariable(jid);
		if (jobVariable != null) {
			final String[] vars = jobVariable.getEnabledVariables();
			
			PCDIDebugModel debugModel = PTPDebugCorePlugin.getDebugModel();
			IPCDISession session = debugModel.getPCDISession(jid);
			if (session != null) {
				int[] tasks = debugModel.getTasks(jid, sid).toArray();
				monitor.beginTask("Updating variables value...", (tasks.length * vars.length + 1));
				for (int i=0; i<tasks.length; i++) {
					if (!monitor.isCanceled()) {
						String[] texts = debugModel.getValues(session, tasks[i], vars, monitor);
						jobVariable.storeValues(new Integer(tasks[i]), texts);
						monitor.worked(1);
					}
				}
			}
		}
		monitor.done();
	}
	public class JobVariable {
		List variableList = new ArrayList();
		Map procValue = new HashMap();
		
		public List getVariableList() {
			return variableList;
		}
		public VariableInfo getVariableInfo(String var) {
			VariableInfo[] vars = getVariables();
			for (int i=0; i<vars.length; i++) {
				if (vars[i].getVar().equals(var))
					return vars[i];
			}
			return null;
		}
		public void addVariableInfo(VariableInfo varInfo) {
			variableList.add(varInfo);
		}
		public boolean hasVariables() {
			return !variableList.isEmpty();
		}
		public String[] getEnabledVariables() {
			List enVars = new ArrayList();
			VariableInfo[] vars = getVariables();
			for (int i=0; i<vars.length; i++) {
				if (vars[i].isEnable()) {
					enVars.add(vars[i].getVar());
				}
			}
			return (String[])enVars.toArray(new String[0]);
		}
		public VariableInfo[] getVariables() {
			return (VariableInfo[])variableList.toArray(new VariableInfo[0]);
		}
		public void removeVariable(VariableInfo varinfo) {
			variableList.remove(varinfo);
		}
		public void storeValues(Integer pid, String[] texts) {
			procValue.put(pid, texts);
		}
		public String[] getValues(Integer pid) {
			Object obj = procValue.get(pid);
			if (obj == null || !(obj instanceof String[]))
				return new String[0];
			
			return (String[])obj;
		}
		public void clearValues() {
			procValue.clear();
		}
		public void clearVariables() {
			variableList.clear();
		}
		public void clearAll() {
			clearVariables();
			clearValues();
		}
	}
	public class VariableInfo {
		private IPJob job;
		private String var;
		private boolean enable;
		private List setList = new ArrayList();
		
		public VariableInfo(IPJob job, String var) {
			this.job = job;
			this.var = var;
			this.enable = true;
		}
		public IPJob getJob() {
			return job;
		}
		public void changeVar(String var) {
			this.var = var;
		}
		public String getVar() {
			return var;
		}
		public boolean isEnable() {
			return enable;
		}
		public void setEnable(boolean enable) {
			this.enable = enable;
		}
		public String[] getSets() {
			return (String[])setList.toArray(new String[0]);
		}
		public void addSet(String sid) {
			setList.add(sid);
		}
		public void removeSet(String sid) {
			setList.remove(sid);
		}
		public boolean isSetExisted(String sid) {
			return setList.contains(sid);
		}
		public void clearSets() {
			setList.clear();
		}
	}	
}
