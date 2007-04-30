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
package org.eclipse.ptp.internal.core.elements;

import java.util.HashMap;

import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.elementcontrols.IPElementControl;
import org.eclipse.ptp.core.elementcontrols.IPJobControl;
import org.eclipse.ptp.core.elementcontrols.IPProcessControl;
import org.eclipse.ptp.core.elementcontrols.IPQueueControl;
import org.eclipse.ptp.core.elements.IPProcess;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.core.elements.IResourceManager;

public class PJob extends Parent implements IPJobControl {
	final public static int BASE_OFFSET = 10000;
	final public static int STATE_NEW = 5000;
	private HashMap<String, IPProcessControl> numberMap = new HashMap<String, IPProcessControl>();

	protected String NAME_TAG = "root ";
	
	protected boolean isDebugJob = false;

	public PJob(String id, IPQueueControl queue, IAttribute[] attrs) {
		super(id, queue, P_JOB, attrs);
	}
	
	public void addProcess(IPProcessControl p) {
		addChild(p);
		String num = p.getProcessNumber();
		if (num != null) {
			numberMap.put(num, p);
		}
	}
	
	public synchronized IPProcess getProcessById(String id) {
		IPElementControl element = findChild(id);
		if (element != null)
			return (IPProcessControl) element;
		return null;
	}

	public synchronized IPProcess getProcessByNumber(String number) {
		return numberMap.get(number);
	}

	/*
	 * returns all the processes in this job, which are the children of the job
	 */
	public synchronized IPProcessControl[] getProcessControls() {
		return (IPProcessControl[]) getCollection().toArray(new IPProcessControl[size()]);
	}

	/*
	 * returns all the processes in this job, which are the children of the job
	 */
	public IPProcess[] getProcesses() {
		return getProcessControls();
	}

	public synchronized IPProcess[] getSortedProcesses() {
		IPProcessControl[] processes = getProcessControls();
		sort(processes);
		return processes;
	}

	public boolean isDebug() {
		return isDebugJob;
	}

	public void removeAllProcesses() {
		IPProcess[] processes = getProcesses();
		for (int i = 0; i < processes.length; i++)
			processes[i].clearOutput();

		removeChildren();
	}
	
	public void setDebug() {
		isDebugJob = true;
	}
	
	public int totalProcesses() {
		return size();
	}

	public IPQueue getQueue() {
		return (IPQueue) getParent();
	}

	public IResourceManager getResourceManager() {
		return getQueue().getResourceManager();
	}

	public boolean isTerminated() {
		for (IPProcessControl proc : getProcessControls()) {
			if (!proc.isTerminated())
				return false;
		}
		return true;
	}
}
