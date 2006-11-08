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
package org.eclipse.ptp.internal.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ptp.core.AttributeConstants;
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.core.IPMachine;
import org.eclipse.ptp.core.IPProcess;
import org.eclipse.ptp.core.elementcontrols.IPElementControl;
import org.eclipse.ptp.core.elementcontrols.IPMachineControl;
import org.eclipse.ptp.core.elementcontrols.IPNodeControl;
import org.eclipse.ptp.core.elementcontrols.IPProcessControl;

public class PNode extends Parent implements IPNodeControl {
	protected String NAME_TAG = "node ";

	public PNode(IPElementControl mac, String name, String key) {
		super(mac, name, key, P_NODE);
		this.setAttribute(AttributeConstants.ATTRIB_NODE_NUMBER, new Integer(key));
	}
	
	public void addProcess(IPProcessControl process) {
		addChild(process);
	}
	
	public IPMachine getMachine() {
		IPElementControl current = this;
		do {
			if (current instanceof IPMachineControl)
				return (IPMachineControl) current;
		} while ((current = current.getParent()) != null);
		return null;
	}
	
	public String getNodeNumber() {
		return ""+((Integer) this.getAttribute(AttributeConstants.ATTRIB_NODE_NUMBER)).intValue()+"";
	}
	
	public int getNodeNumberInt()
	{
		return ((Integer) this.getAttribute(AttributeConstants.ATTRIB_NODE_NUMBER)).intValue();
	}
	
	public IPProcessControl[] getProcessControls() {
		return (IPProcessControl[]) getCollection().toArray(new IPProcessControl[size()]);
	}
	
	public IPProcess[] getProcesses() {
		return getProcessControls();
	}
	
	public IPProcess[] getSortedProcesses() {
		IPProcessControl[] processes = (IPProcessControl[]) getProcesses();
		sort(processes);
		return processes;
	}
	public IPProcess findProcess(String processNumber) {
		IPElementControl element = findChild(processNumber);
		if (element != null)
			return (IPProcessControl) element;
		return null;
	}
	/*
	 * returns a list of jobs that are running on this node - does this by looking at the processes running on this node and seeing which jobs they are part of
	 */
	public IPJob[] getJobs() {
		IPProcess[] processes = getProcesses();
		List array = new ArrayList(0);
		for (int i = 0; i < processes.length; i++) {
			final IPJob job = processes[i].getJob();
			if (job != null && !array.contains(job)) {
				array.add(job);
			}
		}
		return (IPJob[]) array.toArray(new IPJob[array.size()]);
	}
	public Object getAttribute(String key) {
		return this.getAttribute(AttributeConstants.ATTRIB_CLASS_NODE, key);
	}

	public void setAttribute(String key, Object o) {
		this.setAttribute(AttributeConstants.ATTRIB_CLASS_NODE, key, o);
	}
	
	public String[] getAttributeKeys() {
		return this.getAttributeKeys(AttributeConstants.ATTRIB_CLASS_NODE);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.IPNode#getName()
	 */
	public String getName() {
		return getElementName();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.IPNode#getNumProcesses()
	 */
	public int getNumProcesses() {
		return size();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.IPNode#hasChildProcesses()
	 */
	public boolean hasChildProcesses() {
		return getNumProcesses() > 0;
	}
	
	public void removeProcess(IPProcessControl process) {
		removeChild(process);
	}	
}
