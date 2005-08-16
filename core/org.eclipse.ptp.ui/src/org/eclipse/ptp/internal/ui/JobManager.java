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

import org.eclipse.ptp.core.IPElement;
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.core.IPProcess;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.ui.IPTPUIConstants;
import org.eclipse.ptp.ui.model.IElement;
import org.eclipse.ptp.ui.model.IElementHandler;
import org.eclipse.ptp.ui.model.IElementSet;
import org.eclipse.ptp.ui.model.internal.Element;
import org.eclipse.ptp.ui.model.internal.ElementHandler;

/**
 * @author Clement chu
 *
 */
public class JobManager extends AbstractUIManager {
	protected Map jobList = new HashMap();
	protected String cur_job_id = "";
	
	public JobManager() {
		modelManager = PTPCorePlugin.getDefault().getModelManager();
	}
	
	public void shutdown() {
		jobList.clear();
		jobList = null;
		modelManager = null;
		super.shutdown();
	}
	
	public boolean isNoJob(String jid) {
		return (jid == null || jid.length() == 0);
	}
	
	public boolean isJobStop(String job_id) {
		if (isNoJob(job_id))
			return true;
		
		IPJob job = findJobById(job_id);
		return (job == null || job.isAllStop());
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
		return getProcessStatusText(findProcess(job_id, proc_id));
	}
	public String getProcessStatusText(IPProcess proc) {
		switch (getProcessStatus(proc)) {
		case IPTPUIConstants.PROC_STARTING:
			return "Starting";
		case IPTPUIConstants.PROC_RUNNING:
			return "Running";
		case IPTPUIConstants.PROC_EXITED:
			return "Exited";
		case IPTPUIConstants.PROC_EXITED_SIGNAL:
			return "Exited Signal";
		case IPTPUIConstants.PROC_STOPPED:
			return "Stopped";
		case IPTPUIConstants.PROC_ERROR:
			return "Error";
		default:
			return "Error";
		}
	}
	public int getProcessStatus(String job_id, String proc_id) {
		return getProcessStatus(findProcess(job_id, proc_id));
	}
	public int getProcessStatus(IPProcess proc) {
		if (proc != null) {
			String status = proc.getStatus();
			if (status.equals(IPProcess.STARTING))
				return IPTPUIConstants.PROC_STARTING;
			else if (status.equals(IPProcess.RUNNING))
				return IPTPUIConstants.PROC_RUNNING;
			else if (status.equals(IPProcess.EXITED))
				return IPTPUIConstants.PROC_EXITED;
			else if (status.equals(IPProcess.EXITED_SIGNALLED))
				return IPTPUIConstants.PROC_EXITED_SIGNAL;
			else if (status.equals(IPProcess.STOPPED))
				return IPTPUIConstants.PROC_STOPPED;
			else if (status.equals(IPProcess.ERROR))
				return IPTPUIConstants.PROC_ERROR;
		}
		return IPTPUIConstants.PROC_ERROR;
	}
	
	public IPProcess findProcess(String job_id, String id) {
		IPJob job = findJobById(job_id);
		if (job == null)
			return null;
		
		return job.findProcess(id);
	}
	public IPJob findJob(String job_name) {
		IPElement element = modelManager.getUniverse().findJobByName(job_name);
		if (element instanceof IPJob)
			return (IPJob)element;
		
		return null;
	}
	public IPJob findJobById(String job_id) {
		IPElement element = modelManager.getUniverse().findChild(job_id);
		if (element instanceof IPJob)
			return (IPJob)element;
		
		return null;
	}

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
				set.add(new Element(set, pElements[i].getIDString(), String.valueOf(pElements[i].getTaskId())));
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
	
	/*****
	 * Element Set
	 *****/
	public void changeSetEvent(IElementSet curSet, IElementSet preSet) {}
	public void deleteSetEvent(IElementSet set) {}
	public void createSetEvent(IElementSet set, IElement[] elements) {}
	public void addElementsEvent(IElementSet set, IElement[] elements) {}
	public void removeElementsEvent(IElementSet set, IElement[] elements) {}	
}
