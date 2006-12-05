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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.debug.core.DebugJobStorage;
import org.eclipse.ptp.debug.core.PCDIDebugModel;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.debug.core.cdi.IPCDISession;

/**
 * @author Clement chu
 */
public final class PJobVariableManager {
	private DebugJobStorage variableStorage = new DebugJobStorage("Variable");
	private DebugJobStorage varProcStorage = new DebugJobStorage("Variable_Process");
	private final String PROCESS_KEY = "process_key";
	
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
		VariableValue[] values = procVal.getValues(task_id);
		for (int i=0; i<values.length; i++) {
			display.append("<i>");
			display.append(values[i].getVar());
			display.append("</i>");
			display.append(" = ");
			display.append(values[i].getVal());
			display.append("<br>");
		}
		return display.toString();
	}
	public DebugJobStorage getVariableStorage() {
		return variableStorage;
	}
	public boolean isContainVariable(IPJob job, String var) {
		return (variableStorage.getValue(job.getIDString(), var) != null);
	}
	public void addJobVariable(IPJob job, String var, String[] sets) {
		addJobVariable(job, var, sets, true);
	}
	public void addJobVariable(IPJob job, String var, String[] sets, boolean enable) {
		JobVariable jVar = (JobVariable)variableStorage.getValue(job.getIDString(), var);
		if (jVar == null) {
			jVar = new JobVariable(job, var, sets, enable);
		}
		else {
			jVar.setSets(sets);
			jVar.setEnable(enable);
		}
		variableStorage.addValue(job.getIDString(), var, jVar);
		createProcessValue(job);
	}
	private void createProcessValue(IPJob job) {
		ProcessValue procVal = (ProcessValue)varProcStorage.getValue(job.getIDString(), PROCESS_KEY);
		if (procVal == null) {
			procVal = new ProcessValue(job.totalProcesses());
			varProcStorage.addValue(job.getIDString(), PROCESS_KEY, procVal);
		}		
	}
	public JobVariable[] getJobVariables(String job_id) {
		return (JobVariable[])variableStorage.getValues(job_id);
	}
	public void changeJobVariable(IPJob job, IPJob newJob, String[] sets, String var, String newVar, boolean enable) {
		removeJobVariable(job.getIDString(), var);
		addJobVariable(newJob, newVar, sets, enable);
	}
	public void deleteSet(String job_id, String set_id) {
		List removeVars = new ArrayList();
		for (Iterator i=variableStorage.getValueIterator(job_id); i.hasNext();) {
			JobVariable jVar = (JobVariable)i.next();
			if (jVar.containSet(set_id)) {
				jVar.removeSet(set_id);
				if (jVar.getSetSize() == 0) {
					removeVars.add(jVar.getVar());
				}
			}
		}
		for (Iterator i=removeVars.iterator(); i.hasNext();) {
			variableStorage.removeValue(job_id, (String)i.next());
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
		for (Iterator i=varProcStorage.getJobValueIterator(); i.hasNext();) {
			ProcessValue procVal = (ProcessValue)i.next();
			if (procVal != null) {
				procVal.cleanAllValues();
			}
		}
	}
	public String[] getVariables(String job_id, String set_id, boolean enable) {
		List vars = new ArrayList();
		for (Iterator i=variableStorage.getValueIterator(job_id); i.hasNext();) {
			JobVariable jVar = (JobVariable)i.next();
			if (jVar.isEnable() == enable && jVar.containSet(set_id)) {
				vars.add(jVar.getVar());
			}
		}
		return (String[])vars.toArray(new String[0]);
	}
	public void updateJobVariableValues(String job_id, String set_id, IProgressMonitor monitor) throws CoreException {
		ProcessValue procVal = (ProcessValue)varProcStorage.getValue(job_id, PROCESS_KEY);
		if (procVal != null) {
			//only update when current set id contain in VariableInfo
			String[] vars = getVariables(job_id, set_id, true);
			if (vars.length > 0) {
				PCDIDebugModel debugModel = PTPDebugCorePlugin.getDebugModel();
				IPCDISession session = debugModel.getPCDISession(job_id);
				if (session != null) {
					//get suspended tasks only in given job and given set
					int[] tasks = session.getDebugger().filterRunningTasks(debugModel.getTasks(job_id, set_id)).toArray();
					int length = tasks.length;
					
					monitor.beginTask("Updating variables value...", (length * vars.length + 1));
					for (int i=0; i<length; i++) {
						if (!monitor.isCanceled()) {
							VariableValue[] values = new VariableValue[vars.length];
							for (int j=0; j<vars.length; j++) {
								String val = debugModel.getValue(session, tasks[i], vars[j], monitor);
								values[j] = new VariableValue(vars[j], val);
							}
							procVal.setValues(tasks[i], values);
						}
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
			for (int i=0; i<sets.length; i++) {
				if (sets[i].equals(set_id)) {
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
		String val;
		
		VariableValue(String var, String val) {
			this.var = var;
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
			VariableValue[] values = getValues(task_id);
			for (int i=0; i<values.length; i++) {
				if (values[i].getVar().equals(value.getVar())) {
					values[i].setVal(value.getVal());
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
