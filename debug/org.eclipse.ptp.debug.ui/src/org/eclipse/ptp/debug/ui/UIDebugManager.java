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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.ptp.core.IModelManager;
import org.eclipse.ptp.core.IPElement;
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.core.IPProcess;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.debug.core.DebugManager;
import org.eclipse.ptp.debug.core.PProcess;
import org.eclipse.ptp.debug.internal.core.model.PDebugTarget;
import org.eclipse.ptp.ui.IManager;
import org.eclipse.ptp.ui.MachineManager;
import org.eclipse.ptp.ui.model.IElement;
import org.eclipse.ptp.ui.model.IElementSet;
import org.eclipse.ptp.ui.model.ISetManager;
import org.eclipse.ptp.ui.model.internal.Element;
import org.eclipse.ptp.ui.model.internal.SetManager;

/**
 * @author clement chu
 *
 */
public class UIDebugManager implements IManager {
	public final static int PROC_SUSPEND = 6;
	public final static int PROC_HIT = 7;

	protected IModelManager modelManager = null;
	private Map jobList = new HashMap();
	
	//FIXME dummy only
	private boolean dummy = true;
		
	public UIDebugManager() {
		modelManager = PTPCorePlugin.getDefault().getModelManager();
	}

	public ISetManager getSetManager(String id) {
		return (ISetManager)jobList.get(id);
	}
	
	public int size() {
		return jobList.size();
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
		//FIXME dummy only 
		if (dummy)
			return getDummyStatus(proc_id);
		
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
	
	//FIXME dummy only 
	public int getDummyStatus(String id) {
		String status = DebugManager.getInstance().getProcess(id).getStatus();
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
		else
			return MachineManager.PROC_ERROR;
	}
	
	//FIXME using id, or name
	public IPProcess findProcess(String job_id, String id) {
		//FIXME HARDCODE
		return modelManager.getUniverse().findProcessByName("job" + job_id + "_process" + id);
	}
	//FIXME don't know whether it return machine or job
	public String getName(String id) {
		//FIXME dummy only
		if (dummy)
			return "dummy";
		
		IPElement element = modelManager.getUniverse().findChild(id);
		if (element == null)
			return "";
		
		return element.getElementName();
	}
	
	public void addJob(IPJob job) {
		IPElement[] pElements = job.getSortedProcesses();
		int total_element = pElements.length;
		if (total_element > 0) {
			ISetManager setManager = new SetManager();
			setManager.clearAll();
			IElementSet set = setManager.getSetRoot();
			for (int i=0; i<total_element; i++) {
				//FIXME using id, or name
				set.add(new Element(pElements[i].getKeyString()));
			}
			setManager.add(set);
			jobList.put(job.getKeyString(), setManager);
		}
	}	
	
	//FIXME dummy only
	private String dummyInitialProcess() {
		PProcess[] processes = DebugManager.getInstance().getProcesses();
		if (processes.length > 0) {
			ISetManager setManager = new SetManager();
			setManager.clearAll();
			IElementSet group = setManager.getSetRoot();
			for (int j=0; j<processes.length; j++) {
				group.add(new Element(processes[j].getID()));
			}
			jobList.put("dummy", setManager);
			return "dummy";
		}
		return "";
	}
	
	public String initial() {
		//FIXME dummy only
		if (dummy) {
			return dummyInitialProcess();
		}
		
		String firstID = "";
		IPJob[] jobs = modelManager.getUniverse().getSortedJobs();
		if (jobs.length > 0) {
			firstID = jobs[0].getKeyString();
			for (int j=0; j<jobs.length; j++) {
				if (!jobList.containsKey(jobs[j].getKeyString()))
					addJob(jobs[j]);
			}
		}
		return firstID;
	}	
	
	public void unregisterElements(ILaunch launch, PDebugTarget target, IElement[] elements) {
		for (int i=0; i<elements.length; i++) {
			//only unregister some registered elements
			if (elements[i].isRegistered()) {
				//TODO unregister in selected elements in debug view 
			}
		}
	}
	
	public void unregisterElements(IElement[] elements) {
		try {
			ILaunch launch = getLaunch();
			unregisterElements(launch, (PDebugTarget)launch.getDebugTarget(), elements);
		} catch (CoreException e) {
			System.out.println(e.getMessage());
		}
	}
	
	public void registerElements(ILaunch launch, PDebugTarget target, IElement[] elements) {
		//FIXME dummy only 
		if (dummy)
			return;
		
		for (int i=0; i<elements.length; i++) {
			//only register some unregistered elements
			if (!elements[i].isRegistered()) {
				//target.register(elements[i].getIDNum());
			}
		}
	}
	public void registerElements(IElement[] elements) {
		try {
			ILaunch launch = getLaunch();
			registerElements(launch, (PDebugTarget)launch.getDebugTarget(), elements);
		} catch (CoreException e) {
			System.out.println(e.getMessage());
		}
	}
	
	public ILaunch getLaunch() throws CoreException {
		ILaunch[] launches = DebugPlugin.getDefault().getLaunchManager().getLaunches();
		for (int i=0; i<launches.length; i++) {
			if (launches[i].getDebugTarget() instanceof PDebugTarget)
				return launches[i];
		}
		throw new CoreException(new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, "No launch found", null));
	}
}
