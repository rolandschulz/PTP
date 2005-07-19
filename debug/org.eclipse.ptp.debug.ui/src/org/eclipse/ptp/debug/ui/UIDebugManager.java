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
import org.eclipse.ptp.debug.internal.core.model.PDebugTarget;
import org.eclipse.ptp.debug.ui.model.IElement;
import org.eclipse.ptp.debug.ui.model.IElementGroup;
import org.eclipse.ptp.debug.ui.model.IGroupManager;
import org.eclipse.ptp.debug.ui.model.internal.Element;
import org.eclipse.ptp.debug.ui.model.internal.ElementGroup;
import org.eclipse.ptp.debug.ui.model.internal.GroupManager;

/**
 * @author clement chu
 *
 */
public class UIDebugManager {
	private IGroupManager groupManager = null;
	protected IModelManager modelManager = null;	
	
	public UIDebugManager() {
		groupManager = new GroupManager();
		modelManager = PTPCorePlugin.getDefault().getModelManager();
	}
	public void shutdown() {
		groupManager.clearAll();
	}	
	public IGroupManager getGroupManager() {
		return groupManager;
	}	
	public IModelManager getModelManager() {
		return modelManager;
	}
	
	private void addToGroup(IElement[] elements, IElementGroup group) {
		for (int i=0; i<elements.length; i++) {
			group.add(elements[i].cloneElement());
		}
	}
	public void addToGroup(IElement[] elements, String groupID) {
		addToGroup(elements, groupManager.getGroup(groupID));
	}
	public String createGroup(IElement[] elements) {
		IElementGroup group = new ElementGroup(true);
		addToGroup(elements, group);
		groupManager.add(group);
		return group.getID();
	}
	public void removeGroup(String groupID) {
		groupManager.remove(groupID);
	}
	public void removeFromGroup(IElement[] elements, String groupID) {
		IElementGroup group = groupManager.getGroup(groupID);
		for (int i=0; i<elements.length; i++) {
			group.remove(elements[i]);
		}
	}	
	
	public void registerElements(ILaunch launch, PDebugTarget target, IElement[] elements) {
		for (int i=0; i<elements.length; i++) {
			target.setCurrentFocus(elements[i].getIDNum());
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
	
	public IPProcess findProcess(String id) {
		return modelManager.getUniverse().findProcessByName(getProcessName(id));
	}
	
	public void initialProcess() {
		IPJob[] jobs = modelManager.getUniverse().getJobs();
		int total_jobs = jobs.length;

		if (total_jobs > 0) {			
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
	
	//FIXME
	private String getProcessName(String id) {
		//HARD CODE
		return "job0_process" + id;
	}	
}
