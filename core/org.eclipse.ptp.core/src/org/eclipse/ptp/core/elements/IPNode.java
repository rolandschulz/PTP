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

import org.eclipse.ptp.core.elements.listeners.INodeListener;
import org.eclipse.ptp.core.elements.listeners.INodeProcessListener;


public interface IPNode extends IPElement {

	/**
	 * @param listener
	 */
	public void addChildListener(INodeProcessListener listener);

	/**
	 * @param listener
	 */
	public void addElementListener(INodeListener listener);

	/**
	 * Returns the parent Machine that this Node is contained within.
	 * 
	 * @return The Machine this Node is part of
	 */
	public IPMachine getMachine();
	
	/**
	 * Returns the node number of this Node as a String. 
	 * Node numbers are optionally supplied by the resource manager, so may return <code>null</code>.
	 * Providing a node number will enable the node numbering ruler in the UI.
	 * 
	 * @return The node number of this Node as a String
	 */
	public String getNodeNumber();

	/**
	 * Return any processes that are linked to this node
	 * 
	 * @return array of processes
	 */
	public IPProcess[] getProcesses();
	
	/**
	 * Return a sorted array of processes that are linked to this node
	 * 
	 * @return sorted array of processes
	 */
	public IPProcess[] getSortedProcesses();
	
	/**
	 * @param listener
	 */
	public void removeChildListener(INodeProcessListener listener);
	
	/**
	 * @param listener
	 */
	public void removeElementListener(INodeListener listener);
}