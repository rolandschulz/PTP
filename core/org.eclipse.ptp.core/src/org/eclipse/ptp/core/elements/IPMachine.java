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
package org.eclipse.ptp.core.elements;

import org.eclipse.ptp.core.elements.attributes.MachineAttributes;
import org.eclipse.ptp.core.elements.listeners.IMachineChildListener;
import org.eclipse.ptp.core.elements.listeners.IMachineListener;

/**
 * Interface class for a model of a machine. Machines are comprised of nodes
 * which may have processes running on them. The Machine model is intented to be
 * a hardware representation of the machine with methods to get information like
 * the architecture and related interested bits. The model allows for the
 * Machine to have any number of nodes (even a single one to represent a
 * uniprocessor machine).
 * 
 * @author Nathan DeBardeleben
 */
public interface IPMachine extends IPElement {

	/**
	 * Add a listener for events related to children of this machine.
	 * 
	 * @param listener
	 */
	public void addChildListener(IMachineChildListener listener);

	/**
	 * Add a listener for events related to this machine.
	 * 
	 * @param listener
	 */
	public void addElementListener(IMachineListener listener);

	/**
	 * Returns the architecture of this Machine.
	 * 
	 * @return The architecture of this Machine
	 */
	public String getArch();

	/**
	 * Given a node id, attempts to find it as a Node object contained in this
	 * Machine. If found, the Node object is returned. Otherwise,
	 * <code>null</code> is returned.
	 * 
	 * @param id
	 *            The ID to find in this Machine
	 * @return The Node object if found, else <code>null</code>
	 */
	public IPNode getNodeById(String id);

	/**
	 * Returns an array of all the Nodes that this Machine is comprised of.
	 * Returns an empty array if there are no nodes.
	 * 
	 * @return The Nodes in this Machine - <code>null</code> if there are none.
	 */
	public IPNode[] getNodes();

	/**
	 * Get the resource manager that controls this machine.
	 * 
	 * @return the machine's parent resource manager
	 * @since 5.0
	 */
	public IPResourceManager getResourceManager();

	/**
	 * Get the state of the machine
	 * 
	 * @return machine state
	 */
	public MachineAttributes.State getState();

	/**
	 * Add a listener for events related to children of this machine.
	 * 
	 * @param listener
	 */
	public void removeChildListener(IMachineChildListener listener);

	/**
	 * Add a listener for events related to this machine.
	 * 
	 * @param listener
	 */
	public void removeElementListener(IMachineListener listener);

	/**
	 * Sets the architecture of this machine. At this time there are no
	 * standards for how this String should be formatted and the String is
	 * really only used for printing out information about this Machine.
	 * 
	 * @param arch
	 *            The architecture of this machine
	 */
	public void setArch(String arch);

	/**
	 * Counts all the Nodes associated with this Machine and returns that as an
	 * <code>int</code>.
	 * 
	 * @return The number of Nodes in this Machine
	 */
	public int totalNodes();
}
