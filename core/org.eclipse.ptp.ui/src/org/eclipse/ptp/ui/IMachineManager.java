/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.ui;

import org.eclipse.ptp.core.elements.IPMachine;
import org.eclipse.ptp.core.elements.IPNode;


public interface IMachineManager extends IElementManager {
	/** 
	 * Add a new machine to machineList. Add any new nodes to the element
	 * handler for the machine.
	 * 
	 * @param machine machine
	 * @return true if the machine was added
	 */
	public void addMachine(IPMachine machine);
	
	/** 
	 * Add a node to the view
	 * 
	 * @param node node
	 */
	public void addNode(IPNode node);
	
	/**
	 * Find a machine using its ID
	 * 
	 * @param id ID of machine
	 * @return machine
	 */
	public IPMachine findMachineById(String id);
	
	/** 
	 * Find node using its ID
	 * 
	 * @param id node ID
	 * @return null is not found
	 */
	public IPNode findNode(String id);
	
	/** 
	 * Get current machine
	 * 
	 * @return current machine
	 */
	public IPMachine getCurrentMachine();
	
	/** 
	 * Get machines in the view
	 * 
	 * @return machines
	 */
	public IPMachine[] getMachines();
	
	/**
	 * Remove machine from the view
	 * 
	 * @param machine
	 */
	public void removeMachine(IPMachine machine);
	
	/** 
	 * Remove node from the view
	 * 
	 * @param node node
	 */
	public void removeNode(IPNode node);
	
	/** 
	 * Set current machine. If the machine has never been set before, add an entry to
	 * the machineList, and add the nodes to the element handler.
	 * 
	 * @param machine machine
	 */
	public void setMachine(IPMachine machine);
}
