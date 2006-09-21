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
package org.eclipse.ptp.debug.internal.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.core.IPProcess;
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.PCDIDebugModel;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.debug.core.cdi.IPCDISession;
import org.eclipse.ptp.debug.core.events.IPDebugEvent;
import org.eclipse.ptp.debug.core.events.PDebugEvent;
import org.eclipse.ptp.debug.core.events.PDebugInfo;

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
		StringBuffer display = new StringBuffer();

		JobVariable jobVariable = getJobVariable(jid);
		if (jobVariable == null || !jobVariable.hasVariables()) {
			return display.toString();
		}
		
		ValueList values = jobVariable.getValues(new Integer(taskID));
		if (values == null) {
			return display.toString();
		}
	
		ValueInfo[] info = values.getValues();
		for (int i=0; i<info.length; i++) {
			display.append("<i>");
			display.append(info[i].getVariable());
			display.append("</i>");
			display.append(" = ");
			display.append(info[i].getValue());
			display.append("<br>");
		}
		return display.toString();
	}
	public boolean addJobVariable(IPJob job, String[] sets, String var) {
		return addJobVariable(job, sets, var, true);
	}
	public VariableInfo getVariableInfo(IPJob job, String var) {
		if (job == null)
			return null;
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
			jobVariable = new JobVariable(job);
			jobMap.put(job.getIDString(), jobVariable);
		}

		if (var == null || jobVariable.getVariableInfo(var) != null) {
			return false;
		}
		VariableInfo varInfo = new VariableInfo(jobVariable, var);
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
	public boolean hasVariables(String jid) {
		JobVariable jobVariable = getJobVariable(jid);
		if (jobVariable != null) {
			return jobVariable.hasVariables();
		}
		return false;
	}
	private JobVariable getJobVariable(String jid) {
		return (JobVariable)jobMap.get(jid);
	}
	public void cleanupJobVariableValues() {
		for(Iterator i=jobMap.values().iterator(); i.hasNext();) {
			JobVariable jobVar = (JobVariable)i.next();
			if (jobVar != null) {
				IPCDISession session = PTPDebugCorePlugin.getDebugModel().getPCDISession(jobVar.getJob().getIDString());
				BitList taskList = jobVar.getDiffValueTasks();
				if (taskList != null) {
					PDebugInfo baseInfo = new PDebugInfo(jobVar.getJob(), taskList, null, null);						
					PTPDebugCorePlugin.getDefault().fireDebugEvent(new PDebugEvent(session, IPDebugEvent.CHANGE, IPDebugEvent.CONTENT, baseInfo));
				}
				jobVar.clearValues();
			}
		}		
	}
	public void updateJobVariableValues(String jid, String sid, IProgressMonitor monitor) throws CoreException {
		JobVariable jobVariable = getJobVariable(jid);
		if (jobVariable != null) {
			//only update when current set id contain in VariableInfo
			String[] vars = jobVariable.getEnabledVariables(sid);
			if (vars.length > 0) {
				PCDIDebugModel debugModel = PTPDebugCorePlugin.getDebugModel();
				IPCDISession session = debugModel.getPCDISession(jid);
				if (session != null) {
					int[] tasks = debugModel.getTasks(jid, sid).toArray();
					int length = tasks.length;
					BitList taskList = new BitList((length>0)?tasks[tasks.length-1]+1:0);
					
					monitor.beginTask("Updating variables value...", (length * vars.length + 1));
					for (int i=0; i<length; i++) {
						if (!monitor.isCanceled()) {
							//check whether the process is terminated
							IPProcess process = jobVariable.getJob().findProcessByTaskId(tasks[i]);
							if (process != null && !process.isAllStop()) {
								ValueList values = new ValueList();
								for (int j=0; j<vars.length; j++) {
									values.addValue(new ValueInfo(vars[j], debugModel.getValue(session, tasks[i], vars[j], monitor)));
								}
								jobVariable.storeValues(new Integer(tasks[i]), values);
								if (jobVariable.getType() == JobVariable.TYPE_DIFF) {
									taskList.set(tasks[i]);
								}
							}
						}
					}
					if (!taskList.isEmpty()) {
						jobVariable.setDiffValueTasks(taskList);
						PDebugInfo baseInfo = new PDebugInfo(jobVariable.getJob(), taskList, null, null);						
						PTPDebugCorePlugin.getDefault().fireDebugEvent(new PDebugEvent(session, IPDebugEvent.CHANGE, IPDebugEvent.EVALUATION, baseInfo));
					}
				}
			}
		}
		monitor.done();
	}
	public class JobVariable {
		public static final int TYPE_DEFAULT = 0;
		public static final int TYPE_DIFF = 1;
		List variableList = new ArrayList();
		Map procValue = new HashMap();
		IPJob job = null;
		Integer lastPID = null;
		BitList diffValueTasks = null;
		int type = TYPE_DEFAULT;
		
		public JobVariable(IPJob job) {
			this.job = job;
		}
		public IPJob getJob() {
			return job;
		}
		public BitList getDiffValueTasks() {
			return diffValueTasks;
		}
		public void setDiffValueTasks(BitList diffValueTasks) {
			this.diffValueTasks = diffValueTasks;
		}
		public int getType() {
			return type;
		}
		public void setType(int type) {
			this.type = type;
		}
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
		public String[] getEnabledVariables(String sid) {
			List enVars = new ArrayList();
			VariableInfo[] vars = getVariables();
			for (int i=0; i<vars.length; i++) {
				if (vars[i].isEnable()) {
					if (vars[i].isSetExisted(sid)) {
						enVars.add(vars[i].getVar());
					}
				}
			}
			return (String[])enVars.toArray(new String[0]);
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
			lastPID = null;
		}
		public void storeValues(Integer pid, ValueList values) {
			ValueList oldValues = (ValueList)procValue.remove(pid);
			if (oldValues != null) {
				oldValues.clean();
				oldValues = null;
			}
			procValue.put(pid, values);
			if (lastPID != null) {
				if (values.compareTo(getValues(lastPID)) > 0) {
					type = TYPE_DIFF;
				}
			}
			lastPID = pid;
		}
		public ValueList getValues(Integer pid) {
			return (ValueList)procValue.get(pid);
		}
		public void clearValues() {
			procValue.clear();
			type = TYPE_DEFAULT;
			diffValueTasks = null;
			lastPID = null;
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
		JobVariable parent;
		String var;
		boolean enable;
		List setList = new ArrayList();
		
		public VariableInfo(JobVariable parent, String var) {
			this.parent = parent;
			this.var = var;
			this.enable = true;
		}
		public JobVariable getParent() {
			return parent;
		}
		public IPJob getJob() {
			return getParent().getJob();
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
	public class ValueList implements Comparable {
		List values = new ArrayList();
		public void addValue(ValueInfo value) {
			values.add(value);
		}
		public void removeValue(ValueInfo value) {
			values.remove(value);
		}
		public ValueInfo[] getValues() {
			return (ValueInfo[])values.toArray(new ValueInfo[0]);
		}
		public void clean() {
			values.clear();
		}
		/** Compare the value list
		 * @return 1 is different, 0 is the same, -1 is unknown  
		 * 
		 */
		public int compareTo(Object obj) {
			if (obj instanceof ValueList) {
				ValueInfo[] curInfo = getValues();
				ValueInfo[] refInfo = ((ValueList)obj).getValues();
				for (int i=0; i<curInfo.length; i++) {
					if (!curInfo[i].getValue().equals(refInfo[i].getValue())) {
						return 1;
					}
				}
				return 0;
			}
			return -1;
		}
	}
	public class ValueInfo {
		String variable = "";
		String value = "";
		int type = -1;
		public ValueInfo(String variable, String value) {
			this(variable, value, -1);
		}
		public ValueInfo(String variable, String value, int type) {
			this.variable = variable;
			this.value = value;
			this.type = type;
		}
		public String getVariable() {
			return variable;
		}
		public String getValue() {
			return value;
		}
		public int getType() {
			return type;
		}
		public void setVariable(String variable) {
			this.variable = variable;
		}
		public void setValue(String value) {
			this.value = value;
		}
		public void setType(int type) {
			this.type = type;
		}
	}
}
