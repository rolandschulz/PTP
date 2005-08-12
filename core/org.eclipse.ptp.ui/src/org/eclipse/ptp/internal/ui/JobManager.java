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
package org.eclipse.ptp.internal.ui;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ptp.core.IModelManager;
import org.eclipse.ptp.core.IPElement;
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.core.IPProcess;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.ui.IManager;
import org.eclipse.ptp.ui.model.IElementHandler;
import org.eclipse.ptp.ui.model.IElementSet;
import org.eclipse.ptp.ui.model.internal.Element;
import org.eclipse.ptp.ui.model.internal.ElementHandler;

/**
 * @author Clement chu
 *
 */
public class JobManager implements IManager {
	protected IModelManager modelManager = null;
	protected Map jobList = new HashMap();
	protected String cur_set_id = IElementHandler.SET_ROOT_ID;
	protected String cur_job_id = "";
	
	public JobManager() {
		modelManager = PTPCorePlugin.getDefault().getModelManager();
	}
	
	public void shutdown() {
		jobList.clear();
		jobList = null;
		modelManager = null;
	}
	
	public boolean isNoJob() {
		return isNoJob(cur_job_id);
	}
	public boolean isNoJob(String jid) {
		return (jid == null || jid.length() == 0);
	}

	public IElementHandler getElementHandler(String id) {
		return (IElementHandler)jobList.get(id);
	}
	
	public int size() {
		return jobList.size();
	}
	
	public IPJob[] getJobs() {
		return modelManager.getUniverse().getSortedJobs();
	}
	
	public String getCurrentJobId() {
		return cur_job_id;
	}
	public void setCurrentJobId(String job_id) {
		jobChangedEvent(job_id, cur_job_id);
	}
	
	public String getCurrentSetId() {
		return cur_set_id;
	}
	public void setCurrentSetId(String set_id) {
		cur_set_id = set_id;
	}
	
	public String getProcessStatusText(String job_id, String proc_id) {
		switch(getProcessStatus(job_id, proc_id)) {
		case MachineManager.PROC_STARTING:
			return "Starting";
		case MachineManager.PROC_RUNNING:
			return "Running";
		case MachineManager.PROC_EXITED:
			return "Exited";
		case MachineManager.PROC_EXITED_SIGNAL:
			return "Exited Signal";
		case MachineManager.PROC_STOPPED:
			return "Stopped";
		case MachineManager.PROC_ERROR:
			return "Error";
		default:
			return "Error";
		}
	}	
	public int getProcessStatus(String job_id, String proc_id) {
		IPProcess proc = findProcess(job_id, proc_id);
		if (proc != null) {
			String status = proc.getStatus();
			if (status.equals(IPProcess.STARTING))
				return MachineManager.PROC_STARTING;
			else if (status.equals(IPProcess.RUNNING))
				return MachineManager.PROC_RUNNING;
			else if (status.equals(IPProcess.EXITED))
				return MachineManager.PROC_EXITED;
			else if (status.equals(IPProcess.EXITED_SIGNALLED))
				return MachineManager.PROC_EXITED_SIGNAL;
			else if (status.equals(IPProcess.STOPPED))
				return MachineManager.PROC_STOPPED;
			else if (status.equals(IPProcess.ERROR))
				return MachineManager.PROC_ERROR;
		}
		return MachineManager.PROC_ERROR;
	}
	
	//FIXME using id, or name
	public IPProcess findProcessbyName(String job_id, String process_id) {
		//FIXME HARDCODE
		return modelManager.getUniverse().findProcessByName(getName(job_id) + "_process" + process_id);
	}
	
	//FIXME using id, or name
	public IPProcess findProcess(String job_id, String id) {
		IPJob job = findJob(getName(job_id));
		if (job == null)
			return null;
		
		return job.findProcess(id);
	}
	public IPJob findJob(String job_name) {
		return modelManager.getUniverse().findJobByName(job_name);
	}
	public IPJob findJobById(String job_id) {
		IPElement element = modelManager.getUniverse().findChild(job_id);
		if (element == null)
			return findJobById2(job_id);
		return (IPJob)element;
	}
	private IPJob findJobById2(String job_id) {
		IPJob[] jobs = modelManager.getUniverse().getJobs();
		for (int i=0; i<jobs.length; i++) {
			if (jobs[i].getIDString().equals(job_id))
				return jobs[i];
		}
		return null;		
	}
	//FIXME don't know whether it return machine or job
	public String getName(String id) {
		IPElement element = findJobById(id);
		if (element == null)
			return "";
		
		return element.getElementName();
	}
	
	public void addJob(IPJob job) {
		IPProcess[] pElements = job.getSortedProcesses();
		int total_element = pElements.length;
		if (total_element > 0) {
			IElementHandler elementHandler = new ElementHandler();
			elementHandler.clearAll();
			IElementSet set = elementHandler.getSetRoot();
			for (int i=0; i<total_element; i++) {
				//FIXME using id, or name
				set.add(new Element(pElements[i].getIDString(), pElements[i].getPid()));
			}
			elementHandler.add(set);
			jobList.put(job.getIDString(), elementHandler);
		}
	}	
		
	public String initial() {
		IPJob[] jobs = getJobs();
		if (jobs.length > 0) {
			cur_job_id = jobs[0].getIDString();
			for (int j=0; j<jobs.length; j++) {
				if (!jobList.containsKey(jobs[j].getIDString()))
					addJob(jobs[j]);
			}
		}
		return cur_job_id;
	}
	
	public void jobChangedEvent(String cur_jid, String pre_jid) {
		cur_job_id = cur_jid;
	}
}
