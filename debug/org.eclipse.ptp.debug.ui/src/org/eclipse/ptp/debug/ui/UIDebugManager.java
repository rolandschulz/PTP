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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.ptp.core.IModelManager;
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.core.IPProcess;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.debug.core.DebugManager;
import org.eclipse.ptp.debug.core.PProcess;
import org.eclipse.ptp.debug.internal.core.model.PDebugTarget;
import org.eclipse.ptp.debug.ui.model.IElement;
import org.eclipse.ptp.debug.ui.model.IElementGroup;
import org.eclipse.ptp.debug.ui.model.IGroupManager;
import org.eclipse.ptp.debug.ui.model.internal.Element;

/**
 * @author clement chu
 *
 */
public class UIDebugManager {
	protected IModelManager modelManager = null;
	protected UIManager uiManager = null;
	//FIXME dummy only
	private boolean dummy = true;
		
	public UIDebugManager() {
		modelManager = PTPCorePlugin.getDefault().getModelManager();
		uiManager = UIPlugin.getDefault().getUIManager();
	}
	
	public IGroupManager getGroupManager() {
		return uiManager.getGroupManager();
	}
	
	public void unregisterElements(ILaunch launch, PDebugTarget target, IElement[] elements) {
		for (int i=0; i<elements.length; i++) {
			//TODO unregister in selected elements in debug view 
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
		for (int i=0; i<elements.length; i++) {
			//TODO register in selected elements in debug view
			//target.setCurrentFocus(elements[i].getIDNum());
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
		throw new CoreException(new Status(IStatus.ERROR, UIPlugin.getUniqueIdentifier(), IStatus.ERROR, "No launch found", null));
	}
	
	public String getProcessStatus(String id) throws NullPointerException {
		//FIXME dummy only 
		if (dummy)
			return DebugManager.getInstance().getProcess(id).getStatus();
		
		return findProcess(id).getStatus();
		
	}
	public IPProcess findProcess(String id) {
		return modelManager.getUniverse().findProcessByName(getProcessName(id));
	}
	
	//FIXME
	private String getProcessName(String id) {
		//HARD CODE
		return "job0_process" + id;
	}
	
	public void initialProcess() {
		//FIXME dummy only
		if (dummy) {
			dummyInitialProcess();
			return;
		}
		
		IPJob[] jobs = modelManager.getUniverse().getJobs();
		int total_jobs = jobs.length;

		if (total_jobs > 0) {
			IGroupManager groupManager = getGroupManager();
			groupManager.clearAll();
			IElementGroup group = groupManager.getGroupRoot();
			for (int i=0; i<total_jobs; i++) {
				IPProcess[] processes = jobs[i].getProcesses();
				for (int j=0; j<processes.length; j++) {
					group.add(new Element(processes[j].getKeyString()));
				}
			}
			groupManager.add(group);
		}		
	}
	//FIXME dummny only
	private void dummyInitialProcess() {
		PProcess[] processes = DebugManager.getInstance().getProcesses();
		if (processes.length > 0) {
			IGroupManager groupManager = getGroupManager();
			groupManager.clearAll();
			IElementGroup group = groupManager.getGroupRoot();
			for (int j=0; j<processes.length; j++) {
				group.add(new Element(processes[j].getID()));
			}
		}		
	}
}
