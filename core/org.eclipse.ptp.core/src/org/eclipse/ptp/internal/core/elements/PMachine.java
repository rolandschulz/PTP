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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.core.attributes.EnumeratedAttribute;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.attributes.IllegalValueException;
import org.eclipse.ptp.core.attributes.IntegerAttribute;
import org.eclipse.ptp.core.elementcontrols.IPElementControl;
import org.eclipse.ptp.core.elementcontrols.IPMachineControl;
import org.eclipse.ptp.core.elementcontrols.IPNodeControl;
import org.eclipse.ptp.core.elementcontrols.IResourceManagerControl;
import org.eclipse.ptp.core.elements.IPNode;
import org.eclipse.ptp.core.elements.IPResourceManager;
import org.eclipse.ptp.core.elements.attributes.MachineAttributes;
import org.eclipse.ptp.core.elements.attributes.MachineAttributes.State;
import org.eclipse.ptp.core.elements.events.IChangedNodeEvent;
import org.eclipse.ptp.core.elements.events.IMachineChangeEvent;
import org.eclipse.ptp.core.elements.events.INewNodeEvent;
import org.eclipse.ptp.core.elements.events.IRemoveNodeEvent;
import org.eclipse.ptp.core.elements.listeners.IMachineChildListener;
import org.eclipse.ptp.core.elements.listeners.IMachineListener;
import org.eclipse.ptp.core.messages.Messages;
import org.eclipse.ptp.internal.core.elements.events.ChangedNodeEvent;
import org.eclipse.ptp.internal.core.elements.events.MachineChangeEvent;
import org.eclipse.ptp.internal.core.elements.events.NewNodeEvent;
import org.eclipse.ptp.internal.core.elements.events.RemoveNodeEvent;

public class PMachine extends Parent implements IPMachineControl {
	private final ListenerList elementListeners = new ListenerList();
	private final ListenerList childListeners = new ListenerList();
	private String arch = Messages.PMachine_0;

	public PMachine(String id, IResourceManagerControl rm, IAttribute<?, ?, ?>[] attrs) {
		super(id, rm, P_MACHINE, attrs);
		/*
		 * Create required attributes.
		 */
		EnumeratedAttribute<State> machineState = getAttribute(MachineAttributes.getStateAttributeDefinition());
		if (machineState == null) {
			machineState = MachineAttributes.getStateAttributeDefinition().create();
			addAttribute(machineState);
		}
		IntegerAttribute numNodes = getAttribute(MachineAttributes.getNumNodesAttributeDefinition());
		if (numNodes == null) {
			try {
				numNodes = MachineAttributes.getNumNodesAttributeDefinition().create(0);
			} catch (IllegalValueException e) {
				// FIXME
				throw new RuntimeException(e);
			}
			addAttribute(numNodes);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.elements.IPMachine#addChildListener(org.eclipse.
	 * ptp.core.elements.listeners.IMachineNodeListener)
	 */
	public void addChildListener(IMachineChildListener listener) {
		childListeners.add(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.elements.IPMachine#addElementListener(org.eclipse
	 * .ptp.core.elements.listeners.IMachineListener)
	 */
	public void addElementListener(IMachineListener listener) {
		elementListeners.add(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.elementcontrols.IPMachineControl#addNodeAttributes
	 * (java.util.Collection,
	 * org.eclipse.ptp.core.attributes.IAttribute<?,?,?>[])
	 */
	public void addNodeAttributes(Collection<IPNodeControl> nodeControls,
			IAttribute<?, ?, ?>[] attrs) {
		List<IPNode> nodes = new ArrayList<IPNode>(nodeControls.size());
		for (IPNodeControl node : nodeControls) {
			node.addAttributes(attrs);
		}
		fireChangedNodes(nodes);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.elementcontrols.IPMachineControl#addNodes(java.util
	 * .Collection)
	 */
	public void addNodes(Collection<IPNodeControl> nodeControls) {
		List<IPNode> nodes = new ArrayList<IPNode>(nodeControls.size());

		for (IPNodeControl node : nodeControls) {
			addChild(node);
			nodes.add(node);
		}

		try {
			getAttribute(MachineAttributes.getNumNodesAttributeDefinition()).setValue(getChildren().length);
		} catch (IllegalValueException e) {
			// FIXME
			throw new RuntimeException(e);
		}

		fireNewNodes(nodes);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.core.elements.PElement#doAddAttributeHook(java
	 * .util.Map)
	 */
	@Override
	protected void doAddAttributeHook(AttributeManager attrs) {
		fireChangedMachine(attrs);
	}

	/**
	 * @param attrs
	 */
	private void fireChangedMachine(AttributeManager attrs) {
		IMachineChangeEvent e =
				new MachineChangeEvent(this, attrs);

		for (Object listener : elementListeners.getListeners()) {
			((IMachineListener) listener).handleEvent(e);
		}
	}

	/**
	 * Send IChangedNodeEvent to registered listeners
	 * 
	 * @param nodes
	 */
	private void fireChangedNodes(Collection<IPNode> nodes) {
		IChangedNodeEvent e =
				new ChangedNodeEvent(this, nodes);

		for (Object listener : childListeners.getListeners()) {
			((IMachineChildListener) listener).handleEvent(e);
		}
	}

	/**
	 * Send INewNodeEvent to registered listeners
	 * 
	 * @param nodes
	 */
	private void fireNewNodes(Collection<IPNode> nodes) {
		INewNodeEvent e =
				new NewNodeEvent(this, nodes);

		for (Object listener : childListeners.getListeners()) {
			((IMachineChildListener) listener).handleEvent(e);
		}
	}

	/**
	 * @param node
	 */
	private void fireRemoveNodes(Collection<IPNode> nodes) {
		IRemoveNodeEvent e =
				new RemoveNodeEvent(this, nodes);

		for (Object listener : childListeners.getListeners()) {
			((IMachineChildListener) listener).handleEvent(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elements.IPMachine#getArch()
	 */
	public synchronized String getArch() {
		return arch;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.elements.IPMachine#getNodeById(java.lang.String)
	 */
	public IPNode getNodeById(String id) {
		IPElementControl element = findChild(id);
		if (element != null) {
			return (IPNodeControl) element;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.elementcontrols.IPMachineControl#getNodeControls()
	 */
	public Collection<IPNodeControl> getNodeControls() {
		IPElementControl[] children = getChildren();
		List<IPNodeControl> nodes =
				new ArrayList<IPNodeControl>(children.length);
		for (IPElementControl element : children) {
			nodes.add((IPNodeControl) element);
		}
		return nodes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elements.IPMachine#getNodes()
	 */
	public IPNode[] getNodes() {
		Collection<IPNodeControl> nodes = getNodeControls();
		return nodes.toArray(new IPNode[nodes.size()]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.IPMachine#getResourceManager()
	 */
	public IPResourceManager getResourceManager() {
		return (IPResourceManager) getParent();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elements.IPMachine#getState()
	 */
	public State getState() {
		return getAttribute(MachineAttributes.getStateAttributeDefinition()).getValue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.elements.IPMachine#removeChildListener(org.eclipse
	 * .ptp.core.elements.listeners.IMachineNodeListener)
	 */
	public void removeChildListener(IMachineChildListener listener) {
		childListeners.remove(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.elements.IPMachine#removeElementListener(org.eclipse
	 * .ptp.core.elements.listeners.IMachineListener)
	 */
	public void removeElementListener(IMachineListener listener) {
		elementListeners.remove(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.elementcontrols.IPMachineControl#removeNode(org.
	 * eclipse.ptp.core.elementcontrols.IPNodeControl)
	 */
	public void removeNodes(Collection<IPNodeControl> nodeControls) {
		List<IPNode> nodes = new ArrayList<IPNode>(nodeControls.size());

		for (IPNodeControl node : nodeControls) {
			removeChild(node);
			nodes.add(node);
		}

		try {
			getAttribute(MachineAttributes.getNumNodesAttributeDefinition()).setValue(getChildren().length);
		} catch (IllegalValueException e) {
			// FIXME
			throw new RuntimeException(e);
		}

		fireRemoveNodes(nodes);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elements.IPMachine#setArch(java.lang.String)
	 */
	public synchronized void setArch(String arch) {
		this.arch = arch;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elements.IPMachine#totalNodes()
	 */
	public int totalNodes() {
		try {
			getAttribute(MachineAttributes.getNumNodesAttributeDefinition()).setValue(getChildren().length);
		} catch (IllegalValueException e) {
			// FIXME
			throw new RuntimeException(e);
		}
		return size();
	}

}
