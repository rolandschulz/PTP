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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.eclipse.ptp.core.AttributeConstants;
import org.eclipse.ptp.core.INodeEvent;
import org.eclipse.ptp.core.INodeListener;
import org.eclipse.ptp.core.IPElement;
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.core.IPMachine;
import org.eclipse.ptp.core.IPNode;
import org.eclipse.ptp.core.IPProcess;

public class PNode extends Parent implements IPNode {
	protected String NAME_TAG = "node ";
	protected Map attribs = null;
	protected List listeners = new ArrayList();

	public PNode(IPElement element, String name, String key, int nodeNumber) {
		super(element, name, key, P_NODE);
		attribs = new HashMap(0);
		this.setAttrib(AttributeConstants.ATTRIB_NODE_NUMBER, new String("" + nodeNumber + ""));
	}
	public IPMachine getMachine() {
		IPElement current = this;
		do {
			if (current instanceof IPMachine)
				return (IPMachine) current;
		} while ((current = current.getParent()) != null);
		return null;
	}
	public String getNodeNumber() {
		return (String) getAttrib(AttributeConstants.ATTRIB_NODE_NUMBER);
	}
	public int getNodeNumberInt()
	{
		return ((Integer) attribs.get(AttributeConstants.ATTRIB_NODE_NUMBER)).intValue();
	}
	public IPProcess[] getProcesses() {
		return (IPProcess[]) getCollection().toArray(new IPProcess[size()]);
	}
	public IPProcess[] getSortedProcesses() {
		IPProcess[] processes = getProcesses();
		sort(processes);
		return processes;
	}
	public IPProcess findProcess(String processNumber) {
		IPElement element = findChild(processNumber);
		if (element != null)
			return (IPProcess) element;
		return null;
	}
	/*
	 * public String getElementName() { return NAME_TAG + getKey(); }
	 */
	public void setAttrib(String key, Object val) {
		if (attribs.containsKey(key))
			attribs.remove(key);
		attribs.put(key, val);
	}
	public Object getAttrib(String key) {
		if (!attribs.containsKey(key))
			return null;
		return attribs.get(key);
	}
	/*
	 * returns a list of jobs that are running on this node - does this by looking at the processes running on this node and seeing which jobs they are part of
	 */
	public IPJob[] getJobs() {
		IPProcess[] processes = getProcesses();
		List array = new ArrayList(0);
		for (int i = 0; i < processes.length; i++) {
			if (!array.contains(processes[i].getJob())) {
				array.add(processes[i].getJob());
			}
		}
		return (IPJob[]) array.toArray(new IPJob[array.size()]);
	}
	public void fireEvent(INodeEvent event) {
		for (Iterator i=listeners.iterator(); i.hasNext();) {
			INodeListener listener = (INodeListener)i.next();
			listener.nodeEvent(event);
		}
	}	
	//Node Listener
	public void addNodeListener(INodeListener listener) {
		if (!listeners.contains(listener))
			listeners.add(listener);
	}
	public void removerNodeListener(INodeListener listener) {
		if (listeners.contains(listener))
			listeners.remove(listener);
	}	
}
