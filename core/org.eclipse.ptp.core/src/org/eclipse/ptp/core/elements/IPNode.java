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

import java.util.BitSet;
import java.util.Set;

import org.eclipse.ptp.core.elements.attributes.NodeAttributes;
import org.eclipse.ptp.core.elements.listeners.INodeChildListener;
import org.eclipse.ptp.core.elements.listeners.INodeListener;

public interface IPNode extends IPElement {

	/**
	 * Add a listener for events related to children of this node.
	 * 
	 * @param listener
	 */
	public void addChildListener(INodeChildListener listener);

	/**
	 * Add a listener for events related to this node.
	 * 
	 * @param listener
	 */
	public void addElementListener(INodeListener listener);

	/**
	 * @param job
	 * @param processRanks
	 * @since 5.0
	 */
	public void addJobProcessRanks(IPJob job, BitSet processRanks);

	/**
	 * Get the job ranks for processes associated with this node and this job
	 * 
	 * @param job
	 * 
	 * @return collection of process ranks
	 * @since 4.0
	 */
	public BitSet getJobProcessRanks(IPJob job);

	/**
	 * @return the jobs that have processes running on this node
	 * @since 4.0
	 */
	public Set<? extends IPJob> getJobs();

	/**
	 * Returns the parent Machine that this Node is contained within.
	 * 
	 * @return The Machine this Node is part of
	 */
	public IPMachine getMachine();

	/**
	 * Returns the node number of this Node as a String. Node numbers are
	 * optionally supplied by the resource manager, so may return
	 * <code>null</code>. Providing a node number will enable the node numbering
	 * ruler in the UI.
	 * 
	 * @return The node number of this Node as a String
	 */
	public String getNodeNumber();

	/**
	 * Returns the state of the node
	 * 
	 * @return node state
	 */
	public NodeAttributes.State getState();

	/**
	 * Remove a listener for events related to children of this node.
	 * 
	 * @param listener
	 */
	public void removeChildListener(INodeChildListener listener);

	/**
	 * Remove a listener for events related to this node.
	 * 
	 * @param listener
	 */
	public void removeElementListener(INodeListener listener);

	/**
	 * @param job
	 * @param processRanks
	 * @since 5.0
	 */
	public void removeJobProcessRanks(IPJob job, BitSet processRanks);
}