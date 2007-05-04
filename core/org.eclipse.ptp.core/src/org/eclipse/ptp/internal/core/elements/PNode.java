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

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.attributes.IntegerAttribute;
import org.eclipse.ptp.core.elementcontrols.IPElementControl;
import org.eclipse.ptp.core.elementcontrols.IPMachineControl;
import org.eclipse.ptp.core.elementcontrols.IPNodeControl;
import org.eclipse.ptp.core.elementcontrols.IPProcessControl;
import org.eclipse.ptp.core.elements.IPMachine;
import org.eclipse.ptp.core.elements.IPProcess;
import org.eclipse.ptp.core.elements.attributes.NodeAttributes;
import org.eclipse.ptp.core.elements.listeners.INodeListener;
import org.eclipse.ptp.core.elements.listeners.INodeProcessListener;

public class PNode extends Parent implements IPNodeControl {
	private final ListenerList elementListeners = new ListenerList();
	private final ListenerList childListeners = new ListenerList();

	public PNode(String id, IPMachineControl mac, IAttribute[] attrs) {
		super(id, mac, P_NODE, attrs);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.IPNode#addChildListener(org.eclipse.ptp.core.elements.listeners.INodeProcessListener)
	 */
	public void addChildListener(INodeProcessListener listener) {
		childListeners.add(listener);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.IPNode#addElementListener(org.eclipse.ptp.core.elements.listeners.INodeListener)
	 */
	public void addElementListener(INodeListener listener) {
		elementListeners.add(listener);
	}
	
	public synchronized void addProcess(IPProcessControl process) {
		addChild(process);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elementcontrols.IPNodeControl#getMachineControl()
	 */
	public IPMachine getMachine() {
		return getMachineControl();
	}

	public IPMachineControl getMachineControl() {
		IPElementControl current = this;
		do {
			if (current instanceof IPMachineControl)
				return (IPMachineControl) current;
		} while ((current = current.getParent()) != null);
		return null;
	}

	public String getNodeNumber() {
		IntegerAttribute num = (IntegerAttribute)getAttribute(NodeAttributes.getNumberAttributeDefinition());
		if (num != null) {
			return num.getValueAsString();
		}
		return "";
	}
	
	public synchronized IPProcessControl[] getProcessControls() {
		return (IPProcessControl[]) getCollection().toArray(new IPProcessControl[size()]);
	}

	public IPProcess[] getProcesses() {
		return getProcessControls();
	}

	public synchronized IPProcess[] getSortedProcesses() {
		IPProcessControl[] procs = getProcessControls();
		sort(procs);
		return procs;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.IPNode#removeChildListener(org.eclipse.ptp.core.elements.listeners.INodeProcessListener)
	 */
	public void removeChildListener(INodeProcessListener listener) {
		childListeners.remove(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.IPNode#removeElementListener(org.eclipse.ptp.core.elements.listeners.INodeListener)
	 */
	public void removeElementListener(INodeListener listener) {
		elementListeners.remove(listener);
	}

	public synchronized void removeProcess(IPProcessControl process) {
		removeChild(process);
	}
}
