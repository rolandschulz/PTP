/*******************************************************************************
 * Copyright (c) 2006 The Regents of the University of California. 
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
package org.eclipse.ptp.core.elementcontrols;

import java.util.Collection;

import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.elements.IPMachine;

public interface IPMachineControl extends IPMachine {

	/**
	 * Add attributes to a collection of nodes.
	 * 
	 * @param nodes
	 *            collection of IPNodeControls
	 * @param attrs
	 *            array of attributes to add to each node
	 */
	public void addNodeAttributes(Collection<IPNodeControl> nodes, IAttribute<?, ?, ?>[] attrs);

	/**
	 * Add a collection of nodes to the machine.
	 * 
	 * @param node
	 *            collection of IPNodeControls
	 */
	public void addNodes(Collection<IPNodeControl> nodes);

	/**
	 * Get the nodes known by this machine
	 * 
	 * @return array of nodes
	 */
	public Collection<IPNodeControl> getNodeControls();

	/**
	 * Remove a collection of nodes from this machine
	 * 
	 * @param nodes
	 *            to remove
	 */
	public void removeNodes(Collection<IPNodeControl> nodes);
}
