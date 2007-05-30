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

import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.attributes.IllegalValueException;
import org.eclipse.ptp.core.attributes.IntegerAttribute;
import org.eclipse.ptp.core.elementcontrols.IPElementControl;
import org.eclipse.ptp.core.elementcontrols.IPMachineControl;
import org.eclipse.ptp.core.elementcontrols.IPNodeControl;
import org.eclipse.ptp.core.elementcontrols.IResourceManagerControl;
import org.eclipse.ptp.core.elements.IPNode;
import org.eclipse.ptp.core.elements.IResourceManager;
import org.eclipse.ptp.core.elements.attributes.MachineAttributes;
import org.eclipse.ptp.core.elements.events.IMachineChangedEvent;
import org.eclipse.ptp.core.elements.events.IMachineChangedNodeEvent;
import org.eclipse.ptp.core.elements.events.IMachineNewNodeEvent;
import org.eclipse.ptp.core.elements.events.IMachineRemoveNodeEvent;
import org.eclipse.ptp.core.elements.events.INodeChangedEvent;
import org.eclipse.ptp.core.elements.listeners.IMachineListener;
import org.eclipse.ptp.core.elements.listeners.IMachineNodeListener;
import org.eclipse.ptp.core.elements.listeners.INodeListener;
import org.eclipse.ptp.internal.core.elements.events.MachineChangedEvent;
import org.eclipse.ptp.internal.core.elements.events.MachineChangedNodeEvent;
import org.eclipse.ptp.internal.core.elements.events.MachineNewNodeEvent;
import org.eclipse.ptp.internal.core.elements.events.MachineRemoveNodeEvent;

public class PMachine extends Parent implements IPMachineControl, INodeListener {
	private final ListenerList elementListeners = new ListenerList();
	private final ListenerList childListeners = new ListenerList();
	private String arch = "undefined";
    private IntegerAttribute numNodes;
	
	public PMachine(String id, IResourceManagerControl rm, IAttribute<?,?,?>[] attrs) {
		super(id, rm, P_MACHINE, attrs);
        numNodes = getAttribute(MachineAttributes.getNumNodesAttributeDefinition());
        if (numNodes == null) {
            try {
                numNodes = MachineAttributes.getNumNodesAttributeDefinition().create(0);
            } catch (IllegalValueException e) {
                //FIXME
                throw new RuntimeException(e);
            }
            addAttribute(numNodes);
        }
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.IPMachine#addChildListener(org.eclipse.ptp.core.elements.listeners.IMachineNodeListener)
	 */
	public void addChildListener(IMachineNodeListener listener) {
		childListeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.IPMachine#addElementListener(org.eclipse.ptp.core.elements.listeners.IMachineListener)
	 */
	public void addElementListener(IMachineListener listener) {
		elementListeners.add(listener);
	}

	public synchronized void addNode(IPNodeControl node) {
		assert(numNodes.getValue().equals(getNodes().length));
	    addChild(node);
		try {
            numNodes.setValue(getNodes().length);
        } catch (IllegalValueException e) {
            // FIXME
            throw new RuntimeException(e);
        }
		fireNewNode(node);
		node.addElementListener(this);
	}

	/* returns a string representation of the architecture of this machine */
	public String getArch() {
		return arch;
	}

	/* finds a node using a string identifier - returns null if none found */
	public synchronized IPNode getNodeById(String id) {
		IPElementControl element = findChild(id);
		if (element != null)
			return (IPNodeControl) element;
		return null;
	}

	/* returns an array of the nodes that are comprised by this machine */
	public synchronized IPNodeControl[] getNodeControls() {
		return (IPNodeControl[]) getCollection().toArray(new IPNodeControl[size()]);
	}
	
	public IPNode[] getNodes() {
		return getNodeControls();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.IPMachine#getResourceManager()
	 */
	public IResourceManager getResourceManager() {
		return (IResourceManager) getParent();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.listeners.INodeListener#handleEvent(org.eclipse.ptp.core.elements.events.INodeChangedEvent)
	 */
	public void handleEvent(INodeChangedEvent e) {
		IMachineChangedNodeEvent ne = 
			new MachineChangedNodeEvent(this, e.getSource(), e.getAttributes());
		
		for (Object listener : childListeners.getListeners()) {
			((IMachineNodeListener)listener).handleEvent(ne);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.IPMachine#removeChildListener(org.eclipse.ptp.core.elements.listeners.IMachineNodeListener)
	 */
	public void removeChildListener(IMachineNodeListener listener) {
		childListeners.remove(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.IPMachine#removeElementListener(org.eclipse.ptp.core.elements.listeners.IMachineListener)
	 */
	public void removeElementListener(IMachineListener listener) {
		elementListeners.remove(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elementcontrols.IPMachineControl#removeNode(org.eclipse.ptp.core.elementcontrols.IPNodeControl)
	 */
	public void removeNode(IPNodeControl node) {
		node.removeElementListener(this);
		removeChild(node);
		fireRemoveNode(node);
	}

	/* sets the architecture of this machine, which is merely a string */
	public void setArch(String arch) {
		this.arch = arch;
	}

	/*
	 * returns all the nodes comprised by this machine, which is just the size()
	 * of its children group
	 */
	public int totalNodes() {
		return size();
	}

	private void fireChangedMachine(Collection<? extends IAttribute<?,?,?>> attrs) {
		IMachineChangedEvent e = 
			new MachineChangedEvent(this, attrs);
		
		for (Object listener : elementListeners.getListeners()) {
			((IMachineListener)listener).handleEvent(e);
		}
	}

	private void fireNewNode(IPNode node) {
		IMachineNewNodeEvent e = 
			new MachineNewNodeEvent(this, node);
		
		for (Object listener : childListeners.getListeners()) {
			((IMachineNodeListener)listener).handleEvent(e);
		}
	}

	private void fireRemoveNode(IPNode node) {
		IMachineRemoveNodeEvent e = 
			new MachineRemoveNodeEvent(this, node);
		
		for (Object listener : childListeners.getListeners()) {
			((IMachineNodeListener)listener).handleEvent(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.internal.core.elements.PElement#doAddAttributeHook(java.util.List)
	 */
	@Override
	protected void doAddAttributeHook(List<? extends IAttribute<?,?,?>> attrs) {
		fireChangedMachine(attrs);
	}

}
