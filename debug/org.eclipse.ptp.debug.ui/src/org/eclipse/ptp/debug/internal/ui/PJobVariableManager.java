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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.DebugJobStorage;
import org.eclipse.ptp.debug.core.IPSession;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.model.aif.AIFException;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIF;
import org.eclipse.ptp.debug.core.pdi.request.IPDIEvaluateExpressionRequest;
import org.eclipse.ptp.debug.ui.PTPDebugUIPlugin;

/**
 * @deprecated replace with PVariableManager
 * @author Clement chu
 */
public final class PJobVariableManager {
	private DebugJobStorage variableStorage = new DebugJobStorage("Variable");
	private DebugJobStorage varProcStorage = new DebugJobStorage("Variable_Process");
	private final String PROCESS_KEY = "process_key";
	private final String VALUE_UNKNOWN = "Unkwnon";
	private final String VALUE_ERROR = "Error in getting value";
	
	public void shutdown() {
		cleanupJobVariableValues();
		varProcStorage.closeDebugJobStorage();
		variableStorage.closeDebugJobStorage();
	}
	public String getResultDisplay(String job_id, int task_id) {
		ProcessValue procVal = (ProcessValue)varProcStorage.getValue(job_id, PROCESS_KEY);
		if (procVal == null) {
			return "";
		}
		
		StringBuffer display = new StringBuffer();
		for (VariableValue value : procVal.getValues(task_id)) {
			display.append("<i>");
			display.append(value.getVar());
			display.append("</i>");
			display.append(" = ");
			display.append(value.getVal());
			display.append("<br>");
		}
		return display.toString();
	}
	public DebugJobStorage getVariableStorage() {
		return variableStorage;
	}
	public boolean isContainVariable(IPJob job, String var) {
		return (variableStorage.getValue(job.getID(), var) != null);
	}
	public void addJobVariable(IPJob job, String var, String[] sets) {
		addJobVariable(job, var, sets, true);
	}
	public void addJobVariable(IPJob job, String var, String[] sets, boolean enable) {
		JobVariable jVar = (JobVariable)variableStorage.getValue(job.getID(), var);
		if (jVar == null) {
			jVar = new JobVariable(job, var, sets, enable);
		}
		else {
			jVar.setSets(sets);
			jVar.setEnable(enable);
		}
		variableStorage.addValue(job.getID(), var, jVar);
		createProcessValue(job);
	}
	private void createProcessValue(IPJob job) {
		ProcessValue procVal = (ProcessValue)varProcStorage.getValue(job.getID(), PROCESS_KEY);
		if (procVal == null) {
			procVal = new ProcessValue(job.size());
			varProcStorage.addValue(job.getID(), PROCESS_KEY, procVal);
		}		
	}
	public JobVariable[] getJobVariables(String job_id) {
		return variableStorage.getValueCollection(job_id).toArray(new JobVariable[0]);
	}
	public void changeJobVariable(IPJob job, IPJob newJob, String[] sets, String var, String newVar, boolean enable) {
		removeJobVariable(job.getID(), var);
		addJobVariable(newJob, newVar, sets, enable);
	}
	public void deleteSet(String job_id, String set_id) {
		for (JobVariable jVar : getJobVariables(job_id)) {
			if (jVar.containSet(set_id)) {
				jVar.removeSet(set_id);
				if (jVar.getSetSize() == 0) {
					variableStorage.removeValue(job_id, jVar.getVar());
				}
			}
		}
	}
	public boolean deleteSet(String job_id, String var, String set_id) {
		JobVariable jVar = (JobVariable)variableStorage.getValue(job_id, var);
		if (jVar == null) {
			return false;
		}
		if (!jVar.containSet(set_id)) {
			return false;
		}
		jVar.removeSet(set_id);
		if (jVar.getSetSize() == 0) {
			variableStorage.removeValue(job_id, jVar.getVar());
		}
		return true;
	}
	public void removeJobVariable(String job_id, String var) {
		variableStorage.removeValue(job_id, var);
	}
	public void removeJobVariables(String job_id) {
		variableStorage.removeJobStorage(job_id);
	}
	public void cleanupJobVariableValues() {
		for (ProcessValue procVal : varProcStorage.getJobValueCollection().toArray(new ProcessValue[0])) {
			if (procVal != null) {
				((ProcessValue)procVal).cleanAllValues();
			}
		}
	}
	public void cleanupJobVariableValues(String job_id, BitList tasks) {
		ProcessValue procVal = (ProcessValue)varProcStorage.getValue(job_id, PROCESS_KEY);
		if (procVal != null) {
			for (int task_id : tasks.toArray()) {
				procVal.cleanValues(task_id);
			}
		}
	}
	public String[] getVariables(String job_id, String set_id, boolean enable) {
		List<String> vars = new ArrayList<String>();
		for (JobVariable jVar : getJobVariables(job_id)) {
			if (jVar.isEnable() == enable && jVar.containSet(set_id)) {
				vars.add(jVar.getVar());
			}
		}
		return vars.toArray(new String[0]);
	}
	public void storeProcessValue(ProcessValue procVal, BitList tasks, String var, String val) {
		for (int task_id : tasks.toArray()) {
			procVal.addValue(task_id, new VariableValue(var, val));
		}
	}
	public void updateJobVariableValues(String job_id, String set_id, BitList tasks, IProgressMonitor monitor) throws CoreException {
		ProcessValue procVal = (ProcessValue)varProcStorage.getValue(job_id, PROCESS_KEY);
		if (procVal != null) {
			//only update when current set id contain in VariableInfo
			String[] vars = getVariables(job_id, set_id, true);
			if (vars.length > 0) {
				IPSession session = PTPDebugUIPlugin.getUIDebugManager().getDebugSession(job_id);				
				if (session != null) {
					//get suspended tasks only in given job and given set
					BitList suspend_tasks = session.getPDISession().getTaskManager().getSuspendedTasks(tasks);
					monitor.beginTask("Updating variables value...", (suspend_tasks.cardinality() * vars.length + 1));
					monitor.worked(1);
					for (int i=0; i<vars.length; i++) {
						if (monitor.isCanceled())
							break;
						
						IPDIEvaluateExpressionRequest request = session.getPDISession().getRequestFactory().getEvaluateExpressionRequest(suspend_tasks, vars[i]);
						try {
							session.getPDISession().getEventRequestManager().addEventRequest(request);
							Map<BitList, Object> map = request.getResultMap(suspend_tasks);
							for (Iterator<BitList> it = map.keySet().iterator(); it.hasNext();) {
								BitList sTasks = it.next();
								Object value = map.get(sTasks);
								if (value instanceof IAIF) {
									storeProcessValue(procVal, sTasks, vars[i], ((IAIF)value).getValue().getValueString());
									monitor.worked(1);
								}
							}
						}
						catch (PDIException e) {
							storeProcessValue(procVal, suspend_tasks, vars[i], VALUE_ERROR);
						} catch (AIFException e) {
							storeProcessValue(procVal, suspend_tasks, vars[i], VALUE_ERROR);
						}
						/*
						try {
							ICommandResult result = session.getExpressionValue(suspend_tasks, vars[i]);
							BitList[] rTasks = result.getTasksArray();
							Object[] rValues = result.getResultsArray();
							
							for (int j=0; j<rTasks.length; j++) {
								if (rValues[j] instanceof IAIF) {
									storeProcessValue(procVal, rTasks[j], vars[i], ((IAIF)rValues[j]).getValue().getValueString());
									monitor.worked(1);
								}
							}
						} catch (PDIException e) {
							storeProcessValue(procVal, suspend_tasks, vars[i], VALUE_ERROR);
						} catch (AIFException e) {
							storeProcessValue(procVal, suspend_tasks, vars[i], VALUE_ERROR);
						}
						*/
					}
				}
			}
		}
		monitor.done();
	}
	
	public class JobVariable {
		IPJob job;
		String var;
		String[] sets = new String[0];
		boolean enable = false;
		
		public JobVariable(IPJob job, String var, String[] sets, boolean enable) {
			this.job = job;
			this.var = var;
			this.sets = sets;
			this.enable = enable;
		}
		public IPJob getJob() {
			return job;
		}
		public String getVar() {
			return var;
		}
		public String[] getSets() {
			return sets;
		}
		public boolean isEnable() {
			return enable;
		}
		public boolean containSet(String set_id) {
			for (String set : sets) {
				if (set.equals(set_id)) {
					return true;
				}
			}
			return false;
		}
		public void addSet(String set_id) {
			String[] newSets = new String[sets.length + 1];
			System.arraycopy(sets, 0, newSets, 0, sets.length);
			newSets[sets.length] = set_id;
			setSets(newSets);
		}
		public void removeSet(String set_id) {
			String[] newSets = new String[sets.length - 1];
			for (int i=0,j=0; i<sets.length; i++) {
				if (!sets[i].equals(set_id)) {
					newSets[j] = sets[i];
					j++;
				}
			}
			setSets(newSets);
		}
		public int getSetSize() {
			return sets.length;
		}
		public void setVar(String var) {
			this.var = var;
		}
		public void setSets(String[] sets) {
			this.sets = sets;
		}
		public void setEnable(boolean enable) {
			this.enable = enable;
		}
	}
	class VariableValue {
		String var;
		String val = VALUE_UNKNOWN;
		
		VariableValue(String var, String val) {
			this.var = var;
			if (val != null)
				this.val = val;
		}
		String getVar() {
			return var;
		}
		String getVal() {
			return val;
		}
		void setVar(String var) {
			this.var = var;
		}
		void setVal(String val) {
			this.val = val;
		}
	}
	class ProcessValue {
		int total = 0;
		Object[] processValues = new Object[0];
		
		ProcessValue(int total) {
			this.total = total;
			processValues = new Object[total];
		}
		void checkValidTask(int task_id) {
			if (task_id > processValues.length)
				throw new IllegalArgumentException("Invalid task id"); 
		}
		void setValues(int task_id, VariableValue[] values) {
			checkValidTask(task_id);
			processValues[task_id] = values;
		}
		VariableValue[] getValues(int task_id) {
			checkValidTask(task_id);
			Object object = processValues[task_id];
			if (object instanceof VariableValue[]) {
				return (VariableValue[])object;
			}
			return new VariableValue[0];
		}
		void addValue(int task_id, VariableValue value) {
			VariableValue[] values = getValues(task_id);
			VariableValue[] newValues = new VariableValue[values.length + 1];
			if (values.length > 0) {
				System.arraycopy(values, 0, newValues, 0, values.length);
			}
			newValues[values.length] = value;
			setValues(task_id, newValues);
		}
		void updateValue(int task_id, VariableValue value) {
			for (VariableValue v : getValues(task_id)) {
				if (v.getVar().equals(value.getVar())) {
					v.setVal(value.getVal());
				}
			}
		}
		void cleanValues(int task_id) {
			processValues[task_id] = null;
		}
		void cleanAllValues() {
			processValues = new Object[total]; 
		}
	}
}
